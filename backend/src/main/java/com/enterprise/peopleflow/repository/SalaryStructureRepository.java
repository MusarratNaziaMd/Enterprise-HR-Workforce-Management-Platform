package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByEmployeeId(Long employeeId);

    @Query("SELECT ss FROM SalaryStructure ss WHERE ss.employee.id = :empId " +
           "AND ss.effectiveFrom <= :date AND (ss.effectiveTo IS NULL OR ss.effectiveTo >= :date)")
    List<SalaryStructure> findActiveStructuresByEmployee(
            @Param("empId") Long employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT ss FROM SalaryStructure ss WHERE ss.employee.id = :empId " +
           "AND ss.salaryComponent.id = :componentId " +
           "AND ss.effectiveFrom <= :date AND (ss.effectiveTo IS NULL OR ss.effectiveTo >= :date)")
    List<SalaryStructure> findActiveStructureByEmployeeAndComponent(
            @Param("empId") Long employeeId,
            @Param("componentId") Long componentId,
            @Param("date") LocalDate date
    );
}
