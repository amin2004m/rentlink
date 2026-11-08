package rentlink.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import rentlink.booking.dto.BookingResponse;
import rentlink.booking.dto.CreateBookingRequest;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;
import rentlink.booking.repository.BookingRepository;
import rentlink.common.exception.BusinessException;
import rentlink.listing.model.Listing;
import rentlink.listing.repository.ListingRepository;
import rentlink.notification.event.BookingStatusChangedEvent;
import rentlink.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    ListingRepository listingRepository;
    PaymentService paymentService;
    ApplicationEventPublisher eventPublisher;

    public BookingResponse create(String lesseeId, CreateBookingRequest request) {

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> BusinessException.notFound("LISTING_NOT_FOUND", "error.notFound"));

        if (listing.getOwnerId().equals(lesseeId)) {
            throw BusinessException.badRequest("CANNOT_BOOK_OWN_LISTING", "error.forbidden");
        }

        if (listing.getStatus() != rentlink.listing.model.Status.ACTIVE) {
            throw BusinessException.badRequest("LISTING_NOT_ACTIVE", "error.notFound");
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw BusinessException.badRequest("BOOKING_INVALID_DATE_RANGE", "booking.invalidDateRange");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getListingId(),
                List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw BusinessException.conflict("BOOKING_DATE_CONFLICT", "booking.dateConflict");
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()));
        BigDecimal totalAmount = BigDecimal.valueOf(listing.getPricePerDay())
                .multiply(BigDecimal.valueOf(days))
                .add(BigDecimal.valueOf(listing.getDepositAmount()));

        Booking booking = Booking.builder()
                .listingId(request.getListingId())
                .lesseeId(lesseeId)
                .ownerId(listing.getOwnerId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(totalAmount)
                .status(BookingStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking));
        return toResponse(booking);
    }

    public BookingResponse confirm(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking));
        return toResponse(booking);
    }

    public BookingResponse reject(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED);
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking));
        return toResponse(booking);
    }

    public BookingResponse cancel(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertLessee(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED, BookingStatus.CONFIRMED);
        paymentService.refundEscrow(booking.getId());
        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking));
        return toResponse(booking);
    }

    public BookingResponse complete(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.CONFIRMED);
        if (!paymentService.hasReleasablePayment(bookingId)) {
            throw BusinessException.badRequest("BOOKING_NOT_PAID", "booking.notPaid");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);
        paymentService.releaseEscrow(booking.getId());
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking));
        return toResponse(booking);
    }

    public List<BookingResponse> getMyBookingsAsLessee(String lesseeId) {
        return bookingRepository.findByLesseeId(lesseeId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BookingResponse> getMyBookingsAsOwner(String ownerId) {
        return bookingRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Booking findOrThrow(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));
    }

    private void assertOwner(Booking booking, String requesterId) {
        if (!booking.getOwnerId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_BOOKING_OWNER", "error.forbidden");
        }
    }

    private void assertLessee(Booking booking, String requesterId) {
        if (!booking.getLesseeId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_BOOKING_LESSEE", "error.forbidden");
        }
    }

    private void assertStatus(Booking booking, BookingStatus... allowed) {
        for (BookingStatus s : allowed) {
            if (booking.getStatus() == s) return;
        }
        throw BusinessException.badRequest("INVALID_BOOKING_STATUS",
                "Booking status " + booking.getStatus() + " does not allow this operation");
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .listingId(booking.getListingId())
                .lesseeId(booking.getLesseeId())
                .ownerId(booking.getOwnerId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}