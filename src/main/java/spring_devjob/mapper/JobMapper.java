package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.history.JobHistory;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {SkillMapper.class})
public interface JobMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    Job toJob(JobRequest request);

    @Mapping(target = "skills", source = "skills", qualifiedByName = "jobHasSkillToEntityBasic")
    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> jobs);

    JobHistory toJobHistory(Job job);

    List<JobHistory> toJobHistoryList(List<Job> jobs);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJob(@MappingTarget Job job, JobRequest request);


    @Named("jobHasResumeToJobBasic")
    @Mapping(target = "id", source = "jobHasResume.job.id")
    @Mapping(target = "name", source = "jobHasResume.job.name")
    @Mapping(target = "applicationStatus", source = "jobHasResume.applicationStatus")
    JobBasic jobHasResumeToJobBasic(JobHasResume jobHasResume);

    @Named("jobHasResumeToJobBasicSet")
    Set<JobBasic> jobHasResumeToJobBasicSet(Set<JobHasResume> jobHasResumeSet);
}
