package io.poc.inventoryservice.repository;

import io.poc.inventoryservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> getByOrderId(Long orderId);

    Optional<Reservation> findByOrderIdAndFoodId(Long orderId, Long foodId);
}
