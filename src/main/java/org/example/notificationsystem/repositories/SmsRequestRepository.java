package org.example.notificationsystem.repositories;

import org.example.notificationsystem.models.SmsRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Default JPA Repository for interacting with the MySQL table for Sms Requests.
 * */
@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, Long> {
    /**
     * Returns a list of SMS requests having status field == status.
     *
     * @param status The status by which we are querying.
     * @return A list of {@link SmsRequest} objects that match the given status.
     */
    List<SmsRequest> findByStatus(Integer status);

    /**
     * Returns all SMS requests.
     *
     * @return A list of {@link SmsRequest} objects that match the given status.
     */
    List<SmsRequest> findAll();

    /**
     * Returns a list of SMS requests having id field == id.
     *
     * @param id The status by which we are querying.
     * @return A list of {@link SmsRequest} objects that match the given status.
     */
    Optional<SmsRequest> findById(Long id);

    /**
     * Returns a list of SMS requests created between the two Date-s.
     *
     * @param createdAtAfter The start date for querying.
     * @param createdAtBefore The end date for querying.
     * @return A list of {@link SmsRequest} objects that match the given status.
     */
    List<SmsRequest> findByCreatedAtIsBetween(Date createdAtAfter, Date createdAtBefore);
}