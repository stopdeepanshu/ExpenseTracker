package com.example.ExpenseTracker.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ExpenseTracker.Entity.Expense;

public interface MyRepository extends JpaRepository<Expense, Integer> {

	    List<Expense> findTop5ByOrderByDateDesc();
    // Fetch expenses of a specific month & year
    @Query("SELECT e FROM Expense e WHERE MONTH(e.date) = :month AND YEAR(e.date) = :year")
    List<Expense> getExpensesByMonth(@Param("month") int month, @Param("year") int year);

    // Sum of amount for month
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE MONTH(e.date) = :month AND YEAR(e.date) = :year")
    Double getTotalMonthlyAmount(@Param("month") int month, @Param("year") int year);
    
    @Query("SELECT e.category, SUM(e.amount) " +
            "FROM Expense e " +
            "WHERE FUNCTION('MONTH', e.date) = :month AND FUNCTION('YEAR', e.date) = :year " +
            "GROUP BY e.category")
     List<Object[]> getCategoryTotalsByMonth(@Param("month") int month, @Param("year") int year);

     List<Expense> findByDateBetween(LocalDate start, LocalDate end);

     
}
