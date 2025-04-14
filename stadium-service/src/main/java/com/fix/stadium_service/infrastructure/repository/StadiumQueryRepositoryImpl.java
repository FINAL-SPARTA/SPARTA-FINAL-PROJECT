package com.fix.stadium_service.infrastructure.repository;

import com.fix.stadium_service.domain.model.*;
import com.fix.stadium_service.domain.repository.StadiumQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public class StadiumQueryRepositoryImpl implements StadiumQueryRepository {

    @PersistenceContext
    private EntityManager em;
    private final JPAQueryFactory qf;

    public StadiumQueryRepositoryImpl(JPAQueryFactory qf) {
        this.qf = qf;
    }


    @Override
    public List<Stadium> findByStadiumName(StadiumName name, int offset, int size) {
        QStadium stadium = QStadium.stadium;

        return qf.selectFrom(stadium)
                .where( stadium.stadiumName.eq(name),
                        stadium.isDeleted.eq(false)
                )
                .orderBy(stadium.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public long countByStadiumName(StadiumName name)  {

        QStadium stadium = QStadium.stadium;

        return qf.select(stadium.count())
                .from(stadium)
                .where(
                        stadium.stadiumName.eq(name),
                        stadium.isDeleted.eq(false)
                )
                .fetchOne();

    }


    @Override
    public Optional<Stadium> findBySeatId(UUID seatId) {
        QStadium stadium = QStadium.stadium;
        QSeat seat = QSeat.seat;
        return Optional.ofNullable(qf
                .selectFrom(stadium)
                .join(stadium.seats, seat)
                .where(seat.seatId.eq(seatId))
                .fetchOne());
    }

    @Override
    public List<Seat> findSeatsByStadiumIdAndSection(Long stadiumId, String section) {
        QSeat seat = QSeat.seat;
        return qf.selectFrom(seat)
                .where(
                        seat.stadium.stadiumId.eq(stadiumId),
                        seat.section.eq(SeatSection.valueOf(section)),
                        seat.isDeleted.eq(false)
                )
                .fetch();
    }



}
