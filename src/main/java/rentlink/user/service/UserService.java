package rentlink.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rentlink.auth.model.User;
import rentlink.auth.repository.UserRepository;
import rentlink.common.exception.BusinessException;
import rentlink.listing.service.CloudinaryService;
import rentlink.user.dto.PublicProfileResponse;
import rentlink.user.dto.UpdateProfileRequest;
import rentlink.user.dto.UserProfileResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    CloudinaryService cloudinaryService;

    public UserProfileResponse getMe(String userId) {
        return toProfileResponse(findOrThrow(userId));
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = findOrThrow(userId);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return toProfileResponse(userRepository.save(user));
    }

    public UserProfileResponse uploadAvatar(String userId, MultipartFile file) {
        User user = findOrThrow(userId);
        String imageUrl = cloudinaryService.uploadImage(file);
        user.setProfileImageUrl(imageUrl);
        return toProfileResponse(userRepository.save(user));
    }

    public PublicProfileResponse getPublicProfile(String userId) {
        User user = findOrThrow(userId);
        return PublicProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .rating(user.getRating())
                .isVerified(user.getIsVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private User findOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "error.notFound"));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .rating(user.getRating())
                .isVerified(user.getIsVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
