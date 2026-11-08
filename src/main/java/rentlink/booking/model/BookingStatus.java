package rentlink.booking.model;

import lombok.Getter;

@Getter
public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    REJECTED,
    COMPLETED,
    CANCELLED;
}