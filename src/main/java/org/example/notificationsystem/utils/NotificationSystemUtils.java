package org.example.notificationsystem.utils;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;

public class NotificationSystemUtils {
    public static void validateQueryParameters(ElasticSearchRequest query) {

    }

    public static boolean isValidPageRequest(ElasticSearchRequest query) {
        return query.getPage() > 0 && query.getPage() < 10;
    }
}
