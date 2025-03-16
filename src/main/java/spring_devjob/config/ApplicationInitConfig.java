package spring_devjob.config;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import spring_devjob.constants.GenderEnum;
import spring_devjob.constants.PermissionEnum;
import spring_devjob.constants.RoleEnum;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleHasPermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.service.UserHasRoleService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring_devjob.constants.PermissionEnum.*;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;
    private final UserHasRoleService userHasRoleService;

    @NonFinal
    static final String ADMIN_EMAIL = "admin@gmail.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(){
        log.info("INIT APPLICATION....");
        return args ->{

            if (permissionRepository.count() == 0) {
                List<Permission> permissionList = Arrays.stream(PermissionEnum.values())
                        .map(PermissionEnum::toPermission)
                        .collect(Collectors.toList());

                permissionRepository.saveAll(permissionList);
            }

            if(roleRepository.count() == 0){
                Role userRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.USER.name())
                        .description("ROLE_USER")
                        .build());

                Role proRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.PRO.name())
                        .description("ROLE_PRO")
                        .build());
                updateRolePro(proRole);

                Role hrRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.HR.name())
                        .description("ROLE_HR")
                        .build());
                updateRoleHR(hrRole);

                Role adminRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.ADMIN.name())
                        .description("ROLE_ADMIN")
                        .build());
                updateRoleAdmin(adminRole);

                List<Role> roleList = List.of(userRole, proRole, hrRole, adminRole);
                roleRepository.saveAll(roleList);
            }

            if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
                User admin = userRepository.save(User.builder()
                        .name("Admin")
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .gender(GenderEnum.MALE)
                        .build());

                userHasRoleService.saveUserHasRole(admin, RoleEnum.ADMIN);
            }
        };
    }

    private void updateRolePro(Role role) {
        Set<Permission> permissionSet = permissionRepository.findAllByNameIn(
                Set.of(SEND_JOB_NOTIFICATIONS.name(), DOWNLOAD_FILE.name()));

        Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                .map(permission -> new RoleHasPermission(role, permission))
                .collect(Collectors.toSet());

        roleHasPermissionRepository.saveAll(roleHasPermissions);
    }

    private void updateRoleHR(Role role) {
        Set<Permission> permissionSet = permissionRepository.findAllByNameIn(
                Set.of(CREATE_JOB.name(), FETCH_JOB_BY_ID.name(),
                        UPDATE_JOB.name(), DELETE_JOB.name(),
                        FETCH_JOBS_BY_COMPANY.name(), FETCH_RESUMES_BY_JOB.name()));

        Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                .map(permission -> new RoleHasPermission(role, permission))
                .collect(Collectors.toSet());

        roleHasPermissionRepository.saveAll(roleHasPermissions);
    }

    private void updateRoleAdmin(Role role) {
        List<Permission> adminPermissions = permissionRepository.findAll();

        Set<RoleHasPermission> roleHasPermissions = adminPermissions.stream()
                .map(permission -> new RoleHasPermission(role, permission))
                .collect(Collectors.toSet());

        roleHasPermissionRepository.saveAll(roleHasPermissions);
    }
}
