package rentlink.payment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import rentlink.payment.model.EscrowStatus;
import rentlink.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    String id;
    String bookingId;
    BigDecimal amount;
    BigDecimal commission;
    EscrowStatus escrowStatus;
    PaymentStatus status;
    LocalDateTime createdAt;
}
