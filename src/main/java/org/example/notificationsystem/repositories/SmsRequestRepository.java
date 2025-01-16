package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, Long> {
    List<SmsRequest> findByStatus(Integer status);

    List<SmsRequest> findAll();

    Optional<SmsRequest> findById(Long id);

    List<SmsRequestElasticsearch> findByCreatedAtIsBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}