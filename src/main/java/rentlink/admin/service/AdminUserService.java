package rentlink.admin.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import rentlink.admin.dto.AdminUserResponse;
import rentlink.auth.model.User;
import rentlink.auth.repository.UserRepository;
import rentlink.common.exception.BusinessException;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserService {

    UserRepository userRepository;

    public List<AdminUserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse setBlocked(String userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "error.notFound"));
        user.setBlocked(blocked);
        return toResponse(userRepository.save(user));
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .rating(user.getRating())
                .isVerified(user.getIsVerified())
                .blocked(user.getBlocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
