package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.Attendance;
import com.enterprise.peopleflow.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :empId " +
           "AND a.attendanceDate BETWEEN :start AND :end")
    List<Attendance> findByEmployeeAndDateRange(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(a.workHours), 0) FROM Attendance a " +
           "WHERE a.employee.id = :empId AND a.attendanceDate BETWEEN :start AND :end")
    BigDecimal sumWorkHoursByEmployeeAndDateRange(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate
    );

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date AND a.status = :status")
    List<Attendance> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AttendanceStatus status
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId " +
           "AND a.attendanceDate BETWEEN :start AND :end AND a.status = :status")
    Long countByEmployeeAndDateRangeAndStatus(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            @Param("status") AttendanceStatus status
    );
}
