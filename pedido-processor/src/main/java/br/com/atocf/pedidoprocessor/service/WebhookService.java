package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.Users;
import br.com.atocf.pedidoprocessor.repository.OrdersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Map;

@Service
public class WebhookService {

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    RestTemplate restTemplate;

    @Value("${webhook.url}")
    String webhookUrl;

    @Autowired
    ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(FileProcessorService.class);

    public void sendProcessedData(Map<Long, Users> users) {
        log.info("Enviando dados normalizados para: {}", webhookUrl);
        try {
            String jsonPayload = objectMapper.writeValueAsString(users.values());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
            restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}