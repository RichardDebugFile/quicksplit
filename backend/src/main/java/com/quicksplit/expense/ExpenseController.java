package com.quicksplit.expense;

import com.quicksplit.expense.dto.CreateExpenseRequest;
import com.quicksplit.expense.dto.ExpenseDto;
import com.quicksplit.security.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de gastos dentro de un grupo.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@Tag(name = "Gastos", description = "Registro y consulta de gastos por grupo")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @Operation(summary = "Registrar un gasto en el grupo")
    public ResponseEntity<ExpenseDto> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseDto expense = expenseService.createExpense(principal.getId(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @GetMapping
    @Operation(summary = "Listar los gastos del grupo")
    public ResponseEntity<List<ExpenseDto>> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.listExpenses(principal.getId(), groupId));
    }
}
