package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.RedisConstants;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing blacklisted phone numbers.
 * It interacts with both the Redis cache and the database to check and modify
 * blacklisted status for phone numbers.
 */
@Service
public class BlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistService.class);
    private final PhoneNumberRepository phoneNumberRepository;
    private final RedisTemplate<String, Boolean> template;

    /**
     * Constructor for BlacklistService.
     *
     * @param phoneNumberRepository Repository to interact with phone number data in the database.
     * @param template RedisTemplate to interact with Redis for caching blacklisted phone numbers.
     */
    public BlacklistService(PhoneNumberRepository phoneNumberRepository, RedisTemplate<String, Boolean> template) {
        this.phoneNumberRepository = phoneNumberRepository;
        this.template = template;
    }

    /**
     * Checks if a given phone number is blacklisted.
     * First checks Redis cache; if not found, queries the database and updates the cache.
     *
     * @param number The phone number to check.
     * @return True if the number is blacklisted, otherwise false.
     */
    @Transactional
    public Boolean isNumberBlacklisted(String number) {
        logger.info("Checking if number {} is blacklisted", number);

        // Get from Redis first
        Boolean blacklisted = this.template.opsForValue().get(number);

        // If number not in Redis, make DB Query and set in Redis
        if (blacklisted == null) {
            logger.info("Number {} not found in Redis, querying database", number);
            Optional<PhoneNumber> phoneNumber = this.phoneNumberRepository.findByPhoneNumber(number);
            if (phoneNumber.isPresent()) {
                blacklisted = phoneNumber.get().getBlackListed();
                this.template.opsForValue().set(number, blacklisted);
                logger.info("Number {} found in DB, blacklisted status: {}", number, blacklisted);
                return blacklisted;
            } else { // Shouldn't happen usually
                logger.warn("Number {} not found in database", number);
                return false;
            }
        } else {
            logger.info("Number {} found in Redis, blacklisted status: {}", number, blacklisted);
            return blacklisted;
        }
    }

    /**
     * Retrieves a list of all blacklisted phone numbers from the database.
     *
     * @return A list of blacklisted phone numbers as strings.
     */
    @Transactional
    public List<String> getAllBlacklistedNumbers() {
        logger.info("Fetching all blacklisted numbers from DB");
        List<String> blacklistedNumbers = this.phoneNumberRepository.findPhoneNumberStringsByBlackListed(true);
        logger.info("Fetched {} blacklisted numbers", blacklistedNumbers.size());
        return blacklistedNumbers;
    }

    /**
     * Adds a list of phone numbers to the blacklist.
     * Updates the database and Redis cache for each phone number.
     *
     * @param numbers A list of phone numbers to be added to the blacklist.
     */
    @Transactional
    public void addNumbersToBlacklist(List<String> numbers) {
        logger.info("Adding numbers to blacklist: {}", numbers);

        // Get existing numbers from table
        List<PhoneNumber> phoneNumberList = this.phoneNumberRepository.findByPhoneNumberIn(numbers);
        Set<String> existingNumbers = phoneNumberList.stream()
                .map(PhoneNumber::getPhoneNumber)
                .collect(Collectors.toSet());

        // Update their entries to set blacklisted=True
        phoneNumberList.forEach(phoneNumber -> {
            phoneNumber.setBlackListed(true);
            this.template.opsForValue().set(
                    RedisConstants.STRING_KEY_PREFIX + phoneNumber.getPhoneNumber(), Boolean.TRUE
            );
            logger.info("Updated number {} to be blacklisted", phoneNumber.getPhoneNumber());
        });

        // Which of numbers are new? Create them now (with blacklisted=True)
        List<PhoneNumber> newPhoneNumbers = numbers.stream()
                .filter(number -> !existingNumbers.contains(number))
                .map(number -> {
                    PhoneNumber phoneNumber = new PhoneNumber();
                    phoneNumber.setPhoneNumber(number);
                    phoneNumber.setBlackListed(true);
                    return phoneNumber;
                })
                .collect(Collectors.toList());

        // Save and Flush all changes
        this.phoneNumberRepository.saveAllAndFlush(newPhoneNumbers);
        this.phoneNumberRepository.saveAllAndFlush(phoneNumberList);

        logger.info("Successfully added {} new numbers to blacklist", newPhoneNumbers.size());
    }

    /**
     * Removes a list of phone numbers from the blacklist.
     * Updates the database and Redis cache for each phone number.
     *
     * @param numbers A list of phone numbers to be removed from the blacklist.
     */
    @Transactional
    public void removeNumbersFromBlacklist(List<String> numbers) {
        logger.info("Removing numbers to blacklist: {}", numbers);

        // Get existing numbers from table
        List<PhoneNumber> phoneNumberList = this.phoneNumberRepository.findByPhoneNumberIn(numbers);
        Set<String> existingNumbers = phoneNumberList.stream()
                .map(PhoneNumber::getPhoneNumber)
                .collect(Collectors.toSet());

        // Update their entries to set blacklisted=True
        phoneNumberList.forEach(phoneNumber -> {
            phoneNumber.setBlackListed(true);
            this.template.opsForValue().set(
                    RedisConstants.STRING_KEY_PREFIX + phoneNumber.getPhoneNumber(), Boolean.FALSE
            );
            logger.info("Updated number {} to not be blacklisted", phoneNumber.getPhoneNumber());
        });

        // Which of numbers are new? Create them now (with blacklisted=True)
        List<PhoneNumber> newPhoneNumbers = numbers.stream()
                .filter(number -> !existingNumbers.contains(number))
                .map(number -> {
                    PhoneNumber phoneNumber = new PhoneNumber();
                    phoneNumber.setPhoneNumber(number);
                    phoneNumber.setBlackListed(false);
                    return phoneNumber;
                })
                .collect(Collectors.toList());

        // Save and Flush all changes
        this.phoneNumberRepository.saveAllAndFlush(newPhoneNumbers);
        this.phoneNumberRepository.saveAllAndFlush(phoneNumberList);

        logger.info("Successfully removed {} new numbers to blacklist", newPhoneNumbers.size());
    }
}
