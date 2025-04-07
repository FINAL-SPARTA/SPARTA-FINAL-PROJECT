package com.fix.event_service.domain.service;

import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventEntry;
import com.fix.event_service.domain.model.Reward;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EventDomainService {

    // 이벤트 당첨자 선정 로직 : 무작위로 선정(MVP) TODO : 이벤트 당첨자 선정 로직 고도화?
    public List<EventEntry> selectRandomWinners(Event event, List<EventEntry> allEntries) {
        int maxWinners = event.getMaxWinners() != null ? event.getMaxWinners() : 0;
        if (maxWinners <= 0) {
            return Collections.emptyList(); // 당첨자 수가 0 이하이면 아무도 당첨 X
        }
        if (allEntries.size() <= maxWinners) {
            // 전체가 당첨
            for (EventEntry entry : allEntries) {
                entry.markAsWinner();
            }
            return allEntries;
        }

        // 무작위 셔플 후 앞부분만 뽑기
        List<EventEntry> shuffled = new ArrayList<>(allEntries);
        Collections.shuffle(shuffled);

        List<EventEntry> winners = shuffled.subList(0, maxWinners);
        for (EventEntry w : winners) {
            w.markAsWinner();
        }

        // 당첨자 수만큼 경품 재고 차감
        Reward reward = event.getReward();
        if (reward != null && !winners.isEmpty()) {
            reward.decreaseQuantity(winners.size());
        }
        return winners;
    }

}
