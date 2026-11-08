package rentlink.admin.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import rentlink.admin.dto.ReportResponse;
import rentlink.auth.repository.UserRepository;
import rentlink.booking.repository.BookingRepository;
import rentlink.dispute.model.DisputeStatus;
import rentlink.dispute.repository.DisputeRepository;
import rentlink.listing.model.Status;
import rentlink.listing.repository.ListingRepository;
import rentlink.payment.model.EscrowStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReportService {

    UserRepository userRepository;
    ListingRepository listingRepository;
    BookingRepository bookingRepository;
    DisputeRepository disputeRepository;
    MongoTemplate mongoTemplate;

    public ReportResponse getReport() {
        return ReportResponse.builder()
                .totalUsers(userRepository.count())
                .totalListings(listingRepository.count())
                .activeListings(listingRepository.findByStatus(Status.ACTIVE).size())
                .totalBookings(bookingRepository.count())
                .bookingsByStatus(bookingsByStatus())
                .openDisputes(disputeRepository.findByStatus(DisputeStatus.OPEN).size())
                .totalRevenue(totalReleasedCommission())
                .build();
    }

    private Map<String, Long> bookingsByStatus() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("status").count().as("count")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "bookings", Document.class);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Document doc : results.getMappedResults()) {
            counts.put(doc.getString("_id"), ((Number) doc.get("count")).longValue());
        }
        return counts;
    }

    private BigDecimal totalReleasedCommission() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("escrowStatus").is(EscrowStatus.RELEASED.name())),
                Aggregation.group().sum("commission").as("total")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "payments", Document.class);
        Document result = results.getUniqueMappedResult();
        if (result == null || result.get("total") == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(result.get("total").toString());
    }
}
