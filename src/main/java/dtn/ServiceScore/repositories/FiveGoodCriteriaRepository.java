package dtn.ServiceScore.repositories;

import dtn.ServiceScore.model.FiveGoodCriteria;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FiveGoodCriteriaRepository extends JpaRepository<FiveGoodCriteria, Long> {
    Optional<FiveGoodCriteria> findByName(String name);

    List<FiveGoodCriteria> findByIsActiveTrue();

    List<FiveGoodCriteria> findBySemesterId(Long semesterId,  Sort sort);
}
