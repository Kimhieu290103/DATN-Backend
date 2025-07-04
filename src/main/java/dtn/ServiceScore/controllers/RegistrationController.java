package dtn.ServiceScore.controllers;

import dtn.ServiceScore.exceptions.DataNotFoundException;
import dtn.ServiceScore.model.Registration;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.responses.EventRegistrationResponse;
import dtn.ServiceScore.responses.EventRespone;
import dtn.ServiceScore.responses.MessageResponse;
import dtn.ServiceScore.responses.UserResponse;
import dtn.ServiceScore.services.RegistrationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    // đâng kí sự kiện
    @PostMapping("/{eventId}")
    public ResponseEntity<?> registerEvent(@Valid @PathVariable("eventId") Long eventId) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = user.getId();

            Registration registration = registrationService.register_event(eventId, userId);
            return ResponseEntity.ok(new MessageResponse("Đăng kí thành công"));
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
            // Trả về 409 Conflict nếu người dùng đã đăng ký hoặc sự kiện đầy
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }

    }

    // danh sách sinh viên đăng kí sự kiện
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getUsersByEvent(@PathVariable Long eventId) {
        EventRegistrationResponse response = registrationService.getAllStudentByEvent(eventId);
        return ResponseEntity.ok(response);
    }

    // danh sách sự kiện sinh viên đăng kí
    @GetMapping("/user/getevents")
    public ResponseEntity<List<EventRespone>> getEventsByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        List<EventRespone> events = registrationService.getAllEventByStudent(userId);
        return ResponseEntity.ok(events);
    }

    // danh sách các sinh viên đã điểm danh
    @GetMapping("/event/checked/{eventId}")
    public ResponseEntity<List<UserResponse>> getCheckedInStudentsByEvent(@PathVariable Long eventId) {
        List<UserResponse> users = registrationService.getCheckedInStudentsByEvent(eventId);
        return ResponseEntity.ok(users);
    }


    // danb sách các hoạt dộng sinh viên đã tham gia(có điêmr danh)
    @GetMapping("/attended/{userId}")
    public ResponseEntity<?> getAttendedEvents(
            @PathVariable Long userId,
            @RequestParam(required = false) Long semesterId) {  // Thêm tham số semesterId tùy chọn
        List<EventRespone> events = registrationService.getAttendedEvents(userId, semesterId);
        return ResponseEntity.ok(events);
    }


    // danb sách các hoạt dộng sinh viên đã tham gia(có điêmr danh)
    @GetMapping("/register/{userId}")
    public ResponseEntity<?> getRegisterEvents(
            @PathVariable Long userId,
            @RequestParam(required = false) Long semesterId) {  // Thêm tham số semesterId tùy chọn
        List<EventRespone> events = registrationService.getRegisterEvents(userId, semesterId);
        return ResponseEntity.ok(events);
    }

    // xuất danh sách sinh viên đăng kí sự kiện exel
    @GetMapping("/export/{eventId}")
    public ResponseEntity<Resource> exportEventRegistrations(@PathVariable Long eventId) {
        return registrationService.exportEventRegistrationsToExcel(eventId);
    }

    // Hủy đăng kí sự kiện
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        try {
            registrationService.cancelRegistration(id);
            return ResponseEntity.ok("Đã hủy đăng ký thành công!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    // xuất các sự kiện mà sinh viên tham gia , có thể xuất theo kì nếu thêm tham số kì học
    @GetMapping("/export/attended-events")
    public ResponseEntity<Resource> exportAttendedEvents(
            @RequestParam Long userId,
            @RequestParam(required = false) Long semesterId) {
        return registrationService.exportAttendedEventsToExcel(userId, semesterId);
    }
    // quán lí hủy sinh viên trong danh sách đăng kí sự kiện
    @DeleteMapping("/{eventId}/student/{studentId}")
    public ResponseEntity<?> removeStudentFromEvent(
            @PathVariable("eventId") Long eventId,
            @PathVariable("studentId") Long studentId) {
        try {
            registrationService.cancelRegistrationByAdmin(eventId, studentId);
            return ResponseEntity.ok("Đã xóa sinh viên khỏi sự kiện thành công!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }


}
