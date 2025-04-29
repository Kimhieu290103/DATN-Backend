package dtn.ServiceScore.services;

import dtn.ServiceScore.dtos.ChangePasswordDTO;
import dtn.ServiceScore.dtos.ResetPasswordDTO;
import dtn.ServiceScore.dtos.UserDTO;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.responses.LoginRespone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserService {
    void createUser(UserDTO userDTO) throws RuntimeException;

    LoginRespone login(String username, String password) throws RuntimeException;

    //List<User> searchByUsername(String username) throws Exception;
    List<User> findUsersByClassId(Long classId);
    //List<User> searchByUsername(String username) throws RuntimeException;

    User getUserById(Long userId);

    void changePassword(ChangePasswordDTO request);

    Page<User> getUsersByRole(String roleName,  Pageable pageable);

     List<User> getUsersExcludingRoles();
    // Tìm kiếm người dùng theo bộ lọc với phân trang, role là 'SV'
    Page<User> searchUsersPaginated(String search, Pageable pageable);


}
