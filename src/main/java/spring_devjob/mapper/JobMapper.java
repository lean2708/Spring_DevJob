package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.history.JobHistory;

import java.util.List;

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

    EntityBasic toEntityBasic(Job job);
}
