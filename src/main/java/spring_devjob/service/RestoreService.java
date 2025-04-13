package spring_devjob.service;

import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.UserResponse;

public interface RestoreService {

    CompanyResponse restoreCompany(long id);

    JobResponse restoreJob(long id);

    UserResponse restoreUser(long id, EntityStatus oldStatus);

    ResumeResponse restoreResume(long id);
}
