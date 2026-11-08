package rentlink.admin.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse {
    String id;
    String email;
    String fullName;
    String phone;
    List<String> roles;
    Double rating;
    Boolean isVerified;
    Boolean blocked;
    LocalDateTime createdAt;
}
