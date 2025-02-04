package org.example.notificationsystem.services.impl;

import org.example.notificationsystem.constants.RedisConstants;
import org.example.notificationsystem.services.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Service
public class BlacklistServiceImpl implements BlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistServiceImpl.class);
    private final RedisTemplate<String, String> redisTemplate;

    public BlacklistServiceImpl(RedisTemplate<String, String> template) {
        this.redisTemplate = template;
    }

    public Boolean isNumberBlacklisted(String number) {
        logger.info("Checking if number {} is blacklisted", number);

        // Get from Redis first
        Boolean blacklistedRedis = this.redisTemplate.opsForSet().isMember(RedisConstants.blacklisted_key, number);

        // If number not in Redis, make DB Query and set in Redis
        if (blacklistedRedis != null) {
            if (blacklistedRedis) {
                logger.info("Number {} is blacklisted.", number);
                return true;
            } else {
                logger.info("Number {} is not blacklisted.", number);
                return false;
            }
        } else {
            logger.info("Redis query for number {} returned null. Returning blacklisted.", number);
            return true;
        }
    }

    public Set<String> getAllBlacklistedNumbers() {
        logger.info("Fetching all blacklisted numbers from DB");
        Set<String> phoneNumbers = this.redisTemplate.opsForSet().members(RedisConstants.blacklisted_key);
        if (phoneNumbers != null) {
            logger.info("Fetched {} blacklisted numbers", phoneNumbers.size());
            return phoneNumbers;
        } else {
            logger.info("Redis query for blacklisted numbers returned null. Returning empty set.");
            return new HashSet<>();
        }
    }

    @Transactional
    public boolean addNumbersToBlacklist(String[] numbers) {
        logger.info("Adding numbers to blacklist: {}", numbers);

        Long res = this.redisTemplate.opsForSet().add(RedisConstants.blacklisted_key, numbers);
        if (res != null) {
            logger.info("Successfully added {} numbers to blacklist", numbers.length);
            return true;
        } else {
            logger.info("Failed to add {} numbers to blacklist", numbers.length);
            return false;
        }
    }

    @Transactional
    public boolean removeNumbersFromBlacklist(String[] numbers) {
        logger.info("Removing numbers to blacklist: {}", numbers);

        Long res = this.redisTemplate.opsForSet().remove(RedisConstants.blacklisted_key, numbers);
        if (res != null) {
            logger.info("Successfully removed {} numbers to blacklist", numbers.length);
            return true;
        } else {
            logger.info("Failed to remove {} numbers to blacklist", numbers.length);
            return false;
        }
    }
}
