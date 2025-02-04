package org.example.notificationsystem.services;

import java.util.Set;

public interface BlacklistService {
    Boolean isNumberBlacklisted(String number);
    Set<String> getAllBlacklistedNumbers();
    boolean addNumbersToBlacklist(String[] numbers);
    boolean removeNumbersFromBlacklist(String[] numbers);
}

