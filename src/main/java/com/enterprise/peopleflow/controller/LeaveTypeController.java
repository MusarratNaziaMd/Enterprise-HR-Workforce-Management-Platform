package com.enterprise.peopleflow.controller;

import com.enterprise.peopleflow.dto.ApiResponse;
import com.enterprise.peopleflow.dto.LeaveTypeResponse;
import com.enterprise.peopleflow.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getAllActiveLeaveTypes() {
        List<LeaveTypeResponse> leaveTypes = leaveTypeService.getAllActiveLeaveTypes();
        return ResponseEntity.ok(ApiResponse.success(leaveTypes));
    }
}
