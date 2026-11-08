package rentlink.payment.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rentlink.booking.model.Booking;
import rentlink.booking.model.BookingStatus;
import rentlink.booking.repository.BookingRepository;
import rentlink.common.exception.BusinessException;
import rentlink.payment.dto.CreatePaymentIntentRequest;
import rentlink.payment.dto.PaymentIntentResponse;
import rentlink.payment.dto.PaymentResponse;
import rentlink.payment.model.EscrowStatus;
import rentlink.payment.model.Payment;
import rentlink.payment.model.PaymentStatus;
import rentlink.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.10);
    private static final String CURRENCY = "usd";

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;

    @Value("${stripe.webhook-secret}")
    @NonFinal
    String webhookSecret;

    public PaymentIntentResponse createIntent(String requesterId, CreatePaymentIntentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));

        if (!booking.getLesseeId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_BOOKING_LESSEE", "error.forbidden");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw BusinessException.badRequest("BOOKING_NOT_CONFIRMED", "payment.bookingNotConfirmed");
        }
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw BusinessException.conflict("PAYMENT_ALREADY_EXISTS", "payment.alreadyExists");
        }

        BigDecimal amount = booking.getTotalAmount();
        BigDecimal commission = amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        long amountInCents = amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        PaymentIntent intent;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(CURRENCY)
                    .putMetadata("bookingId", booking.getId())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw BusinessException.internal("STRIPE_ERROR", "payment.stripeError");
        }

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .amount(amount)
                .commission(commission)
                .escrowStatus(EscrowStatus.HELD)
                .stripePaymentIntentId(intent.getId())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        return PaymentIntentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(intent.getClientSecret())
                .amount(amount)
                .currency(CURRENCY)
                .build();
    }

    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw BusinessException.badRequest("INVALID_WEBHOOK_SIGNATURE", "payment.invalidWebhookSignature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> updateStatusFromEvent(event, PaymentStatus.SUCCEEDED);
            case "payment_intent.payment_failed" -> updateStatusFromEvent(event, PaymentStatus.FAILED);
            default -> { }
        }
    }

    private void updateStatusFromEvent(Event event, PaymentStatus status) {
        event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
            if (stripeObject instanceof PaymentIntent intent) {
                paymentRepository.findByStripePaymentIntentId(intent.getId())
                        .ifPresent(payment -> {
                            payment.setStatus(status);
                            paymentRepository.save(payment);
                        });
            }
        });
    }

    public boolean hasReleasablePayment(String bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                .filter(p -> p.getEscrowStatus() == EscrowStatus.HELD)
                .isPresent();
    }

    public void releaseEscrow(String bookingId) {
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED
                    && payment.getEscrowStatus() == EscrowStatus.HELD) {
                payment.setEscrowStatus(EscrowStatus.RELEASED);
                paymentRepository.save(payment);
            }
        });
    }

    public void refundEscrow(String bookingId) {
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getEscrowStatus() != EscrowStatus.HELD) {
                return;
            }
            try {
                if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                    RefundCreateParams params = RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .build();
                    Refund.create(params);
                } else {
                    PaymentIntent.retrieve(payment.getStripePaymentIntentId()).cancel();
                }
            } catch (StripeException e) {
                throw BusinessException.internal("STRIPE_ERROR", "payment.stripeError");
            }
            payment.setEscrowStatus(EscrowStatus.REFUNDED);
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });
    }

    public PaymentResponse getByBookingId(String bookingId, String requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));

        if (!booking.getLesseeId().equals(requesterId) && !booking.getOwnerId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_BOOKING_PARTICIPANT", "error.forbidden");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> BusinessException.notFound("PAYMENT_NOT_FOUND", "payment.notFound"));

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .commission(payment.getCommission())
                .escrowStatus(payment.getEscrowStatus())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
