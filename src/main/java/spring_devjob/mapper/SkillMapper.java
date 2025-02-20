package spring_devjob.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.Skill;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toSkill(SkillRequest request);

    SkillResponse toSkillResponse(Skill skill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSkill(@MappingTarget Skill skill, SkillRequest request);
}
