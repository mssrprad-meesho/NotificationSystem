package org.example.notificationsystem;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.example.notificationsystem.services.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    @Mock
    private SmsRequestRepository smsRequestRepository;
    @Mock
    private SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;
    @Mock
    private Producer producer;

    @InjectMocks
    private SmsServiceImpl smsService;

    private List<SmsRequest> smsRequests;
    private List<SmsRequestElasticsearch> smsRequestsElasticsearch;

    @BeforeEach
    void setUp() {
        smsRequests = new ArrayList<>();
        smsRequests.add(SmsRequest.builder().id(1L).phoneNumber("+911111111111").build());
        smsRequestsElasticsearch = new ArrayList<>();
        smsRequestsElasticsearch.add( NotificationSystemUtils.getSmsRequestElasticsearchFromSmsRequest(smsRequests.get(0)));
    }

    @Test
    void createSmsRequest() {
        // Mock
        Mockito.when(smsRequestRepository.saveAndFlush(any())).thenReturn(smsRequests.get(0));
        Mockito.when(smsRequestElasticsearchRepository.save(
                any(SmsRequestElasticsearch.class)
        )).thenReturn(
                NotificationSystemUtils.getSmsRequestElasticsearchFromSmsRequest(smsRequests.get(0))
        );
        Mockito.when(producer.publishSync(any())).thenReturn(true);

        SmsRequest smsRequest = smsService.createSmsRequest(smsRequests.get(0).getPhoneNumber(), smsRequests.get(0).getMessage());
        assertEquals(smsRequests.get(0).getPhoneNumber(), smsRequest.getPhoneNumber());
        assertEquals(smsRequests.get(0).getMessage(), smsRequest.getMessage());
    }

    @Test
    void getAllSmsRequestsElasticsearch() {
        // Mock
        Mockito.when(smsRequestElasticsearchRepository.findAll()).thenReturn(smsRequestsElasticsearch);

        assertEquals(smsRequestsElasticsearch, smsService.getAllSmsRequestsElasticsearch());
    }

    @Test
    void getAllSmsRequestsElasticSearchFromToPageSize() {
        Date from = new Date();
        Date to = new Date();
        int page = 0;
        int size = 10;

        // Mock
        Mockito.when(smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to, PageRequest.of(page, size)))
                .thenReturn(smsRequestsElasticsearch);

        List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticSearchFromToPageSize(from, to, page, size);
        assertEquals(smsRequestsElasticsearch, result);
    }

    @Test
    void getAllSmsRequestsElasticSearchFromTo() {
        Date from = new Date();
        Date to = new Date();

        // Mock
        Mockito.when(smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to))
                .thenReturn(smsRequestsElasticsearch);

        List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticSearchFromTo(from, to);
        assertEquals(smsRequestsElasticsearch, result);
    }

    @Test
    void getAllSmsRequestsElasticSearchContainingFromToPageSize() {
        Date from = new Date();
        Date to = new Date();
        int page = 0;
        int size = 10;
        String substr = "";

        // Mock
        Mockito.when(smsRequestElasticsearchRepository.findByMessageMatchesRegexIgnoreCaseAndCreatedAtIsBetween(substr, from, to, PageRequest.of(page, size)))
                .thenReturn(smsRequestsElasticsearch);

        List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticSearchContainingFromToPageSize(substr, from, to, page, size);
        assertEquals(smsRequestsElasticsearch, result);
    }

    @Test
    void getAllSmsRequestsElasticSearchContainingFromTo() {
        Date from = new Date();
        Date to = new Date();
        String substr = "";

        // Mock
        Mockito.when(smsRequestElasticsearchRepository.findByMessageMatchesRegexIgnoreCaseAndCreatedAtIsBetween(substr, from, to))
                .thenReturn(smsRequestsElasticsearch);

        List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticSearchContainingFromTo(substr, from, to);
        assertEquals(smsRequestsElasticsearch, result);
    }

    @Test
    void getAllSmsRequests() {
        // Mock
        Mockito.when(smsRequestRepository.findAll())
                .thenReturn(smsRequests);

        assertEquals(smsRequests, smsService.getAllSmsRequests());
    }

    @Test
    void getSmsRequests() {
        // Mock
        Mockito.when(smsRequestRepository.findById(1L))
                .thenReturn(Optional.of(smsRequests.get(0)));
        Optional<SmsRequest> smsRequest = smsService.getSmsRequest(1L);
        assertTrue(smsRequest.isPresent());
        assertEquals(smsRequests.get(0), smsRequest.get());
    }

    @Test
    void getFinishedSmsRequests() {
        // Mock
        Mockito.when(smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal()))
                .thenReturn(smsRequests);

        assertEquals(smsRequests, smsService.getFinishedSmsRequests());
    }

    @Test
    void getInProgressSmsRequests() {
        // Mock
        Mockito.when(smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal()))
                .thenReturn(smsRequests);

        assertEquals(smsRequests, smsService.getInProgressSmsRequests());
    }

    @Test
    void getFailedSmsRequests() {
        // Mock
        Mockito.when(smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal()))
                .thenReturn(smsRequests);

        assertEquals(smsRequests, smsService.getFailedSmsRequests());
    }


    @ParameterizedTest
    @EnumSource(StatusConstants.class)
    void setStatus(StatusConstants statusConstant) {
        // Mock
        Mockito.when(smsRequestRepository.findById(1L))
                .thenReturn(Optional.of(smsRequests.get(0)));

        Optional<SmsRequest> optionalSmsRequest = smsService.setStatus(1L, statusConstant);
        assertTrue(optionalSmsRequest.isPresent());
        assertSame(statusConstant.getCode(), optionalSmsRequest.get().getStatus());
    }


    @ParameterizedTest
    @EnumSource(FailureCodeConstants.class)
    void setFailureCode(FailureCodeConstants failureCode) {
        // Mock
        Mockito.when(smsRequestRepository.findById(1L))
                .thenReturn(Optional.of(smsRequests.get(0)));

        Optional<SmsRequest> optionalSmsRequest = smsService.setFailureCode(1L, failureCode);
        assertTrue(optionalSmsRequest.isPresent());
        assertSame(failureCode.getCode(), optionalSmsRequest.get().getFailureCode());
    }

}
