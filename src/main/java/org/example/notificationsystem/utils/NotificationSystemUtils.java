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
import java.util.Date;
import java.util.List;

import static org.example.notificationsystem.constants.Time.INDIA_ZONE_ID;

public class NotificationSystemUtils {

    private static final String thirdPartyApiUrl = "https://notification.free.beeceptor.com/resources/v1/messaging";

    private static final Logger logger = LoggerFactory.getLogger(NotificationSystemUtils.class);

    public static boolean isValidPageRequest(ElasticSearchRequest query) {
        // Check if both page and size are non-null and valid
        return query.getPage() != null
                && query.getSize() != null
                && query.getPage() >= 0
                && query.getSize() > 0;
    }

    public static SmsRequestElasticsearch getSmsRequestElasticsearchFromSmsRequest(SmsRequest smsRequest) {
        SmsRequestElasticsearch smsRequestElasticsearch = new SmsRequestElasticsearch();
        smsRequestElasticsearch.setCreatedAt(smsRequest.getCreatedAt());
        smsRequestElasticsearch.setUpdatedAt(smsRequest.getUpdatedAt());
        smsRequestElasticsearch.setPhoneNumber(smsRequest.getPhoneNumber());
        smsRequestElasticsearch.setMessage(smsRequest.getMessage());
        smsRequestElasticsearch.setSmsRequestId(smsRequest.getId().toString());
        return smsRequestElasticsearch;
    }

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


    public static Date getNowAsDateIST() {
        return Date.from(Instant.now().atZone(INDIA_ZONE_ID).toInstant());
    }
}
