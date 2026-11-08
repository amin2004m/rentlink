package rentlink.dispute.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import rentlink.dispute.model.DisputeStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {
    String id;
    String bookingId;
    String raisedBy;
    String reason;
    DisputeStatus status;
    String resolution;
    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
