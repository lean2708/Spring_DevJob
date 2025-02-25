package spring_devjob.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spring_devjob.entity.Permission;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

    CREATE_USER("Create a user", "USER", "/v1/users", "POST"),
    FETCH_USER_BY_ID("Fetch user by id", "USER", "/v1/users", "GET"),
    FETCH_ALL_USERS("Fetch all users", "USER", "/v1/users", "GET"),
    UPDATE_USER("Update a user", "USER", "/v1/users", "PUT"),
    DELETE_USER("Delete a user", "USER", "/v1/users", "DELETE"),

    CREATE_COMPANY("Create a company", "COMPANY", "/v1/companies", "POST"),
    FETCH_COMPANY_BY_ID("Fetch company by id", "COMPANY", "/v1/companies", "GET"),
    FETCH_ALL_COMPANIES("Fetch all companies", "COMPANY", "/v1/companies", "GET"),
    UPDATE_COMPANY("Update a company", "COMPANY", "/v1/companies", "PUT"),
    DELETE_COMPANY("Delete a company", "COMPANY", "/v1/companies", "DELETE"),
    SEARCH_COMPANY("Search company","COMPANY", "/v1/companies/search", "GET"),

    CREATE_JOB("Create a job", "JOB", "/v1/jobs", "POST"),
    FETCH_JOB_BY_ID("Fetch job by id", "JOB", "/v1/jobs", "GET"),
    FETCH_ALL_JOBS("Fetch all jobs", "JOB", "/v1/jobs", "GET"),
    UPDATE_JOB("Update a job", "JOB", "/v1/jobs", "PUT"),
    DELETE_JOB("Delete a job", "JOB", "/v1/jobs", "DELETE"),
    FETCH_JOBS_BY_COMPANY("Fetch all jobs by company", "JOB", "/v1/company/{companyId}/jobs", "GET"),
    FETCH_APPLIED_JOBS("Fetch all applied jobs by user", "JOB", "/v1/applied-jobs-by-user", "GET"),

    CREATE_SKILL("Create a skill", "SKILL", "/v1/skills", "POST"),
    FETCH_SKILL_BY_ID("Fetch skill by id", "SKILL", "/v1/skills", "GET"),
    FETCH_ALL_SKILLS("Fetch all skills", "SKILL", "/v1/skills", "GET"),
    UPDATE_SKILL("Update a skill", "SKILL", "/v1/skills", "PUT"),
    DELETE_SKILL("Delete a skill", "SKILL", "/v1/skills", "DELETE"),

    CREATE_RESUME("Create a resume", "RESUME", "/v1/resumes", "POST"),
    FETCH_RESUME_BY_ID("Fetch resume by id", "RESUME", "/v1/resumes", "GET"),
    FETCH_ALL_RESUMES("Fetch all resumes", "RESUME", "/v1/resumes", "GET"),
    UPDATE_RESUME("Update a resume", "RESUME", "/v1/resumes", "PUT"),
    DELETE_RESUME("Delete a resume", "RESUME", "/v1/resumes", "DELETE"),
    FETCH_RESUMES_BY_JOB("Fetch all resumes by job","RESUME", "/v1/{jobId}/resumes", "GET"),
    FETCH_RESUMES_BY_USER("Fetch all resumes by user", "RESUME", "/v1/resumes/by-user", "GET"),

    CREATE_ROLE("Create a role", "ROLE", "/v1/roles", "POST"),
    FETCH_ROLE_BY_ID("Fetch role by id", "ROLE", "/v1/roles", "GET"),
    FETCH_ALL_ROLES("Fetch all roles", "ROLE", "/v1/roles", "GET"),
    UPDATE_ROLE("Update a role", "ROLE", "/v1/roles", "PUT"),
    DELETE_ROLE("Delete a role", "ROLE", "/v1/roles", "DELETE"),

    CREATE_PERMISSION("Create a permission", "PERMISSION", "/v1/permissions", "POST"),
    FETCH_PERMISSION_BY_ID("Fetch permission by id", "PERMISSION", "/v1/permissions", "GET"),
    FETCH_ALL_PERMISSIONS("Fetch all permissions", "PERMISSION", "/v1/permissions", "GET"),
    UPDATE_PERMISSION("Update a permission", "PERMISSION", "/v1/permissions", "PUT"),
    DELETE_PERMISSION("Delete a permission", "PERMISSION", "/v1/permissions", "DELETE"),

    UPLOAD_FILE("Upload a file", "FILE", "/v1/file/upload", "POST"),
    DOWNLOAD_FILE("Download a file", "FILE", "/v1/file/download", "GET"),
    DELETE_FILE("Delete a file", "FILE", "/v1/file/delete", "DELETE"),

    FETCH_SUBSCRIBER_BY_EMAIL("Fetch a subscriber by Email", "SUBSCRIBER", "/v1/subscribers/{email}", "GET"),
    FETCH_ALL_SUBSCRIBERS("Fetch all subscribers", "SUBSCRIBER", "/v1/subscribers", "GET"),
    DELETE_SUBSCRIBER_BY_EMAIL("Delete a subscriber by Email", "SUBSCRIBER", "/v1/subscribers/{email}", "DELETE"),
    SEND_JOB_NOTIFICATIONS("Send job notifications to subscribers", "EMAIL", "SCHEDULED_TASK", "SYSTEM");

    private final String name;
    private final String module;
    private final String apiPath;
    private final String method;

    public Permission toPermission() {
        return Permission.builder()
                .name(this.name)
                .module(this.module)
                .apiPath(this.apiPath)
                .method(this.method)
                .build();
    }
}
