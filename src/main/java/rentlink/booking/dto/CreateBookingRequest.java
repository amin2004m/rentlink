package rentlink.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookingRequest {

    @NotBlank(message = "{booking.listingId.required}")
    String listingId;

    @NotNull(message = "{booking.startDate.required}")
    @Future(message = "{booking.startDate.mustBeFuture}")
    LocalDateTime startDate;

    @NotNull(message = "{booking.endDate.required}")
    LocalDateTime endDate;
}
