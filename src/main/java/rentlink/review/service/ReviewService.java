package rentlink.review.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import rentlink.auth.repository.UserRepository;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;
import rentlink.booking.repository.BookingRepository;
import rentlink.common.exception.BusinessException;
import rentlink.review.dto.CreateReviewRequest;
import rentlink.review.dto.ReviewResponse;
import rentlink.review.model.Review;
import rentlink.review.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;

    public ReviewResponse create(String reviewerId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw BusinessException.badRequest("BOOKING_NOT_COMPLETED", "review.bookingNotCompleted");
        }

        String revieweeId;
        if (booking.getLesseeId().equals(reviewerId)) {
            revieweeId = booking.getOwnerId();
        } else if (booking.getOwnerId().equals(reviewerId)) {
            revieweeId = booking.getLesseeId();
        } else {
            throw BusinessException.forbidden("NOT_BOOKING_PARTICIPANT", "error.forbidden");
        }

        if (reviewRepository.findByBookingIdAndReviewerId(booking.getId(), reviewerId).isPresent()) {
            throw BusinessException.conflict("REVIEW_ALREADY_EXISTS", "review.alreadyExists");
        }

        Review review = Review.builder()
                .bookingId(booking.getId())
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);
        recalculateRating(revieweeId);

        return toResponse(review);
    }

    public List<ReviewResponse> getByRevieweeId(String revieweeId) {
        return reviewRepository.findByRevieweeId(revieweeId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void recalculateRating(String revieweeId) {
        List<Review> reviews = reviewRepository.findByRevieweeId(revieweeId);
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        userRepository.findById(revieweeId).ifPresent(user -> {
            user.setRating(average);
            userRepository.save(user);
        });
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBookingId())
                .reviewerId(review.getReviewerId())
                .revieweeId(review.getRevieweeId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
