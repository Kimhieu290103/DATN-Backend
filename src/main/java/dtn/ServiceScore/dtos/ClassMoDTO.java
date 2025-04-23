package dtn.ServiceScore.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassMoDTO {
    private String name;
    private Long departmentId;
    private Long courseId;
    private boolean status = true;
}
