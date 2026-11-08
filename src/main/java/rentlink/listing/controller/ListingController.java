package rentlink.listing.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rentlink.auth.model.User;
import rentlink.listing.dto.CreateListingRequest;
import rentlink.listing.dto.ListingResponse;
import rentlink.listing.dto.UpdateListingRequest;
import rentlink.listing.service.ListingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListingController {
    ListingService listingService;

    @GetMapping
    public List<ListingResponse> getAllListings() {
        return listingService.getAllListings();
    }

    @GetMapping("/my")
    public List<ListingResponse> getMyListings(@AuthenticationPrincipal User currentUser) {
        return listingService.getMyListings(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ListingResponse getListingById(@PathVariable String id) {
        return listingService.getById(id);
    }

    @GetMapping("/owner/{id}")
    public List<ListingResponse> getByOwnerId(@PathVariable String id) {
        return listingService.getByOwnerId(id);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(@AuthenticationPrincipal User currentUser,
                                                  @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingService.create(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable String id,
                                  @AuthenticationPrincipal User currentUser,
                                  @Valid @RequestBody UpdateListingRequest request) {
        return listingService.update(id, currentUser.getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @AuthenticationPrincipal User currentUser) {
        listingService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ListingResponse addImage(@PathVariable String id,
                                    @AuthenticationPrincipal User currentUser,
                                    @RequestPart MultipartFile file) {
        return listingService.addImage(id, currentUser.getId(), file);
    }

    @GetMapping("/search")
    public Page<ListingResponse> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return listingService.search(category, minPrice, maxPrice, location, page, size);
    }

}
