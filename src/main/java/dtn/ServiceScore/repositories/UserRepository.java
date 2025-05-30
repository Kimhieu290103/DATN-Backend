package dtn.ServiceScore.repositories;

import dtn.ServiceScore.model.Class;
import dtn.ServiceScore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByStudentId(String studentId);
    // Trong UserRepository
    Optional<User> findByUsernameOrStudentId(String username, String studentId);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByClazz(Class clazzId);

    @Query("SELECT u FROM User u WHERE " +
            "(:classId IS NULL OR u.clazz.id = :classId) AND " +
            "(:courseId IS NULL OR u.clazz.course.id = :courseId) AND " +
            "(:departmentId IS NULL OR u.clazz.department.id = :departmentId) AND " +
            "u.role.name = 'SV'"
    )
    List<User> findByFilters(@Param("classId") Long classId,
                             @Param("courseId") Integer courseId,
                             @Param("departmentId") Integer departmentId);


    // lấy điểm sinh viên quản lí điểm ở trang cộng tác sinh viên
    @Query("SELECT u FROM User u WHERE " +
            "(:classId IS NULL OR u.clazz.id = :classId) AND " +
            "(:courseId IS NULL OR u.clazz.course.id = :courseId) AND " +
            "(:departmentId IS NULL OR u.clazz.department.id = :departmentId) AND " +
            "u.role.name = 'SV'")
    Page<User> findByFilters(@Param("classId") Long classId,
                             @Param("courseId") Integer courseId,
                             @Param("departmentId") Integer departmentId,
                             Pageable pageable);


    Page<User>  findAllByRole_NameAndIsActiveTrue(String roleName, Pageable pageable);

    Page<User>  findAllByRole_Name(String roleName, Pageable pageable);
    List<User> findAllByRole_NameNotIn(List<String> roleNames);

    @Query("SELECT u FROM User u " +
            "JOIN u.clazz c " +
            "WHERE u.role.name = 'SV' AND (" +
            "u.fullname LIKE %:search% OR " +
            "u.phoneNumber LIKE %:search% OR " +
            "u.studentId LIKE %:search% OR " +
            "u.email LIKE %:search% OR " +
            "u.username LIKE %:search% OR " +
            "c.name LIKE %:search%)")
    Page<User> searchUsersPaginated(@Param("search") String search, Pageable pageable);




}
