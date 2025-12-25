package com.example.ExpenseTracker.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Service.GenService;
import com.example.ExpenseTracker.Service.MonthlySummaryService;

import jakarta.validation.Valid;

import org.springframework.ui.Model;

@Controller
public class GenController {

	@Autowired
	GenService ser;
	
	@Autowired
	MonthlySummaryService summaryService;
	
	@GetMapping("/")
	public String home(Model model) {

		return ser.home(model);
	}

	@GetMapping("/add-expense")
	public String addexpense() {
		return "add-expense";
	}
	
	@PostMapping("/save-expense")
	public String saveExpense(@Valid @ModelAttribute Expense expense,
            BindingResult result,
            ModelMap map) {
		   if (result.hasErrors()) {
		        map.put("error", result.getFieldError().getDefaultMessage());
		        return "add-expense";   
		    }
		return ser.saveExpense(expense,map);
	}
	
	@GetMapping("/view-expenses")
	public String showExpense(ModelMap map) {
		return ser.getAllExpense(map);
	}
	
	@GetMapping("/update/{id}")
	public String updateExpense(@PathVariable int id,ModelMap map) {
		return ser.getExpenseById(id, map);
	}
	
	@PostMapping("/update-Expense")
	public String updateExpense(@ModelAttribute Expense expense,ModelMap map) {
		return ser.updateExpense(expense,map);
	}
	
	@GetMapping("/delete/{id}")
	public String deleteExpense(@PathVariable int id, ModelMap map) {
		return ser.deleteByID(map, id);
	}
	
	@GetMapping("/summary")
	public String summary(Model model) {
       
		LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Expense> monthlyExpenses = summaryService.getMonthlyExpenses(month, year);
        double monthlyTotal = summaryService.getMonthlyTotal(month, year);

        model.addAttribute("month", today.getMonth().toString()); 
        model.addAttribute("monthlyExpenses", monthlyExpenses);
        model.addAttribute("monthlyTotal", monthlyTotal);

        return "monthly-summary";
    }
	
	@GetMapping("/monthlySummary")
	public String showMonthlySummary(@RequestParam("month") String month, Model model) {
		 int selectedYear = Integer.parseInt(month.substring(0, 4));
		 int selectedMonth = Integer.parseInt(month.substring(5, 7));
		 List<Expense> monthlyExpenses =summaryService.getMonthlyExpenses(selectedMonth, selectedYear);
		 double monthlyTotal =summaryService.getMonthlyTotal(selectedMonth, selectedYear);
		  
		 
		 // ⭐ NEW: Fetch category-wise totals
		    Map<String, Double> categoryTotals =
		            summaryService.getCategoryTotalsByMonth(selectedMonth, selectedYear);
		    model.addAttribute("categoryTotals", categoryTotals);  
		 
		 
		    model.addAttribute("monthlyExpenses", monthlyExpenses);
		    model.addAttribute("monthlyTotal", monthlyTotal);
		    model.addAttribute("selectedMonth", month);
		 return "monthly-summary";
	}
}
