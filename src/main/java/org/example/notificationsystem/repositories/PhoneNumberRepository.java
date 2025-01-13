package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
    Optional<PhoneNumber> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT phone_number FROM phone_number WHERE black_listed = :blacklisted", nativeQuery = true)
    List<String> findPhoneNumberStringsByBlackListed(@Param("blacklisted") boolean blacklisted);

    List<PhoneNumber> findByPhoneNumberIn(List<String> numbers);
}
