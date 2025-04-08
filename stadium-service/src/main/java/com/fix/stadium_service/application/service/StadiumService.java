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

import java.util.UUID;

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


    @Transactional(readOnly = true)
    public StadiumResponseDto getStadium(UUID stadiumId){
        Stadium stadium = findStadium(stadiumId);
        return new StadiumResponseDto(stadium);
    }











    private Stadium findStadium(UUID stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new IllegalArgumentException("경기장을 찾을 수 없습니다."));
        return stadium;
    }
}
