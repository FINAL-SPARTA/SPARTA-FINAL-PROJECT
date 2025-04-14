package com.fix.stadium_service.domain.model;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(name = "p_seat", indexes = { @Index(name = "idx_seat_stadium_section",columnList = "stadium_id, section")})
@Where(clause = "is_deleted = false")
public class Seat extends Basic {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "seat_id", updatable = false, nullable = false)
    private UUID seatId;

    @Column(name="seat_row")
    private Integer row;

    @Column(name="seat_number")
    private Integer number;


    @Enumerated(EnumType.STRING)
    private SeatSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "stadium_id")
    private Stadium stadium;


    public void setStadium(Stadium stadium){
        this.stadium = stadium;
    }



    public static Seat createSeat(Integer row, Integer number, SeatSection section) {
        Seat seat = new Seat();
        seat.row = row;
        seat.number = number;
        seat.section = section;
        return seat;
    }



}
