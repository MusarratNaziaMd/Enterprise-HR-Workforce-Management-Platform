package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.DepartmentRequest;
import com.enterprise.peopleflow.dto.DepartmentResponse;
import com.enterprise.peopleflow.entity.Department;
import com.enterprise.peopleflow.exception.ConflictException;
import com.enterprise.peopleflow.exception.ResourceNotFoundException;
import com.enterprise.peopleflow.repository.DepartmentRepository;
import com.enterprise.peopleflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(dept);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ConflictException("Department name already exists: " + request.getName());
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Department code already exists: " + request.getCode());
        }

        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Department saved = departmentRepository.save(dept);
        log.info("Department created: {} ({})", saved.getName(), saved.getCode());
        return mapToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (!dept.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new ConflictException("Department name already exists: " + request.getName());
        }
        if (!dept.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Department code already exists: " + request.getCode());
        }

        dept.setName(request.getName());
        dept.setCode(request.getCode());
        dept.setDescription(request.getDescription());

        Department updated = departmentRepository.save(dept);
        log.info("Department updated: {} ({})", updated.getName(), updated.getCode());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        long empCount = employeeRepository.findByDepartmentId(id).size();
        if (empCount > 0) {
            throw new ConflictException(
                    "Cannot delete department with " + empCount + " active employees. Reassign them first.");
        }

        departmentRepository.delete(dept);
        log.info("Department deleted: {} ({})", dept.getName(), dept.getCode());
    }

    private DepartmentResponse mapToResponse(Department dept) {
        int empCount = employeeRepository.findByDepartmentId(dept.getId()).size();
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .isActive(dept.getIsActive())
                .employeeCount(empCount)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
