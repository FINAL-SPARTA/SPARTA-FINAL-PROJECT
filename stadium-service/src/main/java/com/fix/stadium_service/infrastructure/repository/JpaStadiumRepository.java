package com.fix.stadium_service.infrastructure.repository;

import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.repository.StadiumRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaStadiumRepository extends JpaRepository<Stadium, UUID>, StadiumRepository {
}
