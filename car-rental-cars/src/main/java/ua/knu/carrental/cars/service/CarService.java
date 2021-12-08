package ua.knu.carrental.cars.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.knu.carrental.cars.model.Car;
import ua.knu.carrental.cars.model.Payment;
import ua.knu.carrental.cars.model.User;
import ua.knu.carrental.cars.repository.CarRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository carRepository;
    private final String paymentServiceUrl = "http://localhost:8092";
    private final String usersServiceUrl = "http://localhost:8094";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Car> getAvailableCars() {
        return carRepository.findByUserIsNull();
    }

    public List<Car> getAvailableCars(String manufacturer) {
        return carRepository.findByUserIsNullAndManufacturer(manufacturer);
    }

    public Car getCar(int id) {
        return carRepository.findById(id).get();
    }

    public List<String> getAllCarManufacturers() {
        return carRepository.findAllCarManufacturers();
    }

    public Car addCar(String model, String manufacturer, BigDecimal uahPerDay, User user, String thumbnailUrl, String description, BigDecimal uahPurchase) {
        Car car = new Car();
        car.setModel(model);
        car.setManufacturer(manufacturer);
        car.setUahPerDay(uahPerDay);
        car.setThumbnailUrl(thumbnailUrl);
        car.setDescription(description);
        car.setUahPurchase(uahPurchase);
        car.setUser(user);
        car = carRepository.save(car);

        Payment payment = new Payment();
        payment.setUahAmount(uahPurchase.negate());
        payment.setCar(car);
        payment.setRentRequestId(null);
        payment.setType(Payment.TYPE_PURCHASE_NEW_CAR);
        payment.setTime(Instant.now().toString());
        restTemplate.postForEntity(paymentServiceUrl + "/payments", payment, Payment.class);
        return car;
    }

    public Car setOwner(int carId, long userId) {
        Car car = getCar(carId);

        if (userId == 0) {
            car.setUser(null);
        } else {
            User user = restTemplate.getForObject(usersServiceUrl + "/users/" + userId, User.class);
            car.setUser(user);
        }
        return carRepository.save(car);
    }
}
