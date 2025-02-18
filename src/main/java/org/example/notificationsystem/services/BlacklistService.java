package org.example.notificationsystem.services;

import java.util.Set;

/**
 * Service interface for managing blacklisted phone numbers.
 */
public interface BlacklistService {

    /**
     * Checks if the given phone number is blacklisted.
     *
     * @param number The phone number to check.
     * @return True if the number is blacklisted, false otherwise.
     */
    Boolean isNumberBlacklisted(String number);

    /**
     * Retrieves all blacklisted phone numbers.
     *
     * @return A set of blacklisted phone numbers.
     */
    Set<String> getAllBlacklistedNumbers();

    /**
     * Adds a list of phone numbers to the blacklist.
     *
     * @param numbers The phone numbers to add to the blacklist.
     * @return True if the numbers were successfully added, false otherwise.
     */
    boolean addNumbersToBlacklist(String[] numbers);

    /**
     * Removes a list of phone numbers from the blacklist.
     *
     * @param numbers The phone numbers to remove from the blacklist.
     * @return True if the numbers were successfully removed, false otherwise.
     */
    boolean removeNumbersFromBlacklist(String[] numbers);
}