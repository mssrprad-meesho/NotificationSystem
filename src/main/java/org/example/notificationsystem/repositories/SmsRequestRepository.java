package org.example.notificationsystem.repositories;

import java.util.List;
import java.util.Optional;

import org.example.notificationsystem.models.SmsRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, Long> {
    List<SmsRequest> findByStatus(Integer status);
    List<SmsRequest> findAll();
    Optional<SmsRequest> findById(Long id);
}