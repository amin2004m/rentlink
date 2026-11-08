package rentlink.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import rentlink.booking.dto.BookingResponse;
import rentlink.booking.dto.CreateBookingRequest;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;
import rentlink.booking.repository.BookingRepository;
import rentlink.common.exception.BusinessException;
import rentlink.listing.model.Listing;
import rentlink.listing.repository.ListingRepository;

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

    public BookingResponse create(String lesseeId, CreateBookingRequest request) {

        // 1. Listing mövcuddurmu?
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> BusinessException.notFound("LISTING_NOT_FOUND", "error.notFound"));

        // 2. Lessee öz elanını kirayə götürməyə çalışmır?
        if (listing.getOwnerId().equals(lesseeId)) {
            throw BusinessException.badRequest("CANNOT_BOOK_OWN_LISTING", "error.forbidden");
        }

        // 3. Listing aktiv statusdadırmı?
        if (listing.getStatus() != rentlink.listing.model.Status.ACTIVE) {
            throw BusinessException.badRequest("LISTING_NOT_ACTIVE", "error.notFound");
        }

        // 4. Tarix aralığı düzgündür?
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw BusinessException.badRequest("BOOKING_INVALID_DATE_RANGE", "booking.invalidDateRange");
        }

        // 5. Həmin tarixlərdə overlap var?
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getListingId(),
                List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw BusinessException.conflict("BOOKING_DATE_CONFLICT", "booking.dateConflict");
        }

        // 6. Ümumi məbləği hesabla: pricePerDay × gün sayı + depositAmount
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
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

        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse confirm(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED);
        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse reject(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED);
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse cancel(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertLessee(booking, requesterId);
        assertStatus(booking, BookingStatus.REQUESTED, BookingStatus.CONFIRMED);
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse complete(String bookingId, String requesterId) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, requesterId);
        assertStatus(booking, BookingStatus.CONFIRMED);
        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(bookingRepository.save(booking));
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

    // --- Private helpers ---

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