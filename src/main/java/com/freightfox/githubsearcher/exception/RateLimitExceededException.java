package com.freightfox.githubsearcher.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends GithubApiException {

    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, ErrorCode.GITHUB_RATE_LIMIT);
    }
}
