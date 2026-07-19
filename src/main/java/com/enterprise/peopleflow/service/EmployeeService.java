package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.entity.Department;
import com.enterprise.peopleflow.entity.Employee;
import com.enterprise.peopleflow.entity.Role;
import com.enterprise.peopleflow.entity.User;
import com.enterprise.peopleflow.enums.EmploymentStatus;
import com.enterprise.peopleflow.exception.BadRequestException;
import com.enterprise.peopleflow.exception.ConflictException;
import com.enterprise.peopleflow.exception.ResourceNotFoundException;
import com.enterprise.peopleflow.repository.DepartmentRepository;
import com.enterprise.peopleflow.repository.EmployeeRepository;
import com.enterprise.peopleflow.repository.RoleRepository;
import com.enterprise.peopleflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employeePage = employeeRepository.findAllWithDepartmentAndManager()
                .isEmpty()
                ? employeeRepository.findAll(pageable)
                : employeeRepository.findAll(pageable);

        Page<Employee> employees = employeeRepository.findAll(pageable);
        return mapToPageResponse(employees);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(
            String keyword, Long departmentId, EmploymentStatus status,
            String designation, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employeePage = employeeRepository.searchEmployees(
                keyword, departmentId, status, designation, pageable);

        return mapToPageResponse(employeePage);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "code", code));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userId", userId));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new ConflictException("Employee code already exists: " + request.getEmployeeCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
        }

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pinCode(request.getPinCode())
                .department(department)
                .manager(manager)
                .designation(request.getDesignation())
                .employmentType(request.getEmploymentType())
                .status(EmploymentStatus.PROBATION)
                .dateOfJoining(request.getDateOfJoining())
                .probationEndDate(request.getDateOfJoining().plusMonths(6))
                .build();

        String generatedUsername = null;
        String generatedPassword = null;

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
            if (employeeRepository.existsByUserId(user.getId())) {
                throw new ConflictException("User is already linked to an employee");
            }
            employee.setUser(user);
        } else {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new BadRequestException("Email is required when creating a new user account");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already registered: " + request.getEmail());
            }

            String username = generateUsername(request.getEmployeeCode());
            String tempPassword = generateSecurePassword();
            String email = request.getEmail().trim().toLowerCase();

            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "EMPLOYEE"));

            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(tempPassword))
                    .isActive(true)
                    .roles(Set.of(employeeRole))
                    .build();
            userRepository.save(newUser);
            employee.setUser(newUser);

            generatedUsername = username;
            generatedPassword = tempPassword;
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created: {} ({})", saved.getFullName(), saved.getEmployeeCode());

        EmployeeResponse response = mapToResponse(saved);
        response.setGeneratedUsername(generatedUsername);
        response.setGeneratedPassword(generatedPassword);
        return response;
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null) employee.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) employee.setGender(request.getGender());
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        if (request.getAddress() != null) employee.setAddress(request.getAddress());
        if (request.getCity() != null) employee.setCity(request.getCity());
        if (request.getState() != null) employee.setState(request.getState());
        if (request.getCountry() != null) employee.setCountry(request.getCountry());
        if (request.getPinCode() != null) employee.setPinCode(request.getPinCode());
        if (request.getProfileImageUrl() != null) employee.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getDesignation() != null) employee.setDesignation(request.getDesignation());
        if (request.getEmploymentType() != null) employee.setEmploymentType(request.getEmploymentType());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (request.getDateOfExit() != null) employee.setDateOfExit(request.getDateOfExit());
        if (request.getProbationEndDate() != null) employee.setProbationEndDate(request.getProbationEndDate());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
            if (manager.getId().equals(employee.getId())) {
                throw new BadRequestException("An employee cannot be their own manager");
            }
            employee.setManager(manager);
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated: {} ({})", updated.getFullName(), updated.getEmployeeCode());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(employee);
        log.info("Employee deleted: {} ({})", employee.getFullName(), employee.getEmployeeCode());
    }

    private String generateUsername(String employeeCode) {
        String base = employeeCode.toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
            if (suffix > 999) {
                throw new BadRequestException("Unable to generate unique username for employee code: " + employeeCode);
            }
        }
        return username;
    }

    private String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$%&*!";
        String all = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder(10);
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 10; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    private EmployeeResponse mapToResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getUser() != null ? e.getUser().getEmail() : null)
                .phone(e.getPhone())
                .address(e.getAddress())
                .city(e.getCity())
                .state(e.getState())
                .country(e.getCountry())
                .pinCode(e.getPinCode())
                .gender(e.getGender())
                .dateOfBirth(e.getDateOfBirth())
                .dateOfJoining(e.getDateOfJoining())
                .dateOfExit(e.getDateOfExit())
                .probationEndDate(e.getProbationEndDate())
                .designation(e.getDesignation())
                .employmentType(e.getEmploymentType())
                .status(e.getStatus())
                .profileImageUrl(e.getProfileImageUrl())
                .departmentId(e.getDepartment().getId())
                .departmentName(e.getDepartment().getName())
                .departmentCode(e.getDepartment().getCode())
                .managerId(e.getManager() != null ? e.getManager().getId() : null)
                .managerName(e.getManager() != null ? e.getManager().getFullName() : null)
                .managerEmployeeCode(e.getManager() != null ? e.getManager().getEmployeeCode() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private PageResponse<EmployeeResponse> mapToPageResponse(Page<Employee> page) {
        return PageResponse.<EmployeeResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
