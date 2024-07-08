package br.com.atocf.pedidoprocessor.service;

import br.com.atocf.pedidoprocessor.model.entity.UploadLog;
import br.com.atocf.pedidoprocessor.repository.UploadLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class RabbitMQConsumerTest {

    @Mock
    private FileProcessorService fileProcessorService;

    @Mock
    private UploadLogRepository uploadLogRepository;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsumeMessage() {
        String message = "1";
        UploadLog uploadLog = new UploadLog();
        uploadLog.setId(1L);

        when(uploadLogRepository.findById(1L)).thenReturn(java.util.Optional.of(uploadLog));

        rabbitMQConsumer.consumeMessage(message);

        verify(fileProcessorService, times(1)).processFile(uploadLog);
    }
}