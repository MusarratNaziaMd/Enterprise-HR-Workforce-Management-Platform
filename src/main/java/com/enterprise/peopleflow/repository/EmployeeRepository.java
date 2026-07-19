package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.Employee;
import com.enterprise.peopleflow.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByUserId(Long userId);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :deptId")
    List<Employee> findByDepartmentId(@Param("deptId") Long departmentId);

    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    List<Employee> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT e FROM Employee e WHERE e.status = :status")
    List<Employee> findByStatus(@Param("status") EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:designation IS NULL OR LOWER(e.designation) = LOWER(:designation))")
    Page<Employee> searchEmployees(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            @Param("designation") String designation,
            Pageable pageable
    );

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.manager")
    List<Employee> findAllWithDepartmentAndManager();

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department d " +
           "WHERE d.code = :deptCode")
    List<Employee> findByDepartmentCode(@Param("deptCode") String departmentCode);

    @Query(value = "SELECT DISTINCT e.designation FROM Employee e ORDER BY e.designation",
           countQuery = "SELECT COUNT(DISTINCT e.designation) FROM Employee e")
    Page<String> findDistinctDesignations(Pageable pageable);
}
