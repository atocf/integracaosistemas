package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.*;
import br.com.atocf.pedidoprocessor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FileProcessorService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UploadLogRepository uploadLogRepository;

    @Autowired
    private WebhookService webhookService;

    @Value("${upload.processing.dir}")
    private String processingDir;

    @Value("${upload.completed.dir}")
    private String completedDir;

    public void processFile(UploadLog uploadLog) {
        Path filePath = Paths.get(processingDir, uploadLog.getFileName());

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }

            // Mover o arquivo para o diretório de concluídos
            Files.move(filePath, Paths.get(completedDir, uploadLog.getFileName()));

            // Atualizar o status do UploadLog
            uploadLog.setStatus(UploadLog.UploadStatus.PROCESSED);
            uploadLogRepository.save(uploadLog);

            // Enviar dados processados para o webhook
            webhookService.sendProcessedData();

        } catch (IOException e) {
            uploadLog.setStatus(UploadLog.UploadStatus.ERROR);
            uploadLog.setErrorMessage("Erro ao processar o arquivo: " + e.getMessage());
            uploadLogRepository.save(uploadLog);
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(";");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Formato de linha inválido");
        }

        Long userId = Long.parseLong(parts[0]);
        String userName = parts[1];
        Long orderId = Long.parseLong(parts[2]);
        LocalDate orderDate = LocalDate.parse(parts[3], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Long productId = Long.parseLong(parts[4]);
        Double productValue = Double.parseDouble(parts[5]);

        Users user = usersRepository.findById(userId).orElseGet(() -> {
            Users newUser = new Users();
            newUser.setUserId(userId);
            newUser.setName(userName);
            return usersRepository.save(newUser);
        });

        OrderId orderIdObj = new OrderId(orderId, userId);
        Orders order = ordersRepository.findById(orderIdObj).orElseGet(() -> {
            Orders newOrder = new Orders();
            newOrder.setOrderId(orderIdObj);
            newOrder.setUsers(user);
            newOrder.setDate(orderDate);
            newOrder.setTotal(0.0);
            return ordersRepository.save(newOrder);
        });

        ProductId productIdObj = new ProductId(productId, orderId, userId);
        Products product = new Products();
        product.setProductId(productIdObj);
        product.setOrder(order);
        product.setValue(productValue);
        productsRepository.save(product);

        // Atualizar o total do pedido
        order.setTotal(order.getTotal() + productValue);
        ordersRepository.save(order);
    }
}