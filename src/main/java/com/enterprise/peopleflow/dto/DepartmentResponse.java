package com.enterprise.peopleflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
    private Integer employeeCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
