package com.quicksplit.expense;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByGroupIdOrderByCreatedAtDesc(Long groupId);
}
