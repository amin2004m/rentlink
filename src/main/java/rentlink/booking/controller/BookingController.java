package rentlink.booking.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.auth.model.User;
import rentlink.booking.dto.BookingResponse;
import rentlink.booking.dto.CreateBookingRequest;
import rentlink.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal User currentUser,
                                                  @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(currentUser.getId(), request));
    }

    @PutMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable String id,
                                   @AuthenticationPrincipal User currentUser) {
        return bookingService.confirm(id, currentUser.getId());
    }

    @PutMapping("/{id}/reject")
    public BookingResponse reject(@PathVariable String id,
                                  @AuthenticationPrincipal User currentUser) {
        return bookingService.reject(id, currentUser.getId());
    }

    @PutMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable String id,
                                  @AuthenticationPrincipal User currentUser) {
        return bookingService.cancel(id, currentUser.getId());
    }

    @PutMapping("/{id}/complete")
    public BookingResponse complete(@PathVariable String id,
                                    @AuthenticationPrincipal User currentUser) {
        return bookingService.complete(id, currentUser.getId());
    }

    @GetMapping("/my/lessee")
    public List<BookingResponse> getMyBookingsAsLessee(@AuthenticationPrincipal User currentUser) {
        return bookingService.getMyBookingsAsLessee(currentUser.getId());
    }

    @GetMapping("/my/owner")
    public List<BookingResponse> getMyBookingsAsOwner(@AuthenticationPrincipal User currentUser) {
        return bookingService.getMyBookingsAsOwner(currentUser.getId());
    }
}