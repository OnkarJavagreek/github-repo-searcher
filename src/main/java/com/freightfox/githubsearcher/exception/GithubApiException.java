package com.freightfox.githubsearcher.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GithubApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public GithubApiException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public GithubApiException(String message, Throwable cause, HttpStatus status, ErrorCode errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
