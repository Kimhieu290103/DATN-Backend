package dtn.ServiceScore.services;

import dtn.ServiceScore.dtos.ExternalEventDTO;
import dtn.ServiceScore.model.ExternalEvent;
import dtn.ServiceScore.responses.ExternalEventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ExternalEventService {
    ExternalEventResponse createExternalEvent(ExternalEventDTO dto);

    List<ExternalEventResponse> getPendingEvents();

    List<ExternalEventResponse> getUserEvents(Long userId);

    ExternalEvent findById(Long id);

     ExternalEvent createExternalEventRAR(
            String name,
            String description,
            LocalDate date,
            Long points,
            Long semesterId,
            MultipartFile file
    ) throws IOException;

    List<ExternalEvent> getApprovedEventsByUserAndOptionalSemester(Long userId, Long semesterId);
}
