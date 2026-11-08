package rentlink.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rentlink.auth.model.AuthResponse;
import rentlink.auth.model.LoginRequest;
import rentlink.auth.model.RegisterRequest;
import rentlink.auth.model.User;
import rentlink.auth.model.UserRole;
import rentlink.auth.repository.UserRepository;
import rentlink.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS", "auth.emailAlreadyExists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(List.of(UserRole.USER.name()))
                .rating(0.0)
                .isVerified(false)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> BusinessException.badRequest("INVALID_CREDENTIALS", "auth.invalidCredentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.badRequest("INVALID_CREDENTIALS", "auth.invalidCredentials");
        }

        if (Boolean.TRUE.equals(user.getBlocked())) {
            throw BusinessException.forbidden("USER_BLOCKED", "auth.userBlocked");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!"REFRESH".equals(jwtService.extractTokenType(refreshToken))) {
            throw BusinessException.badRequest("INVALID_TOKEN", "error.unauthorized");
        }

        String userId = jwtService.extractUserId(refreshToken);

        if (!jwtService.isTokenValid(refreshToken, userId)) {
            throw BusinessException.badRequest("INVALID_TOKEN", "error.unauthorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "error.notFound"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}