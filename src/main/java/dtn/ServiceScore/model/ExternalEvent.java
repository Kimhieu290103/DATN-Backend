package dtn.ServiceScore.model;

import dtn.ServiceScore.utils.Enums.ExternalEventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "external_events") // event sinh vien tham gia ben ngoai
public class ExternalEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id sinh vien
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ten event tham gia ben ngoai
    @Column(name = "name", nullable = false)
    private String name;

    // mo ta
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ngay to chuc
    @Column(name = "date", nullable = false)
    private LocalDate date;

    // duong dan den minh chung(vd: link drive)
    @Column(name = "proof_url", length = 500)
    private String proofUrl;


    // Trạng thái (Pending / Approved / Rejected)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExternalEventStatus status = ExternalEventStatus.PENDING;

    // so diem cua hoat dong do
    @Column(name = "points")
    private Long points;

    // thuoc hoc ki nao
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;


}
