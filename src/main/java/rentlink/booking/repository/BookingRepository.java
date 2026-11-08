package rentlink.booking.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByListingId(String listingId);

    List<Booking> findByLesseeId(String lesseeId);

    List<Booking> findByOwnerId(String ownerId);

    @Query("{ 'listingId': ?0, 'status': { $in: ?1 }, 'startDate': { $lt: ?3 }, 'endDate': { $gt: ?2 } }")
    List<Booking> findOverlappingBookings(String listingId,
                                          List<BookingStatus> activeStatuses,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate);
}
