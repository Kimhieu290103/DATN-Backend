package dtn.ServiceScore.repositories;

import dtn.ServiceScore.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class, Long> {
    Optional<Class> findByName(String name);

    List<Class> findByCourseIdAndStatusTrue(Long courseId, Sort sort);

    List<Class> findByDepartmentIdAndStatusTrue(Long departmentId,  Sort sort);

    List<Class> findByDepartmentIdAndCourseIdAndStatusTrue(Long departmentId, Long courseId,  Sort sort);

    // Truy vấn lấy tất cả các lớp có status = true và sắp xếp theo id
    @Query("SELECT c FROM Class c WHERE c.status = true ORDER BY c.id ASC")
    List<Class> findAllByStatusTrue(Sort sort);
}
