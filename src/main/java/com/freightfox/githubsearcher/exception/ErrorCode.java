package com.freightfox.githubsearcher.exception;

public enum ErrorCode {
    VALIDATION_ERROR("VAL_400", "Validation failed for request parameters"),
    GITHUB_RATE_LIMIT("GH_429", "GitHub API rate limit exceeded. Please try again later or configure GITHUB_TOKEN."),
    GITHUB_API_ERROR("GH_502", "External communication with GitHub API failed"),
    INVALID_SORT_PARAMETER("SORT_400", "Invalid sort field. Allowed values are 'stars', 'forks', or 'updated'"),
    INTERNAL_SERVER_ERROR("SYS_500", "An unexpected internal server error occurred");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
