package com.fix.ticket_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.response.TicketReserveResponseDto;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketApplicationService ticketApplicationService;

    @PostMapping("/reserve")
    public ResponseEntity<CommonResponse<List<TicketReserveResponseDto>>> reserveTicket(
        @RequestBody TicketReserveRequestDto request,
        @RequestHeader("x-user-id") Long userId) {
        List<TicketReserveResponseDto> responseList = ticketApplicationService.reserveTicket(request, userId);
        return ResponseEntity.ok(CommonResponse.success(responseList, "티켓 예약 성공"));
    }


}
