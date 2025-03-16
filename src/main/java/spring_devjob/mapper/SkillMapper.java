package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.JobHasSkill;
import spring_devjob.entity.Skill;
import spring_devjob.entity.SubHasSkill;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toSkill(SkillRequest request);

    SkillResponse toSkillResponse(Skill skill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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
