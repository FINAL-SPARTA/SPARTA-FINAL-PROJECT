package com.fix.stadium_service.domain.repository;

import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;

import java.util.List;

public interface StadiumQueryRepository {

    List<Stadium> findByStadiumName(StadiumName name, int offset, int size);

    long countByStadiumName(StadiumName name);

}
