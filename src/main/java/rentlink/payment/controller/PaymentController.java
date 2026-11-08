package rentlink.payment.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.auth.model.User;
import rentlink.payment.dto.CreatePaymentIntentRequest;
import rentlink.payment.dto.PaymentIntentResponse;
import rentlink.payment.dto.PaymentResponse;
import rentlink.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentIntentResponse createIntent(@AuthenticationPrincipal User currentUser,
                                              @Valid @RequestBody CreatePaymentIntentRequest request) {
        return paymentService.createIntent(currentUser.getId(), request);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleWebhookEvent(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/booking/{bookingId}")
    public PaymentResponse getByBooking(@PathVariable String bookingId,
                                        @AuthenticationPrincipal User currentUser) {
        return paymentService.getByBookingId(bookingId, currentUser.getId());
    }
}
