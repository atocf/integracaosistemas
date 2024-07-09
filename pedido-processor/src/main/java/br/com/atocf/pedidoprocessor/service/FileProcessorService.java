package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.*;
import br.com.atocf.pedidoprocessor.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    protected String processingDir;

    @Value("${upload.completed.dir}")
    protected String completedDir;

    private static final Logger log = LoggerFactory.getLogger(FileProcessorService.class);

    @Transactional
    public void processFile(UploadLog uploadLog) {
        Path filePath = Paths.get(processingDir, uploadLog.getFileName());
        Map<Long, Users> users = new HashMap<>();

        log.info("Iniciado processo de leitura do arquivo {}", filePath.getFileName());

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                processarLinhaPedido(line, users, uploadLog);
            }

            usersRepository.saveAll(users.values());

            uploadLog.setStatus(UploadLog.UploadStatus.PROCESSED);
            uploadLogRepository.save(uploadLog);

            log.info("Finalizado o processamento do arquivo");

        } catch (IOException e) {
            uploadLog.setStatus(UploadLog.UploadStatus.ERROR);
            uploadLog.setErrorMessage("Erro ao processar o arquivo: " + e.getMessage());
            uploadLogRepository.save(uploadLog);
            log.error("Erro ao processar o arquivo", e);
        }

        try {
            Files.move(filePath, Paths.get(completedDir, uploadLog.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            uploadLog.setStatus(UploadLog.UploadStatus.ERROR);
            uploadLog.setErrorMessage("Erro ao mover o arquivo: " + e.getMessage());
            uploadLogRepository.save(uploadLog);
            log.error("Erro ao mover o arquivo", e);
        }

        webhookService.sendProcessedData(users);
    }

    @Transactional
    protected void processarLinhaPedido(String line, Map<Long, Users> users, UploadLog uploadLog) {
        log.info("Processando a linha do pedido {}", line);

        if (line.length() < 95) {
            throw new IllegalArgumentException("Linha muito curta: " + line);
        }

        long userId = Long.parseLong(line.substring(0, 10).trim());
        String userName = line.substring(10, 55).trim();
        long orderId = Long.parseLong(line.substring(55, 65).trim());
        long productId = Long.parseLong(line.substring(65, 75).trim());
        double productValue = Double.parseDouble(line.substring(75, 87).trim());
        LocalDate date = LocalDate.parse(line.substring(87, 95).trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Obter ou criar usuário
        Users user = users.computeIfAbsent(userId, k -> {
            Users newUser = new Users();
            newUser.setUserId(userId);
            newUser.setName(userName);
            return newUser;
        });

        // Obter ou criar pedido
        Orders order = user.getOrders().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElseGet(() -> {
                    Orders newOrder = new Orders();
                    newOrder.setOrderId(orderId);
                    newOrder.setDate(date);
                    newOrder.setUser(user);
                    newOrder.setUploadLog(uploadLog);
                    newOrder.setTotal(0.0);
                    user.getOrders().add(newOrder);
                    return newOrder;
                });

        // Criar e adicionar produto
        Products product = new Products();
        product.setProductId(productId);
        product.setValue(productValue);
        product.setOrder(order);
        order.getProducts().add(product);

        // Atualizar o total do pedido
        order.setTotal(order.getTotal() + productValue);

        log.info("Finalizado o processamento da linha do pedido");
    }
}