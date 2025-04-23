package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.dtos.ClassDTO;
import dtn.ServiceScore.dtos.ClassMoDTO;
import dtn.ServiceScore.dtos.ClassSearchRequest;
import dtn.ServiceScore.model.Class;
import dtn.ServiceScore.model.Course;
import dtn.ServiceScore.model.Department;
import dtn.ServiceScore.repositories.ClassRepository;
import dtn.ServiceScore.repositories.CourseRepository;
import dtn.ServiceScore.repositories.DepartmentRepository;
import dtn.ServiceScore.services.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    @Override
    public List<Class> getAllClass() {
        return classRepository.findAllByStatusTrue(Sort.by(Sort.Order.asc("id")));
    }

    @Override
    public List<Class> getClasses(ClassSearchRequest request) {
        if (request.getDepartmentId() != null && request.getCourseId() != null) {
            return classRepository.findByDepartmentIdAndCourseIdAndStatusTrue(request.getDepartmentId(),
                    request.getCourseId(),

                    Sort.by(Sort.Order.asc("id"))  );
        } else if (request.getDepartmentId() != null) {
            return classRepository.findByDepartmentIdAndStatusTrue(request.getDepartmentId(),

                    Sort.by(Sort.Order.asc("id")));
        } else if (request.getCourseId() != null) {
            return classRepository.findByCourseIdAndStatusTrue(request.getCourseId(),

                    Sort.by(Sort.Order.asc("id")));
        } else {
            return classRepository.findAll(Sort.by(Sort.Order.asc("id"))); // Trả về tất cả nếu không có điều kiện nào
        }
    }

    @Override
    public void createClass(ClassDTO classDTO) {
        Department department = departmentRepository.findById(classDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));

        Course course = courseRepository.findByName(classDTO.getCourse())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        Class newClass = Class.builder()
                .name(classDTO.getName())
                .department(department)
                .course(course)
                .status(classDTO.isStatus())
                .build();

        classRepository.save(newClass);
    }

    @Override
    public Class updateClass(Long id, ClassMoDTO classDTO) {
        Optional<Class> existingClassOpt = classRepository.findById(id);
        if (existingClassOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lớp với ID: " + id);
        }

        Class existingClass = existingClassOpt.get();

        // Load department and course by ID
        Department department = departmentRepository.findById(classDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa với ID: " + classDTO.getDepartmentId()));

        Course course = courseRepository.findById(classDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + classDTO.getCourseId()));

        // Set values from DTO
        existingClass.setName(classDTO.getName());
        existingClass.setDepartment(department);
        existingClass.setCourse(course);
        existingClass.setStatus(classDTO.isStatus());

        return classRepository.save(existingClass);
    }

    @Override
    public Class updateClassStatusToFalse(Long classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isPresent()) {
            Class clazz = classOptional.get();
            clazz.setStatus(false);  // Đặt status thành false
            return classRepository.save(clazz);  // Lưu lại thay đổi vào cơ sở dữ liệu
        } else {
            throw new RuntimeException("Không tìm thấy lớp với ID: " + classId);
        }

    }

}
