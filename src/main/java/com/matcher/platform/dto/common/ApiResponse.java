package com.matcher.platform.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Instant timestamp = Instant.now();
    private int status;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    public ApiResponse() {
    }

    public ApiResponse(Instant timestamp, int status, String message, T data, List<ErrorDetail> errors) {
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setStatus(200);
        res.setMessage(message);
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setStatus(201);
        res.setMessage(message);
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setStatus(status);
        res.setMessage(message);
        return res;
    }

    public static <T> ApiResponse<T> error(int status, String message, List<ErrorDetail> errors) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setStatus(status);
        res.setMessage(message);
        res.setErrors(errors);
        return res;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Instant timestamp = Instant.now();
        private int status;
        private String message;
        private T data;
        private List<ErrorDetail> errors;

        public Builder<T> timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<T> status(int status) {
            this.status = status;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> errors(List<ErrorDetail> errors) {
            this.errors = errors;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(timestamp, status, message, data, errors);
        }
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }
}
