package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.components.JwtTokenUtil;
import dtn.ServiceScore.dtos.ChangePasswordDTO;
import dtn.ServiceScore.dtos.ResetPasswordDTO;
import dtn.ServiceScore.dtos.UserDTO;
import dtn.ServiceScore.dtos.UserUpdateDTO;
import dtn.ServiceScore.exceptions.DataNotFoundException;
import dtn.ServiceScore.model.Class;
import dtn.ServiceScore.model.Role;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.repositories.ClassRepository;
import dtn.ServiceScore.repositories.RoleRepository;
import dtn.ServiceScore.repositories.UserRepository;
import dtn.ServiceScore.responses.LoginRespone;
import dtn.ServiceScore.responses.UserResponse;
import dtn.ServiceScore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public void createUser(UserDTO userDTO) throws DataIntegrityViolationException, DataNotFoundException {
        String username = userDTO.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new DataIntegrityViolationException("username existed");
        }
        User newUser = User.builder()
                .fullname(userDTO.getFullname())
                .phoneNumber(userDTO.getPhoneNumber())
                .studentId(userDTO.getStudentId())
                .address(userDTO.getAddress())
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .build();
        Role role = roleRepository.findByName(userDTO.getRoleName()).
                orElseThrow(() -> new DataNotFoundException("Không tìm thấy vai trò"));
        Class _class = classRepository.findByName(userDTO.getClassName()).orElse(null);
        newUser.setRole(role);
        newUser.setClazz(_class);
        newUser.setActive(true);

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);
        userRepository.save(newUser);
    }

    @Override
    public LoginRespone login(String username, String password) throws RuntimeException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Sai tài khoản hoặc mật khẩu"));

        if (!user.isActive()) { // Kiểm tra nếu isActive là false (tài khoản bị khóa)
            throw new BadCredentialsException("Tài khoản của bạn đã bị khóa");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Wrong username or password");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        LoginRespone loginRespone = new LoginRespone();
        loginRespone.setAccessToken(jwtTokenUtil.generateToken(user));
        String role = user.getRole().getName();  // Nếu getRoles() trả về một Role
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId())
                .address(user.getAddress())
                .isActive(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .email(user.getEmail())
                .username(user.getUsername())
                .Department(Optional.ofNullable(user.getClazz())
                        .map(clazz -> clazz.getDepartment())
                        .map(department -> department.getName())
                        .orElse(null)) // Nếu null thì trả về null
                .clazz(Optional.ofNullable(user.getClazz())
                        .map(clazz -> clazz.getName())
                        .orElse(null))
                .build();
        loginRespone.setRole(role);
        loginRespone.setUserResponse(userResponse);
        return loginRespone;
    }


    @Override
    public List<User> findUsersByClassId(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        return userRepository.findByClazz(clazz);
    }

    @Override
    public User getUserById(Long userId) {
        try {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Không tìm thấy người dùng với ID: " + userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changePassword(ChangePasswordDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        // Mã hóa và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    public Page<User> getUsersByRole(String roleName, Pageable pageable) {
        return userRepository. findAllByRole_Name(roleName, pageable);
    }

    @Override
    public List<User> getUsersExcludingRoles() {
        List<String> excludedRoles = Arrays.asList("SV", "LCD");
        return userRepository.findAllByRole_NameNotIn(excludedRoles);
    }

    @Override
    public Page<User> searchUsersPaginated(String search, Pageable pageable) {
        return userRepository.searchUsersPaginated(search, pageable);
    }

    @Override
    public void updateUserProfileById(Long userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setStudentId(dto.getStudentId());
        user.setAddress(dto.getAddress());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setEmail(dto.getEmail());

        if (dto.getClassName() != null && !dto.getClassName().isBlank()) {
            Class clazz = classRepository.findByName(dto.getClassName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + dto.getClassName()));
            user.setClazz(clazz);
        }

        userRepository.save(user);
    }

    @Override
    public User deactivateUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));


        if (user != null) {
            // Đảo ngược trạng thái hiện tại
            user.setActive(!user.isActive()); // Đặt isActive thành false
            return userRepository.save(user); // Lưu lại thay đổi vào cơ sở dữ liệu
        }
        return null; // Trả về null nếu không tìm thấy người dùng
    }


}
