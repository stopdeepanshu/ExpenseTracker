package com.example.ExpenseTracker.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;              
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.MyRepository;

@Service
public class MonthlySummaryService {

	@Autowired
	private MyRepository repo;

	public List<Expense> getMonthlyExpenses(int month, int year) {
		return repo.getExpensesByMonth(month, year);
	}

	public double getMonthlyTotal(int month, int year) {
		Double total = repo.getTotalMonthlyAmount(month, year);
		return total == null ? 0 : total;
	}
	
	 public Map<String, Double> getCategoryTotalsByMonth(int month, int year) {
	        List<Object[]> list = repo.getCategoryTotalsByMonth(month, year);
	        Map<String, Double> map = new LinkedHashMap<>(); 


	        for (Object[] row : list) {
	            String category = row[0] == null ? "Uncategorized" : row[0].toString();
	            Double total = 0.0;
	            if (row[1] != null) {
	                total = ((Number) row[1]).doubleValue();
	            }
	            map.put(category, total);
	        }
	        return map;
	    }
}
