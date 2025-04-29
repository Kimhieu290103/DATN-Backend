package dtn.ServiceScore.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {
    private String token;
    private String newPassword;
}
