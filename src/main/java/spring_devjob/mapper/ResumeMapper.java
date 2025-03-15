package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.Resume;

@Mapper(componentModel = "spring")
public interface ResumeMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    Resume toResume(ResumeRequest request);

    ResumeResponse toResumeResponse(Resume resume);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateResume(@MappingTarget Resume resume, ResumeRequest request);
}
