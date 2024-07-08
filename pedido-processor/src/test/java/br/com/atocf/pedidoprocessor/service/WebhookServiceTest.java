package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.Orders;
import br.com.atocf.pedidoprocessor.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class WebhookServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WebhookService webhookService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendProcessedData() {
        Orders order = new Orders();
        when(ordersRepository.findAll()).thenReturn(Collections.singletonList(order));

        webhookService.sendProcessedData();

        //verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
}