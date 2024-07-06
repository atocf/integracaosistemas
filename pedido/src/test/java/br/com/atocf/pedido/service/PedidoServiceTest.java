package br.com.atocf.pedido.service;

import br.com.atocf.pedido.model.entity.UploadLog;
import br.com.atocf.pedido.repository.UploadLogRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        // Configurar mocks
        doNothing().when(fileOperations).createDirectoryIfNotExists(anyString());
        doNothing().when(fileOperations).writeFile(any(Path.class), any(byte[].class));
        when(uploadLogRepository.save(any(UploadLog.class))).thenReturn(new UploadLog());
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
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());

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

        // Executar o método
        pedidoService.uploadPedidoFile(file);

        // Verificar se os métodos foram chamados
        verify(fileOperations, times(2)).createDirectoryIfNotExists(anyString());
        verify(fileOperations, times(1)).writeFile(any(Path.class), any(byte[].class));
        verify(uploadLogRepository, times(1)).save(any());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());

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
}