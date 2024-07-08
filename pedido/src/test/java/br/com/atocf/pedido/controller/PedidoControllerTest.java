package br.com.atocf.pedido.controller;

import br.com.atocf.pedido.model.dto.PedidoDto;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void consultarPedidos_Success() {
        Long orderId = 1L;
        LocalDate dataInicio = LocalDate.now().minusDays(7);
        LocalDate dataFim = LocalDate.now();
        PedidoDto pedidoDto = new PedidoDto(); // Crie um PedidoDto com dados de exemplo
        when(pedidoService.consultarPedidos(orderId, dataInicio, dataFim)).thenReturn(Arrays.asList(pedidoDto));

        ResponseEntity<?> response = pedidoController.consultarPedidos(orderId, dataInicio, dataFim);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<PedidoDto> resultList = (List<PedidoDto>) response.getBody();
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
    }

    @Test
    void consultarPedidos_NoResults() {
        Long orderId = 1L;
        when(pedidoService.consultarPedidos(orderId, null, null)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = pedidoController.consultarPedidos(orderId, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<PedidoDto> resultList = (List<PedidoDto>) response.getBody();
        assertTrue(resultList.isEmpty());
    }

    @Test
    void consultarPedidos_InvalidDateRange() {
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().minusDays(1);

        ResponseEntity<?> response = pedidoController.consultarPedidos(null, dataInicio, dataFim);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("A data de início deve ser anterior à data de fim.", errorResponse.getErrors().get(0).getMessage());
    }

    @Test
    void consultarPedidos_MissingOrderIdAndDateRange() {
        ResponseEntity<?> response = pedidoController.consultarPedidos(null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("O campo orderId é obrigatório e não pode ser nulo ou vazio quando dataInicio e dataFim não são fornecidos.", errorResponse.getErrors().get(0).getMessage());
    }
}