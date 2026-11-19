package com.uel.bd1.erro;

import org.springframework.http.HttpStatus;

// Erro previsto pela aplicação: leva a mensagem e o código HTTP que a API deve devolver
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
