package rentlink.admin.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    long totalUsers;
    long totalListings;
    long activeListings;
    long totalBookings;
    Map<String, Long> bookingsByStatus;
    long openDisputes;
    BigDecimal totalRevenue;
}
