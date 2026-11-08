package rentlink.payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rentlink.payment.model.Payment;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
