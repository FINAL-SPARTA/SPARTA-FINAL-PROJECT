package com.fix.stadium_service.domain.repository;

import com.fix.stadium_service.domain.model.Stadium;

public interface StadiumRepository {
    Stadium save(Stadium stadium);
}
