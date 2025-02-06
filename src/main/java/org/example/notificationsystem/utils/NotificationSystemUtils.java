package org.example.notificationsystem.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationsystem.constants.ThirdPartyApiResponseCode;
import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.request.ThirdPartySmsApiRequest;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import java.io.OutputStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static org.example.notificationsystem.constants.Time.*;

/**
 * An Utils class to hold some miscellaneous methods used throughout the service.
 *
 * <b>Public Methods: </b>
 * <ul>
 *   <li><b>boolean isValidPageRequest(ElasticSearchRequest query)</b>: Is the ElasticSearchRequest query a pagination query?</li>
 *   <li><b>SmsRequestElasticsearch getSmsRequestElasticsearchFromSmsRequest(SmsRequest smsRequest)</b>: Used to create the SmsRequestElasticSearch object from the SmsRequest object for insertion into the index.</li>
 *   <li><b>ThirdPartyApiResponseCode send2ThirdPartyApi(List<ThirdPartySmsApiRequest> req)</b>: Handles the third party api request and if any error occurred, returns the type of the error.</li>
 *   <li><b>Date getNowAsDateIST()</b>: The current Time as IST.
 *   <li><b>Date parseIstToUtcDate(String dateString)</b>: Convert the readable IST strings in ElasticSearchRequest to a Date object which can be used to filter the ElasticSearch query.
 *   <li><b>String DateToElasticSearchTimestamp(Date date)</b>: Convert a Date to the String representation of the Date as stored in ElasticSearch (basic_date_time).</li>
 *   <li><b>Date ElasticSearchTimestampToDate(String timestamp)</b>: Convert the String representation of the Date as stored in ElasticSearch to a readable Date object.</li>
 * </ul>
 * @author Malladi Pradyumna
 */
public final class NotificationSystemUtils {

    private static final String thirdPartyApiUrl = "https://notification.free.beeceptor.com/resources/v1/messaging";

    private static final Logger logger = LoggerFactory.getLogger(NotificationSystemUtils.class);

    /**
     * Checks if the given ElasticSearchRequest query is a valid pagination request.
     * @param query the ElasticSearchRequest to validate
     * @return true if both page and size are valid, false otherwise
     */
    public static boolean isValidPageRequest(ElasticSearchRequest query) {
        // Check if both page and size are non-null and valid
        return query.getPage() != null
                && query.getSize() != null
                && query.getPage() >= 0
                && query.getSize() > 0;
    }

    /**
     * Converts a SmsRequest to a SmsRequestElasticsearch for indexing.
     * @param smsRequest the SmsRequest to convert
     * @return SmsRequestElasticsearch representation of the given SmsRequest
     */
    public static SmsRequestElasticsearch getSmsRequestElasticsearchFromSmsRequest(SmsRequest smsRequest) {
        SmsRequestElasticsearch smsRequestElasticsearch = new SmsRequestElasticsearch();
        smsRequestElasticsearch.setCreatedAt(smsRequest.getCreatedAt());
        smsRequestElasticsearch.setUpdatedAt(smsRequest.getUpdatedAt());
        smsRequestElasticsearch.setPhoneNumber(smsRequest.getPhoneNumber());
        smsRequestElasticsearch.setMessage(smsRequest.getMessage());
        smsRequestElasticsearch.setSmsRequestId(smsRequest.getId().toString());
        return smsRequestElasticsearch;
    }

    /**
     * Sends a request to the third party API and returns the corresponding response code.
     * @param req list of ThirdPartySmsApiRequest objects to send
     * @return ThirdPartyApiResponseCode indicating the outcome of the API call
     */
    public static ThirdPartyApiResponseCode send2ThirdPartyApi(List<ThirdPartySmsApiRequest> req) {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing request body", e);
            return ThirdPartyApiResponseCode.INVALID_REQUEST_BODY;
        }
        logger.info("Request Body: {}", requestBody);

        HttpURLConnection connection = null;

        try {
            logger.info("Sending request to ThirdParty URL: {}", thirdPartyApiUrl);
            URL url = new URL(thirdPartyApiUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try {
                OutputStream os = connection.getOutputStream();
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            } catch (IOException e) {
                logger.error("Error serializing request body", e);
                return ThirdPartyApiResponseCode.INVALID_REQUEST_BODY;
            }

            int responseCode = connection.getResponseCode();
            logger.info("Response Code: {}", responseCode);

            StringBuilder response = getStringBuilder(responseCode, connection);

            logger.info("Response Body: {}", response.toString());
            if(responseCode == 200) {
                logger.info("Successfully sent request to ThirdParty API");
                return ThirdPartyApiResponseCode.SUCCESS;
            } else {
                logger.error("Request Failed. Received non 200 Response Code: {}", responseCode);
                return ThirdPartyApiResponseCode.API_ERROR;
            }
        } catch (MalformedURLException e) {
            logger.error("Error building URL. Malformed URL.", e);
            return ThirdPartyApiResponseCode.MALFORMED_URL;
        } catch (java.net.SocketTimeoutException e) {
            logger.error("Timeout in sending request to third party api.", e);
            return ThirdPartyApiResponseCode.TIMEOUT;
        }
        catch (IOException e) {
            return ThirdPartyApiResponseCode.API_ERROR;
        }
    }

    /**
     * Reads the response from the connection and returns it as a StringBuilder.
     * Handles whether to read from the error stream or input stream based on the responseCode
     * @param responseCode the HTTP response code
     * @param connection the HttpURLConnection to read from
     * @return StringBuilder containing the response
     * @throws IOException if an I/O error occurs
     */
    private static StringBuilder getStringBuilder(int responseCode, HttpURLConnection connection) throws IOException {
        BufferedReader br = null;
        if (100 <= responseCode && responseCode <= 399) {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String strCurrentLine;
        while ((strCurrentLine = br.readLine()) != null) {
            response.append(strCurrentLine);
        }
        return response;
    }

    /**
     * Gets the current time as a Date object in IST.
     * @return current Date in IST
     */
    public static Date getNowAsDateIST() {
        return Date.from(Instant.now().atZone(INDIA_ZONE_ID).toInstant());
    }

    /**
     * Parses an IST date string into a UTC Date object.
     * @param dateString the date string in IST format
     * @return UTC Date corresponding to the input string
     */
    public static Date parseIstToUtcDate(String dateString) {
        // dateString must be "^(\\d{2})-(\\d{2})-(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})$\n"
        logger.info("Parsing ISO 8601 date: {}", dateString);
        // Parse the input (IST) string into a LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);

        // Convert the LocalDateTime to Date
        ZonedDateTime istZonedDateTime = localDateTime.atZone(INDIA_ZONE_ID);
        ZonedDateTime utcZonedDateTime = istZonedDateTime.withZoneSameInstant(UTC_ZONE_ID);
        return Date.from(utcZonedDateTime.toInstant());
    }

    /**
     * Converts a Date to an Elasticsearch timestamp string.
     * @param date the Date to convert
     * @return timestamp string formatted for Elasticsearch
     */
    public static String DateToElasticSearchTimestamp(Date date){
        return ELASTICSEARCH_TIMESTAMP_FORMATTER.format(date.toInstant());
    }

    /**
     * Converts an Elasticsearch timestamp string to a Date object.
     * @param timestamp the Elasticsearch timestamp string
     * @return Date object parsed from the timestamp
     */
    public static Date ElasticSearchTimestampToDate(String timestamp){
        Instant instant = Instant.from(ELASTICSEARCH_TIMESTAMP_FORMATTER.parse(timestamp));
        return Date.from(instant);
    }

}
