package com.quicksplit.expense.dto;

import com.quicksplit.expense.Expense;
import com.quicksplit.user.dto.UserDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Detalle de un gasto para la respuesta de la API.
 */
public record ExpenseDto(
        Long id,
        String description,
        BigDecimal amount,
        UserDto paidBy,
        Instant createdAt,
        List<ShareDto> shares) {

    public static ExpenseDto from(Expense expense) {
        List<ShareDto> shareDtos = expense.getShares().stream()
                .map(s -> new ShareDto(UserDto.from(s.getUser()), s.getAmount()))
                .toList();
        return new ExpenseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                UserDto.from(expense.getPaidBy()),
                expense.getCreatedAt(),
                shareDtos);
    }
}
