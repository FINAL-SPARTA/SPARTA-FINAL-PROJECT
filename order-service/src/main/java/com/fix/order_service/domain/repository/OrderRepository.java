package com.fix.order_service.domain.repository;

import com.fix.order_service.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT DISTINCT o.userId FROM Order o WHERE o.gameId = :gameId")
    List<Long> findUserIdsByGameId(@Param("gameId") UUID gameId);
}
