package com.fix.stadium_service.domain.repository;

import com.fix.stadium_service.domain.model.Seat;
import com.fix.stadium_service.domain.model.SeatSection;
import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StadiumQueryRepository {

    List<Stadium> findByStadiumName(StadiumName name, int offset, int size);

    long countByStadiumName(StadiumName name);

    Optional<Stadium> findBySeatId(UUID seatId);

    List<Seat>findSeatsByStadiumIdAndSection(Long stadiumId, String section);

}
