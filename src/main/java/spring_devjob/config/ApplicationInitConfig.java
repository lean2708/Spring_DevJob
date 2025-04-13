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
import spring_devjob.entity.relationship.RoleHasPermission;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.relationship.RoleHasPermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.service.relationship.UserHasRoleService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring_devjob.constants.PermissionEnum.*;

@Slf4j(topic = "APPLICATION-INITIALIZATION")
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
    static final String ADMIN_PASSWORD = "123456";

    @Bean
    ApplicationRunner applicationRunner(){
        log.info("INIT APPLICATION STARTING....");

        return args ->{

            if (permissionRepository.count() == 0) {
                log.info("Initializing permissions...");

                List<Permission> permissionList = Arrays.stream(PermissionEnum.values())
                        .map(PermissionEnum::toPermission)
                        .collect(Collectors.toList());

                permissionRepository.saveAllAndFlush(permissionList);
            }

            if(roleRepository.count() == 0){
                log.info("Initializing roles...");

                Role userRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.USER.name())
                        .description("ROLE_USER")
                        .build());
                updateRoleUser(userRole);

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
                roleRepository.saveAllAndFlush(roleList);
            }

            if (userRepository.countByEmail(ADMIN_EMAIL) == 0) {
                log.info("Creating default admin account...");

                User admin = userRepository.save(User.builder()
                        .name("Admin")
                        .email(ADMIN_EMAIL)
                        .phone("099999999")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .age(100)
                        .gender(GenderEnum.MALE)
                        .address("VIET NAM")
                        .build());

                userHasRoleService.saveUserHasRole(admin, RoleEnum.ADMIN);
            }
        };
    }

    private void updateRolePro(Role role) {
        Set<Permission> permissionSet = permissionRepository.findAllByNameIn(
                Set.of(FETCH_TOP_RATED_COMPANIES.name(), SEND_JOB_NOTIFICATIONS.name()
                )
        );

        Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                .map(permission -> new RoleHasPermission(role, permission))
                .collect(Collectors.toSet());

        roleHasPermissionRepository.saveAll(roleHasPermissions);
    }

    private void updateRoleHR(Role role) {
        Set<Permission> permissionSet = permissionRepository.findAllByNameIn(
                Set.of(UPDATE_COMPANY.name(), DELETE_COMPANY.name(), RESTORE_COMPANY.name(),
                        FETCH_JOBS_BY_COMPANY.name(), FETCH_REVIEWS_BY_COMPANY.name(),
                        DELETE_REVIEWS.name(), CREATE_JOB.name(), FETCH_JOB_BY_ID.name(), UPDATE_JOB.name(),
                        DELETE_JOB.name(), DELETE_MULTIPLE_JOBS.name(), RESTORE_JOB.name(),
                        FETCH_RESUMES_BY_JOB.name(), UPDATE_CV_STATUS.name(),
                        UPLOAD_IMAGE.name(), UPLOAD_VIDEO.name(), GET_ALL_FILES.name(), DELETE_FILE.name()
                )
        );

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

    private void updateRoleUser(Role role) {
        Set<Permission> permissionSet = permissionRepository.findAllByNameIn(
                Set.of(CREATE_RESUME.name(), FETCH_RESUME_BY_ID.name(),
                        FETCH_ALL_RESUMES.name(), UPDATE_RESUME.name(),
                        DELETE_RESUME.name(), DELETE_MULTIPLE_RESUMES.name(), RESTORE_RESUME.name(),
                        SAVE_JOB.name(), GET_SAVED_JOBS.name(), REMOVE_SAVED_JOB.name(),
                        CREATE_REVIEW.name(), GET_REVIEW_BY_ID.name(),
                        GET_ALL_REVIEWS.name(), UPDATE_REVIEW.name(), DELETE_REVIEW.name(),
                        FETCH_JOB_BY_ID.name(), FETCH_ALL_JOBS.name(),
                        SEARCH_JOBS_BY_SKILLS.name(), APPLY_RESUME_TO_JOB.name(),
                        FETCH_ALL_COMPANIES.name(), FETCH_COMPANY_BY_ID.name(),
                        SEARCH_COMPANIES.name(), FETCH_REVIEWS_BY_COMPANY.name(),
                        CREATE_VNPAY_PAYMENT_URL.name(), VNPAY_PAYMENT_CALLBACK.name(),
                        FETCH_SUBSCRIBER_BY_ID.name(), CHECK_PRO_STATUS.name(), UPLOAD_CV.name()
                )
        );

        Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                .map(permission -> new RoleHasPermission(role, permission))
                .collect(Collectors.toSet());

        roleHasPermissionRepository.saveAll(roleHasPermissions);
    }
}
