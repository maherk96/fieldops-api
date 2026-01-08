package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.config.CacheConfig;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {
                UserService.class,
                CacheConfig.class,
                UserServiceCachingIntegrationTest.TestConfig.class
        })
@Import(CacheConfig.class)
class UserServiceCachingIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void reset() {
        cacheManager.getCacheNames().forEach(
                name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
        Mockito.reset(userRepository);
    }

    @Test
    void get_shouldUseCache_afterFirstInvocation() {
        User user = sampleUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // First call → DB hit
        var first = userService.get(userId);

        // Second call → cache hit
        var second = userService.get(userId);

        assertThat(second.getEmail()).isEqualTo("cache@example.com");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findAll_shouldCacheResult() {
        when(userRepository.findAll(any(Sort.class))).thenReturn(java.util.List.of(sampleUser()));

        userService.findAll();
        userService.findAll();

        verify(userRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    void existsByEmail_shouldCacheBooleanResult() {
        when(userRepository.findByEmail("cache@example.com"))
                .thenReturn(Optional.of(sampleUser()));

        boolean first = userService.existsByEmail("cache@example.com");
        boolean second = userService.existsByEmail("cache@example.com");

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        verify(userRepository, times(1)).findByEmail("cache@example.com");
    }

    @Test
    void create_shouldEvictCaches() {
        UserDTO dto = new UserDTO();
        dto.setEmail("new@example.com");
        dto.setFullName("New User");
        dto.setRole("ENGINEER");
        dto.setActive(true);

        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        userService.create(dto);

        cacheManager.getCacheNames().forEach(name -> {
            var cache = Objects.requireNonNull(cacheManager.getCache(name));
            Object nativeCache = cache.getNativeCache();
            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
                assertThat(caffeineCache.asMap())
                        .as(name + " cache")
                        .isEmpty();
            }
        });
    }

    private User sampleUser() {
        User user = new User();
        user.setId(userId);
        user.setEmail("cache@example.com");
        user.setFullName("Cached User");
        user.setRole("ENGINEER");
        user.setActive(true);
        user.setVersion(1);
        user.setChangeVersion(1L);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        ApplicationEventPublisher applicationEventPublisher() {
            return Mockito.mock(ApplicationEventPublisher.class);
        }
    }
}
