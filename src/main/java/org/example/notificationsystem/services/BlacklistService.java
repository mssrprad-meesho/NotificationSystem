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
        List<PhoneNumber> phoneNumberList = this.phoneNumberRepository.findByPhoneNumberIn(numbers);
        phoneNumberList.forEach(phoneNumber -> {
            phoneNumber.setBlackListed(true);
            this.template.opsForValue().set(
                    RedisConstants.STRING_KEY_PREFIX + phoneNumber.getPhoneNumber(), Boolean.TRUE
            );
        });
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
