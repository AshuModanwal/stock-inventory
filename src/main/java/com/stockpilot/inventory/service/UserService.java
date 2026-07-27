package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.auth.RegisterRequest;
import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.user.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.RoleName;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsersByCompany(Long companyId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = search != null && !search.isBlank()
                ? userRepository.searchByCompany(companyId, search, pageable)
                : userRepository.findByCompanyId(companyId, pageable);

        return PagedResponse.<UserResponse>builder()
                .content(users.getContent().stream().map(authService::mapUserToResponse).toList())
                .page(users.getNumber()).size(users.getSize())
                .totalElements(users.getTotalElements()).totalPages(users.getTotalPages())
                .last(users.isLast()).build();
    }

    @Transactional
    public UserResponse createUser(RegisterRequest request, UserPrincipal currentUser) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        Long companyId = request.getCompanyId() != null ? request.getCompanyId() : currentUser.getCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        RoleName roleName = request.getRoleName() != null
                ? RoleName.valueOf(request.getRoleName()) : RoleName.SALESPERSON;
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .company(company)
                .roles(Set.of(role))
                .active(true)
                .build();

        return authService.mapUserToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, UserPrincipal currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Company-level isolation check
        if (currentUser.getCompanyId() != null && !currentUser.getCompanyId().equals(
                user.getCompany() != null ? user.getCompany().getId() : null)) {
            throw new ForbiddenException("Cannot modify users from another company");
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getActive() != null) user.setActive(request.getActive());

        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(RoleName.valueOf(request.getRoleName()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRoleName()));
            user.setRoles(Set.of(role));
        }

        return authService.mapUserToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return authService.mapUserToResponse(user);
    }

    @Transactional
    public void toggleUserActive(Long userId, UserPrincipal currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot deactivate your own account");
        }
        user.setActive(!user.getActive());
        userRepository.save(user);
    }
}
