package dtn.ServiceScore.repositories;

import dtn.ServiceScore.model.FiveGoodCriteria;
import dtn.ServiceScore.model.FiveGoodCriteriaLcd;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiveGoodCriteriaLcdRepository extends JpaRepository<FiveGoodCriteriaLcd, Long> {
    List<FiveGoodCriteriaLcd> findByIsActiveTrue();

    List<FiveGoodCriteriaLcd> findBySemesterId(Long semesterId, Sort sort);
}
