package ua.knu.carrental.payments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.knu.carrental.payments.model.Car;
import ua.knu.carrental.payments.model.Payment;
import ua.knu.carrental.payments.model.RentRequest;
import ua.knu.carrental.payments.repository.PaymentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentsService {
    private final PaymentRepository paymentRepository;
    private final String carsServiceUrl = "http://localhost:8091";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsForCar(int carId) {
        Car car = restTemplate.getForObject(carsServiceUrl + "/car/" + carId, Car.class);
        return paymentRepository.findAllByCarOrderByTimeDesc(car);
    }

    public Payment add(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment getRevenueForRentRequest(int requestId) {
        return paymentRepository.getFirstByRentRequestIdAndType(requestId, Payment.TYPE_REVENUE);
    }
}
