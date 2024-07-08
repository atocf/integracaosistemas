package br.com.atocf.pedido.controller;

import br.com.atocf.pedido.model.dto.PedidoDto;
import br.com.atocf.pedido.model.dto.SuccessResponse;
import br.com.atocf.pedido.model.entity.Orders;
import br.com.atocf.pedido.model.entity.Users;
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
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public List<PedidoDto> consultarPedidos(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        return service.consultarPedidos(orderId, dataInicio, dataFim);
    }
}