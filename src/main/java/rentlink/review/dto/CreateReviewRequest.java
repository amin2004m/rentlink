package rentlink.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotBlank(message = "{review.bookingId.required}")
    String bookingId;

    @Min(value = 1, message = "{review.rating.range}")
    @Max(value = 5, message = "{review.rating.range}")
    int rating;

    @Size(max = 1000, message = "{review.comment.tooLong}")
    String comment;
}
