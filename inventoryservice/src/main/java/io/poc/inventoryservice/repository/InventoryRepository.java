package io.poc.inventoryservice.repository;

import io.poc.inventoryservice.entity.Food;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Food, Long> {


    @Query("SELECT f FROM Food f")
    List<Food> getAllFood();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Food f WHERE f.foodId = :id")
    Food lockFoodById(@Param("id") Long id);

}
