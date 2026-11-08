package rentlink.dispute.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "disputes")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {

    @Id
    String id;
    String bookingId;
    String raisedBy;
    String reason;
    DisputeStatus status;
    String resolution;
    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
