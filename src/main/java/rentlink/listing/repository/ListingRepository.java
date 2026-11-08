package rentlink.listing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rentlink.listing.model.Listing;
import rentlink.listing.model.Status;

import java.util.List;

public interface ListingRepository extends MongoRepository<Listing, String> {
    List<Listing> findByStatus(Status status);

    List<Listing> findByOwnerId(String ownerId);

    List<Listing> findByOwnerIdAndStatus(String ownerId, Status status);
}
