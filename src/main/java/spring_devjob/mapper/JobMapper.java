package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.JobHasSkill;
import spring_devjob.entity.UserHasRole;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SkillMapper.class})
public interface JobMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    Job toJob(JobRequest request);

    @Mapping(target = "skills", source = "skills", qualifiedByName = "jobHasSkillToEntityBasic")
    JobResponse toJobResponse(Job job);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJob(@MappingTarget Job job, JobRequest request);

    EntityBasic toEntityBasic(Job job);
}
