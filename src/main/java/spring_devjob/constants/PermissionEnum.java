package spring_devjob.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spring_devjob.entity.Permission;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

    CREATE_USER("/v1/users","USER","POST"),
    FETCH_USER_BY_ID("/v1/users/{id}","USER","GET"),
    FETCH_ALL_USERS("/v1/users","USER","GET"),
    UPDATE_USER("/v1/users/{id}","USER","PUT"),
    DELETE_USER("/v1/users/{id}","USER","DELETE"),
    DELETE_MULTIPLE_USERS("/v1/users", "USER", "DELETE"),

    CREATE_COMPANY("/v1/companies","COMPANY","POST"),
    FETCH_COMPANY_BY_ID("/v1/companies/{id}","COMPANY","GET"),
    FETCH_ALL_COMPANIES("/v1/companies","COMPANY","GET"),
    UPDATE_COMPANY("/v1/companies/{id}","COMPANY","PUT"),
    DELETE_COMPANY("/v1/companies/{id}","COMPANY","DELETE"),
    DELETE_MULTIPLE_COMPANIES("/v1/companies", "COMPANY", "DELETE"),
    SEARCH_COMPANIES("/v1/companies/search","COMPANY","GET"),
    FETCH_JOBS_BY_COMPANY("/v1/company/{companyId}/jobs","JOB","GET"),

    CREATE_JOB("/v1/jobs","JOB","POST"),
    FETCH_JOB_BY_ID("/v1/jobs/{id}","JOB","GET"),
    FETCH_ALL_JOBS("/v1/jobs","JOB","GET"),
    UPDATE_JOB("/v1/jobs/{id}","JOB","PUT"),
    DELETE_JOB("/v1/jobs/{id}","JOB","DELETE"),
    DELETE_MULTIPLE_JOBS("/v1/jobs", "JOB", "DELETE"),
    FETCH_APPLIED_JOBS("/v1/applied-jobs-by-user","JOB","GET"),
    SEARCH_JOBS_BY_SKILLS("/v1/jobs/search-by-skills", "JOB", "GET"),

    CREATE_SKILL("/v1/skills","SKILL","POST"),
    FETCH_SKILL_BY_ID("/v1/skills/{id}","SKILL","GET"),
    FETCH_ALL_SKILLS("/v1/skills","SKILL","GET"),
    UPDATE_SKILL("/v1/skills/{id}","SKILL","PUT"),
    DELETE_SKILL("/v1/skills/{id}","SKILL","DELETE"),
    DELETE_MULTIPLE_SKILLS("/v1/skills", "SKILL", "DELETE"),

    CREATE_RESUME("/v1/resumes","RESUME","POST"),
    FETCH_RESUME_BY_ID("/v1/resumes/{id}","RESUME","GET"),
    FETCH_ALL_RESUMES("/v1/resumes","RESUME","GET"),
    UPDATE_RESUME("/v1/resumes/{id}","RESUME","PUT"),
    DELETE_RESUME("/v1/resumes/{id}","RESUME","DELETE"),
    DELETE_MULTIPLE_RESUMES("/v1/resumes", "RESUME", "DELETE"),
    FETCH_RESUMES_BY_JOB("/v1/{jobId}/resumes","RESUME","GET"),
    FETCH_RESUMES_BY_USER("/v1/resumes/by-user","RESUME","GET"),

    CREATE_ROLE("/v1/roles","ROLE","POST"),
    FETCH_ROLE_BY_ID("/v1/roles","ROLE","GET"),
    FETCH_ALL_ROLES("/v1/roles","ROLE","GET"),
    UPDATE_ROLE("/v1/roles","ROLE","PUT"),
    DELETE_ROLE("/v1/roles","ROLE","DELETE"),

    FETCH_PERMISSION_BY_ID("/v1/permissions","PERMISSION","GET"),
    FETCH_ALL_PERMISSIONS("/v1/permissions","PERMISSION","GET"),

    UPLOAD_FILE("/v1/file/upload","FILE","POST"),
    DOWNLOAD_FILE("/v1/file/download","FILE","GET"),
    DELETE_FILE("/v1/file/delete", "FILE","DELETE"),

    FETCH_SUBSCRIBER_BY_EMAIL("/v1/subscribers/{email}","SUBSCRIBER","GET"),
    FETCH_ALL_SUBSCRIBERS("/v1/subscribers","SUBSCRIBER","GET"),
    DELETE_SUBSCRIBER_BY_EMAIL("/v1/subscribers/{email}","SUBSCRIBER","DELETE"),
    SEND_JOB_NOTIFICATIONS("SCHEDULED_TASK","EMAIL","SYSTEM");

    private final String apiPath;
    private final String module;
    private final String method;

    public Permission toPermission() {
        return Permission.builder()
                .name(this.name())
                .apiPath(this.apiPath)
                .module(this.module)
                .method(this.method)
                .build();
    }
}
