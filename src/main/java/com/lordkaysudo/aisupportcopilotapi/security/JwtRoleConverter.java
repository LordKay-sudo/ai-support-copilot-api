package com.lordkaysudo.aisupportcopilotapi.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object claim = jwt.getClaim("roles");
        if (claim == null) {
            return List.of();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (claim instanceof Collection<?> raw) {
            for (Object element : raw) {
                if (element != null) {
                    authorities.add(toAuthority(element.toString()));
                }
            }
        } else if (claim instanceof String single) {
            authorities.add(toAuthority(single));
        }
        return authorities;
    }

    private static SimpleGrantedAuthority toAuthority(String role) {
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(normalized);
        }
        return new SimpleGrantedAuthority("ROLE_" + normalized);
    }
}
