package rentlink.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDisputeRequest {

    @NotBlank(message = "{dispute.bookingId.required}")
    String bookingId;

    @NotBlank(message = "{dispute.reason.required}")
    @Size(max = 1000, message = "{dispute.reason.tooLong}")
    String reason;
}
