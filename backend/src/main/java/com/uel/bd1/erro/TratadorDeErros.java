package com.uel.bd1.erro;

import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> tratarApi(ApiException e) {
        return ResponseEntity.status(e.status()).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> tratarEntrada(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Confira o formato dos dados enviados."));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> tratarBanco(SQLException e) {
        String estado = e.getSQLState();
        if ("23505".equals(estado)) {
            return ResponseEntity.status(409).body(Map.of("erro", "Já existe um registro com esses dados únicos."));
        }
        if ("23503".equals(estado) || "23001".equals(estado)) {
            return ResponseEntity.status(409).body(Map.of("erro", "Uma referência não existe ou há registros vinculados que impedem a operação."));
        }
        if ("23514".equals(estado) || "23502".equals(estado) || "22001".equals(estado)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Os dados não atendem às regras de preenchimento."));
        }
        return ResponseEntity.internalServerError().body(Map.of("erro", "Não foi possível concluir a operação no banco de dados."));
    }
}
