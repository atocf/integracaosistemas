package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.*;
import br.com.atocf.pedidoprocessor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class FileProcessorServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ProductsRepository productsRepository;

    @Mock
    private UploadLogRepository uploadLogRepository;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private FileProcessorService fileProcessorService;

    @Value("${upload.processing.dir}")
    private String processingDir;

    @Value("${upload.completed.dir}")
    private String completedDir;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicializar as variáveis de diretório
        fileProcessorService.processingDir = "../upload_files/processing";
        fileProcessorService.completedDir = "../upload_files/completed";
    }

    @Test
    public void testProcessFile() throws IOException {
        UploadLog uploadLog = new UploadLog();
        uploadLog.setId(1L);
        uploadLog.setFileName("testfile.txt");
        uploadLog.setStatus(UploadLog.UploadStatus.PENDING);
        uploadLog.setUploadTimestamp(LocalDateTime.now());

        Path filePath = Paths.get(fileProcessorService.processingDir, uploadLog.getFileName());
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308".getBytes());

        Users user = new Users();
        user.setUserId(1234567890L);
        user.setName("User Name");
        user.setOrders(new ArrayList<>()); // Inicializar a lista de pedidos
        when(usersRepository.findById(1234567890L)).thenReturn(Optional.of(user));
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        fileProcessorService.processFile(uploadLog);

        verify(usersRepository, times(1)).saveAll(anyCollection());
        verify(uploadLogRepository, times(1)).save(uploadLog);
    }

    @Test
    public void testProcessarLinhaPedido() {
        Map<Long, Users> users = new HashMap<>();
        UploadLog uploadLog = new UploadLog();
        String line = "0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308";

        Users user = new Users();
        user.setUserId(1234567890L);
        user.setName("User Name");
        user.setOrders(new ArrayList<>()); // Inicializar a lista de pedidos

        when(usersRepository.findById(1234567890L)).thenReturn(Optional.of(user));
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        fileProcessorService.processarLinhaPedido(line, users, uploadLog);

        verify(usersRepository, times(2)).save(any(Users.class)); // Esperar duas chamadas
        verify(ordersRepository, times(1)).save(any(Orders.class));
    }
}