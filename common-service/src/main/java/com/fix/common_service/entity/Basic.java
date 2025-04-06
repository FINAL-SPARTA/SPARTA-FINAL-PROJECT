package com.fix.common_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@Where(clause = "is_deleted = false")      // 조회시, 삭제된 데이터 자동 필터링
@EntityListeners(AuditingEntityListener.class)
public abstract class Basic {

    @CreatedDate
    protected LocalDateTime createdAt;

    @CreatedBy
    protected Long createdBy;

    @LastModifiedDate
    protected LocalDateTime updatedAt;

    @LastModifiedBy
    protected Long updatedBy;

    protected LocalDateTime deletedAt;

    protected Long deletedBy;

    protected Boolean isDeleted;

    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.isDeleted != null && this.isDeleted) {
            throw new IllegalStateException("Cannot persist a deleted entity");
        }
    }

}
