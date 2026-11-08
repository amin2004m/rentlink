package rentlink.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateListingRequest {

    @NotBlank
    String title;

    String description;

    @NotBlank
    String category;

    @Positive(message = "{listing.price.mustBePositive}")
    double pricePerDay;

    @PositiveOrZero(message = "{listing.deposit.mustBeNonNegative}")
    double depositAmount;

    @NotBlank
    String location;
}
