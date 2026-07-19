package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.Leave;
import com.enterprise.peopleflow.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    Page<Leave> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.status = :status")
    Page<Leave> findByStatus(@Param("status") LeaveStatus status, Pageable pageable);

    @Query("SELECT l FROM Leave l WHERE l.employee.manager.id = :managerId AND l.status = 'PENDING'")
    List<Leave> findPendingLeavesByManager(@Param("managerId") Long managerId);

    @Query("SELECT l FROM Leave l WHERE l.status = 'PENDING'")
    List<Leave> findAllPendingLeaves();

    @Query("SELECT COALESCE(SUM(l.totalDays), 0) FROM Leave l " +
           "WHERE l.employee.id = :empId AND l.leaveType.id = :leaveTypeId " +
           "AND l.status IN ('APPROVED', 'AUTO_APPROVED') " +
           "AND YEAR(l.startDate) = :year")
    BigDecimal sumApprovedDaysByEmployeeAndTypeAndYear(
            @Param("empId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") int year
    );

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :empId " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate " +
           "AND l.status IN ('PENDING', 'APPROVED', 'AUTO_APPROVED')")
    List<Leave> findOverlappingLeaves(
            @Param("empId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT l FROM Leave l WHERE l.employee.department.id = :deptId " +
           "AND l.status = :status")
    Page<Leave> findByDepartmentAndStatus(
            @Param("deptId") Long departmentId,
            @Param("status") LeaveStatus status,
            Pageable pageable
    );

    @Query("SELECT l FROM Leave l WHERE l.startDate >= :from AND l.endDate <= :to")
    List<Leave> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
