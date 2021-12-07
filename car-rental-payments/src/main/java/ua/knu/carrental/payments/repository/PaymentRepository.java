package ua.knu.carrental.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.knu.carrental.payments.model.Car;
import ua.knu.carrental.payments.model.Payment;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment getFirstByRentRequestIdAndType(int requestId, int type);

    List<Payment> findAllByCarOrderByTimeDesc(Car car);
}
