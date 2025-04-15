package dtn.ServiceScore.dtos;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassDTO {
    private String name;
    private Long departmentId;
    private String course;
    private boolean status = true;
}
