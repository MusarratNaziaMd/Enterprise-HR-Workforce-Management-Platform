package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {

    Optional<SalaryComponent> findByName(String name);

    Optional<SalaryComponent> findByCode(String code);

    List<SalaryComponent> findByIsEarningTrue();

    List<SalaryComponent> findByIsEarningFalse();

    List<SalaryComponent> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
