package com.quicksplit.group;

import com.quicksplit.common.ConflictException;
import com.quicksplit.common.ForbiddenException;
import com.quicksplit.common.NotFoundException;
import com.quicksplit.group.dto.AddMemberRequest;
import com.quicksplit.group.dto.CreateGroupRequest;
import com.quicksplit.group.dto.GroupDto;
import com.quicksplit.group.dto.GroupSummaryDto;
import com.quicksplit.user.User;
import com.quicksplit.user.UserRepository;
import com.quicksplit.user.dto.UserDto;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de los grupos: creacion, listado, detalle y gestion de miembros.
 */
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupDto createGroup(Long ownerId, CreateGroupRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Group group = new Group();
        group.setName(request.name().trim());
        group.setDescription(request.description() == null ? null : request.description().trim());
        group.setOwner(owner);
        group.setCreatedAt(Instant.now());
        Group saved = groupRepository.save(group);

        addMembership(saved, owner);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryDto> listMyGroups(Long userId) {
        return groupRepository.findAllByMemberId(userId).stream()
                .map(g -> new GroupSummaryDto(
                        g.getId(),
                        g.getName(),
                        g.getDescription(),
                        memberRepository.findAllByGroupId(g.getId()).size(),
                        g.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDto getGroupDetail(Long userId, Long groupId) {
        Group group = requireMembership(userId, groupId);
        return toDto(group);
    }

    @Transactional
    public GroupDto addMember(Long requesterId, Long groupId, AddMemberRequest request) {
        Group group = requireMembership(requesterId, groupId);

        String email = request.email().trim().toLowerCase();
        User target = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el email " + email));

        if (memberRepository.existsByGroupIdAndUserId(groupId, target.getId())) {
            throw new ConflictException("El usuario ya es miembro del grupo");
        }
        addMembership(group, target);
        return toDto(group);
    }

    /**
     * Devuelve el grupo si el usuario es miembro; si no, lanza la excepcion correspondiente.
     * Reutilizado por los servicios de gastos y balances.
     */
    @Transactional(readOnly = true)
    public Group requireMembership(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));
        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("No eres miembro de este grupo");
        }
        return group;
    }

    /** Lista de usuarios miembros del grupo. */
    @Transactional(readOnly = true)
    public List<User> membersOf(Long groupId) {
        return memberRepository.findAllByGroupId(groupId).stream()
                .map(GroupMember::getUser)
                .toList();
    }

    private void addMembership(Group group, User user) {
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setJoinedAt(Instant.now());
        memberRepository.save(member);
    }

    private GroupDto toDto(Group group) {
        List<UserDto> members = memberRepository.findAllByGroupId(group.getId()).stream()
                .map(m -> UserDto.from(m.getUser()))
                .toList();
        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getOwner().getId(),
                group.getCreatedAt(),
                members);
    }
}
