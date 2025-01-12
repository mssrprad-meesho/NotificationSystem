package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
    Optional<PhoneNumber> findByPhoneNumber(String phoneNumber);
}
