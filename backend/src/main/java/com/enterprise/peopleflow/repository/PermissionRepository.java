package com.enterprise.peopleflow.repository;

import com.enterprise.peopleflow.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    Optional<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByResource(String resource);

    boolean existsByCode(String code);

    boolean existsByResourceAndAction(String resource, String action);
}
