package com.fix.ticket_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.ticket_service.application.aop.ValidateUser;
import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.dtos.response.PageResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketDetailResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketReserveResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketResponseDto;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketApplicationService ticketApplicationService;

    // ✅ 티켓 예약 API
    @PostMapping("/reserve")
    public ResponseEntity<CommonResponse<List<TicketReserveResponseDto>>> reserveTicket(
        @RequestBody TicketReserveRequestDto request,
        @RequestHeader("x-user-id") Long userId) {
        List<TicketReserveResponseDto> responseDto = ticketApplicationService.reserveTicket(request, userId);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "티켓 예약 성공"));
    }

    // ✅ 티켓 단건 상세 조회 API
    @GetMapping("/{ticketId}")
    public ResponseEntity<CommonResponse<TicketDetailResponseDto>> getTicket(
        @PathVariable("ticketId") UUID ticketId,
        @RequestHeader("x-user-id") Long userId,
        @RequestHeader("x-user-role") String userRole) {
        TicketDetailResponseDto responseDto = ticketApplicationService.getTicket(ticketId, userId, userRole);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "티켓 조회 성공"));
    }

    // ✅ 로그인한 유저의 티켓 목록 조회 API
    @GetMapping()
    public ResponseEntity<CommonResponse<PageResponseDto<TicketResponseDto>>> getTickets(
        @RequestHeader("x-user-id") Long userId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<TicketResponseDto> responseDto = ticketApplicationService.getTickets(userId, page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "티켓 목록 조회 성공"));
    }

    // ✅ 티켓 검색 API
    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageResponseDto<TicketResponseDto>>> searchTickets(
        @RequestParam(value = "gameId") UUID gameId,
        @RequestParam(value = "userId", required = false) Long userId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<TicketResponseDto> responseDto =
            ticketApplicationService.searchTickets(gameId, userId, page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "티켓 검색 성공"));
    }

    // ✅ 주문 생성 및 결제 처리가 완료된 티켓 목록 업데이트 API
    @PatchMapping("/sold")
    public ResponseEntity<CommonResponse<Void>> updateTicketStatus(
        @RequestBody TicketSoldRequestDto requestDto) {
        ticketApplicationService.updateTicketStatus(requestDto);
        return ResponseEntity.ok(CommonResponse.success(null, "티켓 상태 업데이트 (RESERVED -> SOLD) 성공"));
    }

    // ✅ 주문이 취소된 티켓(List) 상태 업데이트 API
    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<CommonResponse<Void>> cancelTicketStatus(
        @PathVariable("orderId") UUID orderId) {
        ticketApplicationService.cancelTicketStatus(orderId);
        return ResponseEntity.ok(CommonResponse.success(null, "티켓 상태 업데이트 (SOLD -> CANCELLED) 성공"));
    }


    // ✅ 티켓 단건 삭제 API
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<CommonResponse<Void>> deleteTicket(
        @PathVariable("ticketId") UUID ticketId,
        @RequestHeader("x-user-id") Long userId,
        @RequestHeader("x-user-role") String userRole) {
        ticketApplicationService.deleteTicket(ticketId, userId, userRole);
        return ResponseEntity.ok(CommonResponse.success(null, "티켓 삭제 성공"));
    }

    // ✅ 예약 상태인 티켓 일괄 삭제 API (스케쥴링용 API)
    @DeleteMapping("/delete-reserved")
    public ResponseEntity<CommonResponse<Void>> deleteReservedTickets() {
        ticketApplicationService.deleteReservedTickets();
        return ResponseEntity.ok(CommonResponse.success(null, "예약 상태인 티켓 일괄 삭제 성공"));
    }
}
