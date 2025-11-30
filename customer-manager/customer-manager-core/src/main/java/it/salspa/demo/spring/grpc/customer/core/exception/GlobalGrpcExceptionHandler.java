package it.salspa.demo.spring.grpc.customer.core.exception;


import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.util.NoSuchElementException;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    /*
    * Gestione generale delle eccezioni gRPC.
    * Mappa le eccezioni generiche a uno status gRPC INTERNAL con una descrizione dell'errore.
    * Codici di stato gRPC comuni:
    *   - INVALID_ARGUMENT - dati di input non validi
    *   - NOT_FOUND - risorsa non trovata
    *   - ALREADY_EXISTS - risorsa già esistente
    *   - PERMISSION_DENIED - permessi insufficienti
    *   - INTERNAL - errore generico del server
     */

    @GrpcExceptionHandler(Exception.class)
    public StatusException handleGeneralException(Exception ex) {
        return Status.INTERNAL
                .withDescription("Errore interno nel server: " + ex.getMessage())
                .asException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException ex) {
        return Status.INVALID_ARGUMENT
                .withDescription("Argomento non valido: " + ex.getMessage())
                .asException();
    }

    @GrpcExceptionHandler(NoSuchElementException.class)
    public StatusException handleNoSuchElementException(NoSuchElementException ex) {
        return Status.NOT_FOUND
                .withDescription("Elemento non trovato: " + ex.getMessage())
                .asException();
    }
}
