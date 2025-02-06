package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


/**
 * Default JPA Repository for interacting with the Elasticsearch index for Sms Requests.
 * */
@Repository
public interface SmsRequestElasticsearchRepository extends ElasticsearchRepository<SmsRequestElasticsearch, String> {

}
