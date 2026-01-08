package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setEmail("engineer@example.com");
        sampleUser.setFullName("John Engineer");
        sampleUser.setRole("ENGINEER");
        sampleUser.setActive(true);
        sampleUser.setVersion(1);
        sampleUser.setChangeVersion(1L);
        sampleUser.setCreatedAt(OffsetDateTime.now());
        sampleUser.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void findAll_returnsMappedList() {
        when(userRepository.findAll(Sort.by("id"))).thenReturn(List.of(sampleUser));

        var result = userService.findAll();

        assertThat(result).hasSize(1);
        var dto = result.get(0);
        assertThat(dto.getEmail()).isEqualTo("engineer@example.com");
        verify(userRepository).findAll(Sort.by("id"));
    }

    @Test
    void get_returnsUserDTO_whenFound() {
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        var result = userService.get(sampleUser.getId());

        assertThat(result.getEmail()).isEqualTo("engineer@example.com");
        verify(userRepository).findById(sampleUser.getId());
    }

    @Test
    void get_throwsNotFound_whenMissing() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.get(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void existsByEmail_returnsTrueWhenFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        assertThat(userService.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalseWhenMissing() {
        when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());
        assertThat(userService.existsByEmail("none@example.com")).isFalse();
    }

    @Test
    void create_savesUserAndReturnsId() {
        UserDTO dto = new UserDTO();
        dto.setEmail("New@User.com");
        dto.setFullName("New User");
        dto.setRole("ENGINEER");
        dto.setActive(true);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UUID id = userService.create(dto);

        assertThat(id).isNotNull();
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("new@user.com")
                        && u.getFullName().equals("New User")
                        && u.getRole().equals("ENGINEER")
        ));
    }

    @Test
    void update_updatesExistingUser() {
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        UserDTO dto = new UserDTO();
        dto.setEmail("Updated@Example.com");
        dto.setFullName("Updated Name");
        dto.setRole("ADMIN");
        dto.setActive(false);

        userService.update(sampleUser.getId(), dto);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("updated@example.com")
                        && u.getRole().equals("ADMIN")
                        && !u.getActive()
        ));
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.update(UUID.randomUUID(), new UserDTO()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_publishesEventAndDeletes() {
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        userService.delete(sampleUser.getId());

        InOrder order = inOrder(publisher, userRepository);
        order.verify(publisher).publishEvent(isA(BeforeDeleteUser.class));
        order.verify(userRepository).delete(sampleUser);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.delete(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
        verify(publisher, never()).publishEvent(any());
        verify(userRepository, never()).delete(any());
    }
}