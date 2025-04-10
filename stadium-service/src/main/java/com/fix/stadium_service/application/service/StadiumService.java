package com.fix.stadium_service.application.service;

import com.fix.stadium_service.application.dtos.request.SeatRequestDto;
import com.fix.stadium_service.application.dtos.request.SeatUpdateRequestDto;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.request.StadiumUpdateRequest;
import com.fix.stadium_service.application.dtos.response.PageResponseDto;
import com.fix.stadium_service.application.dtos.response.StadiumFeignResponse;
import com.fix.stadium_service.application.dtos.response.StadiumResponseDto;
import com.fix.stadium_service.domain.model.Seat;
import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import com.fix.stadium_service.domain.repository.StadiumQueryRepository;
import com.fix.stadium_service.domain.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;
    private final StadiumQueryRepository stadiumQueryRepository;


    @Transactional
    public StadiumResponseDto createStadium(StadiumCreateRequest requestDto) {

        Stadium stadium = Stadium.createStadium(
                requestDto.getStadiumName(),
                requestDto.getStadiumName().getSeatCapacity()
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


    @Transactional
    public StadiumResponseDto updateStadium(Long stadiumId, StadiumUpdateRequest requestDto) {
        Stadium stadium = findStadium(stadiumId);

        stadium.updateStadium(
                requestDto.getStadiumName(),
                requestDto.getQuantity()
        );

        if(requestDto.getSeats() != null){
            for(SeatUpdateRequestDto dto : requestDto.getSeats()){
                stadium.updateSeat(dto.getSeatId(),dto.getRow(),dto.getNumber(),dto.getSection());
            }
        }

        return new StadiumResponseDto(stadium);

    }


    @Transactional(readOnly = true)
    public StadiumResponseDto getStadium(Long stadiumId) {
        Stadium stadium = findStadium(stadiumId);
        return new StadiumResponseDto(stadium);
    }


    @Transactional(readOnly = true)
    public PageResponseDto getStadiums(int page, int size) {
        int offset = page * size;
        List<Stadium> stadiums = stadiumRepository.findWithPaging(offset, size);
        long totalCount = stadiumRepository.count(); // 전체 개수 조회
        return new PageResponseDto(stadiums, page, size, totalCount);
    }


    @Transactional(readOnly = true)
    public PageResponseDto searchStadiums (StadiumName stadiumName, int page, int size) {
        int offset = page * size;
        List<Stadium> stadiums = stadiumQueryRepository.findByStadiumName(stadiumName, offset, size);
        long totalCount = stadiumQueryRepository.countByStadiumName(stadiumName);
        return new PageResponseDto(stadiums,page,size,totalCount);

    }


    @Transactional
    public void deleteStadium(Long stadiumId) {
        Stadium stadium = findStadium(stadiumId);
        stadium.softDelete(1L); // userID
    }

    @Transactional(readOnly = true)
    public StadiumFeignResponse getStadiumInfo(Long stadiumId){
        Stadium stadium = findStadium(stadiumId);
        return StadiumFeignResponse.from(stadium);
    }






    private Stadium findStadium(Long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new IllegalArgumentException("경기장을 찾을 수 없습니다."));
    }


}
