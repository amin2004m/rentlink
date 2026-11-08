package rentlink.notification.event;

import rentlink.booking.model.Booking;

public record BookingStatusChangedEvent(Booking booking) {
}
