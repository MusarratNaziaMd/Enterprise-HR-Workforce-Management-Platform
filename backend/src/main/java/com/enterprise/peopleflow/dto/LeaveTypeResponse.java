package com.enterprise.peopleflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveTypeResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer defaultDays;
    private Boolean isCarryForward;
    private Integer maxCarryDays;
    private Boolean isActive;
}
