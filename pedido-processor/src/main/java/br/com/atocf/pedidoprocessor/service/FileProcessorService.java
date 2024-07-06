package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.*;
import br.com.atocf.pedidoprocessor.repository.*;
import jakarta.transaction.Transactional;
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
import java.util.*;

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
                processarLinhaPedido(line, users, uploadLog);
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

    @Transactional
    protected void processarLinhaPedido(String line, Map<Long, Users> users, UploadLog uploadLog) {
        long userId = Long.parseLong(line.substring(0, 10).trim());
        String userName = line.substring(10, 55).trim();
        long orderId = Long.parseLong(line.substring(55, 65).trim());
        long productId = Long.parseLong(line.substring(65, 75).trim());
        double productValue = Double.parseDouble(line.substring(75, 87).trim());
        LocalDate date = LocalDate.parse(line.substring(87, 95).trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));

        Users user = users.computeIfAbsent(userId, id -> {
            Users newUser = usersRepository.findById(id).orElse(new Users());
            newUser.setUserId(id);
            newUser.setName(userName);
            return usersRepository.save(newUser); // Salva o usuário imediatamente
        });

        Orders order = new Orders();
        order.setUser(user);
        order.setUploadLog(uploadLog);
        order.setOrderId(orderId);
        order.setDate(date);
        order.setTotal(productValue);

        Products product = new Products();
        product.setOrder(order);
        product.setProductId(productId);
        product.setValue(productValue);

        order.setProducts(new ArrayList<>(List.of(product)));

        order = ordersRepository.save(order); // Salva o pedido

        user.getOrders().add(order);
        usersRepository.save(user);
    }
}