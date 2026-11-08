package rentlink.listing.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import rentlink.listing.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingResponse {

    String id;
    String ownerId;
    String title;
    String description;
    String category;
    double pricePerDay;
    double depositAmount;
    Status status;
    List<String> images;
    String location;
    LocalDateTime createdAt;
}