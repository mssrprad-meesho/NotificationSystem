package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.RedisConstants;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlacklistService {

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private RedisTemplate<String, Boolean> template;

    @Transactional
    public Boolean isNumberBlacklisted(String number) {
        // Get from Redis first
        Boolean blacklisted = this.template.opsForValue().get(number);

        // If number not in Redis, make DB Query and set in Redis
        if (blacklisted == null) {
            Optional<PhoneNumber> phoneNumber = this.phoneNumberRepository.findByPhoneNumber(number);
            if (phoneNumber.isPresent()) {
                blacklisted = phoneNumber.get().getBlackListed();
                this.template.opsForValue().set(number, blacklisted);
                return blacklisted;
            } else {
                // Shouldn't happen ig
                return false;
            }
        } else {
            return blacklisted;
        }
    }

    @Transactional
    public List<String> getAllBlacklistedNumbers() {
        return this.phoneNumberRepository.findPhoneNumberStringsByBlackListed(true);
    }

    @Transactional
    public void addNumbersToBlacklist(List<String> numbers) {

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
        this.phoneNumberRepository.saveAll(newPhoneNumbers);
        this.phoneNumberRepository.saveAllAndFlush(phoneNumberList);
    }

    @Transactional
    public void removeNumbersFromBlacklist(List<String> numbers) {
        List<PhoneNumber> phoneNumberList = this.phoneNumberRepository.findByPhoneNumberIn(numbers);
        phoneNumberList.forEach(phoneNumber -> {
            phoneNumber.setBlackListed(false);
            this.template.opsForValue().set(
                    RedisConstants.STRING_KEY_PREFIX + phoneNumber.getPhoneNumber(), Boolean.FALSE
            );
        });
        this.phoneNumberRepository.saveAllAndFlush(phoneNumberList);
    }
}
