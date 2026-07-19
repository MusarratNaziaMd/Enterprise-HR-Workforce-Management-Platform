package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.SalaryRecord;
import com.enterprise.peopleflow.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    Optional<SalaryRecord> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);

    Page<SalaryRecord> findByEmployeeId(Long employeeId, Pageable pageable);

    List<SalaryRecord> findByMonthAndYear(int month, int year);

    List<SalaryRecord> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT sr FROM SalaryRecord sr WHERE sr.month = :month AND sr.year = :year " +
           "AND sr.paymentStatus = :status")
    List<SalaryRecord> findByMonthYearAndStatus(
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") PaymentStatus status
    );

    @Query("SELECT sr FROM SalaryRecord sr WHERE sr.employee.department.id = :deptId " +
           "AND sr.month = :month AND sr.year = :year")
    List<SalaryRecord> findByDepartmentAndMonthYear(
            @Param("deptId") Long departmentId,
            @Param("month") int month,
            @Param("year") int year
    );

    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
}
