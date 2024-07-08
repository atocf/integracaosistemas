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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Mock
    private UploadLogRepository uploadLogRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private FileOperations fileOperations;

    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ProductsRepository productsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pedidoService, "uploadDir", "/tmp/upload");
        ReflectionTestUtils.setField(pedidoService, "processingDir", "/tmp/processing");
        ReflectionTestUtils.setField(pedidoService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(pedidoService, "routingkey", "test-routing-key");
    }

    @Test
    void uploadPedidoFile_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        // Criar um UploadLog mock com um ID
        UploadLog mockUploadLog = new UploadLog();
        mockUploadLog.setId(1L); // Definir um ID para o UploadLog

        // Configurar mocks
        doNothing().when(fileOperations).createDirectoryIfNotExists(anyString());
        doNothing().when(fileOperations).writeFile(any(Path.class), any(byte[].class));
        when(uploadLogRepository.save(any(UploadLog.class))).thenReturn(mockUploadLog);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // Act
        try {
            pedidoService.uploadPedidoFile(file);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        // Assert
        verify(fileOperations, times(2)).createDirectoryIfNotExists(anyString());
        verify(fileOperations, times(1)).writeFile(any(Path.class), any(byte[].class));
        verify(uploadLogRepository, times(1)).save(any(UploadLog.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), eq("1")); // Verificar se o ID correto é enviado

        assertTrue(true, "Upload de arquivo concluído com sucesso");
    }

    @Test
    void uploadPedidoFile_DirectoryCreation() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        // Configurar o mock para simular a criação de diretórios
        doAnswer(invocation -> {
            String dirPath = invocation.getArgument(0);
            // Simular a criação do diretório retornando true
            return null; // void method
        }).when(fileOperations).createDirectoryIfNotExists(anyString());

        // Configurar o mock para simular a escrita do arquivo
        doNothing().when(fileOperations).writeFile(any(Path.class), any(byte[].class));

        // Criar um UploadLog mock com um ID
        UploadLog mockUploadLog = new UploadLog();
        mockUploadLog.setId(1L); // Definir um ID para o UploadLog
        when(uploadLogRepository.save(any(UploadLog.class))).thenReturn(mockUploadLog);

        // Executar o método
        pedidoService.uploadPedidoFile(file);

        // Verificar se os métodos foram chamados
        verify(fileOperations, times(2)).createDirectoryIfNotExists(anyString());
        verify(fileOperations, times(1)).writeFile(any(Path.class), any(byte[].class));
        verify(uploadLogRepository, times(1)).save(any());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), eq("1")); // Verificar se o ID correto é enviado

        // Se chegou até aqui sem lançar exceções, o teste passou
        assertTrue(true);
    }

    @Test
    void uploadPedidoFile_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        doThrow(new IOException("Simulated IO error"))
                .when(fileOperations).writeFile(any(Path.class), any(byte[].class));

        assertThrows(IOException.class, () -> pedidoService.uploadPedidoFile(file));

        verify(uploadLogRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void consultarPedidos_ComOrderId() {
        Long orderId = 1L;
        Orders order = new Orders();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setDate(LocalDate.now());
        order.setTotal(100.00);
        order.setUserId(1L);
        order.setUploadLogId(1L);

        Users user = new Users();
        user.setUserId(1L);

        UploadLog uploadLog = new UploadLog();
        uploadLog.setId(1L);

        Products product = new Products();
        product.setId(1L);

        when(ordersRepository.findByOrderId(orderId)).thenReturn(Collections.singletonList(order));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(uploadLogRepository.findById(1L)).thenReturn(Optional.of(uploadLog));
        when(productsRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(product));

        List<PedidoDto> result = pedidoService.consultarPedidos(orderId, null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getOrderId());

        verify(ordersRepository).findByOrderId(orderId);
        verify(usersRepository).findById(1L);
        verify(uploadLogRepository).findById(1L);
        verify(productsRepository).findByOrderId(1L);
    }

    @Test
    void consultarPedidos_ComDataRange() {
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();

        Orders order = new Orders();
        order.setId(1L);
        order.setOrderId(1L);
        order.setDate(LocalDate.now().minusDays(3));
        order.setTotal(100.00);
        order.setUserId(1L);
        order.setUploadLogId(1L);

        Users user = new Users();
        user.setUserId(1L);

        UploadLog uploadLog = new UploadLog();
        uploadLog.setId(1L);

        Products product = new Products();
        product.setId(1L);

        when(ordersRepository.findByDateRange(dataInicio, dataFim)).thenReturn(Collections.singletonList(order));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(uploadLogRepository.findById(1L)).thenReturn(Optional.of(uploadLog));
        when(productsRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(product));

        List<PedidoDto> result = pedidoService.consultarPedidos(null, dataInicio, dataFim);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDate().isAfter(dataInicio) && result.get(0).getDate().isBefore(dataFim.plusDays(1)));

        verify(ordersRepository).findByDateRange(dataInicio, dataFim);
        verify(usersRepository).findById(1L);
        verify(uploadLogRepository).findById(1L);
        verify(productsRepository).findByOrderId(1L);
    }

    @Test
    void consultarPedidos_ComOrderIdEDataRange() {
        Long orderId = 1L;
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();

        Orders order = new Orders();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setDate(LocalDate.now().minusDays(3));
        order.setTotal(100.00);
        order.setUserId(1L);
        order.setUploadLogId(1L);

        Users user = new Users();
        user.setUserId(1L);

        UploadLog uploadLog = new UploadLog();
        uploadLog.setId(1L);

        Products product = new Products();
        product.setId(1L);

        when(ordersRepository.findByOrderIdAndDateRange(orderId, dataInicio, dataFim)).thenReturn(Collections.singletonList(order));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(uploadLogRepository.findById(1L)).thenReturn(Optional.of(uploadLog));
        when(productsRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(product));

        List<PedidoDto> result = pedidoService.consultarPedidos(orderId, dataInicio, dataFim);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getOrderId());
        assertTrue(result.get(0).getDate().isAfter(dataInicio) && result.get(0).getDate().isBefore(dataFim.plusDays(1)));

        verify(ordersRepository).findByOrderIdAndDateRange(orderId, dataInicio, dataFim);
        verify(usersRepository).findById(1L);
        verify(uploadLogRepository).findById(1L);
        verify(productsRepository).findByOrderId(1L);
    }

    @Test
    void consultarPedidos_NenhumEncontrado() {
        Long orderId = 1L;

        when(ordersRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

        List<PedidoDto> result = pedidoService.consultarPedidos(orderId, null, null);

        assertTrue(result.isEmpty());

        verify(ordersRepository).findByOrderId(orderId);
        verifyNoInteractions(usersRepository, uploadLogRepository, productsRepository);
    }

    @Test
    void consultarPedidos_UsuarioNaoEncontrado() {
        Long orderId = 1L;
        Orders order = new Orders();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setUserId(1L);

        when(ordersRepository.findByOrderId(orderId)).thenReturn(Collections.singletonList(order));
        when(usersRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pedidoService.consultarPedidos(orderId, null, null));

        verify(ordersRepository).findByOrderId(orderId);
        verify(usersRepository).findById(1L);
    }

    @Test
    void consultarPedidos_UploadLogNaoEncontrado() {
        Long orderId = 1L;
        Orders order = new Orders();
        order.setId(1L);
        order.setOrderId(orderId);
        order.setUserId(1L);
        order.setUploadLogId(1L);

        Users user = new Users();
        user.setUserId(1L);

        when(ordersRepository.findByOrderId(orderId)).thenReturn(Collections.singletonList(order));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(uploadLogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pedidoService.consultarPedidos(orderId, null, null));

        verify(ordersRepository).findByOrderId(orderId);
        verify(usersRepository).findById(1L);
        verify(uploadLogRepository).findById(1L);
    }
}