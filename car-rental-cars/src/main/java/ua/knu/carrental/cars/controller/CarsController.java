package ua.knu.carrental.cars.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.knu.carrental.cars.service.CarService;
import ua.knu.carrental.cars.model.Car;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class CarsController {
    private final CarService carService;

    @GetMapping("/cars-available")
    public ResponseEntity<List<Car>> getAllAvailableCars() {
        return ResponseEntity.ok(carService.getAvailableCars());
    }

    @GetMapping("/cars-available/{manufacturer}")
    public ResponseEntity<List<Car>> getAllAvailableCars(@PathVariable("manufacturer") String manufacturer) {
        return ResponseEntity.ok(carService.getAvailableCars(manufacturer));
    }

    @GetMapping("/car/{id}")
    public ResponseEntity<Car> getCar(@PathVariable("id") int carId) {
        Car car = carService.getCar(carId);
        return ResponseEntity.ok(car);
    }

    @GetMapping("/car-manufacturers")
    public ResponseEntity<List<String>> getManufacturers() {
        return ResponseEntity.ok(carService.getAllCarManufacturers());
    }

    @Data
    public static class NewRequest {
        public String model;
        public String manufacturer;
        public BigDecimal uahPerDay;
        public String thumbnailUrl;
        public String description;
        public BigDecimal uahPurchase;
    }

    @PostMapping("/new-car")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity addNewCar(@Validated @RequestBody NewRequest request) {
        carService.addCar(
                request.model,
                request.manufacturer,
                request.uahPerDay,
                null,
                request.thumbnailUrl,
                request.description,
                request.uahPurchase
        );
        return ResponseEntity.ok().build();
    }
}
