package com.enterprise.peopleflow.controller;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn(
            @Valid @RequestBody ClockInOutRequest request) {
        AttendanceResponse response = attendanceService.clockIn(request);
        return ResponseEntity.ok(ApiResponse.success("Clocked in successfully", response));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut(
            @Valid @RequestBody ClockInOutRequest request) {
        AttendanceResponse response = attendanceService.clockOut(request);
        return ResponseEntity.ok(ApiResponse.success("Clocked out successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AttendanceResponse> response = attendanceService.getEmployeeAttendance(
                employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceResponse> response = attendanceService.getAttendanceByDateRange(
                employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
