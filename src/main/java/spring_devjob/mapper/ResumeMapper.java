package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.entity.Resume;
import spring_devjob.entity.Review;
import spring_devjob.entity.history.ResumeHistory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResumeMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    Resume toResume(ResumeRequest request);

    ResumeResponse toResumeResponse(Resume resume);

    List<ResumeResponse> toResumeResponseList(List<Resume> resumes);

    ResumeHistory toResumeHistory(Resume resume);

    List<ResumeHistory> toResumeHistoryList(List<Resume> resumes);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateResume(@MappingTarget Resume resume, ResumeRequest request);
}
