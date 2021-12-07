package ua.knu.carrental.payments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.knu.carrental.payments.model.Payment;
import ua.knu.carrental.payments.service.PaymentsService;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentsService paymentsService;

    @GetMapping("/payments")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentsService.getAllPayments());
    }

    @GetMapping("/payments/car/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<List<Payment>> getPaymentsForCar(@PathVariable("id") int carId) {
        return ResponseEntity.ok(paymentsService.getPaymentsForCar(carId));
    }

    @GetMapping("/payments/revenue/{id}")
    public ResponseEntity<Payment> getRevenueForRequest(@PathVariable("id") int rentRequestId) {
        return ResponseEntity.ok(paymentsService.getRevenueForRentRequest(rentRequestId));
    }

    @PostMapping("payments")
    public ResponseEntity<Payment> add(Payment payment) {
        return ResponseEntity.ok(paymentsService.add(payment));
    }
}
