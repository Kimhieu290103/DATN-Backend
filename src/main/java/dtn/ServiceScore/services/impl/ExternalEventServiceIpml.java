package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.dtos.ExternalEventDTO;
import dtn.ServiceScore.model.ExternalEvent;
import dtn.ServiceScore.model.Semester;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.repositories.ExternalEventRepository;
import dtn.ServiceScore.repositories.SemesterRepository;
import dtn.ServiceScore.responses.ExternalEventResponse;
import dtn.ServiceScore.services.ExternalEventService;
import dtn.ServiceScore.services.SupabaseService;
import dtn.ServiceScore.utils.Enums.ExternalEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalEventServiceIpml implements ExternalEventService {
    private final ExternalEventRepository externalEventRepository;
    private final SemesterRepository semesterRepository;
    private final SupabaseService supabaseService;
    @Override
    public ExternalEventResponse createExternalEvent(ExternalEventDTO dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        ExternalEvent externalEvent = ExternalEvent.builder()
                .user(user)
                .name(dto.getName())
                .description(dto.getDescription())
                .date(dto.getDate())
                .proofUrl(dto.getProofUrl())
                .status(ExternalEventStatus.PENDING)
                .points(dto.getPoints())
                .semester(semester)
                .build();

        externalEvent = externalEventRepository.save(externalEvent);

        return ExternalEventResponse.builder()
                .id(externalEvent.getId())
                .user_id(externalEvent.getUser().getId())
                .nameStudent(externalEvent.getUser().getFullname())
                .name(externalEvent.getName())
                .description(externalEvent.getDescription())
                .date(externalEvent.getDate())
                .proofUrl(externalEvent.getProofUrl())
                .status(externalEvent.getStatus().name())
                .points(externalEvent.getPoints())
                .semester(externalEvent.getSemester().getName())
                .studentName(externalEvent.getUser().getFullname())
                .clazz(externalEvent.getUser().getClazz() != null ? externalEvent.getUser().getClazz().getName() : "N/A")
                .created_at(externalEvent.getCreatedAt())
                .build();
    }

    @Override
    public List<ExternalEventResponse> getPendingEvents() {
        List<ExternalEvent> pendingEvents = externalEventRepository.findByStatus(ExternalEventStatus.PENDING);
        return pendingEvents.stream()
                .sorted(Comparator.comparing(event -> event.getUser().getId()))
                .map(event -> ExternalEventResponse.builder()
                        .id(event.getId())
                        .studentId(event.getUser().getStudentId())
                        .user_id(event.getUser().getId())
                        .nameStudent(event.getUser().getFullname())
                        .name(event.getName())
                        .description(event.getDescription())
                        .date(event.getDate())
                        .proofUrl(event.getProofUrl())
                        .status(event.getStatus().name())
                        .points(event.getPoints())
                        .studentName(event.getUser().getFullname())
                        .semester(event.getSemester().getName())
                        .clazz(event.getUser().getClazz() != null ? event.getUser().getClazz().getName() : "N/A")
                        .created_at(event.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public List<ExternalEventResponse> getUserEvents(Long userId) {
        List<ExternalEvent> userEvents = externalEventRepository.findByUserId(userId);

        return userEvents.stream()
                .map(event -> ExternalEventResponse.builder()
                        .id(event.getId())
                        .user_id(event.getUser().getId())
                        .nameStudent(event.getUser().getFullname())
                        .name(event.getName())
                        .description(event.getDescription())
                        .date(event.getDate())
                        .proofUrl(event.getProofUrl())
                        .status(event.getStatus().name())
                        .points(event.getPoints())
                        .studentName(event.getUser().getFullname())
                        .semester(event.getSemester().getName())
                        .clazz(event.getUser().getClazz() != null ? event.getUser().getClazz().getName() : "N/A")
                        .created_at(event.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public ExternalEvent findById(Long id) {
        Optional<ExternalEvent> event = externalEventRepository.findById(id);
        return event.orElse(null);
    }

    @Override
    public ExternalEvent createExternalEventRAR(String name, String description, LocalDate date, Long points, Long semesterId, MultipartFile file)
            throws IOException {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".rar")) {
            throw new IllegalArgumentException("File phải là định dạng .rar");
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String url = supabaseService.uploadFile(file.getBytes(), fileName);

        ExternalEvent event = ExternalEvent.builder()
                .name(name)
                .description(description)
                .date(date)
                .points(points)
                .proofUrl(url)
                .status(ExternalEventStatus.PENDING)
                .semester(semesterRepository.findById(semesterId).orElseThrow())
                .user(user)
                .build();

        return externalEventRepository.save(event);
    }

    @Override
    public List<ExternalEvent> getApprovedEventsByUserAndOptionalSemester(Long userId, Long semesterId) {
        return externalEventRepository.findApprovedEventsByUserAndOptionalSemester(
                userId,
                ExternalEventStatus.APPROVED,
                semesterId
        );
    }


}
