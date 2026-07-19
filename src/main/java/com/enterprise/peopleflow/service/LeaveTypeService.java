package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.LeaveTypeResponse;
import com.enterprise.peopleflow.entity.LeaveType;
import com.enterprise.peopleflow.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LeaveTypeResponse mapToResponse(LeaveType lt) {
        return LeaveTypeResponse.builder()
                .id(lt.getId())
                .name(lt.getName())
                .code(lt.getCode())
                .description(lt.getDescription())
                .defaultDays(lt.getDefaultDays())
                .isCarryForward(lt.getIsCarryForward())
                .maxCarryDays(lt.getMaxCarryDays())
                .isActive(lt.getIsActive())
                .build();
    }
}
