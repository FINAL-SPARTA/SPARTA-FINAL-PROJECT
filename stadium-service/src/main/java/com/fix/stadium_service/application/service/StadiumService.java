package com.fix.stadium_service.application.service;

import java.util.List;
import java.util.UUID;

import com.fix.common_service.dto.StadiumFeignResponse;
import com.fix.stadium_service.application.dtos.response.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.stadium_service.application.dtos.request.SeatPriceRequestDto;
import com.fix.stadium_service.application.dtos.request.SeatRequestDto;
import com.fix.stadium_service.application.dtos.request.SeatUpdateRequestDto;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.request.StadiumUpdateRequest;
import com.fix.stadium_service.application.exception.StadiumException;
import com.fix.stadium_service.domain.model.Seat;
import com.fix.stadium_service.domain.model.SeatSection;
import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import com.fix.stadium_service.domain.repository.StadiumQueryRepository;
import com.fix.stadium_service.domain.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;
    private final StadiumQueryRepository stadiumQueryRepository;


    @Transactional
    public StadiumResponseDto createStadium(StadiumCreateRequest requestDto) {
        boolean exist  = stadiumRepository.findByStadiumName(requestDto.getStadiumName()).isPresent();
        if (exist){
            throw new StadiumException(StadiumException.StadiumErrorType.STADIUM_DUPLICATE);
        }

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

        if (requestDto.getSeats() != null) {
            for (SeatUpdateRequestDto dto : requestDto.getSeats()) {
                stadium.updateSeat(dto.getSeatId(), dto.getRow(), dto.getNumber(), dto.getSection());
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
    public PageResponseDto searchStadiums(StadiumName stadiumName, int page, int size) {
        int offset = page * size;
        List<Stadium> stadiums = stadiumQueryRepository.findByStadiumName(stadiumName, offset, size);
        long totalCount = stadiumQueryRepository.countByStadiumName(stadiumName);
        return new PageResponseDto(stadiums, page, size, totalCount);

    }


    @Transactional
    public void deleteStadium(Long stadiumId, Long userId) {
        Stadium stadium = findStadium(stadiumId);
        stadium.softDelete(userId);
    }

    // 경기 도메인의 호출
    @Cacheable(value = "stadiumInfoCache" , key = "#teamName")
    @Transactional(readOnly = true)
    public StadiumFeignResponse getStadiumInfoByName(String teamName) {
        StadiumName stadiumName = StadiumName.fromTeamName(teamName);
        Stadium stadium = stadiumRepository.findByStadiumName(stadiumName).orElseThrow(
                () -> new StadiumException(StadiumException.StadiumErrorType.STADIUM_NAME_NOT_FOUND));
        return StadiumFeignResponse.builder()
                .stadiumId(stadium.getStadiumId())
                .stadiumName(stadium.getStadiumName().toString())
                .seatQuantity(stadium.getQuantity())
                .build();
    }

    // 티켓 도메인의 호출
    @Transactional(readOnly = true)
    public SeatInfoListResponseDto getSeatBySection(Long stadiumId, String section) {
        List<Seat>  stadiumSeats = stadiumQueryRepository.findSeatsByStadiumIdAndSection(stadiumId,section);
        List<SeatInfoResponseDto> seatInfoList = stadiumSeats.stream()
                .filter(seat -> Boolean.FALSE.equals(seat.getIsDeleted())) //소프트 삭제 좌석 제외
                .map(seat -> new SeatInfoResponseDto(
                        seat.getSeatId(),
                        seat.getSection().name(),
                        seat.getRow(),
                        seat.getNumber(),
                        seat.getSection().getPrice()
                ))
                .toList();
        return new SeatInfoListResponseDto(seatInfoList);

    }





    private Stadium findStadium(Long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumException(StadiumException.StadiumErrorType.STADIUM_NOT_FOUND));
    }


}
