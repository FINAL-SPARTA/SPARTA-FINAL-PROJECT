package com.fix.stadium_service.domain.repository;

import com.fix.stadium_service.domain.model.Stadium;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StadiumRepository {
    Stadium save(Stadium stadium);
    Optional<Stadium> findById(UUID id);
    List<Stadium> findWithPaging(int offset, int size);
    long count();

}
