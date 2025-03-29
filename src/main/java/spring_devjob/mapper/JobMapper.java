package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.request.JobCreationRequest;
import spring_devjob.dto.request.JobUpdateRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.relationship.JobHasResume;

import java.util.List;
import java.util.Set;


@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {SkillMapper.class})
public interface JobMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    Job toJob(JobCreationRequest request);

    @Mapping(target = "skills", source = "skills", qualifiedByName = "jobHasSkillToEntityBasic")
    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> jobs);


    @Mapping(target = "skills", ignore = true)
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);


    @Mapping(target = "id", source = "jobHasResume.job.id")
    @Mapping(target = "name", source = "jobHasResume.job.name")
    @Mapping(target = "applicationStatus", source = "jobHasResume.applicationStatus")
    JobBasic jobHasResumeToJobBasic(JobHasResume jobHasResume);

    @Named("jobHasResumeToJobBasicSet")
    Set<JobBasic> jobHasResumeToJobBasicSet(Set<JobHasResume> jobHasResumeSet);
}
