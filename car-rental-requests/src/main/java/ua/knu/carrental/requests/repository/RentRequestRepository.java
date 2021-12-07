package ua.knu.carrental.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.knu.carrental.requests.model.Car;
import ua.knu.carrental.requests.model.RentRequest;

import java.util.List;

@Repository
public interface RentRequestRepository extends JpaRepository<RentRequest, Integer> {
    List<RentRequest> findAllByCarAndStatus(Car car, int status);

    List<RentRequest> findAllByStatus(int status);

    List<RentRequest> findAllByStatusAndUserId(int status, long userId);
}
