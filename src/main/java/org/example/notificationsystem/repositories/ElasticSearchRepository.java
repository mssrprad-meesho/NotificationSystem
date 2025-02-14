package org.example.notificationsystem.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.example.notificationsystem.constants.ElasticsearchConstants.indexName;

/**
 * Elasticsearch repository for interacting with Sms Requests stored in Elasticsearch.
 */
@Repository
public class ElasticSearchRepository {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchRepository.class);
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private final SearchRequest matchAllSearchRequest;
    private final SearchRequest matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest;

    @Autowired
    public ElasticSearchRepository(RestHighLevelClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();

        // matchAllSearchRequest
        this.matchAllSearchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(10000);
        this.matchAllSearchRequest.source(searchSourceBuilder1);

        // matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest
        this.matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest = new SearchRequest(indexName);
    }

    /**
     * Returns all SMS requests from Elasticsearch.
     *
     * @return A list of {@link SmsRequestElasticsearch} objects.
     */
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch() {
        List<SmsRequestElasticsearch> results = new ArrayList<>();
        try {
            SearchResponse searchResponse = client.search(matchAllSearchRequest, RequestOptions.DEFAULT);
            logSearchResponseMetadata(searchResponse);

            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Optional<SmsRequestElasticsearch> optionalParsed = hitToSmsRequest(hit);
                optionalParsed.ifPresent(results::add);
            }
        } catch (IOException e) {
            logger.error("Error while searching for sms requests in Elasticsearch: ", e);
        }
        return results;
    }

    /**
     * Returns SMS requests with createdAt between specified dates and message containing terms and optional phone number.
     *
     * @param from   The start date for querying.
     * @param to     The end date for querying.
     * @param number The optional phone number to query.
     * @param terms  The list of terms to match in the message.
     * @return A list of {@link SmsRequestElasticsearch} objects.
     */
    public List<SmsRequestElasticsearch> getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequest(Date from, Date to, Optional<String> number, List<String> terms, String substr) {
        List<SmsRequestElasticsearch> results = new ArrayList<>();
        BoolQueryBuilder boolQuery = getBoolQuery(from, to, number, terms, substr);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(0)
                .size(10000);

        return searchAndGetSmsRequestsElasticSearch(results, searchSourceBuilder);
    }

    /**
     * Returns SMS requests based on a query, with pagination.
     *
     * @param from   The start date for querying.
     * @param to     The end date for querying.
     * @param number The optional phone number to query.
     * @param terms  The list of terms to match in the message.
     * @param page   The page number.
     * @param size   The size of each page.
     * @return A list of {@link SmsRequestElasticsearch} objects.
     */
    public List<SmsRequestElasticsearch> getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequestPageSize(Date from, Date to, Optional<String> number, List<String> terms, int page, int size, String substr) {
        List<SmsRequestElasticsearch> results = new ArrayList<>();
        BoolQueryBuilder boolQuery = getBoolQuery(from, to, number, terms, substr);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(page * size)
                .size(size);

        return searchAndGetSmsRequestsElasticSearch(results, searchSourceBuilder);
    }

    private List<SmsRequestElasticsearch> searchAndGetSmsRequestsElasticSearch(List<SmsRequestElasticsearch> results, SearchSourceBuilder searchSourceBuilder) {
        matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(this.matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Optional<SmsRequestElasticsearch> optionalParsed = hitToSmsRequest(hit);
                optionalParsed.ifPresent(results::add);
            }
        } catch (IOException e) {
            return results;
        }
        return results;
    }

    private BoolQueryBuilder getBoolQuery(Date from, Date to, Optional<String> number, List<String> terms, String substr) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("createdAt")
                        .gte(NotificationSystemUtils.DateToElasticSearchTimestamp(from))
                        .lte(NotificationSystemUtils.DateToElasticSearchTimestamp(to))
                );

        number.ifPresent(num -> boolQuery.must(QueryBuilders.matchQuery("phone_number", num)));
        terms.forEach(term -> boolQuery.filter(QueryBuilders.matchQuery("message", term)));

        logger.info("Substring must be: {}", substr);
        if (substr != null) {
            boolQuery.filter(QueryBuilders.wildcardQuery("message", substr));
        }

        return boolQuery;
    }

    private Optional<SmsRequestElasticsearch> hitToSmsRequest(SearchHit hit) {
        try {
            SmsRequestElasticsearch smsRequestElasticsearch = objectMapper.readValue(hit.getSourceAsString(), SmsRequestElasticsearch.class);
            if (smsRequestElasticsearch != null) smsRequestElasticsearch.setId(hit.getId());
            return Optional.ofNullable(smsRequestElasticsearch);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse document (id={}) into SmsRequestElasticsearch: {}", hit.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    private void logSearchResponseMetadata(SearchResponse searchResponse) {
        logger.info("Response status: {}", searchResponse.status());
        logger.info("Total time: {} ms", searchResponse.getTook().millis());
        logger.info("Terminated early: {}", searchResponse.isTerminatedEarly());
        logger.info("Timed out: {}", searchResponse.isTimedOut());
        logger.info("Total shards: {}", searchResponse.getTotalShards());
        logger.info("Successful shards: {}", searchResponse.getSuccessfulShards());
        logger.info("Failed shards: {}", searchResponse.getFailedShards());

        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        if (totalHits == null) return;
        logger.info("Total hits: {}", totalHits.value);
        logger.info("Hits relation: {}", totalHits.relation);
        logger.info("Max score: {}", hits.getMaxScore());
    }
}