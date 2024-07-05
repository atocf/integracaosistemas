package br.com.atocf.pedido.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import br.com.atocf.pedido.model.error.ErrorObject;
import br.com.atocf.pedido.model.error.ErrorResponse;

public class RestExceptionCustom {

    public static ErrorResponse getErrorResponse(String objeto_name, HttpStatus status, ErrorObject error) {
        List<ErrorObject> errors= new ArrayList<ErrorObject>();
        errors.add(error);
        return new ErrorResponse("Requisição possui campos inválidos!", status.value(),
                status.getReasonPhrase(), objeto_name, errors);
    }
}