package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.UploadLog;
import br.com.atocf.pedidoprocessor.repository.UploadLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    @Autowired
    private FileProcessorService fileProcessorService;

    @Autowired
    private UploadLogRepository uploadLogRepository;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeMessage(String message) {
        try {
            // Assumindo que a mensagem é o ID do UploadLog
            Long uploadLogId = Long.parseLong(message);
            UploadLog uploadLog = uploadLogRepository.findById(uploadLogId)
                    .orElseThrow(() -> new RuntimeException("UploadLog não encontrado"));

            fileProcessorService.processFile(uploadLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}