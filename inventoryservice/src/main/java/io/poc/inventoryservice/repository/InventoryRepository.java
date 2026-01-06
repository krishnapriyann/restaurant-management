package io.poc.inventoryservice.repository;

import io.poc.inventoryservice.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Food, Long> {


    @Query("SELECT f FROM Food f")
    List<Food> getAllFood();

    Food getByFoodId(Long foodId);

    List<Food> getFoodByIds(List<Long> foodId);

    @Query("SELECT f FROM Food f WHERE f.foodId = :id")
    Food lockProductById(Long id);
}
