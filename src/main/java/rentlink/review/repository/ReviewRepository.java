package rentlink.review.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rentlink.review.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByRevieweeId(String revieweeId);

    Optional<Review> findByBookingIdAndReviewerId(String bookingId, String reviewerId);
}
