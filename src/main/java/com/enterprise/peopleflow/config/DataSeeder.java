package com.enterprise.peopleflow.config;

import com.enterprise.peopleflow.entity.*;
import com.enterprise.peopleflow.enums.*;
import com.enterprise.peopleflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepo,
            PermissionRepository permRepo,
            UserRepository userRepo,
            DepartmentRepository deptRepo,
            EmployeeRepository empRepo,
            LeaveTypeRepository leaveTypeRepo,
            SalaryComponentRepository salCompRepo,
            PasswordEncoder encoder
    ) {
        return args -> {

            // ── Migration: reassign users away from removed roles ──
            var removedRoleNames = Set.of("SUPER_ADMIN", "DEPT_MANAGER");
            boolean needsMigration = roleRepo.findAll().stream()
                    .anyMatch(r -> removedRoleNames.contains(r.getName()));
            if (needsMigration) {
                log.info("Migrating users from removed roles (SUPER_ADMIN, DEPT_MANAGER)...");
                Role hrAdmin = roleRepo.findByName("HR_ADMIN").orElse(null);
                if (hrAdmin == null) {
                    hrAdmin = roleRepo.save(Role.builder()
                            .name("HR_ADMIN").description("HR administrative access").build());
                    var allPerms = permRepo.findAll();
                    hrAdmin.setPermissions(allPerms.stream()
                            .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                                    "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL",
                                    "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                                    "LEAVE_READ_OWN", "LEAVE_READ_ALL",
                                    "SALARY_READ_OWN", "SALARY_MANAGE", "SALARY_PROCESS",
                                    "USER_MANAGE", "AUDIT_READ", "REPORT_VIEW")
                                    .contains(p.getCode()))
                            .collect(Collectors.toSet()));
                    roleRepo.save(hrAdmin);
                }
                Role finalHrAdmin = hrAdmin;
                userRepo.findAllWithRoles().stream()
                        .filter(u -> u.getRoles().stream()
                                .anyMatch(r -> removedRoleNames.contains(r.getName())))
                        .forEach(u -> {
                            u.getRoles().clear();
                            u.getRoles().add(finalHrAdmin);
                            userRepo.save(u);
                            log.info("Reassigned user '{}' from removed role to HR_ADMIN", u.getUsername());
                        });
                roleRepo.findAll().stream()
                        .filter(r -> removedRoleNames.contains(r.getName()))
                        .forEach(r -> {
                            roleRepo.delete(r);
                            log.info("Deleted removed role: {}", r.getName());
                        });
                log.info("Migration complete.");
            }

            // ── Migration: create HR_MANAGER role if missing ──
            if (roleRepo.findByName("HR_MANAGER").isEmpty()) {
                log.info("Creating HR_MANAGER role...");
                var allPerms = permRepo.findAll();
                Role hrManager = roleRepo.save(Role.builder()
                        .name("HR_MANAGER").description("HR manager with leave approval access").build());
                hrManager.setPermissions(allPerms.stream()
                        .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                                "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE",
                                "ATTENDANCE_MARK", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL", "ATTENDANCE_UPDATE",
                                "LEAVE_APPLY", "LEAVE_READ_OWN", "LEAVE_READ_ALL", "LEAVE_APPROVE", "LEAVE_CANCEL",
                                "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                                "SALARY_READ_OWN", "SALARY_MANAGE", "REPORT_VIEW")
                                .contains(p.getCode()))
                        .collect(Collectors.toSet()));
                roleRepo.save(hrManager);
                log.info("HR_MANAGER role created with leave approval permissions");
            }

            if (userRepo.count() > 0 && permRepo.count() >= 24 && !needsMigration) {
                log.info("Database already seeded, skipping.");
                return;
            }

            if (permRepo.count() < 24) {
                log.info("Seeding/updating permissions...");
                List<Permission> perms = List.of(
                    Permission.builder().code("EMPLOYEE_CREATE").name("Create Employee").resource("EMPLOYEE").action("CREATE").build(),
                    Permission.builder().code("EMPLOYEE_READ").name("Read Employee").resource("EMPLOYEE").action("READ").build(),
                    Permission.builder().code("EMPLOYEE_READ_ALL").name("Read All Employees").resource("EMPLOYEE").action("READ_ALL").build(),
                    Permission.builder().code("EMPLOYEE_UPDATE").name("Update Employee").resource("EMPLOYEE").action("UPDATE").build(),
                    Permission.builder().code("EMPLOYEE_DELETE").name("Delete Employee").resource("EMPLOYEE").action("DELETE").build(),
                    Permission.builder().code("ATTENDANCE_MARK").name("Mark Attendance").resource("ATTENDANCE").action("MARK").build(),
                    Permission.builder().code("ATTENDANCE_READ").name("Read Attendance").resource("ATTENDANCE").action("READ").build(),
                    Permission.builder().code("ATTENDANCE_READ_ALL").name("Read All Attendance").resource("ATTENDANCE").action("READ_ALL").build(),
                    Permission.builder().code("LEAVE_APPLY").name("Apply Leave").resource("LEAVE").action("APPLY").build(),
                    Permission.builder().code("LEAVE_READ_OWN").name("Read Own Leaves").resource("LEAVE").action("READ_OWN").build(),
                    Permission.builder().code("LEAVE_READ_ALL").name("Read All Leaves").resource("LEAVE").action("READ_ALL").build(),
                    Permission.builder().code("LEAVE_APPROVE").name("Approve Leave").resource("LEAVE").action("APPROVE").build(),
                    Permission.builder().code("LEAVE_CANCEL").name("Cancel Leave").resource("LEAVE").action("CANCEL").build(),
                    Permission.builder().code("DEPARTMENT_READ").name("Read Department").resource("DEPARTMENT").action("READ").build(),
                    Permission.builder().code("DEPARTMENT_CREATE").name("Create Department").resource("DEPARTMENT").action("CREATE").build(),
                    Permission.builder().code("DEPARTMENT_UPDATE").name("Update Department").resource("DEPARTMENT").action("UPDATE").build(),
                    Permission.builder().code("DEPARTMENT_DELETE").name("Delete Department").resource("DEPARTMENT").action("DELETE").build(),
                    Permission.builder().code("SALARY_READ_OWN").name("Read Own Salary").resource("SALARY").action("READ_OWN").build(),
                    Permission.builder().code("SALARY_MANAGE").name("Manage Salary").resource("SALARY").action("MANAGE").build(),
                    Permission.builder().code("SALARY_PROCESS").name("Process Salary").resource("SALARY").action("PROCESS").build(),
                    Permission.builder().code("ROLE_MANAGE").name("Manage Roles").resource("ROLE").action("MANAGE").build(),
                    Permission.builder().code("USER_MANAGE").name("Manage Users").resource("USER").action("MANAGE").build(),
                    Permission.builder().code("AUDIT_READ").name("Read Audit Logs").resource("AUDIT").action("READ").build(),
                    Permission.builder().code("REPORT_VIEW").name("View Reports").resource("REPORT").action("VIEW").build()
                );
                permRepo.saveAll(perms);
                log.info("Permissions seeded: {}", permRepo.count());
            }

            if (permRepo.count() > 0) {
                var allPerms = permRepo.findAll();
                List<Role> roles = roleRepo.findAll();
                for (Role role : roles) {
                    switch (role.getName()) {
                        case "HR_ADMIN" -> role.setPermissions(allPerms.stream()
                                .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                                        "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL",
                                        "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                                        "LEAVE_READ_OWN", "LEAVE_READ_ALL",
                                        "SALARY_READ_OWN", "SALARY_MANAGE", "SALARY_PROCESS",
                                        "USER_MANAGE", "AUDIT_READ", "REPORT_VIEW")
                                        .contains(p.getCode()))
                                .collect(Collectors.toSet()));
                        case "EMPLOYEE" -> role.setPermissions(allPerms.stream()
                                .filter(p -> Set.of("EMPLOYEE_READ", "ATTENDANCE_MARK", "ATTENDANCE_READ",
                                        "LEAVE_APPLY", "LEAVE_READ_OWN", "LEAVE_CANCEL", "SALARY_READ_OWN")
                                        .contains(p.getCode()))
                                .collect(Collectors.toSet()));
                    }
                    roleRepo.save(role);
                }

                if (roles.stream().noneMatch(r -> r.getName().equals("HR_MANAGER"))) {
                    Role hrManager = roleRepo.save(Role.builder()
                            .name("HR_MANAGER").description("HR manager with leave approval access").build());
                    hrManager.setPermissions(allPerms.stream()
                            .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                                    "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE",
                                    "ATTENDANCE_MARK", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL", "ATTENDANCE_UPDATE",
                                    "LEAVE_APPLY", "LEAVE_READ_OWN", "LEAVE_READ_ALL", "LEAVE_APPROVE", "LEAVE_CANCEL",
                                    "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                                    "SALARY_READ_OWN", "SALARY_MANAGE", "REPORT_VIEW")
                                    .contains(p.getCode()))
                            .collect(Collectors.toSet()));
                    roleRepo.save(hrManager);
                    log.info("Created HR_MANAGER role with leave approval permissions");
                }

                log.info("Permissions assigned to {} roles", roles.size());
            }

            if (userRepo.count() > 0) {
                log.info("Users already exist, skipping user/department seeding.");
                return;
            }

            log.info("Seeding development data...");

            // Departments
            Department engineering = deptRepo.save(Department.builder()
                    .name("Engineering").code("ENG").description("Software Engineering").build());
            Department hr = deptRepo.save(Department.builder()
                    .name("Human Resources").code("HR").description("People Operations").build());
            Department finance = deptRepo.save(Department.builder()
                    .name("Finance").code("FIN").description("Finance & Accounting").build());

            // Roles
            Role hrAdmin = roleRepo.save(Role.builder()
                    .name("HR_ADMIN").description("HR administrative access").build());
            Role hrManager = roleRepo.save(Role.builder()
                    .name("HR_MANAGER").description("HR manager with leave approval access").build());
            Role employeeRole = roleRepo.save(Role.builder()
                    .name("EMPLOYEE").description("Standard employee").build());

            // Assign permissions to roles
            var allPerms = permRepo.findAll();

            hrAdmin.setPermissions(allPerms.stream()
                    .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                            "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL",
                            "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                            "LEAVE_READ_OWN", "LEAVE_READ_ALL",
                            "SALARY_READ_OWN", "SALARY_MANAGE", "SALARY_PROCESS",
                            "USER_MANAGE", "AUDIT_READ", "REPORT_VIEW")
                            .contains(p.getCode()))
                    .collect(Collectors.toSet()));
            roleRepo.save(hrAdmin);

            hrManager.setPermissions(allPerms.stream()
                    .filter(p -> Set.of("EMPLOYEE_CREATE", "EMPLOYEE_READ", "EMPLOYEE_READ_ALL",
                            "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE",
                            "ATTENDANCE_MARK", "ATTENDANCE_READ", "ATTENDANCE_READ_ALL", "ATTENDANCE_UPDATE",
                            "LEAVE_APPLY", "LEAVE_READ_OWN", "LEAVE_READ_ALL", "LEAVE_APPROVE", "LEAVE_CANCEL",
                            "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                            "SALARY_READ_OWN", "SALARY_MANAGE", "REPORT_VIEW")
                            .contains(p.getCode()))
                    .collect(Collectors.toSet()));
            roleRepo.save(hrManager);

            employeeRole.setPermissions(allPerms.stream()
                    .filter(p -> Set.of("EMPLOYEE_READ", "ATTENDANCE_MARK", "ATTENDANCE_READ",
                            "LEAVE_APPLY", "LEAVE_READ_OWN", "LEAVE_CANCEL", "SALARY_READ_OWN")
                            .contains(p.getCode()))
                    .collect(Collectors.toSet()));
            roleRepo.save(employeeRole);

            // Users
            User adminUser = userRepo.save(User.builder()
                    .username("admin").email("admin@hrpilot.com")
                    .passwordHash(encoder.encode("Admin@123"))
                    .isActive(true).roles(Set.of(hrAdmin)).build());
            User hrUser = userRepo.save(User.builder()
                    .username("priya.sharma").email("priya@hrpilot.com")
                    .passwordHash(encoder.encode("Password@1"))
                    .isActive(true).roles(Set.of(hrManager)).build());
            User emp = userRepo.save(User.builder()
                    .username("amit.kumar").email("amit@hrpilot.com")
                    .passwordHash(encoder.encode("Password@1"))
                    .isActive(true).roles(Set.of(employeeRole)).build());

            // Employees
            empRepo.save(Employee.builder()
                    .user(adminUser).employeeCode("EMP001").firstName("System").lastName("Admin")
                    .designation("System Administrator").department(hr)
                    .employmentType(EmploymentType.FULL_TIME).status(EmploymentStatus.ACTIVE)
                    .dateOfJoining(LocalDate.of(2020, 1, 1)).gender("Male")
                    .phone("+91-9876543210").probationEndDate(LocalDate.of(2020, 7, 1))
                    .build());

            empRepo.save(Employee.builder()
                    .user(emp).employeeCode("EMP002").firstName("Amit").lastName("Kumar")
                    .designation("Software Engineer").department(engineering)
                    .employmentType(EmploymentType.FULL_TIME).status(EmploymentStatus.ACTIVE)
                    .dateOfJoining(LocalDate.of(2023, 7, 1)).gender("Male")
                    .phone("+91-9876543211").probationEndDate(LocalDate.of(2024, 1, 1))
                    .build());

            empRepo.save(Employee.builder()
                    .user(hrUser).employeeCode("EMP003").firstName("Priya").lastName("Sharma")
                    .designation("HR Manager").department(hr)
                    .employmentType(EmploymentType.FULL_TIME).status(EmploymentStatus.ACTIVE)
                    .dateOfJoining(LocalDate.of(2021, 1, 10)).gender("Female")
                    .phone("+91-9876543212").probationEndDate(LocalDate.of(2021, 7, 10))
                    .build());

            // Leave Types
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Casual Leave").code("CL").description("For personal or casual reasons")
                    .defaultDays(12).isCarryForward(false).maxCarryDays(0).build());
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Sick Leave").code("SL").description("For medical or health reasons")
                    .defaultDays(12).isCarryForward(true).maxCarryDays(6).build());
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Earned Leave").code("EL").description("Earned privilege leave")
                    .defaultDays(15).isCarryForward(true).maxCarryDays(30).build());
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Maternity Leave").code("ML").description("For maternity purposes")
                    .defaultDays(182).isCarryForward(false).maxCarryDays(0).build());
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Paternity Leave").code("PL").description("For paternity purposes")
                    .defaultDays(5).isCarryForward(false).maxCarryDays(0).build());
            leaveTypeRepo.save(LeaveType.builder()
                    .name("Unpaid Leave").code("UL").description("Leave without pay")
                    .defaultDays(0).isCarryForward(false).maxCarryDays(0).build());

            // Salary Components
            salCompRepo.save(SalaryComponent.builder()
                    .name("Basic Salary").code("BASIC").isEarning(true).isTaxable(true).build());
            salCompRepo.save(SalaryComponent.builder()
                    .name("House Rent Allowance").code("HRA").isEarning(true).isTaxable(true).build());
            salCompRepo.save(SalaryComponent.builder()
                    .name("Provident Fund").code("PF").isEarning(false).isTaxable(false).build());

            log.info("✓ Seed data loaded: 3 departments, 3 roles, 3 users, 3 employees, 6 leave types");
        };
    }
}
