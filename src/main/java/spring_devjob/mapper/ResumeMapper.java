package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.entity.Resume;
import spring_devjob.entity.Review;
import spring_devjob.entity.history.ResumeHistory;

import java.util.List;

@Mapper(componentModel = "spring", uses = {JobMapper.class})
public interface ResumeMapper {
    @Mapping(target = "user", ignore = true)
    Resume toResume(ResumeRequest request);

    @Mapping(target = "jobs", source = "jobs", qualifiedByName = "jobHasResumeToJobBasicSet")
    ResumeResponse toResumeResponse(Resume resume);

    List<ResumeResponse> toResumeResponseList(List<Resume> resumes);

    ResumeHistory toResumeHistory(Resume resume);

    List<ResumeHistory> toResumeHistoryList(List<Resume> resumes);

    @Mapping(target = "user", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateResume(@MappingTarget Resume resume, ResumeRequest request);
}
