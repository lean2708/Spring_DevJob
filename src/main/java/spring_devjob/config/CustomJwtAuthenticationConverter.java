package spring_devjob.config;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email  = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Permission> permissionSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(user.getRoles())){
            Set<Long> roleIds = user.getRoles().stream()
                    .map(UserHasRole::getRole)
                    .map(Role::getId)
                    .collect(Collectors.toSet());

            permissionSet = permissionRepository.findAllByRoleIds(roleIds);
        }

        List<GrantedAuthority> authorities = permissionSet.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
