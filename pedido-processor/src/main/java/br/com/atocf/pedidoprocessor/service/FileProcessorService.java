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
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        Map<Long, Users> users = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                processarLinhaPedido(line, users);
            }

            usersRepository.saveAll(users.values());

            uploadLog.setStatus(UploadLog.UploadStatus.PROCESSED);
            uploadLogRepository.save(uploadLog);

            // webhookService.sendProcessedData();

        } catch (IOException e) {
            uploadLog.setStatus(UploadLog.UploadStatus.ERROR);
            uploadLog.setErrorMessage("Erro ao processar o arquivo: " + e.getMessage());
            uploadLogRepository.save(uploadLog);
            e.printStackTrace();
        }

        try {
            Files.move(filePath, Paths.get(completedDir, uploadLog.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            uploadLog.setStatus(UploadLog.UploadStatus.ERROR);
            uploadLog.setErrorMessage("Erro ao mover o arquivo: " + e.getMessage());
            uploadLogRepository.save(uploadLog);
            e.printStackTrace();
        }
    }

    private void processarLinhaPedido(String line, Map<Long, Users> users) {
        long userId = Long.parseLong(line.substring(0, 10).trim());
        String userName = line.substring(10, 55).trim();
        long orderId = Long.parseLong(line.substring(55, 65).trim());
        long productId = Long.parseLong(line.substring(65, 75).trim());
        double productValue = Double.parseDouble(line.substring(75, 87).trim());
        LocalDate date = LocalDate.parse(line.substring(87, 95).trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));

        Users user = users.computeIfAbsent(userId, id -> new Users(userId, userName, new ArrayList<>()));

        Orders order = user.getOrders().stream()
                .filter(o -> o.getOrderId().getOrderId().equals(orderId))
                .findFirst()
                .orElseGet(() -> {
                    OrderId newOrderId = new OrderId(orderId, userId);
                    Orders newOrder = new Orders(newOrderId, user, date, 0.0, new ArrayList<>());
                    user.getOrders().add(newOrder);
                    return newOrder;
                });

        order.setTotal(order.getTotal() + productValue);

        ProductId productIdKey = new ProductId(productId, orderId, userId);
        Products product = order.getProducts().stream()
                .filter(p -> p.getProductId().equals(productIdKey))
                .findFirst()
                .orElse(null);

        if (product == null) {
            product = new Products(productIdKey, order, productValue);
            order.getProducts().add(product);
        } else if (product.getValue() != productValue) {
            product.setValue(productValue);
        }
    }
}