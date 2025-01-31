package org.example.notificationsystem.services;

import java.util.Set;

public interface BlacklistService {
    Boolean isNumberBlacklisted(String number);
    Set<String> getAllBlacklistedNumbers();
    void addNumbersToBlacklist(String[] numbers);
    void removeNumbersFromBlacklist(String[] numbers);
}

