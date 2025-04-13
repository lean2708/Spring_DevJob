package spring_devjob.service;

import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.SkillResponse;

import java.util.Set;

public interface SkillService {

    SkillResponse create(SkillRequest request);

    SkillResponse fetchSkillById(long id);

    PageResponse<SkillResponse> fetchAllSkills(int pageNo, int pageSize, String sortBy);

    SkillResponse update(long id, SkillRequest request);

    void delete(long id);

    void deleteSkills(Set<Long> ids);
}
