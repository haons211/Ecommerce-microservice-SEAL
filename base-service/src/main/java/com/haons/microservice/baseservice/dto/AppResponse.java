package com.haons.microservice.baseservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.ErrorResponse;

@Getter
@Setter
public class AppResponse<T> {
    private T data;
    private ErrorResponse error;

    public AppResponse(T data) {
        this.data = data;
    }

    public AppResponse(ErrorResponse error) {
        this.error = error;
    }
}
