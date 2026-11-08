package rentlink.listing.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rentlink.common.exception.BusinessException;
import rentlink.listing.dto.CreateListingRequest;
import rentlink.listing.dto.ListingResponse;
import rentlink.listing.dto.UpdateListingRequest;
import rentlink.listing.model.Listing;
import rentlink.listing.model.Status;
import rentlink.listing.repository.ListingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListingService {
    ListingRepository listingRepository;
    CloudinaryService cloudinaryService;
    MongoTemplate mongoTemplate;

    public List<ListingResponse> getAllListings() {
        return listingRepository.findByStatus(Status.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    public ListingResponse getById(String id) {
        return toResponse(findListingOrThrow(id));
    }

    public List<ListingResponse> getByOwnerId(String ownerId) {
        return listingRepository.findByOwnerIdAndStatus(ownerId, Status.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ListingResponse> getMyListings(String ownerId) {
        return listingRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ListingResponse create(String ownerId, CreateListingRequest request) {
        Listing listing = Listing.builder()
                .ownerId(ownerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .pricePerDay(request.getPricePerDay())
                .depositAmount(request.getDepositAmount())
                .location(request.getLocation())
                .status(Status.PENDING)
                .images(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(listingRepository.save(listing));
    }

    public ListingResponse update(String id, String requesterId, UpdateListingRequest request) {
        Listing listing = findListingOrThrow(id);
        assertOwnership(listing, requesterId);

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setPricePerDay(request.getPricePerDay());
        listing.setDepositAmount(request.getDepositAmount());
        listing.setLocation(request.getLocation());

        return toResponse(listingRepository.save(listing));
    }

    public void delete(String id, String requesterId) {
        Listing listing = findListingOrThrow(id);
        assertOwnership(listing, requesterId);
        listingRepository.delete(listing);
    }

    public ListingResponse addImage(String id, String requesterId, MultipartFile file) {
        Listing listing = findListingOrThrow(id);
        assertOwnership(listing, requesterId);

        String imageUrl = cloudinaryService.uploadImage(file);
        listing.getImages().add(imageUrl);

        return toResponse(listingRepository.save(listing));
    }

    public Page<ListingResponse> search(String category,
                                        Double minPrice,
                                        Double maxPrice,
                                        String location,
                                        int page,
                                        int size) {

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is(Status.ACTIVE));

        if (category != null && !category.isBlank()) {
            criteriaList.add(Criteria.where("category").is(category));
        }
        if (minPrice != null) {
            criteriaList.add(Criteria.where("pricePerDay").gte(minPrice));
        }
        if (maxPrice != null) {
            criteriaList.add(Criteria.where("pricePerDay").lte(maxPrice));
        }
        if (location != null && !location.isBlank()) {
            criteriaList.add(Criteria.where("location").regex(location, "i"));
        }

        Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(finalCriteria);

        long total = mongoTemplate.count(query, Listing.class);

        query.with(PageRequest.of(page, size));
        List<Listing> listings = mongoTemplate.find(query, Listing.class);

        List<ListingResponse> responses = listings.stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(responses, PageRequest.of(page, size), total);
    }

    public List<ListingResponse> getPendingListings() {
        return listingRepository.findByStatus(Status.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    public ListingResponse approve(String id) {
        Listing listing = findListingOrThrow(id);
        listing.setStatus(Status.ACTIVE);
        return toResponse(listingRepository.save(listing));
    }

    public ListingResponse reject(String id) {
        Listing listing = findListingOrThrow(id);
        listing.setStatus(Status.INACTIVE);
        return toResponse(listingRepository.save(listing));
    }

    private Listing findListingOrThrow(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("LISTING_NOT_FOUND", "error.notFound"));
    }

    private void assertOwnership(Listing listing, String requesterId) {
        if (!listing.getOwnerId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_LISTING_OWNER", "error.forbidden");
        }
    }

    private ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .ownerId(listing.getOwnerId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .category(listing.getCategory())
                .pricePerDay(listing.getPricePerDay())
                .depositAmount(listing.getDepositAmount())
                .status(listing.getStatus())
                .images(listing.getImages())
                .location(listing.getLocation())
                .createdAt(listing.getCreatedAt())
                .build();
    }

}
