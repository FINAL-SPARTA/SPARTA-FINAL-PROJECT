package com.fix.stadium_service.domain.model;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "p_seat")
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
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    private SeatSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "stadium_id")
    private Stadium stadium;


    public void setStadium(Stadium stadium){
        this.stadium = stadium;
    }


//    @Builder
//    public Seat (Integer row, Integer number, SeatSection section) {
//        this.row = row;
//        this.number = number;
//        this.section = section;
//    }

    public static Seat createSeat(Integer row, Integer number, SeatSection section) {
        Seat seat = new Seat();
        seat.row = row;
        seat.number = number;
        seat.section = section;
        seat.status = SeatStatus.AVAILABLE;
        return seat;
    }

}
