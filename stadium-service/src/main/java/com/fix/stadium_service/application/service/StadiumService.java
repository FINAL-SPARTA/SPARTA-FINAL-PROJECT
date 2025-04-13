package com.fix.stadium_service.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.stadium_service.application.dtos.request.SeatPriceRequestDto;
import com.fix.stadium_service.application.dtos.request.SeatRequestDto;
import com.fix.stadium_service.application.dtos.request.SeatUpdateRequestDto;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.request.StadiumUpdateRequest;
import com.fix.stadium_service.application.dtos.response.PageResponseDto;
import com.fix.stadium_service.application.dtos.response.SeatPriceListResponseDto;
import com.fix.stadium_service.application.dtos.response.SeatPriceResponseDto;
import com.fix.stadium_service.application.dtos.response.StadiumFeignResponse;
import com.fix.stadium_service.application.dtos.response.StadiumResponseDto;
import com.fix.stadium_service.application.exception.StadiumException;
import com.fix.stadium_service.domain.model.Seat;
import com.fix.stadium_service.domain.model.SeatSection;
import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import com.fix.stadium_service.domain.repository.StadiumQueryRepository;
import com.fix.stadium_service.domain.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        return StadiumFeignResponse.from(stadium);
    }

    // 티켓 도메인의 호출

    @Transactional(readOnly = true)
    public SeatPriceListResponseDto getPrices(SeatPriceRequestDto request) {
        List<UUID> seatIds = request.getSeatIds();
        UUID seatId = seatIds.get(0);

        // seatId 기준으로 stadium 조회 (seatId 하나면 충분)
        Stadium stadium = stadiumQueryRepository.findBySeatId(seatId)
                .orElseThrow(() -> new StadiumException(StadiumException.StadiumErrorType.STADIUM_NOT_FOUND));

        // 해당 stadium의 좌석 중 요청 seatIds에 해당하는 좌석만 필터링
        List<SeatPriceResponseDto> seatPrices = stadium.getSeats().stream()
                .filter(seat -> seatIds.contains(seat.getSeatId()))
                .map(seat -> {
                    if (Boolean.TRUE.equals(seat.getIsDeleted())) {
                        throw new StadiumException(StadiumException.StadiumErrorType.SEAT_NOT_AVAILABLE);
                    }
                    int price = sectionToPrice(seat.getSection());
                    return new SeatPriceResponseDto(seat.getSeatId(), price);
                })
                .toList();
        return new SeatPriceListResponseDto(seatPrices);
    }


    private int sectionToPrice(SeatSection section) {

        if (section == SeatSection.VIP) {
            return SeatSection.VIP.getPrice();
        } else if (section == SeatSection.R_SECTION) {
            return SeatSection.R_SECTION.getPrice();
        } else if (section == SeatSection.S_SECTION) {
            return SeatSection.S_SECTION.getPrice();
        } else if (section == SeatSection.A_SECTION) {
            return SeatSection.A_SECTION.getPrice();
        } else if (section == SeatSection.B_SECTiON) {
            return SeatSection.B_SECTiON.getPrice();
        } else if (section == SeatSection.OUTFIELD) {
            return SeatSection.OUTFIELD.getPrice();
        } else {
            throw new StadiumException(StadiumException.StadiumErrorType.SEAT_SECTION_NOT_FOUND);
        }
    }


    private Stadium findStadium(Long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumException(StadiumException.StadiumErrorType.STADIUM_NOT_FOUND));
    }


}
