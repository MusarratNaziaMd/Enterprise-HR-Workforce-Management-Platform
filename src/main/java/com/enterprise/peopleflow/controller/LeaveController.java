package com.enterprise.peopleflow.controller;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody LeaveRequest request) {
        LeaveResponse response = leaveService.applyLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave applied successfully", response));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request) {
        LeaveResponse response = leaveService.approveLeave(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave approved", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request) {
        LeaveResponse response = leaveService.rejectLeave(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave rejected", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        LeaveResponse response = leaveService.cancelLeave(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Leave cancelled", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getEmployeeLeaves(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LeaveResponse> response = leaveService.getEmployeeLeaves(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/manager/{managerId}/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getPendingLeavesByManager(
            @PathVariable Long managerId) {
        List<LeaveResponse> response = leaveService.getPendingLeavesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getAllPendingLeaves() {
        List<LeaveResponse> response = leaveService.getAllPendingLeaves();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam int year) {
        List<LeaveBalanceResponse> response = leaveService.getLeaveBalance(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
