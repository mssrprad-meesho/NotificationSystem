package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SmsRequestElasticsearchRepository extends ElasticsearchRepository<SmsRequestElasticsearch, String> {
    List<SmsRequestElasticsearch> findByPhoneNumberAndCreatedAtIsBetween(String phoneNumber, Date from, Date to, Pageable pageable);
    List<SmsRequestElasticsearch> findByPhoneNumberAndMessageMatchesRegexIgnoreCaseAndCreatedAtIsBetween(String phoneNumber, String message, Date from, Date to, Pageable pageable);
    List<SmsRequestElasticsearch> findByCreatedAtIsBetween(Date createdAtAfter, Date createdAtBefore, Pageable pageable);
    List<SmsRequestElasticsearch> findByMessageMatchesRegexIgnoreCaseAndCreatedAtIsBetween(String message, Date createdAtAfter, Date createdAtBefore, Pageable pageable);
}
