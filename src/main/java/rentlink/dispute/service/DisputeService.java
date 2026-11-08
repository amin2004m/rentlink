package rentlink.dispute.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import rentlink.booking.model.Booking;
import rentlink.booking.repository.BookingRepository;
import rentlink.common.exception.BusinessException;
import rentlink.dispute.dto.CreateDisputeRequest;
import rentlink.dispute.dto.DisputeOutcome;
import rentlink.dispute.dto.DisputeResponse;
import rentlink.dispute.dto.ResolveDisputeRequest;
import rentlink.dispute.model.Dispute;
import rentlink.dispute.model.DisputeStatus;
import rentlink.dispute.repository.DisputeRepository;
import rentlink.payment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DisputeService {

    DisputeRepository disputeRepository;
    BookingRepository bookingRepository;
    PaymentService paymentService;

    public DisputeResponse create(String raiserId, CreateDisputeRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));

        if (!booking.getLesseeId().equals(raiserId) && !booking.getOwnerId().equals(raiserId)) {
            throw BusinessException.forbidden("NOT_BOOKING_PARTICIPANT", "error.forbidden");
        }

        if (disputeRepository.findByBookingIdAndStatus(booking.getId(), DisputeStatus.OPEN).isPresent()) {
            throw BusinessException.conflict("DISPUTE_ALREADY_OPEN", "dispute.alreadyOpen");
        }

        Dispute dispute = Dispute.builder()
                .bookingId(booking.getId())
                .raisedBy(raiserId)
                .reason(request.getReason())
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(disputeRepository.save(dispute));
    }

    public DisputeResponse resolve(String disputeId, ResolveDisputeRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> BusinessException.notFound("DISPUTE_NOT_FOUND", "error.notFound"));

        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw BusinessException.badRequest("DISPUTE_ALREADY_RESOLVED", "dispute.alreadyResolved");
        }

        DisputeOutcome outcome = request.getOutcome() == null ? DisputeOutcome.NO_ACTION : request.getOutcome();
        switch (outcome) {
            case REFUND_LESSEE -> paymentService.refundEscrow(dispute.getBookingId());
            case RELEASE_OWNER -> paymentService.releaseEscrow(dispute.getBookingId());
            case NO_ACTION -> { }
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(request.getResolution());
        dispute.setResolvedAt(LocalDateTime.now());

        return toResponse(disputeRepository.save(dispute));
    }

    public List<DisputeResponse> getByBookingId(String bookingId, String requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BusinessException.notFound("BOOKING_NOT_FOUND", "error.notFound"));

        if (!booking.getLesseeId().equals(requesterId) && !booking.getOwnerId().equals(requesterId)) {
            throw BusinessException.forbidden("NOT_BOOKING_PARTICIPANT", "error.forbidden");
        }

        return disputeRepository.findByBookingId(bookingId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DisputeResponse> getAll(DisputeStatus status) {
        List<Dispute> disputes = status == null
                ? disputeRepository.findAll()
                : disputeRepository.findByStatus(status);
        return disputes.stream().map(this::toResponse).toList();
    }

    private DisputeResponse toResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .bookingId(dispute.getBookingId())
                .raisedBy(dispute.getRaisedBy())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .resolution(dispute.getResolution())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }
}
