package dtn.ServiceScore.repositories;

import dtn.ServiceScore.model.ExternalEvent;
import dtn.ServiceScore.utils.Enums.ExternalEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExternalEventRepository extends JpaRepository<ExternalEvent, Long> {
    List<ExternalEvent> findByStatus(ExternalEventStatus status);

    List<ExternalEvent> findByUserId(Long userId);

    @Query("SELECT e FROM ExternalEvent e " +
            "WHERE e.user.id = :userId " +
            "AND e.status = :status " +
            "AND (:semesterId IS NULL OR e.semester.id = :semesterId)")
    List<ExternalEvent> findApprovedEventsByUserAndOptionalSemester(
            @Param("userId") Long userId,
            @Param("status") ExternalEventStatus status,
            @Param("semesterId") Long semesterId
    );

}
