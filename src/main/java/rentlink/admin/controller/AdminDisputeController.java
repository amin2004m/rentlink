package rentlink.admin.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rentlink.dispute.dto.DisputeResponse;
import rentlink.dispute.dto.ResolveDisputeRequest;
import rentlink.dispute.model.DisputeStatus;
import rentlink.dispute.service.DisputeService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDisputeController {

    DisputeService disputeService;

    @GetMapping
    public List<DisputeResponse> getAll(@RequestParam(required = false) DisputeStatus status) {
        return disputeService.getAll(status);
    }

    @PutMapping("/{id}/resolve")
    public DisputeResponse resolve(@PathVariable String id,
                                   @Valid @RequestBody ResolveDisputeRequest request) {
        return disputeService.resolve(id, request);
    }
}
