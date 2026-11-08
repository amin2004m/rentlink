package rentlink.dispute.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rentlink.dispute.model.Dispute;
import rentlink.dispute.model.DisputeStatus;

import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends MongoRepository<Dispute, String> {

    List<Dispute> findByBookingId(String bookingId);

    Optional<Dispute> findByBookingIdAndStatus(String bookingId, DisputeStatus status);

    List<Dispute> findByStatus(DisputeStatus status);
}
