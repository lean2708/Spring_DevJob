package spring_devjob.mapper;

import org.mapstruct.*;
import org.springframework.context.annotation.DependsOn;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.Resume;

import java.util.List;

@Mapper(componentModel = "spring",nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {JobMapper.class})
public interface ResumeMapper {
    @Mapping(target = "user", ignore = true)
    Resume toResume(ResumeRequest request);

    @Mapping(target = "jobs", source = "jobs", qualifiedByName = "jobHasResumeToJobBasicSet")
    ResumeResponse toResumeResponse(Resume resume);

    List<ResumeResponse> toResumeResponseList(List<Resume> resumes);


    @Mapping(target = "user", ignore = true)
    void updateResume(@MappingTarget Resume resume, ResumeRequest request);
}
