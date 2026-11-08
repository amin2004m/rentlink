package rentlink.dispute.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.auth.model.User;
import rentlink.dispute.dto.CreateDisputeRequest;
import rentlink.dispute.dto.DisputeResponse;
import rentlink.dispute.service.DisputeService;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DisputeController {

    DisputeService disputeService;

    @PostMapping
    public ResponseEntity<DisputeResponse> create(@AuthenticationPrincipal User currentUser,
                                                  @Valid @RequestBody CreateDisputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disputeService.create(currentUser.getId(), request));
    }

    @GetMapping("/booking/{bookingId}")
    public List<DisputeResponse> getByBooking(@PathVariable String bookingId,
                                              @AuthenticationPrincipal User currentUser) {
        return disputeService.getByBookingId(bookingId, currentUser.getId());
    }
}
