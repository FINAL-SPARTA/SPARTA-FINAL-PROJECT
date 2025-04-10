package com.fix.stadium_service.domain.model;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "p_stadium")
@Where(clause = "is_deleted = false")
public class Stadium extends Basic {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "stadium_id", updatable = false, nullable = false)
    private UUID stadiumId;


    @Enumerated(EnumType.STRING)
    private  StadiumName stadiumName;
    private Integer quantity;


    @OneToMany(mappedBy = "stadium", cascade = CascadeType.PERSIST,orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    public void addSeat(Seat seat){
        seats.add(seat);
        seat.setStadium(this);

    }

    @Builder
    public Stadium(StadiumName stadiumName, Integer quantity) {
        this.stadiumName = stadiumName;
        this.quantity = quantity;
    }


    public static Stadium createStadium(StadiumName stadiumName , Integer quantity) {
        return Stadium.builder()
                .stadiumName(stadiumName)
                .quantity(stadiumName.getSeatCapacity())
                .build();
    }

    public void updateStadium(StadiumName name, Integer quantity) {
        if (name != null) this.stadiumName = name;
        if (quantity != null) this.quantity = quantity;

    }



    public void updateSeat(UUID seatId, Integer row, Integer number, SeatSection section) {
        Seat seat = this.seats.stream()
                .filter(s -> s.getSeatId().equals(seatId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 좌석이 경기장에 존재하지 않습니다."));
        if (row != null) seat.setRow(row);
        if (number != null) seat.setNumber(number);
        if (section != null) seat.setSection(section);
    }



    @Override
    public void softDelete(Long userId){
        super.softDelete(userId);

        if(this.seats !=null && !this.seats.isEmpty()){
            for(Seat seat: this.seats){
                seat.softDelete(userId);
            }

        }

    }


}
