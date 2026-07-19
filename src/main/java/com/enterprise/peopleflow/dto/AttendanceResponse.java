package com.enterprise.peopleflow.dto;

import com.enterprise.peopleflow.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate attendanceDate;
    private ZonedDateTime clockIn;
    private ZonedDateTime clockOut;
    private AttendanceStatus status;
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    private String remarks;
}
