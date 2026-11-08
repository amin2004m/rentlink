package rentlink.auth.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    String id;
    @Indexed(unique = true)
    String email;
    String passwordHash;
    String fullName;
    String phone;
    List<String> roles;
    Double rating;
    Boolean isVerified;
    Boolean blocked;
    String profileImageUrl;
    LocalDateTime createdAt;

}
