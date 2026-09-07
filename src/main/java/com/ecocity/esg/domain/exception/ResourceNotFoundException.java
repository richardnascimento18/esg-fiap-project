package com.ecocity.esg.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String id) {
        super("%s nao encontrado(a) para o id: %s".formatted(resourceName, id));
    }
}
