package com.enterprise.peopleflow.dto;

import com.enterprise.peopleflow.enums.EmploymentStatus;
import com.enterprise.peopleflow.enums.EmploymentType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    private LocalDate dateOfBirth;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private String gender;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 10, message = "Pin code must not exceed 10 characters")
    private String pinCode;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    private String profileImageUrl;

    private Long departmentId;

    private Long managerId;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    private EmploymentType employmentType;

    private EmploymentStatus status;

    private LocalDate dateOfExit;

    private LocalDate probationEndDate;
}
