package spring_devjob.service;

import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;

public interface EntityDeactivationService {

    void deactivateCompany(Company company);

    void deactivateJob(Job job);

    void deactivateUser(User user, EntityStatus status);

    void deactivateResume(Resume resume);
}
