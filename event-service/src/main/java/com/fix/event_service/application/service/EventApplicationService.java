package com.fix.event_service.application.service;

import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.request.EventUpdateRequestDto;
import com.fix.event_service.application.dtos.response.*;
import com.fix.event_service.application.exception.EventException;
import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventEntry;
import com.fix.event_service.domain.model.EventStatus;
import com.fix.event_service.domain.model.Reward;
import com.fix.event_service.domain.repository.EventRepository;
import com.fix.event_service.domain.service.EventDomainService;
import com.fix.event_service.infrastructure.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final EventDomainService eventDomainService;
    private final UserClient userClient;

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

        return new EventDetailResponseDto(event);
    }

    @Transactional
    public void applyEvent(UUID eventId, Long userId) {
        Event event = findEventById(eventId);

        event.isEventOpenForApplication();

        userClient.deductPoints(userId, event.getRequiredPoints());

        EventEntry entry = EventEntry.createEventEntry(event, userId);

        event.addEntry(entry);
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

        return new EventDetailResponseDto(event);
    }

    @Transactional
    public WinnerListResponseDto announceWinners(UUID eventId) {
        Event event = findEventById(eventId);

        List<EventEntry> allEntries = event.getEntries();

        List<EventEntry> winners = eventDomainService.selectRandomWinners(event, allEntries);

        List<Long> winnerUserIds = winners.stream()
            .map(EventEntry::getUserId)
            .collect(Collectors.toList());

        int remaining = event.getReward().getQuantity();

        return new WinnerListResponseDto(
            event.getEventId(),
            winnerUserIds,
            winners.size(),
            remaining
        );
    }

    @Transactional
    public void deleteEvent(UUID eventId, Long userId) {
        Event event = findEventById(eventId);

        event.checkDeletable();

        event.softDelete(userId);
    }

    private Event findEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventException.EventErrorType.EVENT_NOT_FOUND));
    }
}
