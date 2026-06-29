package com.quicksplit.settlement;

import com.quicksplit.expense.Expense;
import com.quicksplit.expense.ExpenseRepository;
import com.quicksplit.expense.ExpenseShare;
import com.quicksplit.group.GroupService;
import com.quicksplit.settlement.dto.BalanceDto;
import com.quicksplit.settlement.dto.GroupSettlementDto;
import com.quicksplit.settlement.dto.SettlementTransactionDto;
import com.quicksplit.user.User;
import com.quicksplit.user.dto.UserDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcula los balances de un grupo a partir de sus gastos y produce el plan de pagos
 * minimo usando el {@link SettlementCalculator}.
 */
@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;
    private final SettlementCalculator settlementCalculator;

    public BalanceService(
            ExpenseRepository expenseRepository,
            GroupService groupService,
            SettlementCalculator settlementCalculator) {
        this.expenseRepository = expenseRepository;
        this.groupService = groupService;
        this.settlementCalculator = settlementCalculator;
    }

    @Transactional(readOnly = true)
    public GroupSettlementDto getSettlement(Long requesterId, Long groupId) {
        groupService.requireMembership(requesterId, groupId);

        List<User> members = groupService.membersOf(groupId);
        Map<Long, User> userById = new LinkedHashMap<>();
        for (User member : members) {
            userById.put(member.getId(), member);
        }

        Map<Long, BigDecimal> balances = computeBalances(groupId, members);

        List<BalanceDto> balanceDtos = balances.entrySet().stream()
                .map(e -> new BalanceDto(UserDto.from(userById.get(e.getKey())), e.getValue()))
                .sorted(Comparator.comparing((BalanceDto b) -> b.balance()).reversed())
                .toList();

        List<SettlementTransactionDto> transactions = settlementCalculator.simplify(balances).stream()
                .map(t -> new SettlementTransactionDto(
                        UserDto.from(userById.get(t.fromUserId())),
                        UserDto.from(userById.get(t.toUserId())),
                        t.amount()))
                .toList();

        return new GroupSettlementDto(balanceDtos, transactions);
    }

    /**
     * Balance neto por usuario: lo que pago menos lo que le corresponde pagar.
     * Los miembros sin movimientos quedan con balance cero.
     */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> computeBalances(Long groupId, List<User> members) {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        for (User member : members) {
            balances.put(member.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        for (Expense expense : expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId)) {
            Long payerId = expense.getPaidBy().getId();
            balances.merge(payerId, expense.getAmount(), BigDecimal::add);
            for (ExpenseShare share : expense.getShares()) {
                balances.merge(share.getUser().getId(), share.getAmount().negate(), BigDecimal::add);
            }
        }

        balances.replaceAll((id, value) -> value.setScale(2, RoundingMode.HALF_UP));
        return balances;
    }
}
