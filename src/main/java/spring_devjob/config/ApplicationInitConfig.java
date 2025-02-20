package spring_devjob.config;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import spring_devjob.constants.GenderEnum;
import spring_devjob.constants.RoleEnum;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

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

            if (!userRepository.existsByEmail(ADMIN_EMAIL)) {

                List<Permission> permissions = createAllPermissions();
                permissionRepository.saveAll(permissions);

                List<Permission> permissionPro = createProPermissions();

                List<Permission> permissionHR = createHRPermissions();

                Role userRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.USER.name())
                        .description("ROLE_USER")
                        .build());

                Role proRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.PRO.name())
                        .description("ROLE_PRO")
                        .permissions(permissionPro)
                        .build());

                Role hrRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.HR.name())
                        .description("ROLE_HR")
                        .permissions(permissionHR)
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(RoleEnum.ADMIN.name())
                        .description("ROLE_ADMIN")
                        .permissions(permissions)
                        .build());

                roleRepository.saveAll(List.of(userRole, proRole, hrRole, adminRole));

                User admin = User.builder()
                        .name("ADMIN")
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(List.of(adminRole))
                        .gender(GenderEnum.MALE)
                        .build();

                userRepository.save(admin);
            }
        };
    }

    private List<Permission> createAllPermissions(){
        return List.of(
                Permission.builder().name("Create a user").module("USER").apiPath("/v1/user").method("POST").build(),
                Permission.builder().name("Fetch user by id").module("USER").apiPath("/v1/user").method("GET").build(),
                Permission.builder().name("Fetch all users").module("USER").apiPath("/v1/users").method("GET").build(),
                Permission.builder().name("Update a user").module("USER").apiPath("/v1/users").method("PUT").build(),
                Permission.builder().name("Delete a user").module("USER").apiPath("/v1/users").method("DELETE").build(),

                Permission.builder().name("Create a company").module("COMPANY").apiPath("/v1/company").method("POST").build(),
                Permission.builder().name("Fetch all companies").module("COMPANY").apiPath("/v1/companies").method("GET").build(),
                Permission.builder().name("Update a company").module("COMPANY").apiPath("/v1/company").method("PUT").build(),
                Permission.builder().name("Delete a company").module("COMPANY").apiPath("/v1/company").method("DELETE").build(),

                Permission.builder().name("Create a job").module("JOB").apiPath("/v1/job").method("POST").build(),
                Permission.builder().name("Fetch job by id").module("JOB").apiPath("/v1/job").method("GET").build(),
                Permission.builder().name("Fetch all jobs").module("JOB").apiPath("/v1/jobs").method("GET").build(),
                Permission.builder().name("Update a job").module("JOB").apiPath("/v1/job").method("PUT").build(),
                Permission.builder().name("Delete a job").module("JOB").apiPath("/v1/job").method("DELETE").build(),
                Permission.builder().name("Fetch all jobs by company").module("JOB").apiPath("/v1/company/{companyId}/jobs").method("GET").build(),

                Permission.builder().name("Create a skill").module("SKILL").apiPath("/v1/skill").method("POST").build(),
                Permission.builder().name("Fetch skill by id").module("SKILL").apiPath("/v1/skill").method("GET").build(),
                Permission.builder().name("Fetch all skills").module("SKILL").apiPath("/v1/skills").method("GET").build(),
                Permission.builder().name("Update a skill").module("SKILL").apiPath("/v1/skill").method("PUT").build(),
                Permission.builder().name("Delete a skill").module("SKILL").apiPath("/v1/skill").method("DELETE").build(),

                Permission.builder().name("Create a resume").module("RESUME").apiPath("/v1/resume").method("POST").build(),
                Permission.builder().name("Fetch resume by id").module("RESUME").apiPath("/v1/resume").method("GET").build(),
                Permission.builder().name("Fetch all resumes").module("RESUME").apiPath("/v1/resumes").method("GET").build(),
                Permission.builder().name("Update a resume").module("RESUME").apiPath("/v1/resume").method("PUT").build(),
                Permission.builder().name("Delete a resume").module("RESUME").apiPath("/v1/resume").method("DELETE").build(),
                Permission.builder().name("Fetch all resumes by job").module("RESUME").apiPath("/v1/job/{jobId}/resumes").method("GET").build(),

                Permission.builder().name("Create a role").module("ROLE").apiPath("/v1/role").method("POST").build(),
                Permission.builder().name("Fetch role by id").module("ROLE").apiPath("/v1/role").method("GET").build(),
                Permission.builder().name("Fetch all roles").module("ROLE").apiPath("/v1/roles").method("GET").build(),
                Permission.builder().name("Update a role").module("ROLE").apiPath("/v1/role").method("PUT").build(),
                Permission.builder().name("Delete a role").module("ROLE").apiPath("/v1/role").method("DELETE").build(),

                Permission.builder().name("Create a permission").module("PERMISSION").apiPath("/v1/permission").method("POST").build(),
                Permission.builder().name("Fetch permission by id").module("PERMISSION").apiPath("/v1/permission").method("GET").build(),
                Permission.builder().name("Fetch all permissions").module("PERMISSION").apiPath("/v1/permissions").method("GET").build(),
                Permission.builder().name("Update a permission").module("PERMISSION").apiPath("/v1/permission").method("PUT").build(),
                Permission.builder().name("Delete a permission").module("PERMISSION").apiPath("/v1/permission").method("DELETE").build(),

                Permission.builder().name("Fetch a subscriber by Email").module("SUBSCRIBER").apiPath("/v1/subscriber/{email}").method("GET").build(),
                Permission.builder().name("Fetch all subscribers").module("SUBSCRIBER").apiPath("/v1/subscribers").method("GET").build(),
                Permission.builder().name("Delete a subscriber by Email").module("SUBSCRIBER").apiPath("/v1/subscriber/{email}").method("DELETE").build(),
                Permission.builder().name("Send job notifications to subscribers").module("EMAIL").apiPath("SCHEDULED_TASK").method("SYSTEM").build(),

                Permission.builder().name("Upload a file").module("FILE").apiPath("/v1/file/upload").method("POST").build(),
                Permission.builder().name("Download a file").module("FILE").apiPath("/v1/file/download").method("GET").build(),
                Permission.builder().name("Delete a file").module("FILE").apiPath("/v1/file/delete").method("DELETE").build()
        );
    }

    private List<Permission> createProPermissions() {
        return List.of(
                Permission.builder().name("Download a file").module("FILE").apiPath("/v1/file/download").method("GET").build(),
                Permission.builder().name("Send job notifications to subscribers").module("EMAIL").apiPath("SCHEDULED_TASK").method("SYSTEM").build()
                );
    }

    private List<Permission> createHRPermissions() {
        return List.of(
                Permission.builder().name("Create a job").module("JOB").apiPath("/v1/job").method("POST").build(),
                Permission.builder().name("Fetch job by id").module("JOB").apiPath("/v1/job").method("GET").build(),
                Permission.builder().name("Update a job").module("JOB").apiPath("/v1/job").method("PUT").build(),
                Permission.builder().name("Delete a job").module("JOB").apiPath("/v1/job").method("DELETE").build(),
                Permission.builder().name("Fetch all jobs by company").module("JOB").apiPath("/v1/company/{companyId}/jobs").method("GET").build(),
                Permission.builder().name("Fetch all resumes by job").module("RESUME").apiPath("/v1/job/{jobId}/resumes").method("GET").build()
        );
    }
}
