package com.enterprise.peopleflow.dto;

import com.enterprise.peopleflow.enums.LeaveStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private Long leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeCode;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;

    private LeaveStatus status;

    private Long approvedById;
    private String approvedByName;
    private OffsetDateTime approvedAt;
    private String rejectionReason;

    private OffsetDateTime createdAt;
}
