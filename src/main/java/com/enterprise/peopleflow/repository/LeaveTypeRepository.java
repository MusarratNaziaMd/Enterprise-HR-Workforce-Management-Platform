package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByName(String name);

    Optional<LeaveType> findByCode(String code);

    List<LeaveType> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
