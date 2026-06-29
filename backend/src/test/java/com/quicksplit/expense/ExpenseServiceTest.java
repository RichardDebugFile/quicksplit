package com.quicksplit.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.quicksplit.common.BadRequestException;
import com.quicksplit.expense.dto.CreateExpenseRequest;
import com.quicksplit.expense.dto.ExpenseDto;
import com.quicksplit.expense.dto.ShareDto;
import com.quicksplit.expense.dto.ShareInput;
import com.quicksplit.group.Group;
import com.quicksplit.group.GroupService;
import com.quicksplit.user.User;
import com.quicksplit.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas del servicio de gastos: reparto equitativo al centavo y validaciones.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long GROUP_ID = 7L;

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupService groupService;

    @InjectMocks
    private ExpenseService expenseService;

    private User ana;
    private User beto;
    private User caro;

    @BeforeEach
    void setUp() {
        ana = User.builder().id(1L).name("Ana").email("ana@x.com").build();
        beto = User.builder().id(2L).name("Beto").email("beto@x.com").build();
        caro = User.builder().id(3L).name("Caro").email("caro@x.com").build();

        Group group = new Group();
        group.setId(GROUP_ID);

        when(groupService.requireMembership(anyLong(), any())).thenReturn(group);
        when(groupService.membersOf(GROUP_ID)).thenReturn(List.of(ana, beto, caro));
        // El guardado solo ocurre en los casos exitosos; lenient evita el error de stub no usado.
        lenient().when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubUserLookups() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(ana));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(beto));
        when(userRepository.findById(3L)).thenReturn(java.util.Optional.of(caro));
    }

    @Test
    @DisplayName("Split EQUAL reparte los centavos sobrantes de forma exacta")
    void equalSplitDistributesCents() {
        stubUserLookups();
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Cena", new BigDecimal("10.00"), 1L, SplitType.EQUAL,
                List.of(1L, 2L, 3L), null);

        ExpenseDto dto = expenseService.createExpense(1L, GROUP_ID, request);

        Map<Long, BigDecimal> byUser = byUser(dto);
        // 10.00 / 3 -> 3.34, 3.33, 3.33 (el centavo extra al primero)
        assertThat(byUser.get(1L)).isEqualByComparingTo("3.34");
        assertThat(byUser.get(2L)).isEqualByComparingTo("3.33");
        assertThat(byUser.get(3L)).isEqualByComparingTo("3.33");
        assertThat(sum(dto)).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Split EQUAL sin participantes usa a todo el grupo")
    void equalSplitDefaultsToAllMembers() {
        stubUserLookups();
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Taxi", new BigDecimal("9.00"), 1L, SplitType.EQUAL, null, null);

        ExpenseDto dto = expenseService.createExpense(1L, GROUP_ID, request);

        assertThat(dto.shares()).hasSize(3);
        assertThat(sum(dto)).isEqualByComparingTo("9.00");
    }

    @Test
    @DisplayName("Split EXACT valida que las partes sumen el total")
    void exactSplitMustSumTotal() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Compra", new BigDecimal("10.00"), 1L, SplitType.EXACT, null,
                List.of(new ShareInput(1L, new BigDecimal("5.00")),
                        new ShareInput(2L, new BigDecimal("3.00"))));

        assertThatThrownBy(() -> expenseService.createExpense(1L, GROUP_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    @DisplayName("Quien paga debe ser miembro del grupo")
    void payerMustBeMember() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Hotel", new BigDecimal("20.00"), 99L, SplitType.EQUAL, List.of(1L, 2L), null);

        assertThatThrownBy(() -> expenseService.createExpense(1L, GROUP_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("miembro");
    }

    @Test
    @DisplayName("Un participante ajeno al grupo es rechazado")
    void participantMustBeMember() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "Cena", new BigDecimal("10.00"), 1L, SplitType.EQUAL, List.of(1L, 42L), null);

        assertThatThrownBy(() -> expenseService.createExpense(1L, GROUP_ID, request))
                .isInstanceOf(BadRequestException.class);
    }

    private static Map<Long, BigDecimal> byUser(ExpenseDto dto) {
        return dto.shares().stream()
                .collect(java.util.stream.Collectors.toMap(s -> s.user().id(), ShareDto::amount));
    }

    private static BigDecimal sum(ExpenseDto dto) {
        return dto.shares().stream().map(ShareDto::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
