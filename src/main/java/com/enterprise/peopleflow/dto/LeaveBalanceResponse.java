package com.enterprise.peopleflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {

    private Long employeeId;
    private Long leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeCode;
    private Integer totalEntitled;
    private BigDecimal totalUsed;
    private BigDecimal remaining;
}
