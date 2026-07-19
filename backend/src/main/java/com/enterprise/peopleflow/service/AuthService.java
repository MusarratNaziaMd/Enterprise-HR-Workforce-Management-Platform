package com.enterprise.peopleflow.service;

import com.enterprise.peopleflow.dto.*;
import com.enterprise.peopleflow.entity.Role;
import com.enterprise.peopleflow.entity.User;
import com.enterprise.peopleflow.exception.BadRequestException;
import com.enterprise.peopleflow.exception.ConflictException;
import com.enterprise.peopleflow.exception.ResourceNotFoundException;
import com.enterprise.peopleflow.repository.RoleRepository;
import com.enterprise.peopleflow.repository.UserRepository;
import com.enterprise.peopleflow.security.CustomUserDetails;
import com.enterprise.peopleflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> ADMIN_ROLES = Set.of("HR_ADMIN", "HR_MANAGER");

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));

        String userRoleName = user.getRoles().stream()
                .filter(Role::getIsActive)
                .map(Role::getName)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("User has no active role assigned"));

        String portal = request.getPortal().toUpperCase();
        boolean isAdminPortal = "ADMIN".equals(portal);
        boolean isEmployeePortal = "EMPLOYEE".equals(portal);

        if (!isAdminPortal && !isEmployeePortal) {
            throw new BadRequestException("Invalid portal: " + portal + ". Must be ADMIN or EMPLOYEE.");
        }

        if (isAdminPortal && !ADMIN_ROLES.contains(userRoleName)) {
            log.warn("User '{}' with role '{}' attempted admin portal login", user.getUsername(), userRoleName);
            throw new BadRequestException(
                    "Access denied. Your role (" + userRoleName + ") does not have admin portal access. "
                    + "Please select the Employee portal.");
        }

        if (isEmployeePortal && ADMIN_ROLES.contains(userRoleName)) {
            log.warn("User '{}' with role '{}' attempted employee portal login", user.getUsername(), userRoleName);
            throw new BadRequestException(
                    "Access denied. Your role (" + userRoleName + ") should use the Admin portal. "
                    + "Please select the Admin portal.");
        }

        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        updateLastLogin(userDetails.getId());

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .portal(portal)
                .authorities(authorities)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        Role role;
        if (request.getRoleName() != null) {
            role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRoleName()));
        } else {
            role = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "EMPLOYEE"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isLocked(false)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(authorities)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(authorities)
                .build();
    }

    private void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }
}
