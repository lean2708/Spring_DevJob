package spring_devjob.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import spring_devjob.dto.basic.PermissionBasic;
import spring_devjob.dto.basic.SkillBasic;
import spring_devjob.dto.request.PermissionRequest;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Skill;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);

    List<PermissionBasic> toPermissionBasic(List<Permission> permissions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermission(@MappingTarget Permission permission, PermissionRequest request);
}
