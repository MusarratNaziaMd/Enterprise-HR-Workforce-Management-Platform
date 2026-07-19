package com.enterprise.peopleflow.dto;

import com.enterprise.peopleflow.enums.EmploymentStatus;
import com.enterprise.peopleflow.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pinCode;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
    private LocalDate dateOfExit;
    private LocalDate probationEndDate;
    private String designation;
    private EmploymentType employmentType;
    private EmploymentStatus status;
    private String profileImageUrl;

    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    private Long managerId;
    private String managerName;
    private String managerEmployeeCode;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String generatedUsername;
    private String generatedPassword;
}
