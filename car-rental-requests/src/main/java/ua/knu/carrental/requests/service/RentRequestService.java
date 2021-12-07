package ua.knu.carrental.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ua.knu.carrental.requests.model.Car;
import ua.knu.carrental.requests.model.Payment;
import ua.knu.carrental.requests.model.RentRequest;
import ua.knu.carrental.requests.repository.RentRequestRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentRequestService {
    private final RentRequestRepository rentRequestRepository;
    private final String carsServiceUrl = "http://localhost:8081";
    private final String paymentsServiceUrl = "http://localhost:8082";
    private final String usersServiceUrl = "http://localhost:8084";
    private final RestTemplate restTemplate = new RestTemplate();


    public List<RentRequest> getRequestsWithStatus(int status) {
        return rentRequestRepository.findAllByStatus(status);
    }

    public List<RentRequest> getRequestsWithStatusForUser(int status, String keycloakId) {
        Long userId = restTemplate.getForObject(usersServiceUrl + "/users/id/" + keycloakId, Long.class);
        return rentRequestRepository.findAllByStatusAndUserId(status, userId);
    }

    public List<RentRequest> getActiveOutdatedRequests() {
        LocalDate now = LocalDate.now();
        return getRequestsWithStatus(RentRequest.STATUS_ACTIVE)
                .stream().filter(rentRequest -> rentRequest.getEndDate().compareTo(now) < 0)
                .collect(Collectors.toList());
    }

    public List<RentRequest> getActiveOutdatedRequests(String keycloakId) {
        LocalDate now = LocalDate.now();
        return getRequestsWithStatusForUser(RentRequest.STATUS_ACTIVE, keycloakId)
                .stream().filter(rentRequest -> rentRequest.getEndDate().compareTo(now) < 0)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentRequest addNewPending(String keycloakId, int carId, int days, LocalDate startDate, BigDecimal uahAmount) {
        Long userId = restTemplate.getForObject(usersServiceUrl + "/users/id/" + keycloakId, Long.class);
        Car car = restTemplate.getForObject(carsServiceUrl + "/car/" + carId, Car.class);

        if (uahAmount.compareTo(car.getUahPerDay().multiply(BigDecimal.valueOf(days))) < 0)
            throw new IllegalArgumentException("Payment amount is not high enough");

        RentRequest request = new RentRequest();
        request.setUserId(userId);
        request.setDays(days);
        request.setCar(car);
        request.setStartDate(startDate);
        request.setStatus(RentRequest.STATUS_PENDING);
        request = rentRequestRepository.save(request);

        Payment newPayment = new Payment();
        newPayment.setTime(Instant.now());
        newPayment.setRentRequestId(request.getId());
        newPayment.setType(Payment.TYPE_REVENUE);
        newPayment.setCar(car);
        newPayment.setUahAmount(uahAmount);
        HttpEntity<Payment> entity = new HttpEntity<>(newPayment);
        restTemplate.exchange(paymentsServiceUrl + "/payments/add", HttpMethod.POST, entity, Void.class);

        return request;
    }

    @Transactional
    public RentRequest approve(int id, Authentication auth) {
        RentRequest request = rentRequestRepository.getById(id);

        if (request.getStatus() != RentRequest.STATUS_PENDING) {
            throw new IllegalArgumentException("Bad rent request ID");
        }

        request.setStatus(RentRequest.STATUS_ACTIVE);
        request = rentRequestRepository.save(request);

        Car car = request.getCar();
        put(carsServiceUrl + "/cars/" + car.getId() + "/set-owner", request.getUserId(), auth);

        List<RentRequest> requests = rentRequestRepository.findAllByCarAndStatus(car, RentRequest.STATUS_PENDING);
        for (RentRequest pendingRequest : requests) {
            if (pendingRequest.getId() != id)
                deny(id, "Another user got this car", auth);
        }

        return request;
    }

    public RentRequest deny(int id, String message, Authentication auth) {
        RentRequest request = rentRequestRepository.getById(id);

        if (request.getStatus() != RentRequest.STATUS_PENDING) {
            throw new IllegalArgumentException("Bad rent request ID");
        }

        request.setStatus(RentRequest.STATUS_DENIED);
        request.setStatusMessage(message);
        request = rentRequestRepository.save(request);

        //Do refund

        Payment payment = get(paymentsServiceUrl + "/payments/revenue/" + id, Payment.class, auth);
        Payment newPayment = new Payment();
        newPayment.setTime(Instant.now());
        newPayment.setRentRequestId(id);
        newPayment.setType(Payment.TYPE_REFUND);
        newPayment.setCar(payment.getCar());
        newPayment.setUahAmount(payment.getUahAmount().negate());

        restTemplate.postForEntity(paymentsServiceUrl + "/payments", newPayment, Payment.class);
        return request;
    }

    public RentRequest endSuccessfully(int id, BigDecimal maintenanceCostUah, Authentication auth) {
        RentRequest request = rentRequestRepository.getById(id);

        if (request.getStatus() != RentRequest.STATUS_ACTIVE) {
            throw new IllegalArgumentException("Bad rent request ID");
        }

        request.setStatus(RentRequest.STATUS_ENDED);
        request = rentRequestRepository.save(request);

        Car car = request.getCar();
        put(carsServiceUrl + "/cars/" + car.getId() + "/set-owner", 0, auth);

        Payment newPayment = new Payment();
        newPayment.setTime(Instant.now());
        newPayment.setRentRequestId(id);
        newPayment.setType(Payment.TYPE_MAINTENANCE);
        newPayment.setCar(car);
        newPayment.setUahAmount(maintenanceCostUah.negate());
        restTemplate.postForEntity(paymentsServiceUrl + "/payments", newPayment, Payment.class);

        return request;
    }

    public RentRequest setNeedsRepair(int id, String message, BigDecimal paymentCost, Authentication auth) {
        RentRequest request = rentRequestRepository.getById(id);

        if (request.getStatus() != RentRequest.STATUS_ACTIVE) {
            throw new IllegalArgumentException("Bad rent request ID");
        }

        request.setStatus(RentRequest.STATUS_REPAIR_NEEDED);
        request.setStatusMessage(message);
        request.setRepairCost(paymentCost);
        request = rentRequestRepository.save(request);

        Payment newPayment = new Payment();
        newPayment.setTime(Instant.now());
        newPayment.setRentRequestId(id);
        newPayment.setType(Payment.TYPE_REPAIR_COST);
        newPayment.setCar(request.getCar());
        newPayment.setUahAmount(paymentCost.negate());
        restTemplate.postForEntity(paymentsServiceUrl + "/payments", newPayment, Payment.class);

        return request;
    }

    public RentRequest payForRepair(int id, BigDecimal uahAmount, Authentication auth) {
        RentRequest request = rentRequestRepository.getById(id);
        if (request.getStatus() != RentRequest.STATUS_REPAIR_NEEDED)
            throw new IllegalArgumentException("Bad rent request ID");

        if (!Objects.equals(uahAmount, request.getRepairCost())
                && uahAmount.compareTo(request.getRepairCost()) < 0)
            throw new IllegalArgumentException("Amount is not high enough");

        request.setStatus(RentRequest.STATUS_ENDED);
        request.setStatusMessage(null);
        request = rentRequestRepository.save(request);

        Car car = request.getCar();
        put(carsServiceUrl + "/cars/" + car.getId() + "/set-owner", 0, auth);

        Payment newPayment = new Payment();
        newPayment.setTime(Instant.now());
        newPayment.setRentRequestId(id);
        newPayment.setType(Payment.TYPE_REPAIR_PAID_BY_CUSTOMER);
        newPayment.setCar(car);
        newPayment.setUahAmount(uahAmount);
        restTemplate.postForEntity(paymentsServiceUrl + "/payments", newPayment, Payment.class);

        return request;
    }

    private <T> void put(String url, T object, Authentication auth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getName());
        HttpEntity<T> entity = new HttpEntity<>(object, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    private <T> T get(String url, Class<T> cls, Authentication auth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getName());
        HttpEntity entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, cls).getBody();
    }
}
