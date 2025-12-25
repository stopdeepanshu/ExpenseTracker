package com.example.ExpenseTracker.Ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.MyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final MyRepository repo;
    private final HttpClient http;
    private final ObjectMapper mapper;

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    public AiService(MyRepository repo) {
        this.repo = repo;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // Compute local stats then call OpenAI to craft human-friendly messages
    public String savingsSuggestion(String month) throws IOException, InterruptedException {
        YearMonth ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> list = repo.findByDateBetween(start, end);

        if (list.isEmpty()) {
            return "No expenses for " + ym + ".";
        }

        Map<String, Integer> totals = new HashMap<>();
        int total = 0;
        for (Expense e : list) {
            totals.put(e.getCategory(), totals.getOrDefault(e.getCategory(), 0) + e.getAmount());
            total += e.getAmount();
        }

        // find top category
        String topCategory = totals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("others");

        int topAmount = totals.getOrDefault(topCategory, 0);

        // propose 15% reduction
        int reductionPercent = 15;
        int estimatedSave = (topAmount * reductionPercent) / 100;

        // Build prompt
        String stats = String.format("For month %s: total=%d. Category totals: %s. Top category: %s (%d).",
                ym.toString(), total, totals.toString(), topCategory, topAmount);

        String userPrompt = "You are a helpful financial assistant. Based on the stats below, produce a single short actionable savings suggestion (1 sentence). Use user's name 'Deepanshu' if helpful.\n\n"
                + stats + "\n\nSuggested reduction percent: " + reductionPercent + "%\nEstimated saving: ₹" + estimatedSave + "\n\nOutput example: “Reduce eating-out expenses by 15% to save ₹1200 next month.”";

        return callOpenAi(userPrompt);
    }

    public String spendingInsights(String month) throws IOException, InterruptedException {
        YearMonth ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> current = repo.findByDateBetween(start, end);
        YearMonth prevYm = ym.minusMonths(1);
        List<Expense> previous = repo.findByDateBetween(prevYm.atDay(1), prevYm.atEndOfMonth());

        Map<String, Integer> curTotals = sumByCategory(current);
        Map<String, Integer> prevTotals = sumByCategory(previous);

        StringBuilder summary = new StringBuilder();
        summary.append("This month (" + ym + ") vs previous month (" + prevYm + "):\n");
        Set<String> cats = new HashSet<>();
        cats.addAll(curTotals.keySet());
        cats.addAll(prevTotals.keySet());

        for (String c : cats) {
            int cur = curTotals.getOrDefault(c, 0);
            int prev = prevTotals.getOrDefault(c, 0);
            if (prev == 0 && cur == 0) continue;
            String trend;
            if (prev == 0) trend = "increased (no data previous month)";
            else {
                double diffPercent = ((double)(cur - prev) / prev) * 100;
                trend = diffPercent >= 0 ? String.format("increased by %.0f%%", diffPercent) : String.format("decreased by %.0f%%", -diffPercent);
            }
            summary.append(String.format("%s: ₹%d (%s). ", c, cur, trend));
        }

        String userPrompt = "You are a concise financial analyst. Rewrite the following summary into a short friendly insight for the user starting with their name (Deepanshu). Keep it 1-2 sentences.\n\n" + summary.toString();
        return callOpenAi(userPrompt);
    }

    public String chat(String userMessage, String month) throws IOException, InterruptedException {
        // include short summary of current month as context
        YearMonth ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Expense> list = repo.findByDateBetween(start, end);
        Map<String, Integer> totals = sumByCategory(list);
        int total = totals.values().stream().mapToInt(Integer::intValue).sum();

        String context = "User expenses summary for " + ym + ": total ₹" + total + ". Category totals: " + totals.toString() + ".";
        String prompt = "You are a helpful personal finance assistant. Use the context below and answer the user's question concisely and practically.\n\nContext:\n" + context + "\n\nUser question:\n" + userMessage;
        return callOpenAi(prompt);
    }

    private Map<String, Integer> sumByCategory(List<Expense> list) {
        Map<String, Integer> totals = new HashMap<>();
        for (Expense e : list) {
            totals.put(e.getCategory(), totals.getOrDefault(e.getCategory(), 0) + e.getAmount());
        }
        return totals;
    }

    private String callOpenAi(String prompt) throws IOException, InterruptedException {
        if (openAiKey == null || openAiKey.isBlank()) {
            // fallback: return local computed short answer if key missing
            return "OpenAI key not configured. Computed summary: " + (prompt.length() > 300 ? prompt.substring(0, 300) + "..." : prompt);
        }

        // Build request payload for Chat Completions
        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(message));
        payload.put("max_tokens", 200);
        payload.put("temperature", 0.6);

        String body = mapper.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            return "OpenAI request failed: " + resp.statusCode() + " - " + resp.body();
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode content = root.at("/choices/0/message/content");
        if (content.isMissingNode()) {
            // fallback older path
            content = root.at("/choices/0/text");
        }
        return content.asText();
    }
}
