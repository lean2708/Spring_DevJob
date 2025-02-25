package spring_devjob.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.basic.SkillBasic;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.Company;
import spring_devjob.entity.Role;
import spring_devjob.entity.Skill;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toSkill(SkillRequest request);

    List<SkillBasic> toSkillBasics(List<Skill> skills);

    SkillResponse toSkillResponse(Skill skill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSkill(@MappingTarget Skill skill, SkillRequest request);
}
