package com.example.ExpenseTracker.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.MyRepository;


@Service
public class GenService {
	
	@Autowired
	MyRepository repo;
	
	@Autowired
	MonthlySummaryService summaryService;

	public String saveExpense(Expense expense, ModelMap map) {
		repo.save(expense);
		map.put("mssg", "Expense saved succesfully");
		map.put("data",repo.findAll());
		return "redirect:/view-expenses";
	}

	public String getAllExpense(ModelMap map) {
		map.put("data",repo.findAll());
		return "view-expenses";
	}

	public String getExpenseById(int id, ModelMap map) {
		Expense expense=repo.findById(id).orElse(null);
		map.put("exp", expense);
		return "edit-expense";
	}

	public String updateExpense(Expense expense, ModelMap map) {
		repo.save(expense);
		map.put("data",repo.findAll());
		return "view-expenses";
	}

	public String deleteByID(ModelMap map, int id) {
		repo.deleteById(id);
		map.put("data",repo.findAll());
		return "view-expenses";
	}

	public String home(Model model) {

		LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        double monthlyTotal = summaryService.getMonthlyTotal(month, year);

        model.addAttribute("monthlyTotal", monthlyTotal);

		
		List<Expense> fiveExp=repo.findTop5ByOrderByDateDesc();
		model.addAttribute("homeData", fiveExp);
		return "home";
	}
}
