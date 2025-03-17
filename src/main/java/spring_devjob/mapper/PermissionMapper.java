package spring_devjob.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.entity.Permission;
import spring_devjob.entity.relationship.RoleHasPermission;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toPermissionResponse(Permission permission);

    List<PermissionResponse> toPermissionResponseList(List<Permission> permissions);

    @Mapping(target = "id", source = "roleHasPermission.permission.id")
    @Mapping(target = "name", source = "roleHasPermission.permission.name")
    @Named("roleHasPermissionToEntityBasic")
    EntityBasic roleHasPermissionToEntityBasic(RoleHasPermission roleHasPermission);

}
