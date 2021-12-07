package ua.knu.carrental.requests.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.knu.carrental.requests.model.RentRequest;
import ua.knu.carrental.requests.service.RentRequestService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class RequestsController {
    private final RentRequestService rentRequestService;

    @GetMapping("/requests/my-outdated")
    @PreAuthorize("hasAnyAuthority('user')")
    public ResponseEntity<List<RentRequest>> getOutdatedRequests(Authentication auth) {
        return ResponseEntity.ok(rentRequestService.getActiveOutdatedRequests(auth.getName()));
    }

    @GetMapping("/requests/status/my/{status}")
    @PreAuthorize("hasAnyAuthority('user')")
    public ResponseEntity<List<RentRequest>> getRequests(@PathVariable("status") int status, Authentication auth) {
        return ResponseEntity.ok(rentRequestService.getRequestsWithStatusForUser(status, auth.getName()));
    }

    @GetMapping("/requests/outdated")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<List<RentRequest>> getOutdatedRequests() {
        return ResponseEntity.ok(rentRequestService.getActiveOutdatedRequests());
    }

    @GetMapping("/requests/status/{status}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<List<RentRequest>> getRequests(@PathVariable("status") int status) {
        return ResponseEntity.ok(rentRequestService.getRequestsWithStatus(status));
    }

    @PostMapping("/requests/approve/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity approve(@PathVariable("id") int requestId, Authentication auth) {
        rentRequestService.approve(requestId, auth);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class DenyRequest {
        private String message;
    }

    @PostMapping("/requests/deny/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity deny(@PathVariable("id") int requestId, @Validated @RequestBody DenyRequest request, Authentication auth) {
        rentRequestService.deny(requestId, request.message, auth);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class EndSuccessfullyRequest {
        private BigDecimal maintenanceCostUah;
    }

    @PostMapping("/requests/end/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity end(@PathVariable("id") int requestId, @Validated @RequestBody EndSuccessfullyRequest request, Authentication auth) {
        rentRequestService.endSuccessfully(requestId, request.maintenanceCostUah, auth);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class BrokenRequest {
        private String message;
        private BigDecimal repairCostUah;
    }

    @PostMapping("/requests/broken/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity broken(@PathVariable("id") int requestId, @Validated @RequestBody BrokenRequest request, Authentication auth) {
        rentRequestService.setNeedsRepair(requestId, request.message, request.repairCostUah, auth);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class RepairRequest {
        private BigDecimal paymentUah;
    }

    @PostMapping("/requests/repair/{id}")
    public ResponseEntity repair(@PathVariable("id") int requestId, @Validated @RequestBody RepairRequest request, Authentication auth) {
        rentRequestService.payForRepair(requestId, request.paymentUah, auth);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class NewRentRequest {
        private BigDecimal paymentUah;
        private int carId;
        private int days;
        private LocalDate startDate;
    }

    @PostMapping("/requests/new")
    @PreAuthorize("hasAnyAuthority('user')")
    public ResponseEntity<Integer> newPending(@Validated @RequestBody NewRentRequest request, Authentication auth) {
        return ResponseEntity.ok(rentRequestService.addNewPending(
                auth.getName(),
                request.carId,
                request.days,
                request.startDate,
                request.paymentUah
        ).getId());
    }
}
