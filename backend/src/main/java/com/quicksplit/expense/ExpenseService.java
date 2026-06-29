package com.quicksplit.expense;

import com.quicksplit.common.BadRequestException;
import com.quicksplit.common.NotFoundException;
import com.quicksplit.expense.dto.CreateExpenseRequest;
import com.quicksplit.expense.dto.ExpenseDto;
import com.quicksplit.expense.dto.ShareInput;
import com.quicksplit.group.Group;
import com.quicksplit.group.GroupService;
import com.quicksplit.user.User;
import com.quicksplit.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de los gastos: creacion (con reparto) y listado.
 */
@Service
public class ExpenseService {

    private static final BigDecimal CENT_TOLERANCE = new BigDecimal("0.01");

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            GroupService groupService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
    }

    @Transactional
    public ExpenseDto createExpense(Long requesterId, Long groupId, CreateExpenseRequest request) {
        Group group = groupService.requireMembership(requesterId, groupId);

        Set<Long> memberIds = groupService.membersOf(groupId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        if (!memberIds.contains(request.paidByUserId())) {
            throw new BadRequestException("Quien pago debe ser miembro del grupo");
        }

        Map<Long, BigDecimal> shareByUser = resolveShares(request, memberIds);

        User paidBy = userRepository.findById(request.paidByUserId())
                .orElseThrow(() -> new NotFoundException("Usuario pagador no encontrado"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setPaidBy(paidBy);
        expense.setCreatedAt(Instant.now());

        for (Map.Entry<Long, BigDecimal> entry : shareByUser.entrySet()) {
            User user = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NotFoundException("Participante no encontrado"));
            ExpenseShare share = new ExpenseShare();
            share.setExpense(expense);
            share.setUser(user);
            share.setAmount(entry.getValue());
            expense.getShares().add(share);
        }

        Expense saved = expenseRepository.save(expense);
        return ExpenseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> listExpenses(Long requesterId, Long groupId) {
        groupService.requireMembership(requesterId, groupId);
        return expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(ExpenseDto::from)
                .toList();
    }

    /** Calcula el monto que le corresponde a cada participante segun el tipo de division. */
    private Map<Long, BigDecimal> resolveShares(CreateExpenseRequest request, Set<Long> memberIds) {
        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);

        if (request.splitType() == SplitType.EXACT) {
            return resolveExactShares(request.shares(), amount, memberIds);
        }
        return resolveEqualShares(request.participantUserIds(), amount, memberIds);
    }

    private Map<Long, BigDecimal> resolveExactShares(
            List<ShareInput> shares, BigDecimal amount, Set<Long> memberIds) {
        if (shares == null || shares.isEmpty()) {
            throw new BadRequestException("Debe indicar los montos por participante (shares)");
        }
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ShareInput share : shares) {
            if (!memberIds.contains(share.userId())) {
                throw new BadRequestException("El participante " + share.userId()
                        + " no es miembro del grupo");
            }
            if (result.containsKey(share.userId())) {
                throw new BadRequestException("Participante duplicado: " + share.userId());
            }
            BigDecimal value = share.amount().setScale(2, RoundingMode.HALF_UP);
            result.put(share.userId(), value);
            total = total.add(value);
        }
        if (total.subtract(amount).abs().compareTo(CENT_TOLERANCE) > 0) {
            throw new BadRequestException(
                    "La suma de las partes (" + total + ") no coincide con el monto (" + amount + ")");
        }
        return result;
    }

    private Map<Long, BigDecimal> resolveEqualShares(
            List<Long> participantUserIds, BigDecimal amount, Set<Long> memberIds) {
        List<Long> participants = (participantUserIds == null || participantUserIds.isEmpty())
                ? new ArrayList<>(memberIds)
                : participantUserIds;

        if (participants.isEmpty()) {
            throw new BadRequestException("No hay participantes para repartir el gasto");
        }
        for (Long id : participants) {
            if (!memberIds.contains(id)) {
                throw new BadRequestException("El participante " + id + " no es miembro del grupo");
            }
        }
        // Distribuye en partes iguales repartiendo los centavos sobrantes de forma deterministica.
        List<Long> distinct = participants.stream().distinct().toList();
        long totalCents = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        int n = distinct.size();
        long base = totalCents / n;
        long remainder = totalCents % n;

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            long cents = base + (i < remainder ? 1 : 0);
            result.put(distinct.get(i), BigDecimal.valueOf(cents).movePointLeft(2));
        }
        return result;
    }
}
