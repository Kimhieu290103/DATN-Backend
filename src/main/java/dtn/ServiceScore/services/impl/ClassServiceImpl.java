package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.dtos.ClassDTO;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    @Override
    public List<Class> getAllClass() {
        return classRepository.findAll();
    }

    @Override
    public List<Class> getClasses(ClassSearchRequest request) {
        if (request.getDepartmentId() != null && request.getCourseId() != null) {
            return classRepository.findByDepartmentIdAndCourseId(request.getDepartmentId(), request.getCourseId());
        } else if (request.getDepartmentId() != null) {
            return classRepository.findByDepartmentId(request.getDepartmentId());
        } else if (request.getCourseId() != null) {
            return classRepository.findByCourseId(request.getCourseId());
        } else {
            return classRepository.findAll(); // Trả về tất cả nếu không có điều kiện nào
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

}
