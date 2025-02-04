package org.example.notificationsystem;

import org.example.notificationsystem.constants.RedisConstants;
import org.example.notificationsystem.services.impl.BlacklistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private BlacklistServiceImpl blacklistService;


    private String[] numbers;

    @BeforeEach
    void setUp() {
        numbers = new String[]{
                "+911111111111",
                "+911111111112",
                "+911111111113",
                "+911111111114",
                "+911111111115",
                "+911111111116",
                "+911111111117",
                "+911111111118",
                "+911111111119"};
    }

    @Test
    void addNumbersToBlacklist() {
        // Mocking
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add(RedisConstants.blacklisted_key, numbers))
                .thenReturn(1L);

        boolean result = blacklistService.addNumbersToBlacklist(numbers);
        assertTrue(result);
    }

    @Test
    void removeNumbersFromBlacklist() {
        // Mocking
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.remove(RedisConstants.blacklisted_key, numbers))
                .thenReturn(1L);

        boolean result = blacklistService.removeNumbersFromBlacklist(numbers);
        assertTrue(result);
    }

    @Test
    void getAllBlacklistedNumbers() {
        // Mocking
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(RedisConstants.blacklisted_key))
                .thenReturn(Arrays.stream(numbers).collect(Collectors.toSet()));

        Set<String> result = blacklistService.getAllBlacklistedNumbers();
        assertTrue(result.containsAll(Arrays.asList(numbers)));
        assertEquals(result.size(), numbers.length);
    }

    @Test
    void isNumberBlacklisted() {
        // Mocking
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // When number is blacklisted
        when(setOperations.isMember(RedisConstants.blacklisted_key, "+911111111111"))
                .thenReturn(true);

        boolean result = blacklistService.isNumberBlacklisted("+911111111111");
        assertTrue(result);

        // When number is NOT blacklisted
        when(setOperations.isMember(RedisConstants.blacklisted_key, "+911111111112"))
                .thenReturn(false);

        result = blacklistService.isNumberBlacklisted("+911111111112");
        assertFalse(result);
    }
}
