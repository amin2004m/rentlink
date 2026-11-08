package rentlink.user.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rentlink.auth.model.User;
import rentlink.user.dto.PublicProfileResponse;
import rentlink.user.dto.UpdateProfileRequest;
import rentlink.user.dto.UserProfileResponse;
import rentlink.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getMe(@AuthenticationPrincipal User currentUser) {
        return userService.getMe(currentUser.getId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@AuthenticationPrincipal User currentUser,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUser.getId(), request);
    }

    @PostMapping("/me/avatar")
    public UserProfileResponse uploadAvatar(@AuthenticationPrincipal User currentUser,
                                            @RequestPart MultipartFile file) {
        return userService.uploadAvatar(currentUser.getId(), file);
    }

    @GetMapping("/{id}/profile")
    public PublicProfileResponse getPublicProfile(@PathVariable String id) {
        return userService.getPublicProfile(id);
    }
}
