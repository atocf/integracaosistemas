package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.Users;
import br.com.atocf.pedidoprocessor.repository.OrdersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WebhookServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Logger log;

    @InjectMocks
    private WebhookService webhookService;

    @Value("${webhook.url}")
    private String webhookUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webhookService = new WebhookService();
        webhookService.ordersRepository = ordersRepository;
        webhookService.restTemplate = restTemplate;
        webhookService.objectMapper = objectMapper;
        webhookService.webhookUrl = "http://example.com/webhook";
    }

    @Test
    void testSendProcessedData() throws JsonProcessingException {
        // Given
        Map<Long, Users> usersMap = new HashMap<>();
        Users user = new Users();
        user.setUserId(1L);
        user.setName("Test User");
        usersMap.put(1L, user);

        String jsonPayload = "[{\"id\":1,\"name\":\"Test User\"}]";

        when(objectMapper.writeValueAsString(usersMap.values())).thenReturn(jsonPayload);

        // When
        webhookService.sendProcessedData(usersMap);

        // Then
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(webhookService.webhookUrl), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class));

        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        assertEquals(jsonPayload, capturedEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON, capturedEntity.getHeaders().getContentType());
    }
}