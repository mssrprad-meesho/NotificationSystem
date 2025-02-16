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

    // Constructor Injection
    private final RestHighLevelClient client;

    // Useful fields
    private final ObjectMapper objectMapper;
    private final SearchRequest matchAllSearchRequest;
    private final SearchRequest matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest;

    @Autowired
    public ElasticSearchRepository(RestHighLevelClient client) {
        // Initialize Elasticsearch client and JSON object mapper.
        this.client = client;
        this.objectMapper = new ObjectMapper();

        // Initialize fields
        this.matchAllSearchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(10000);

        this.matchAllSearchRequest.source(searchSourceBuilder1);
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
            // Execute the match-all search query.
            SearchResponse searchResponse = client.search(matchAllSearchRequest, RequestOptions.DEFAULT);

            // Log stuff
            logSearchResponseMetadata(searchResponse);

            // Handle the hits
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Optional<SmsRequestElasticsearch> optionalParsed = hitToSmsRequest(hit);
                optionalParsed.ifPresent(results::add);
            }
        } catch (IOException e) {
            // Log error if search operation fails.
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
    public List<SmsRequestElasticsearch> getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequest(Date from, Date to, Optional<String> number, List<String> terms) {
        List<SmsRequestElasticsearch> results = new ArrayList<>();
        // Build boolean query with date range, phone number (if provided), and message terms.
        BoolQueryBuilder boolQuery = getBoolQuery(from, to, number, terms);

        // Construct search source with the built query, set pagination parameters.
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(0)
                .size(10000);

        // Execute search and parse results.
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
    public List<SmsRequestElasticsearch> getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequestPageSize(Date from, Date to, Optional<String> number, List<String> terms, int page, int size) {
        List<SmsRequestElasticsearch> results = new ArrayList<>();
        // Build boolean query using provided date range, phone number, and message terms.
        BoolQueryBuilder boolQuery = getBoolQuery(from, to, number, terms);

        // Configure search source for pagination: calculate offset based on page number and size.
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(page * size)
                .size(size);

        // Execute search query and return the parsed SMS requests.
        return searchAndGetSmsRequestsElasticSearch(results, searchSourceBuilder);
    }

    /**
     * Executes the given search query and parses the results into a list of SmsRequestElasticsearch objects.
     *
     * @param results             The list to accumulate parsed results.
     * @param searchSourceBuilder The search source builder containing the query and pagination settings.
     * @return A list of parsed {@link SmsRequestElasticsearch} objects.
     */
    private List<SmsRequestElasticsearch> searchAndGetSmsRequestsElasticSearch(List<SmsRequestElasticsearch> results, SearchSourceBuilder searchSourceBuilder) {
        // Set the search source for the pre-configured filtered search request.
        matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest.source(searchSourceBuilder);
        try {
            // Execute the search request.
            SearchResponse searchResponse = client.search(
                    this.matchCreatedAtIsBetweenAndMessageContainingAndPhoneNumberSearchRequest,
                    RequestOptions.DEFAULT
            );
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                // Convert each search hit to SmsRequestElasticsearch and add to results.
                Optional<SmsRequestElasticsearch> optionalParsed = hitToSmsRequest(hit);
                optionalParsed.ifPresent(results::add);
            }
        } catch (IOException e) {
            // If an exception occurs, return the results accumulated so far.
            return results;
        }
        return results;
    }

    /**
     * Constructs a BoolQueryBuilder to filter SMS requests by createdAt date range,
     * optional phone number, and message containing the specific phrases.
     *
     * @param from   The start date.
     * @param to     The end date.
     * @param number The optional phone number.
     * @param terms  The list of message phrases.
     * @return A {@link BoolQueryBuilder} representing the constructed query.
     */
    private BoolQueryBuilder getBoolQuery(Date from, Date to, Optional<String> number, List<String> terms) {
        // Create a BoolQuery with a range filter on 'createdAt' field.
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("createdAt")
                        .gte(NotificationSystemUtils.DateToElasticSearchTimestamp(from))
                        .lte(NotificationSystemUtils.DateToElasticSearchTimestamp(to))
                );

        // If a phone number is provided
        number.ifPresent(num -> boolQuery.must(QueryBuilders.matchQuery("phone_number", num)));

        // Filter by each term in Message Containing
        terms.forEach(term -> boolQuery.filter(QueryBuilders.matchPhraseQuery("message", term)));

        return boolQuery;
    }

    /**
     * Converts a SearchHit from Elasticsearch into a SmsRequestElasticsearch object.
     *
     * @param hit The search hit to convert.
     * @return An optional {@link SmsRequestElasticsearch} containing the parsed SmsRequestElasticsearch object if successful, or empty if parsing fails.
     */
    private Optional<SmsRequestElasticsearch> hitToSmsRequest(SearchHit hit) {
        try {
            // Deserialize the hit body into SmsRequestElasticsearch object.
            SmsRequestElasticsearch smsRequestElasticsearch = objectMapper.readValue(hit.getSourceAsString(), SmsRequestElasticsearch.class);

            // Handle null
            if (smsRequestElasticsearch != null) smsRequestElasticsearch.setId(hit.getId());
            return Optional.ofNullable(smsRequestElasticsearch);
        } catch (JsonProcessingException e) {
            // Log warning if JSON parsing fails.
            logger.warn("Failed to parse document (id={}) into SmsRequestElasticsearch: {}", hit.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Logs metadata and other information from the Elasticsearch search response.
     *
     * @param searchResponse The search response received from the Elasticsearch query.
     */
    private void logSearchResponseMetadata(SearchResponse searchResponse) {
        // Log Elasticsearch Stuff....
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
