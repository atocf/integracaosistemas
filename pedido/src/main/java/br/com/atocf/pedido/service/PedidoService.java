package br.com.atocf.pedido.service;

import br.com.atocf.pedido.model.entity.UploadLog;
import br.com.atocf.pedido.repository.UploadLogRepository;
import br.com.atocf.pedido.utils.FileOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Autowired
    private FileOperations fileOperations;

    @Value("${upload.dir:/app/upload_files}")
    private String uploadDir;

    @Value("${processing.dir:/app/upload_files/processing}")
    private String processingDir;

    @Value("${rabbitmq.exchange:pedido-exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey:pedido-routing-key}")
    private String routingkey;

    public void uploadPedidoFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        fileOperations.createDirectoryIfNotExists(uploadDir);
        fileOperations.createDirectoryIfNotExists(processingDir);

        Path filePath = Paths.get(processingDir, fileName);
        fileOperations.writeFile(filePath, file.getBytes());

        UploadLog uploadLog = new UploadLog();
        uploadLog.setFileName(fileName);
        uploadLog.setUploadTimestamp(LocalDateTime.now());
        uploadLog.setStatus(UploadLog.UploadStatus.PENDING);
        uploadLog = uploadLogRepository.save(uploadLog);

        rabbitTemplate.convertAndSend(exchange, routingkey, uploadLog.getId().toString());
    }
}