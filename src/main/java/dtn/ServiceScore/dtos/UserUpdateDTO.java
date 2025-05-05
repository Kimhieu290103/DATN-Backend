package dtn.ServiceScore.dtos;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserUpdateDTO {
    private String fullname;
    private String phoneNumber;
    private String studentId;
    private String address;
    private LocalDate dateOfBirth;
    private String email;
    private String className; // tên lớp
}
