package com.fix.order_serivce.domain.repository;

import com.fix.order_serivce.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
