package com.fix.order_serivce.domain.repository;

import com.fix.order_serivce.application.dtos.request.OrderSearchCondition;
import com.fix.order_serivce.application.dtos.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryRepository {
    Page<OrderResponse> search(OrderSearchCondition condition, Pageable pageable);
}
