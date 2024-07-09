package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.*;
import br.com.atocf.pedidoprocessor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

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
        Files.write(filePath, (
                "0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308\n" +
                        "0000000070                              Palmer Prosacco00000007530000000004      973.2320210308"
        ).getBytes());

        Users user = new Users();
        user.setUserId(70L);
        user.setName("Palmer Prosacco");
        user.setOrders(new ArrayList<>());
        when(usersRepository.findById(70L)).thenReturn(Optional.of(user));
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        fileProcessorService.processFile(uploadLog);

        verify(usersRepository, atLeastOnce()).save(any(Users.class));

        verify(ordersRepository, times(1)).save(any(Orders.class));

        verify(productsRepository, times(2)).save(any(Products.class));

        verify(uploadLogRepository, times(1)).save(uploadLog);
    }

    @Test
    public void testProcessarLinhaPedido() {
        Map<Long, Users> users = new HashMap<>();
        UploadLog uploadLog = new UploadLog();
        String line1 = "0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308";
        String line2 = "0000000070                              Palmer Prosacco00000007530000000004      973.2320210308";

        Users user = new Users();
        user.setUserId(70L);
        user.setName("Palmer Prosacco");
        user.setOrders(new ArrayList<>());

        when(usersRepository.findById(70L)).thenReturn(Optional.empty());

        fileProcessorService.processarLinhaPedido(line1, users, uploadLog);
        fileProcessorService.processarLinhaPedido(line2, users, uploadLog);

        assert users.containsKey(70L);

        Users savedUser = users.get(70L);
        assert savedUser != null;
        assert savedUser.getOrders().size() == 1;

        Orders order = savedUser.getOrders().get(0);

        assert order.getProducts().size() == 2;
        assert order.getTotal() == 1836.74 + 973.23;
    }
}