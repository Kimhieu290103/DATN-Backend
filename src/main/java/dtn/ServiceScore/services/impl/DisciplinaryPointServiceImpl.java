package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.model.*;
import dtn.ServiceScore.repositories.*;
import dtn.ServiceScore.responses.PointResponse;
import dtn.ServiceScore.responses.StudentPointResponse;
import dtn.ServiceScore.services.DisciplinaryPointService;
import dtn.ServiceScore.utils.Enums.ExternalEventStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DisciplinaryPointServiceImpl implements DisciplinaryPointService {
    private final DisciplinaryPointRepository disciplinaryPointRepository;
    private final RegistrationRepository registrationRepository;
    private final EventCriteriaRepository eventCriteriaRepository;
    private final StudentCriteriaRepository studentCriteriaRepository;
    private final ExternalEventRepository externalEventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DisciplinaryPoint Addpoint(User user, Event event) {
        Registration registration = registrationRepository.findByUserAndEvent(user, event);
        // Nếu không tìm thấy bản đăng ký -> Trả về lỗi
        if (registration == null) {
            throw new IllegalStateException("Người dùng chưa đăng ký sự kiện này.");
        }

        if (registration.isAttendances()) {
            return null;
        }

        DisciplinaryPoint disciplinaryPoint = disciplinaryPointRepository.findByUserAndSemester(user, event.getSemester())
                .orElseGet(() -> {
                    // Nếu chưa có, tạo bản ghi mới
                    DisciplinaryPoint newPoint = DisciplinaryPoint.builder()
                            .user(user)
                            .semester(event.getSemester())
                            .points(0L) // Set điểm event
                            .build();
                    return disciplinaryPointRepository.save(newPoint);
                });

        if (disciplinaryPoint.getId() != null) {
            disciplinaryPoint.setPoints(disciplinaryPoint.getPoints() + event.getScore());
        }

        List<EventCriteria> eventCriteriaList = eventCriteriaRepository.findByEventId(event.getId());
        for (EventCriteria eventCriteria : eventCriteriaList) {
            StudentCriteria studentCriteria = StudentCriteria.builder()
                    .student(user)
                    .criteria(eventCriteria.getCriteria())
                    .semester(event.getSemester())// Liên kết với tiêu chí 5 tốt
                    .achievedAt(LocalDate.now())
                    .isCompleted(true)
                    .build();

            studentCriteriaRepository.save(studentCriteria);
        }

        // Cập nhật trạng thái đã điểm danh
        registration.setAttendances(true);
        registrationRepository.save(registration);
        return disciplinaryPointRepository.save(disciplinaryPoint);
    }


    // Duyệt sự kiên sinh viên bên ngoài
    @Override
    @Transactional
    public DisciplinaryPoint AddPointForExternalEvent(User user, ExternalEvent externalEvent) {

        // Nếu sự kiện đã được duyệt, ném exception để Controller xử lý
        if (externalEvent.getStatus() == ExternalEventStatus.APPROVED) {
            throw new IllegalStateException("Sự kiện này đã được duyệt trước đó.");
        }
        // Kiểm tra xem đã có bản ghi trong bảng DisciplinaryPoint chưa
        DisciplinaryPoint disciplinaryPoint = disciplinaryPointRepository.findByUserAndSemester(user, externalEvent.getSemester())
                .orElse(null);


        if (disciplinaryPoint == null) {
            // Nếu chưa có, tạo mới bản ghi
            DisciplinaryPoint newPoint = DisciplinaryPoint.builder()
                    .user(user)
                    .semester(externalEvent.getSemester())
                    .points(externalEvent.getPoints()) // Lấy điểm từ ExternalEvent
                    .build();

            // Cập nhật trạng thái của ExternalEvent thành APPROVED
            externalEvent.setStatus(ExternalEventStatus.APPROVED);
            externalEventRepository.save(externalEvent);

            // Lưu vào database
            return disciplinaryPointRepository.save(newPoint);

        } else {
            // Nếu đã có bản ghi, cộng thêm điểm của externalEvent vào điểm hiện tại
            disciplinaryPoint.setPoints(disciplinaryPoint.getPoints() + externalEvent.getPoints());
        }

        // Cập nhật trạng thái của ExternalEvent thành APPROVED
        externalEvent.setStatus(ExternalEventStatus.APPROVED);
        externalEventRepository.save(externalEvent);

        return disciplinaryPointRepository.save(disciplinaryPoint);
    }

    // từ chối sự kiện sinh viên đề xuất bên ngoài
    @Override
    public String rejectExternalEvent(Long eventId) {
        ExternalEvent externalEvent = externalEventRepository.findById(eventId).orElse(null);
        if (externalEvent == null) {
            return "External Event not found";
        }

        externalEvent.setStatus(ExternalEventStatus.REJECTED);
        externalEventRepository.save(externalEvent);

        return "External Event has been rejected.";
    }

    // điêmr danh tất cả sinh viên
    @Override
    public Map<String, Object> addPointsForAllRegisteredUsers(Event event) {
        List<Registration> registrations = registrationRepository.findByEvent(event);
        List<DisciplinaryPoint> disciplinaryPoints = new ArrayList<>();
        List<Long> skippedUsers = new ArrayList<>();

        for (Registration registration : registrations) {
            User user = registration.getUser();
            DisciplinaryPoint disciplinaryPoint = Addpoint(user, event);

            if (disciplinaryPoint == null) {
                skippedUsers.add(user.getId());
            } else {
                disciplinaryPoints.add(disciplinaryPoint);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("disciplinaryPoints", disciplinaryPoints);
        response.put("skippedUsers", skippedUsers);
        return response;
    }


    @Override
    public List<PointResponse> getDisciplinaryPointsByUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        List<DisciplinaryPoint> disciplinaryPoints = disciplinaryPointRepository.findByUser_Id(userId);
        // Tạo danh sách kết quả

        return disciplinaryPoints.stream()
                .map(dp -> PointResponse.builder()
                        .id(dp.getId())
                        .semester(dp.getSemester().getName())
                        .points(dp.getPoints())
                        .build())
                .toList();
    }

    @Override
    public Map<String, Object> getDisciplinaryPointsWithTotal() {
        List<PointResponse> pointsList = getDisciplinaryPointsByUserId();

        Long totalPoints = pointsList.stream()
                .mapToLong(PointResponse::getPoints)
                .sum();

        return Map.of(
                "disciplinaryPoints", pointsList,
                "totalPoints", totalPoints
        );
    }

//    @Override
//    public List<StudentPointResponse> getStudentsWithTotalPoints(
//            Long classId, Integer courseId, Integer departmentId, Long semesterId) {
//
//        // Lấy danh sách sinh viên theo tiêu chí
//        List<User> students = userRepository.findByFilters(classId, courseId, departmentId);
//
//        return students.stream().map(student -> {
//            long totalPoints;
//
//            if (semesterId != null) {
//                // Nếu có học kỳ, lấy điểm theo học kỳ
//                totalPoints = Optional.ofNullable(
//                        disciplinaryPointRepository.getTotalPointsByUserAndSemester(student.getId(), semesterId)
//                ).orElse(0L);
//            } else {
//                // Nếu không có học kỳ, lấy điểm từ bảng DisciplinaryPoint
//                totalPoints = disciplinaryPointRepository.findByUser(student)
//                        .stream()
//                        .mapToLong(DisciplinaryPoint::getPoints)
//                        .sum();
//            }
//
//            // Trả về kết quả
//            return StudentPointResponse.builder()
//                    .id(student.getId())
//                    .studentId(student.getStudentId())
//                    .studentName(student.getFullname())
//                    .className(student.getClazz().getName())
//                    .email(student.getEmail())
//                    .phoneNumber(student.getPhoneNumber())
//                    .dateOfBirth(student.getDateOfBirth())
//                    .Department(student.getClazz().getDepartment().getName())
//                    .address(student.getAddress())
//                    .totalPoints(totalPoints)
//                    .build();
//        }).toList();
//    }
    @Override
    public Page<StudentPointResponse> getStudentsWithTotalPoints(
            Long classId, Integer courseId, Integer departmentId, Long semesterId, Pageable pageable) {

        // Lấy danh sách sinh viên theo tiêu chí và phân trang
        Page<User> studentsPage = userRepository.findByFilters(classId, courseId, departmentId, pageable);

        // Chuyển đổi từng User thành StudentPointResponse
        Page<StudentPointResponse> responsePage = studentsPage.map(student -> {
            long totalPoints;

            if (semesterId != null) {
                // Nếu có học kỳ, lấy điểm theo học kỳ
                totalPoints = Optional.ofNullable(
                        disciplinaryPointRepository.getTotalPointsByUserAndSemester(student.getId(), semesterId)
                ).orElse(0L);
            } else {
                // Nếu không có học kỳ, lấy tất cả điểm của sinh viên
                totalPoints = disciplinaryPointRepository.findByUser(student)
                        .stream()
                        .mapToLong(DisciplinaryPoint::getPoints)
                        .sum();
            }

            return StudentPointResponse.builder()
                    .id(student.getId())
                    .studentId(student.getStudentId())
                    .studentName(student.getFullname())
                    .className(student.getClazz().getName())
                    .email(student.getEmail())
                    .phoneNumber(student.getPhoneNumber())
                    .dateOfBirth(student.getDateOfBirth())
                    .Department(student.getClazz().getDepartment().getName())
                    .address(student.getAddress())
                    .totalPoints(totalPoints)
                    .build();
        });

        return responsePage;
    }

    public void cancelAttendance(User user, Event event) {
        Registration registration = registrationRepository.findByUserAndEvent(user, event);

        if (registration == null) {
            throw new IllegalStateException("Người dùng chưa đăng ký sự kiện này.");
        }

        if (!registration.isAttendances()) {
            throw new IllegalStateException("Sinh viên chưa được điểm danh.");
        }

        // Trừ điểm nếu đã có DisciplinaryPoint
        Optional<DisciplinaryPoint> optionalPoint = disciplinaryPointRepository.findByUserAndSemester(user, event.getSemester());
        if (optionalPoint.isPresent()) {
            DisciplinaryPoint disciplinaryPoint = optionalPoint.get();
            long newPoints = disciplinaryPoint.getPoints() - event.getScore();
            disciplinaryPoint.setPoints(Math.max(0, newPoints)); // Không cho điểm âm
            disciplinaryPointRepository.save(disciplinaryPoint);
        }

        // Xóa tiêu chí đã gắn
        List<EventCriteria> eventCriteriaList = eventCriteriaRepository.findByEventId(event.getId());
        for (EventCriteria eventCriteria : eventCriteriaList) {
            studentCriteriaRepository.deleteByStudentAndCriteriaAndSemester(user, eventCriteria.getCriteria(), event.getSemester());
        }

        // Đặt lại trạng thái chưa điểm danh
        registration.setAttendances(false);
        registrationRepository.save(registration);
    }


}
