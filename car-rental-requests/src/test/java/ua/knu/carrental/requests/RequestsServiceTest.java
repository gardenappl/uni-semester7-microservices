package ua.knu.carrental.requests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ua.knu.carrental.requests.model.Car;
import ua.knu.carrental.requests.model.Payment;
import ua.knu.carrental.requests.model.RentRequest;
import ua.knu.carrental.requests.repository.RentRequestRepository;
import ua.knu.carrental.requests.service.RentRequestService;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.core.Authentication;
@SpringBootTest
class RequestsServiceTest {
    @MockBean
    private RentRequestRepository requestRepository;

    private RentRequestService requestService;

    private static final Car testCar = new Car();

    @BeforeAll
    public static void init() {
        testCar.setModel("testModel");
        testCar.setManufacturer("testManufacturer");
        testCar.setUahPerDay(BigDecimal.valueOf(500));
        testCar.setThumbnailUrl("thumb.jpg");
        testCar.setDescription("desc");
        testCar.setUahPurchase(BigDecimal.valueOf(50000));
    }

    @Test
    public void addNewPending() {

        int carId = 0;
        String keyCloakId = "1L";
        int days = 5;
        LocalDate date = LocalDate.now();
        BigDecimal uahAmount = testCar.getUahPerDay().multiply(BigDecimal.valueOf(days));

        RentRequest pendingRequest = new RentRequest();
        pendingRequest.setStatus(RentRequest.STATUS_PENDING);
        pendingRequest.setStatusMessage(null);
        pendingRequest.setUserId(1L);
        pendingRequest.setCar(testCar);
        pendingRequest.setDays(days);
        pendingRequest.setStartDate(date);
        pendingRequest.setRepairCost(null);
        when(requestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        requestService.addNewPending(keyCloakId, carId, days, date, uahAmount);

        verify(requestRepository).save(pendingRequest);
    }

    @Test
    public void addNewPending_badPayment() {
      //  when(carRepository.getById(0)).thenReturn(testCar);

        int carId = 0;
        String keyCloakId = "1L";
        long userId = 1L;
        int days = 5;
        LocalDate date = LocalDate.now();
        BigDecimal uahAmount = testCar.getUahPerDay().multiply(BigDecimal.valueOf(days))
                .subtract(BigDecimal.ONE);

        RentRequest pendingRequest = new RentRequest();
        pendingRequest.setStatus(RentRequest.STATUS_PENDING);
        pendingRequest.setStatusMessage(null);
        pendingRequest.setUserId(userId);
        pendingRequest.setCar(testCar);
        pendingRequest.setDays(days);
        pendingRequest.setStartDate(date);
        pendingRequest.setRepairCost(null);
        when(requestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        assertThrows(RuntimeException.class, () ->
                requestService.addNewPending(keyCloakId, carId, days, date, uahAmount)
        );
    }


    @Test
    public void approve() {
       // when(carRepository.getById(0)).thenReturn(testCar);

        long userId = 1L;
        int requestId = 2;
        int days = 5;
        LocalDate date = LocalDate.now();

        RentRequest pendingRequest = new RentRequest();
        pendingRequest.setId(requestId);
        pendingRequest.setStatus(RentRequest.STATUS_PENDING);
        pendingRequest.setStatusMessage(null);
        pendingRequest.setUserId(userId);
        pendingRequest.setCar(testCar);
        pendingRequest.setDays(days);
        pendingRequest.setStartDate(date);
        pendingRequest.setRepairCost(null);

        RentRequest approvedRequest = new RentRequest();
        approvedRequest.setId(requestId);
        approvedRequest.setStatus(RentRequest.STATUS_ACTIVE);
        approvedRequest.setStatusMessage(null);
        approvedRequest.setUserId(userId);
        approvedRequest.setCar(testCar);
        approvedRequest.setDays(days);
        approvedRequest.setStartDate(date);
        approvedRequest.setRepairCost(null);

        when(requestRepository.getById(requestId)).thenReturn(pendingRequest);
        when(requestRepository.save(approvedRequest)).thenReturn(approvedRequest);

        //requestService.approve(requestId);

        verify(requestRepository).save(approvedRequest);
       // verify(requestRepository).deleteAllByCarAndStatus(testCar, RentRequest.STATUS_PENDING);
    }

    @Test
    public void deny() {
      //  when(carRepository.getById(0)).thenReturn(testCar);

        long userId = 1L;
        int requestId = 2;
        int days = 5;
        LocalDate date = LocalDate.now();
        String statusMessage = "denialTest";

        RentRequest pendingRequest = new RentRequest();
        pendingRequest.setId(requestId);
        pendingRequest.setStatus(RentRequest.STATUS_PENDING);
        pendingRequest.setStatusMessage(null);
        pendingRequest.setUserId(userId);
        pendingRequest.setCar(testCar);
        pendingRequest.setDays(days);
        pendingRequest.setStartDate(date);
        pendingRequest.setRepairCost(null);

        RentRequest deniedRequest = new RentRequest();
        deniedRequest.setId(requestId);
        deniedRequest.setStatus(RentRequest.STATUS_DENIED);
        deniedRequest.setStatusMessage(statusMessage);
        deniedRequest.setUserId(userId);
        deniedRequest.setCar(testCar);
        deniedRequest.setDays(days);
        deniedRequest.setStartDate(date);
        deniedRequest.setRepairCost(null);

        when(requestRepository.getById(requestId)).thenReturn(pendingRequest);
        when(requestRepository.save(deniedRequest)).thenReturn(deniedRequest);

        Payment payment = new Payment();
        payment.setRentRequestId(requestId);
        payment.setType(Payment.TYPE_REVENUE);
        payment.setUahAmount(BigDecimal.valueOf(100));
        payment.setCar(testCar);
        //when(paymentRepository.getFirstByRentRequestIdAndType(requestId, Payment.TYPE_REVENUE))
         //       .thenReturn(payment);

        requestService.deny(requestId, statusMessage);

        verify(requestRepository).save(deniedRequest);
    }

    @Test
    public void end() {


        long userId = 1L;
        int requestId = 2;
        int days = 5;
        LocalDate date = LocalDate.now();

        RentRequest activeRequest = new RentRequest();
        activeRequest.setId(requestId);
        activeRequest.setStatus(RentRequest.STATUS_ACTIVE);
        activeRequest.setStatusMessage(null);
        activeRequest.setUserId(userId);
        activeRequest.setCar(testCar);
        activeRequest.setDays(days);
        activeRequest.setStartDate(date);
        activeRequest.setRepairCost(null);

        RentRequest endedRequest = new RentRequest();
        endedRequest.setId(requestId);
        endedRequest.setStatus(RentRequest.STATUS_ENDED);
        endedRequest.setStatusMessage(null);
        endedRequest.setUserId(userId);
        endedRequest.setCar(testCar);
        endedRequest.setDays(days);
        endedRequest.setStartDate(date);
        endedRequest.setRepairCost(null);

        when(requestRepository.getById(requestId)).thenReturn(activeRequest);
        when(requestRepository.save(endedRequest)).thenReturn(endedRequest);

        requestService.endSuccessfully(requestId, BigDecimal.TEN);

        verify(requestRepository).save(endedRequest);
    }
}