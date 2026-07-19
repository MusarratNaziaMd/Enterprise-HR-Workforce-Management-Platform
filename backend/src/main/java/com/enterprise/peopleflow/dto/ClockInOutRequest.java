package com.enterprise.peopleflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockInOutRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}
