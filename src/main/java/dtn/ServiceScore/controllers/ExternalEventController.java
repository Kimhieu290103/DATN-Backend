package dtn.ServiceScore.controllers;

import dtn.ServiceScore.dtos.ExternalEventDTO;
import dtn.ServiceScore.model.ExternalEvent;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.responses.ExternalEventResponse;
import dtn.ServiceScore.services.ExternalEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/external-events")
@RequiredArgsConstructor
public class ExternalEventController {
    private final ExternalEventService externalEventService;

    @PostMapping
    public ResponseEntity<ExternalEventResponse> createExternalEvent(@RequestBody ExternalEventDTO externalEventDTO) {
        ExternalEventResponse response = externalEventService.createExternalEvent(externalEventDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ExternalEventResponse>> getPendingEvents() {
        return ResponseEntity.ok(externalEventService.getPendingEvents());
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<ExternalEventResponse>> getUserEvents() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(externalEventService.getUserEvents(userId));
    }

    @PostMapping("/create-externalevent")
    public ResponseEntity<?> createExternalEventRar(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("points") Long points,
            @RequestParam("semesterId") Long semesterId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            ExternalEvent event = externalEventService.createExternalEventRAR(
                    name, description, date, points, semesterId, file
            );
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ExternalEvent>> getApprovedEventsByUser(
            @RequestParam Long userId,
            @RequestParam(required = false) Long semesterId
    ) {
        List<ExternalEvent> events = externalEventService.getApprovedEventsByUserAndOptionalSemester(userId, semesterId);
        return ResponseEntity.ok(events);
    }
}
