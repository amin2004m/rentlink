package rentlink.notification.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.auth.model.User;
import rentlink.notification.dto.RegisterDeviceTokenRequest;
import rentlink.notification.model.DeviceToken;
import rentlink.notification.repository.DeviceTokenRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/device-tokens")
    public ResponseEntity<Void> registerDeviceToken(@AuthenticationPrincipal User currentUser,
                                                     @Valid @RequestBody RegisterDeviceTokenRequest request) {
        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(() -> DeviceToken.builder()
                        .token(request.getToken())
                        .createdAt(LocalDateTime.now())
                        .build());
        deviceToken.setUserId(currentUser.getId());
        deviceTokenRepository.save(deviceToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
