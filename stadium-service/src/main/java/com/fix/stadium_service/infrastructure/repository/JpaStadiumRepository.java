package com.fix.stadium_service.infrastructure.repository;

import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import com.fix.stadium_service.domain.repository.StadiumRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaStadiumRepository extends JpaRepository<Stadium, UUID>, StadiumRepository {
    @Query(value = "SELECT * FROM p_stadium  ORDER BY created_at DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Stadium> findWithPaging(@Param("offset") int offset, @Param("size") int size);

}
