package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.utils.CustomUserUtil;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil userUtil;

    @Mock
    private UserDetailsProjection projection1;

    @Mock
    private UserDetailsProjection projection2;

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {

        UserEntity user = new UserEntity();
        user.setUsername("maria");

        when(userUtil.getLoggedUsername()).thenReturn("maria");
        when(repository.findByUsername("maria"))
                .thenReturn(Optional.of(user));

        UserEntity result = service.authenticated();

        assertNotNull(result);
        assertEquals("maria", result.getUsername());
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        when(userUtil.getLoggedUsername()).thenReturn("maria");
        when(repository.findByUsername("maria"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.authenticated());
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

        when(projection1.getUsername()).thenReturn("maria");
        when(projection1.getPassword()).thenReturn("123456");
        when(projection1.getRoleId()).thenReturn(1L);
        when(projection1.getAuthority()).thenReturn("ROLE_MEMBER");
        when(projection2.getRoleId()).thenReturn(2L);
        when(projection2.getAuthority()).thenReturn("ROLE_VISITOR");

        when(repository.searchUserAndRolesByUsername("maria"))
                .thenReturn(Arrays.asList(projection1, projection2));

        UserDetails result = service.loadUserByUsername("maria");

        assertNotNull(result);
        assertEquals("maria", result.getUsername());
        assertEquals("123456", result.getPassword());

        UserEntity user = (UserEntity) result;
        assertEquals(2, user.getRoles().size());
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        when(repository.searchUserAndRolesByUsername(anyString()))
                .thenReturn(Collections.emptyList());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown"));
    }
}