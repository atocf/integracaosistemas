package br.com.atocf.pedido.service;

import br.com.atocf.pedido.model.dto.PedidoDto;
import br.com.atocf.pedido.model.entity.Orders;
import br.com.atocf.pedido.model.entity.Products;
import br.com.atocf.pedido.model.entity.UploadLog;
import br.com.atocf.pedido.model.entity.Users;
import br.com.atocf.pedido.repository.OrdersRepository;
import br.com.atocf.pedido.repository.ProductsRepository;
import br.com.atocf.pedido.repository.UploadLogRepository;
import br.com.atocf.pedido.repository.UsersRepository;
import br.com.atocf.pedido.utils.FileOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    @Autowired
    private UploadLogRepository uploadLogRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

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

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

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

    public List<PedidoDto> consultarPedidos(Long orderId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        // Primeiro, buscamos os pedidos
        List<Orders> orders = ordersRepository.findByOrderId(orderId);

        // Criamos uma lista para armazenar os PedidoDto
        List<PedidoDto> pedidoDtos = new ArrayList<>();

        for (Orders order : orders) {
            // Buscamos o usuário vinculado ao pedido
            Users user = usersRepository.findById(order.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o pedido: " + order.getOrderId()));

            // Buscamos o upload log vinculado ao pedido
            UploadLog uploadLog = uploadLogRepository.findById(order.getUploadLogId())
                    .orElseThrow(() -> new RuntimeException("Upload log não encontrado para o pedido: " + order.getOrderId()));

            // Buscamos os produtos vinculados ao pedido
            List<Products> products = productsRepository.findByOrderId(order.getId());

            // Criamos um novo PedidoDto com todas as informações
            PedidoDto pedidoDto = new PedidoDto(
                    user,
                    uploadLog,
                    order.getOrderId(),
                    order.getDate(),
                    order.getTotal(),
                    products
            );

            pedidoDtos.add(pedidoDto);
        }

        return pedidoDtos;
    }
};