package com.quicksplit.group;

import com.quicksplit.group.dto.AddMemberRequest;
import com.quicksplit.group.dto.CreateGroupRequest;
import com.quicksplit.group.dto.GroupDto;
import com.quicksplit.group.dto.GroupSummaryDto;
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
 * Endpoints de grupos y de gestion de miembros.
 */
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grupos", description = "Creacion y gestion de grupos de gastos")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo grupo (el creador queda como miembro)")
    public ResponseEntity<GroupDto> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupDto group = groupService.createGroup(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping
    @Operation(summary = "Listar los grupos del usuario autenticado")
    public ResponseEntity<List<GroupSummaryDto>> myGroups(
            @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(groupService.listMyGroups(principal.getId()));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Obtener el detalle de un grupo y sus miembros")
    public ResponseEntity<GroupDto> detail(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(principal.getId(), groupId));
    }

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Agregar un usuario existente al grupo por su email")
    public ResponseEntity<GroupDto> addMember(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(groupService.addMember(principal.getId(), groupId, request));
    }
}
