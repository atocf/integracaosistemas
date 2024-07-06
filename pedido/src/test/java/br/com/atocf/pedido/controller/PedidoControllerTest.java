package br.com.atocf.pedido.controller;

import br.com.atocf.pedido.model.dto.SuccessResponse;
import br.com.atocf.pedido.model.error.ErrorResponse;
import br.com.atocf.pedido.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadPedidoFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        ResponseEntity<?> response = pedidoController.uploadPedidoFile(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        assertEquals("Importação realizada com sucesso.", ((SuccessResponse) response.getBody()).getMessage());
        verify(pedidoService, times(1)).uploadPedidoFile(file);
    }

    @Test
    void uploadPedidoFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        ResponseEntity<?> response = pedidoController.uploadPedidoFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Por favor, envie um arquivo não vazio", errorResponse.getErrors().get(0).getMessage());
    }

    @Test
    void uploadPedidoFile_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

        ResponseEntity<?> response = pedidoController.uploadPedidoFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Tipo de arquivo inválido. Somente arquivos TXT são permitidos.", errorResponse.getErrors().get(0).getMessage());
    }

    @Test
    void uploadPedidoFile_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        doThrow(new IOException("Test exception")).when(pedidoService).uploadPedidoFile(file);

        ResponseEntity<?> response = pedidoController.uploadPedidoFile(file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Erro ao processar arquivo: Test exception", errorResponse.getErrors().get(0).getMessage());
    }
}