package com.fix.event_service.application.service;

import com.fix.common_service.kafka.dto.PointDeductionFailedPayload;
import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.request.EventUpdateRequestDto;
import com.fix.event_service.application.dtos.response.EventDetailResponseDto;
import com.fix.event_service.application.dtos.response.EventEntryResponseDto;
import com.fix.event_service.application.dtos.response.EventResponseDto;
import com.fix.event_service.application.dtos.response.PageResponseDto;
import com.fix.event_service.application.exception.EventException;
import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventEntry;
import com.fix.event_service.domain.model.EventStatus;
import com.fix.event_service.domain.model.Reward;
import com.fix.event_service.domain.repository.EventRepository;
import com.fix.event_service.domain.service.EventDomainService;
import com.fix.event_service.infrastructure.kafka.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final EventDomainService eventDomainService;
    private final EventProducer eventProducer;
    private final QuartzSchedulerService schedulerService;

    @Transactional
    public EventDetailResponseDto createEvent(EventCreateRequestDto requestDto) {
        Event event = Event.createEvent(
                requestDto.getEventName(),
                requestDto.getDescription(),
                requestDto.getEventStartAt(),
                requestDto.getEventEndAt(),
                requestDto.getMaxWinners(),
                requestDto.getRequiredPoints()
        );

        Reward reward = Reward.createReward(
                requestDto.getReward().getRewardName(),
                requestDto.getReward().getQuantity(),
                requestDto.getReward().getDescription()
        );

        event.addReward(reward);

        eventRepository.save(event);

        // 이벤트 시작과 종료 시점에 Quartz Job 등록
        enrollQuartzJob(event);

        return new EventDetailResponseDto(event);
    }

    @Transactional
    public void applyEvent(UUID eventId, Long userId) {
        log.info("이벤트 응모 신청 : eventId={}, userId={}", eventId, userId);

        Event event = findEventById(eventId);

        event.isEventOpenForApplication();

        EventEntry entry = EventEntry.createEventEntry(event, userId);

        event.addEntry(entry);

        log.info("Kafka 이벤트 발행 시도 (ApplyEvent): eventId={}, userId={}, entryId={}", eventId, userId, entry.getEntryId());
        // 기존의 Feign 호출 제거, 대신 Kafka 이벤트 발행
        eventProducer.sendEventApplyRequest(eventId, userId, event.getRequiredPoints(), entry.getEntryId());
        log.info("이벤트 응모 신청 성공 : eventId={}, userId={}, entryId={}", eventId, userId, entry.getEntryId());
    }

    @Transactional(readOnly = true)
    public EventDetailResponseDto getEvent(UUID eventId) {
        Event event = findEventById(eventId);

        return new EventDetailResponseDto(event);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<EventResponseDto> getEvents(int page, int size) {
        Page<EventResponseDto> mappedPage = eventRepository.findAll(page, size).map(EventResponseDto::new);
        return new PageResponseDto<>(mappedPage);
    }

    public PageResponseDto<EventResponseDto> searchEvents(EventStatus status, String keyword, int page, int size) {
        Page<EventResponseDto> mappedPage = eventRepository.searchEvents(status, keyword, page, size)
                .map(EventResponseDto::new);

        return new PageResponseDto<>(mappedPage);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<EventEntryResponseDto> getEventEntries(UUID eventId, int page, int size) {
        Event event = findEventById(eventId);

        Pageable pageable = PageRequest.of(page, size);
        List<EventEntryResponseDto> entryDtoList = event.getEntries().stream()
            .map(EventEntryResponseDto::new)
            .toList();

        Page<EventEntryResponseDto> mappedPage = new PageImpl<>(entryDtoList, pageable, entryDtoList.size());

        return new PageResponseDto<>(mappedPage);
    }

    @Transactional
    public EventDetailResponseDto updateEvent(UUID eventId, EventUpdateRequestDto requestDto) {
        Event event = findEventById(eventId);

        event.checkUpdatable();

        Reward newReward = Reward.createReward(
                requestDto.getReward().getRewardName(),
                requestDto.getReward().getQuantity(),
                requestDto.getReward().getDescription()
        );

        event.updateEvent(
                requestDto.getEventName(),
                requestDto.getDescription(),
                requestDto.getEventStartAt(),
                requestDto.getEventEndAt(),
                requestDto.getMaxWinners(),
                requestDto.getRequiredPoints(),
                newReward
        );

        // 기존 Quartz Job 삭제 후 재등록
        schedulerService.removeEventJobs(eventId);
        enrollQuartzJob(event);

        return new EventDetailResponseDto(event);
    }

    @Transactional
    public void cancelEventApply(PointDeductionFailedPayload payload) {
        Event event = findEventById(payload.getEventId());

        Optional<EventEntry> entryToRemove = event.getEntries().stream()
                .filter(e -> e.getEntryId().equals(payload.getEventEntryId()) && e.getUserId().equals(payload.getUserId()))
                .findFirst();

        entryToRemove.ifPresent(event::removeEntry);
    }

    @Transactional
    public void deleteEvent(UUID eventId, Long userId) {
        Event event = findEventById(eventId);

        event.checkDeletable();

        schedulerService.removeEventJobs(eventId);

        event.softDelete(userId);
    }

    @Transactional
    public void startEventByScheduler(UUID eventId) {
        Event event = findEventById(eventId);
        event.startEvent();
        log.info("Quartz 스케줄러에 의해 이벤트 시작됨 : eventId={}", eventId);
    }

    @Transactional
    public void endEventAndAnnounceByScheduler(UUID eventId) {
        Event event = findEventById(eventId);
        event.endEvent();

        // 당첨자 선정
        List<EventEntry> winners = eventDomainService.selectRandomWinners(event, event.getEntries());
        List<Long> winnerIds = winners.stream().map(EventEntry::getUserId).toList();
        log.info("Quartz 스케쥴러에 의해 이벤트 종료 및 당첨자 선정 완료: eventId={}", eventId);
        // 알림 발송 요청
        eventProducer.sendEventWinnersNotification(eventId, winnerIds);
    }

    private Event findEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventException.EventErrorType.EVENT_NOT_FOUND));
    }

    private void enrollQuartzJob(Event event) {
        Date startAt = Date.from(event.getEventPeriod().getEventStartAt().atZone(ZoneId.systemDefault()).toInstant());
        Date endAt = Date.from(event.getEventPeriod().getEventEndAt().atZone(ZoneId.systemDefault()).toInstant());

        schedulerService.scheduleEventJob(event.getEventId(), startAt, "START");
        schedulerService.scheduleEventJob(event.getEventId(), endAt, "END");
    }
}
