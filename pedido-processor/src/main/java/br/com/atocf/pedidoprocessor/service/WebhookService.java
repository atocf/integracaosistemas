package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.Orders;
import br.com.atocf.pedidoprocessor.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class WebhookService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${webhook.url}")
    private String webhookUrl;

    public void sendProcessedData() {
        List<Orders> processedOrders = ordersRepository.findAll();
        // Aqui você pode transformar os pedidos em um formato JSON adequado
        // antes de enviar para o webhook

        // restTemplate.postForEntity(webhookUrl, processedOrders, String.class);
    }
}