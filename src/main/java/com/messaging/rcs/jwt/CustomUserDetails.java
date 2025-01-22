package com.messaging.rcs.jwt;


import com.messaging.rcs.domain.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Rahul 2023-04-10
 *
 */
public class CustomUserDetails implements UserDetails {

    private String userName;
    private String password;
    private boolean active;
    private Long userId;
    private boolean multipleLoginAllowed;
    private List<GrantedAuthority> authorities;

    public CustomUserDetails(UserEntity userEntity) {
        this.userName = userEntity.getUserName();
        this.password = userEntity.getUserPassword();
        this.active = userEntity.getActive().equalsIgnoreCase("Y")  ? true : false;
        this.authorities = Arrays.asList( new SimpleGrantedAuthority("ROLE_USER"));
        this.userId = userEntity.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isMultipleLoginAllowed() {
        return multipleLoginAllowed;
    }
}
