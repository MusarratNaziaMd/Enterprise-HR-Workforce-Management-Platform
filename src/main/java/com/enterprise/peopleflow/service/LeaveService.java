package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.entity.Employee;
import com.enterprise.peopleflow.entity.Leave;
import com.enterprise.peopleflow.entity.LeaveType;
import com.enterprise.peopleflow.enums.LeaveStatus;
import com.enterprise.peopleflow.exception.BadRequestException;
import com.enterprise.peopleflow.exception.ResourceNotFoundException;
import com.enterprise.peopleflow.repository.EmployeeRepository;
import com.enterprise.peopleflow.repository.LeaveRepository;
import com.enterprise.peopleflow.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional
    public LeaveResponse applyLeave(LeaveRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave Type", "id", request.getLeaveTypeId()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }

        List<Leave> overlapping = leaveRepository.findOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Leave dates overlap with an existing leave request");
        }

        BigDecimal totalDays = request.getTotalDays() != null
                ? request.getTotalDays()
                : BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(
                        request.getStartDate(), request.getEndDate()) + 1);

        Leave leave = Leave.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        Leave saved = leaveRepository.save(leave);
        log.info("Leave applied: employee={}, type={}, dates={} to {}",
                employee.getEmployeeCode(), leaveType.getCode(),
                request.getStartDate(), request.getEndDate());
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse approveLeave(Long leaveId, LeaveApprovalRequest request) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be approved");
        }

        Employee approver = employeeRepository.findById(request.getApprovedById())
                .orElseThrow(() -> new ResourceNotFoundException("Approver", "id", request.getApprovedById()));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(OffsetDateTime.now());

        Leave saved = leaveRepository.save(leave);
        log.info("Leave approved: id={}, approvedBy={}", leaveId, approver.getEmployeeCode());
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long leaveId, LeaveApprovalRequest request) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be rejected");
        }

        Employee approver = employeeRepository.findById(request.getApprovedById())
                .orElseThrow(() -> new ResourceNotFoundException("Approver", "id", request.getApprovedById()));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(OffsetDateTime.now());
        leave.setRejectionReason(request.getRejectionReason());

        Leave saved = leaveRepository.save(leave);
        log.info("Leave rejected: id={}, by={}", leaveId, approver.getEmployeeCode());
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse cancelLeave(Long leaveId, Long employeeId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("You can only cancel your own leaves");
        }
        if (leave.getStatus() != LeaveStatus.PENDING && leave.getStatus() != LeaveStatus.APPROVED) {
            throw new BadRequestException("Only pending or approved leaves can be cancelled");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        Leave saved = leaveRepository.save(leave);
        log.info("Leave cancelled: id={}", leaveId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getEmployeeLeaves(Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Leave> leavePage = leaveRepository.findByEmployeeId(employeeId, pageable);
        return mapToPageResponse(leavePage);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeavesByManager(Long managerId) {
        return leaveRepository.findPendingLeavesByManager(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllPendingLeaves() {
        return leaveRepository.findAllPendingLeaves()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getLeaveBalance(Long employeeId, int year) {
        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        return leaveTypes.stream().map(lt -> {
            BigDecimal used = leaveRepository.sumApprovedDaysByEmployeeAndTypeAndYear(
                    employeeId, lt.getId(), year);
            int entitled = lt.getDefaultDays();
            return LeaveBalanceResponse.builder()
                    .employeeId(employeeId)
                    .leaveTypeId(lt.getId())
                    .leaveTypeName(lt.getName())
                    .leaveTypeCode(lt.getCode())
                    .totalEntitled(entitled)
                    .totalUsed(used)
                    .remaining(BigDecimal.valueOf(entitled).subtract(used))
                    .build();
        }).toList();
    }

    private LeaveResponse mapToResponse(Leave l) {
        return LeaveResponse.builder()
                .id(l.getId())
                .employeeId(l.getEmployee().getId())
                .employeeCode(l.getEmployee().getEmployeeCode())
                .employeeName(l.getEmployee().getFullName())
                .leaveTypeId(l.getLeaveType().getId())
                .leaveTypeName(l.getLeaveType().getName())
                .leaveTypeCode(l.getLeaveType().getCode())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .totalDays(l.getTotalDays())
                .reason(l.getReason())
                .status(l.getStatus())
                .approvedById(l.getApprovedBy() != null ? l.getApprovedBy().getId() : null)
                .approvedByName(l.getApprovedBy() != null ? l.getApprovedBy().getFullName() : null)
                .approvedAt(l.getApprovedAt())
                .rejectionReason(l.getRejectionReason())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private PageResponse<LeaveResponse> mapToPageResponse(Page<Leave> page) {
        return PageResponse.<LeaveResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
