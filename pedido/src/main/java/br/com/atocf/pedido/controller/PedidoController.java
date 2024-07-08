package br.com.atocf.pedido.controller;

import br.com.atocf.pedido.model.dto.PedidoDto;
import br.com.atocf.pedido.model.dto.SuccessResponse;
import br.com.atocf.pedido.model.error.ErrorResponse;
import br.com.atocf.pedido.service.PedidoService;
import br.com.atocf.pedido.model.error.ErrorObject;
import br.com.atocf.pedido.exception.RestExceptionCustom;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pedido")
@Tag(name = "Pedido", description = "API para gerenciamento de pedidos")
public class PedidoController {

    @Autowired
    private PedidoService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de arquivo de pedidos", description = "Faz o upload de um arquivo TXT contendo informações de pedidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato incorreto"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<?> uploadPedidoFile(
            @Parameter(description="Arquivo TXT contendo informações de pedidos", required=true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            ErrorObject errorObject = new ErrorObject("Por favor, envie um arquivo não vazio", "file", file.getOriginalFilename());
            ErrorResponse errorResponse = RestExceptionCustom.getErrorResponse("uploadPedidoFile", HttpStatus.BAD_REQUEST, errorObject);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            ErrorObject errorObject = new ErrorObject("Tipo de arquivo inválido. Somente arquivos TXT são permitidos.", "file", file.getOriginalFilename());
            ErrorResponse errorResponse = RestExceptionCustom.getErrorResponse("uploadPedidoFile", HttpStatus.BAD_REQUEST, errorObject);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        try {
            service.uploadPedidoFile(file);
            return new ResponseEntity<>(new SuccessResponse("Importação realizada com sucesso."), HttpStatus.OK);
        } catch (IOException e) {
            ErrorObject errorObject = new ErrorObject("Erro ao processar arquivo: " + e.getMessage(), "file", file.getOriginalFilename());
            ErrorResponse errorResponse = RestExceptionCustom.getErrorResponse("uploadPedidoFile", HttpStatus.INTERNAL_SERVER_ERROR, errorObject);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> consultarPedidos(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim) {

        ErrorObject errorObject = null;

        if (dataInicio != null && (dataFim == null || dataFim.toString().trim().isEmpty())) {
            errorObject = new ErrorObject("O campo dataFim é obrigatório e não pode ser nulo ou vazio quando dataInicio é fornecido.", "dataFim", dataFim);
        } else if (dataFim != null && (dataInicio == null || dataInicio.toString().trim().isEmpty())) {
            errorObject = new ErrorObject("O campo dataInicio é obrigatório e não pode ser nulo ou vazio quando dataFim é fornecido.", "dataInicio", dataInicio);
        } else if (dataInicio != null && dataInicio.isAfter(dataFim)) {
            errorObject = new ErrorObject("A data de início deve ser anterior à data de fim.", "dataInicio", dataInicio);
        } else if (dataInicio == null && (orderId == null || orderId.toString().trim().isEmpty())) {
            errorObject = new ErrorObject("O campo orderId é obrigatório e não pode ser nulo ou vazio quando dataInicio e dataFim não são fornecidos.", "orderId", orderId);
        }

        if(errorObject != null) {
            ErrorResponse errorResponse = RestExceptionCustom.getErrorResponse("consultarPedidos", HttpStatus.BAD_REQUEST, errorObject);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        List<PedidoDto> pedidos = service.consultarPedidos(orderId, dataInicio, dataFim);
        if(pedidos.isEmpty()){
            return new ResponseEntity<>(pedidos, HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(pedidos);
        }
    }
}