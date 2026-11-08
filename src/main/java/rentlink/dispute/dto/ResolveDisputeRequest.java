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
public class ResolveDisputeRequest {

    @NotBlank(message = "{dispute.resolution.required}")
    @Size(max = 1000, message = "{dispute.resolution.tooLong}")
    String resolution;

    DisputeOutcome outcome;
}
