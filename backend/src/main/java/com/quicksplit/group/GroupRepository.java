package com.quicksplit.group;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    /** Grupos en los que el usuario es miembro (incluye los que creo). */
    @Query("SELECT DISTINCT g FROM Group g JOIN g.members m WHERE m.user.id = :userId ORDER BY g.createdAt DESC")
    List<Group> findAllByMemberId(@Param("userId") Long userId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :id")
    Optional<Group> findByIdWithMembers(@Param("id") Long id);
}
