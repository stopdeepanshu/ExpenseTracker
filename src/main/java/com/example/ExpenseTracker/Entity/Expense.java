package com.example.ExpenseTracker.Entity;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Expense {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
	String title;
	int amount;
	String category;
	@PastOrPresent(message = "Expense date cannot be in the future")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;
	String description;

}
