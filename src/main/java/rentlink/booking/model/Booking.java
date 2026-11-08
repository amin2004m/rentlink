package rentlink.booking.model;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
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