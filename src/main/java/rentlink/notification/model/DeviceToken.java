package rentlink.notification.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "device_tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceToken {

    @Id
    String id;
    String userId;
    @Indexed(unique = true)
    String token;
    LocalDateTime createdAt;
}
