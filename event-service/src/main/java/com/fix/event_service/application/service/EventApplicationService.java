package com.fix.event_service.application.service;

import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.response.EventDetailResponseDto;
import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventEntry;
import com.fix.event_service.domain.model.Reward;
import com.fix.event_service.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventApplicationService {

    private final EventRepository eventRepository;

    @Transactional
    public EventDetailResponseDto createEvent(EventCreateRequestDto requestDto) {
        // TODO : Event의 중복 검사 로직? 필요 없을지도..

        Event event = Event.createEvent(
                requestDto.getEventName(),
                requestDto.getDescription(),
                requestDto.getEventStartAt(),
                requestDto.getEventEndAt(),
                requestDto.getMaxWinners()
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
    public void applyEvent(UUID eventId) {
        Long userId = 1L; // TODO : 실제 유저 Id 넣기

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.isEventOpenForApplication()) {
            throw new IllegalStateException("이벤트 응모 기간이 아닙니다.");
        }

        EventEntry entry = EventEntry.createEventEntry(event, userId);

        event.addEntry(entry);
    }
}
