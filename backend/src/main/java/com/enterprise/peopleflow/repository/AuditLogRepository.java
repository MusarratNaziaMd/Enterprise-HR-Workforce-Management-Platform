package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTableName(String tableName, Pageable pageable);

    Page<AuditLog> findByRecordIdAndTableName(Long recordId, String tableName, Pageable pageable);

    Page<AuditLog> findByPerformedById(Long userId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.tableName = :table AND a.recordId = :recordId " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLog> findByTableAndRecord(
            @Param("table") String tableName,
            @Param("recordId") Long recordId,
            Pageable pageable
    );

    @Query("SELECT a FROM AuditLog a WHERE a.performedAt BETWEEN :from AND :to " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLog> findByDateRange(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}
