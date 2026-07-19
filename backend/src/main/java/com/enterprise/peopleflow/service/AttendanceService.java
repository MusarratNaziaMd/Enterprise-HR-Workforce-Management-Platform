package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.entity.Attendance;
import com.enterprise.peopleflow.entity.Employee;
import com.enterprise.peopleflow.enums.AttendanceStatus;
import com.enterprise.peopleflow.exception.BadRequestException;
import com.enterprise.peopleflow.exception.ResourceNotFoundException;
import com.enterprise.peopleflow.repository.AttendanceRepository;
import com.enterprise.peopleflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponse clockIn(ClockInOutRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LocalDate today = LocalDate.now();
        Attendance existing = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today)
                .orElse(null);

        if (existing != null && existing.getClockIn() != null) {
            throw new BadRequestException("Employee has already clocked in today");
        }

        Attendance attendance;
        if (existing != null) {
            attendance = existing;
        } else {
            attendance = Attendance.builder()
                    .employee(employee)
                    .attendanceDate(today)
                    .status(AttendanceStatus.PRESENT)
                    .build();
        }

        attendance.setClockIn(ZonedDateTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Clock-in recorded for employee: {} on {}", employee.getEmployeeCode(), today);
        return mapToResponse(saved);
    }

    @Transactional
    public AttendanceResponse clockOut(ClockInOutRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today)
                .orElseThrow(() -> new BadRequestException("No clock-in record found for today"));

        if (attendance.getClockIn() == null) {
            throw new BadRequestException("Cannot clock out without clocking in first");
        }
        if (attendance.getClockOut() != null) {
            throw new BadRequestException("Employee has already clocked out today");
        }

        attendance.setClockOut(ZonedDateTime.now());

        Duration workDuration = Duration.between(attendance.getClockIn(), attendance.getClockOut());
        BigDecimal workHours = BigDecimal.valueOf(workDuration.toMinutes() / 60.0)
                .setScale(2, RoundingMode.HALF_UP);
        attendance.setWorkHours(workHours);

        if (workHours.compareTo(BigDecimal.valueOf(8)) > 0) {
            attendance.setOvertimeHours(workHours.subtract(BigDecimal.valueOf(8)));
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Clock-out recorded for employee: {} — {} hours", employee.getEmployeeCode(), workHours);
        return mapToResponse(saved);
    }

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), request.getAttendanceDate())
                .orElse(Attendance.builder()
                        .employee(employee)
                        .attendanceDate(request.getAttendanceDate())
                        .build());

        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getRemarks() != null) attendance.setRemarks(request.getRemarks());

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getEmployeeAttendance(
            Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<Attendance> attendancePage = attendanceRepository.findByEmployeeId(employeeId, pageable);

        return PageResponse.<AttendanceResponse>builder()
                .content(attendancePage.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(attendancePage.getNumber())
                .pageSize(attendancePage.getSize())
                .totalElements(attendancePage.getTotalElements())
                .totalPages(attendancePage.getTotalPages())
                .first(attendancePage.isFirst())
                .last(attendancePage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .employeeName(a.getEmployee().getFullName())
                .attendanceDate(a.getAttendanceDate())
                .clockIn(a.getClockIn())
                .clockOut(a.getClockOut())
                .status(a.getStatus())
                .workHours(a.getWorkHours())
                .overtimeHours(a.getOvertimeHours())
                .remarks(a.getRemarks())
                .build();
    }
}
