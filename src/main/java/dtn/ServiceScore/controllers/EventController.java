package dtn.ServiceScore.controllers;

import dtn.ServiceScore.dtos.EventDTO;
import dtn.ServiceScore.model.Event;
import dtn.ServiceScore.model.EventImage;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.responses.*;
import dtn.ServiceScore.services.CloudinaryService;
import dtn.ServiceScore.services.EventImageService;
import dtn.ServiceScore.services.EventService;
import dtn.ServiceScore.services.impl.QRCodeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {
    public final EventService eventService;
    public final EventImageService eventImageService;
    private final CloudinaryService cloudinaryService;
    private final QRCodeService qrCodeService;
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventDTO eventDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }


            eventService.createEvent(eventDTO);
            return ResponseEntity.ok(new MessageResponse("dang  ki thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all/{id}")
    public ResponseEntity<?> getEventById(@PathVariable("id") Long eventId) {
        try {
            Event existingEvent = eventService.getEventById(eventId);
            // Lấy hỉnh ảnh
            List<EventImage> eventImages = eventImageService.findByEventId(eventId);
            // Lấy danh sách tiêu chí
            EventCriteriaResponse eventCriteriaResponse = eventService.getFilteredEventCriteria(eventId);

            List<EventImageRespone> eventImageResponses = eventImages.stream()
                    .map(image -> EventImageRespone.builder()
                            .id(image.getId())
                            .eventID(image.getEvent().getId())
                            .imageUrl(image.getImageUrl())
                            .build())
                    .toList();

            EventRespone eventRespone = EventRespone.builder()
                    .id(existingEvent.getId())
                    .name(existingEvent.getName())
                    .description(existingEvent.getDescription())
                    .date(existingEvent.getDate())
                    .endDate(existingEvent.getEndDate())
                    .registrationStartDate(existingEvent.getRegistrationStartDate())
                    .registrationEndDate(existingEvent.getRegistrationEndDate())
                    .semester(SemesterRespone.builder()
                            .id(existingEvent.getSemester().getId())
                            .name(existingEvent.getSemester().getName())
                            .build())
                    .user_id(existingEvent.getUser().getId())
                    .score(existingEvent.getScore())
                    .maxRegistrations(existingEvent.getMaxRegistrations())
                    .currentRegistrations(existingEvent.getCurrentRegistrations())
                    .location(existingEvent.getLocation())
                    .additionalInfo(existingEvent.getAdditionalInfo())
                    .eventType(existingEvent.getEventType().getName())
                    .eventImage(eventImageResponses)
                    .eventCriteria(eventCriteriaResponse)
                    .build();
            return ResponseEntity.ok(eventRespone);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("all")
    public ResponseEntity<EventListResponse> getAllEvents(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
//        PageRequest pageRequest = PageRequest.of(page,
//                limit);
        // Thêm sắp xếp mặc định theo registrationStartDate giảm dần
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "id"));

        Page<Event> eventPages = eventService.getAllEvents(pageRequest);
        int totalPages = eventPages.getTotalPages();

        // Map từ Event -> EventRespone
        List<EventRespone> eventResponses = eventPages.getContent().stream().map(event -> {
            // Lấy danh sách ảnh từ EventImageService
            List<EventImageRespone> eventImages = eventImageService.findByEventId(event.getId())
                    .stream()
                    .map(image -> new EventImageRespone(image.getId(), image.getEvent().getId(), image.getImageUrl()))
                    .toList();

            return EventRespone.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .date(event.getDate())
                    .endDate(event.getEndDate())
                    .registrationStartDate(event.getRegistrationStartDate())
                    .registrationEndDate(event.getRegistrationEndDate())
                    .semester(SemesterRespone.builder()
                            .id(event.getSemester().getId())
                            .name(event.getSemester().getName())
                            .build())
                    .user_id(event.getUser().getId())
                    .score(event.getScore())
                    .maxRegistrations(event.getMaxRegistrations())
                    .currentRegistrations(event.getCurrentRegistrations())
                    .location(event.getLocation())
                    .additionalInfo(event.getAdditionalInfo())
                    .eventType(event.getEventType().getName())
                    .additionalInfo(event.getAdditionalInfo())
                    .location(event.getLocation())
                    .eventImage(eventImages)
                    .build();
        }).toList();

        return ResponseEntity.ok(EventListResponse.builder()
                .events(eventResponses)
                .totalPage(totalPages)
                .build());
    }

    @GetMapping("/all/events-by-type")
    public ResponseEntity<EventListResponse> getEventsByType(
            @RequestParam("eventTypeId") Long eventTypeId,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC,  "id"));
        Page<Event> eventPages = eventService.getEventsByEventType(eventTypeId, pageRequest);
        int totalPages = eventPages.getTotalPages();

        List<EventRespone> eventResponses = eventPages.getContent().stream().map(event -> {
            List<EventImageRespone> eventImages = eventImageService.findByEventId(event.getId())
                    .stream()
                    .map(image -> new EventImageRespone(image.getId(), image.getEvent().getId(), image.getImageUrl()))
                    .toList();

            return EventRespone.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .date(event.getDate())
                    .endDate(event.getEndDate())
                    .registrationStartDate(event.getRegistrationStartDate())
                    .registrationEndDate(event.getRegistrationEndDate())
                    .semester(SemesterRespone.builder()
                            .id(event.getSemester().getId())
                            .name(event.getSemester().getName())
                            .build())
                    .user_id(event.getUser().getId())
                    .score(event.getScore())
                    .maxRegistrations(event.getMaxRegistrations())
                    .currentRegistrations(event.getCurrentRegistrations())
                    .location(event.getLocation())
                    .additionalInfo(event.getAdditionalInfo())
                    .eventType(event.getEventType().getName())
                    .eventImage(eventImages)
                    .build();
        }).toList();

        return ResponseEntity.ok(EventListResponse.builder()
                .events(eventResponses)
                .totalPage(totalPages)
                .build());
    }
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/createEventImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadEventImage(@Valid @ModelAttribute EventDTO eventDTO,
                                              BindingResult bidingresult)
    // @RequestParam("file")MultipartFile file)
    {
        try {
            if (bidingresult.hasErrors()) {
                List<String> errors = bidingresult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }
            Event newEvent = eventService.createEvent(eventDTO);
            List<MultipartFile> files = eventDTO.getFiles();
            files = files == null ? new ArrayList<>() : files;
            for (MultipartFile file : files) {
                if (file != null) {
                    // kiem tra kich thuoc file anh
                    if (file.getSize() > 50 * 1024 * 1024) {
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("this file too laget, maximum size is 10mb");

                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("file must be an iamge");

                    }
//                    String filename = storeFile(file);
//                    EventImage eventImage = eventService.createEventImage(newEvent.getId(),
//                            EventImage.builder()
//                                    .imageUrl(filename)
//                                    .build());
                    // Upload ảnh lên Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file);

                    // Lưu URL ảnh vào database
                    EventImage eventImage = eventService.createEventImage(newEvent.getId(),
                            EventImage.builder()
                                    .imageUrl(imageUrl)
                                    .build());
                }

            }

            return ResponseEntity.ok(new MessageResponse("Create Event Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getLocalizedMessage()));
        }

    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/uploadImage/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImage(@RequestParam("files") List<MultipartFile> files,
                                                @Valid @PathVariable("id") Long eventID
    ) {
        try {

            Event existingEvent = eventService.getEventById(eventID);
            List<EventImage> eventImages = new ArrayList<>();
            files = files == null ? new ArrayList<>() : files;
            if (files.size() > 5) {
                return ResponseEntity.badRequest().body(new MessageResponse("1 san pham khong duoc qua 5 anh"));
            }
            for (MultipartFile file : files) {
                if (file != null) {
                    // kiem tra kich thuoc file anh
                    if (file.getSize() > 50 * 1024 * 1024) {
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("this file too laget, maximum size is 10mb");

                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("file must be an iamge");

                    }
//                    String filename = storeFile(file);
//                    EventImage eventImage = eventService.createEventImage(existingEvent.getId(),
//                            EventImage.builder()
//                                    .imageUrl(filename)
//                                    .build());
//                    eventImages.add(eventImage);
                    // Upload ảnh lên Cloudinary
                    String imageUrl = storeFile(file);

                    // Lưu thông tin ảnh vào database
                    EventImage eventImage = eventService.createEventImage(existingEvent.getId(),
                            EventImage.builder().imageUrl(imageUrl).build());
                    eventImages.add(eventImage);
                }

            }

            return ResponseEntity.ok(eventImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getLocalizedMessage()));
        }

    }

//    private String storeFile(MultipartFile file) throws IOException {
//        String filename = StringUtils.cleanPath(file.getOriginalFilename());
//        // theem UUID vao truoc ten file de ten file la duy nhat
//        String uniqueFilename = UUID.randomUUID() + "_" + filename;
//        // duong dan den thu muc chua file anh
//        java.nio.file.Path uploadDir = Paths.get("uploads/images");
//        if (!Files.exists(uploadDir)) {
//            Files.createDirectories(uploadDir);
//        }
//        // duong dan day du den file
//        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
//        // sao chep file vao thu muc dich
//        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
//        return uniqueFilename;
//    }
private String storeFile(MultipartFile file) throws IOException {
    if (file.getSize() > 10 * 1024 * 1024) {
        throw new IOException("File quá lớn! Giới hạn là 10MB.");
    }
    return cloudinaryService.uploadFile(file);
}

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }


    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEventById(@PathVariable("id") Long eventID) {
        try {
            eventService.deleteEvent(eventID);
            return ResponseEntity.ok(new MessageResponse("xoa thanh cong"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@Valid @PathVariable("id") Long id,
                                         @Valid @ModelAttribute EventDTO eventDTO) {
        try {
            Event event = eventService.updateEvent(id, eventDTO);
            return ResponseEntity.ok(event);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/criteria/{eventId}")
    public ResponseEntity<?> getEventCriteria(@PathVariable Long eventId) {
        EventCriteriaResponse response = eventService.getFilteredEventCriteria(eventId);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/createEventImageAD", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadEventImageAdmin(@Valid @ModelAttribute EventDTO eventDTO,
                                                   BindingResult bidingresult)
    // @RequestParam("file")MultipartFile file)
    {
        try {
            if (bidingresult.hasErrors()) {
                List<String> errors = bidingresult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }
            Event newEvent = eventService.createEvent(eventDTO);
            List<MultipartFile> files = eventDTO.getFiles();
            files = files == null ? new ArrayList<>() : files;
            for (MultipartFile file : files) {
                if (file != null) {
                    // kiem tra kich thuoc file anh
                    if (file.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("this file too laget, maximum size is 10mb");

                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("file must be an iamge");

                    }
//                    String filename = storeFile(file);
//                    EventImage eventImage = eventService.createEventImage(newEvent.getId(),
//                            EventImage.builder()
//                                    .imageUrl(filename)
//                                    .build());
                    // Upload ảnh lên Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file);

                    // Lưu URL ảnh vào database
                    EventImage eventImage = eventService.createEventImage(newEvent.getId(),
                            EventImage.builder()
                                    .imageUrl(imageUrl)
                                    .build());
                }

            }

            return ResponseEntity.ok(new MessageResponse("Tạo sự kiện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getLocalizedMessage()));
        }

    }
    // Lấy dánh sách sự kiện mà bản thân đã tạo
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-events")
    public ResponseEntity<EventListResponse> getMyEvents(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();

        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<Event> eventPages = eventService.getEventByUser(pageRequest);
        int totalPages = eventPages.getTotalPages();

        List<EventRespone> eventResponses = eventPages.getContent().stream().map(event -> {
            List<EventImageRespone> eventImages = eventImageService.findByEventId(event.getId())
                    .stream()
                    .map(image -> new EventImageRespone(image.getId(), image.getEvent().getId(), image.getImageUrl()))
                    .toList();

            return EventRespone.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .date(event.getDate())
                    .endDate(event.getEndDate())
                    .registrationStartDate(event.getRegistrationStartDate())
                    .registrationEndDate(event.getRegistrationEndDate())
                    .semester(SemesterRespone.builder()
                            .id(event.getSemester().getId())
                            .name(event.getSemester().getName())
                            .build())
                    .user_id(event.getUser().getId())
                    .score(event.getScore())
                    .maxRegistrations(event.getMaxRegistrations())
                    .currentRegistrations(event.getCurrentRegistrations())
                    .location(event.getLocation())
                    .additionalInfo(event.getAdditionalInfo())
                    .eventType(event.getEventType().getName())
                    .eventImage(eventImages)
                    .build();
        }).toList();

        return ResponseEntity.ok(EventListResponse.builder()
                .events(eventResponses)
                .totalPage(totalPages)
                .build());
    }


    // tạo mã QR
    @GetMapping("/generateQR/{eventId}")
    public ResponseEntity<String> generateEventQRCode(@PathVariable Long eventId) {
        try {
            String qrText = "https://your-frontend.com/attend?eventId=" + eventId;
            String qrBase64 = qrCodeService.generateQRCode(qrText, 300, 300);
            return ResponseEntity.ok(qrBase64);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tạo QR: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEventsByName(@RequestParam String name) {
        List<Event> events = eventService.searchEventsByName(name);
        if (events.isEmpty()) {
            return ResponseEntity.noContent().build(); // Trả về không có dữ liệu nếu không tìm thấy sự kiện
        }
        return ResponseEntity.ok(events); // Trả về danh sách sự kiện tìm được
    }
    @GetMapping("/search-myevents")
    public List<Event> searchEvents(@RequestParam String name) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return eventService.searchEventsByName(name, user);
    }

}
