package dtn.ServiceScore.services;

import dtn.ServiceScore.model.Registration;
import dtn.ServiceScore.responses.EventRegistrationResponse;
import dtn.ServiceScore.responses.EventRespone;
import dtn.ServiceScore.responses.UserResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RegistrationService {
    Registration register_event(Long eventId, Long userId) throws RuntimeException;

//    void checkInEvent(Long registrationId) throws RuntimeException;
//
//    void checkInEvent(Long eventId, Long userId) throws RuntimeException;
//
//    void multiCheckInEvent(List<Long> registrationIds) throws RuntimeException;
//
//    void multiCheckInEvent(Long eventId, List<Long> userIds) throws RuntimeException;
//
//    void allCheckInEvent(Long eventId) throws RuntimeException;

    void cancelRegistration(Long eventId);

    boolean isUserRegistered(Long eventId, Long userId) throws RuntimeException;

    // danh sách sinh viên đăng kí sự kiện
    EventRegistrationResponse getAllStudentByEvent(Long eventId);

    List<EventRespone> getAllEventByStudent(Long sudentId);

    List<UserResponse> getCheckedInStudentsByEvent(Long eventId);

    // danh sách các sự kiện sinh viên đã điểm danh
    List<EventRespone> getAttendedEvents(Long userId, Long semesterId);


    // lấy danh sách sự kiện sinh viên đã đăng kí
    List<EventRespone> getRegisterEvents(Long userId, Long semesterId);
//    // danh sách các sự kiện sinh viên đã điểm danh phân theo kì
//    List<EventRespone> getAttendedEventsBySemester(Long userId, String semesterName);

    ResponseEntity<Resource> exportEventRegistrationsToExcel(Long eventID);

    ResponseEntity<Resource> exportAttendedEventsToExcel(Long userId, Long semesterId);

    // quản lí xóa sinh viên đangn kí sự kiện
    public void cancelRegistrationByAdmin(Long eventId, Long userId);
}
