package rentlink.review.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.auth.model.User;
import rentlink.review.dto.CreateReviewRequest;
import rentlink.review.dto.ReviewResponse;
import rentlink.review.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@AuthenticationPrincipal User currentUser,
                                                 @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(currentUser.getId(), request));
    }

    @GetMapping("/user/{userId}")
    public List<ReviewResponse> getByUser(@PathVariable String userId) {
        return reviewService.getByRevieweeId(userId);
    }
}
