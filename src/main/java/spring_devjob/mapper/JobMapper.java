package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Job;

@Mapper(componentModel = "spring")
public interface JobMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    Job toJob(JobRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    JobResponse toJobResponse(Job job);

    JobBasic toJobBasic(Job job);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJob(@MappingTarget Job job, JobRequest request);
}
