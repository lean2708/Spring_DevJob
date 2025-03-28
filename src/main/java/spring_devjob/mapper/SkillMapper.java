package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.entity.Skill;
import spring_devjob.entity.relationship.SubHasSkill;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface SkillMapper {

    Skill toSkill(SkillRequest request);

    SkillResponse toSkillResponse(Skill skill);

    List<SkillResponse> toSkillResponseList(List<Skill> skills);

    void updateSkill(@MappingTarget Skill skill, SkillRequest request);

    @Mapping(target = "id", source = "subHasSkill.skill.id")
    @Mapping(target = "name", source = "subHasSkill.skill.name")
    @Named("subHasSkillToEntityBasic")
    EntityBasic subHasSkillToEntityBasic(SubHasSkill subHasSkill);

    @Mapping(target = "id", source = "jobHasSkill.skill.id")
    @Mapping(target = "name", source = "jobHasSkill.skill.name")
    @Named("jobHasSkillToEntityBasic")
    EntityBasic jobHasSkillToEntityBasic(JobHasSkill jobHasSkill);
}
