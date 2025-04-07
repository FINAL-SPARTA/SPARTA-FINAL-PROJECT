package com.fix.stadium_service.application.service;

import com.fix.stadium_service.application.dtos.request.SeatRequestDto;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.response.StadiumResponseDto;
import com.fix.stadium_service.domain.model.Seat;
import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;


    @Transactional
    public StadiumResponseDto createStadium(StadiumCreateRequest requestDto) {

        Stadium stadium = Stadium.createStadium(
                requestDto.getStadiumName(),
                requestDto.getQuantity()
        );

        for (SeatRequestDto seatDto : requestDto.getSeats()) {
            Seat seat = Seat.createSeat(
                    seatDto.getRow(),
                    seatDto.getNumber(),
                    seatDto.getSection()
            );
            stadium.addSeat(seat);
        }
        stadiumRepository.save(stadium);
        return new StadiumResponseDto(stadium);


    }
}
