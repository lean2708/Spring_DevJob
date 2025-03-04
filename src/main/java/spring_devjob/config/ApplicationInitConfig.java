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
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
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
                List<Permission> adminPermissions = permissionRepository.findAll();

                Role userRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.USER.name())
                        .description("ROLE_USER")
                        .build());

                Role proRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.PRO.name())
                        .description("ROLE_PRO")
                        .permissions(getProPermissions())
                        .build());

                Role hrRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.HR.name())
                        .description("ROLE_HR")
                        .permissions(getHRPermissions())
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.ADMIN.name())
                        .description("ROLE_ADMIN")
                        .permissions(adminPermissions)
                        .build());

                List<Role> roleList = List.of(userRole, proRole, hrRole, adminRole);
                roleRepository.saveAll(roleList);
            }

            if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
                Role adminRole = roleRepository.findByName(RoleEnum.ADMIN.name()).orElseThrow(
                        () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

                User admin = User.builder()
                        .name("Admin")
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(List.of(adminRole))
                        .gender(GenderEnum.MALE)
                        .build();

                userRepository.save(admin);
            }
        };
    }

    private List<Permission> getProPermissions() {
        return permissionRepository.findAllByNameIn(
                List.of(SEND_JOB_NOTIFICATIONS.name(), DOWNLOAD_FILE.name())
        );
    }

    private List<Permission> getHRPermissions() {
        return permissionRepository.findAllByNameIn(
                List.of(
                        CREATE_JOB.name(), FETCH_JOB_BY_ID.name(),
                        UPDATE_JOB.name(), DELETE_JOB.name(),
                        FETCH_JOBS_BY_COMPANY.name(), FETCH_RESUMES_BY_JOB.name()
                )
        );
    }
}
