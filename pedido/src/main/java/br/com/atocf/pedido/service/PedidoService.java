package br.com.atocf.pedido.service;

import br.com.atocf.pedido.model.entity.UploadLog;
import br.com.atocf.pedido.repository.UploadLogRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PedidoService {

    @Autowired
    private UploadLogRepository uploadLogRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${upload.dir:/app/upload_files}")
    private String uploadDir;

    @Value("${processing.dir:/app/upload_files/processing}")
    private String processingDir;

    @Value("${rabbitmq.exchange:pedido-exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey:pedido-routing-key}")
    private String routingkey;

    public void uploadPedidoFile(MultipartFile file) throws IOException {
        // Gerar um nome único para o arquivo
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Criar os diretórios necessários se não existirem
        createDirectoryIfNotExists(uploadDir);
        createDirectoryIfNotExists(processingDir);

        // Salvar o arquivo no diretório de upload
        Path filePath = Paths.get(processingDir, fileName);
        Files.write(filePath, file.getBytes());

        // Criar e salvar o log de upload
        UploadLog uploadLog = new UploadLog();
        uploadLog.setFileName(fileName);
        uploadLog.setUploadTimestamp(LocalDateTime.now());
        uploadLog.setStatus(UploadLog.UploadStatus.PENDING);
        uploadLogRepository.save(uploadLog);

        // Enviar mensagem para o RabbitMQ
        rabbitTemplate.convertAndSend(exchange, routingkey, fileName);
    }

    private void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}