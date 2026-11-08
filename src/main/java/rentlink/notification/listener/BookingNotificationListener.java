package rentlink.notification.listener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rentlink.auth.model.User;
import rentlink.auth.repository.UserRepository;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;
import rentlink.listing.model.Listing;
import rentlink.listing.repository.ListingRepository;
import rentlink.notification.event.BookingStatusChangedEvent;
import rentlink.notification.service.EmailService;
import rentlink.notification.service.PushService;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingNotificationListener {

    UserRepository userRepository;
    ListingRepository listingRepository;
    EmailService emailService;
    PushService pushService;

    @Async
    @EventListener
    public void onBookingStatusChanged(BookingStatusChangedEvent event) {
        Booking booking = event.booking();
        String recipientId = recipientFor(booking);
        if (recipientId == null) {
            return;
        }

        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return;
        }

        String listingTitle = listingRepository.findById(booking.getListingId())
                .map(Listing::getTitle)
                .orElse("your listing");

        String subject = subjectFor(booking.getStatus());
        String body = bodyFor(booking, listingTitle);

        emailService.send(recipient.getEmail(), subject, body);
        pushService.sendToUser(recipient.getId(), subject, body);
    }

    private String recipientFor(Booking booking) {
        return switch (booking.getStatus()) {
            case REQUESTED, CANCELLED -> booking.getOwnerId();
            case CONFIRMED, REJECTED, COMPLETED -> booking.getLesseeId();
        };
    }

    private String subjectFor(BookingStatus status) {
        return switch (status) {
            case REQUESTED -> "New booking request";
            case CONFIRMED -> "Booking confirmed";
            case REJECTED -> "Booking rejected";
            case CANCELLED -> "Booking cancelled";
            case COMPLETED -> "Booking completed";
        };
    }

    private String bodyFor(Booking booking, String listingTitle) {
        return switch (booking.getStatus()) {
            case REQUESTED -> "You have a new booking request for \"" + listingTitle + "\".";
            case CONFIRMED -> "Your booking for \"" + listingTitle + "\" has been confirmed.";
            case REJECTED -> "Your booking request for \"" + listingTitle + "\" was rejected.";
            case CANCELLED -> "The booking for \"" + listingTitle + "\" was cancelled by the lessee.";
            case COMPLETED -> "Your booking for \"" + listingTitle + "\" is now complete. Leave a review!";
        };
    }
}
