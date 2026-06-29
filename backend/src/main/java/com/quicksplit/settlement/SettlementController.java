package com.quicksplit.settlement;

import com.quicksplit.security.AppUserPrincipal;
import com.quicksplit.settlement.dto.GroupSettlementDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint que expone los balances y el plan de pagos minimo de un grupo.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
@Tag(name = "Balances", description = "Balances y plan de pagos minimo para saldar el grupo")
public class SettlementController {

    private final BalanceService balanceService;

    public SettlementController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    @Operation(summary = "Obtener balances y transferencias sugeridas para saldar el grupo")
    public ResponseEntity<GroupSettlementDto> settlement(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.getSettlement(principal.getId(), groupId));
    }
}
