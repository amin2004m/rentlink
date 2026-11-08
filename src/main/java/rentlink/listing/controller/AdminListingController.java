package rentlink.listing.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.listing.dto.ListingResponse;
import rentlink.listing.service.ListingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/listings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminListingController {

    ListingService listingService;

    @GetMapping("/pending")
    public List<ListingResponse> getPendingListings() {
        return listingService.getPendingListings();
    }

    @PutMapping("/{id}/approve")
    public ListingResponse approve(@PathVariable String id) {
        return listingService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public ListingResponse reject(@PathVariable String id) {
        return listingService.reject(id);
    }
}