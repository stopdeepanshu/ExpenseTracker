package com.example.ExpenseTracker.Ai;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/savings-suggestion")
    public AiResponse savingsSuggestion(@RequestParam(required = false) String month) throws IOException, InterruptedException {
        String reply = aiService.savingsSuggestion(month);
        return new AiResponse(reply);
    }

    @GetMapping("/spending-insights")
    public AiResponse spendingInsights(@RequestParam(required = false) String month) throws IOException, InterruptedException {
        String reply = aiService.spendingInsights(month);
        return new AiResponse(reply);
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AiResponse chat(@RequestBody AiRequest request) throws IOException, InterruptedException {
        String reply = aiService.chat(request.getMessage(), request.getMonth());
        return new AiResponse(reply);
    }
}
