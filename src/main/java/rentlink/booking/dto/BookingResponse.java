package rentlink.booking.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import rentlink.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String id;
    String listingId;
    String lesseeId;
    String ownerId;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime createdAt;
    BigDecimal totalAmount;
    BookingStatus status;
}
