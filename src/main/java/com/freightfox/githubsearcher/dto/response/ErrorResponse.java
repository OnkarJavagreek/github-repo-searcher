package com.freightfox.githubsearcher.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponse {

    @Schema(description = "HTTP Status Code", example = "400")
    private int status;

    @Schema(description = "Application specific error code", example = "VAL_400")
    private String errorCode;

    @Schema(description = "Descriptive error message", example = "Search query must not be blank")
    private String message;

    @Schema(description = "Detailed field validation errors if applicable")
    private Map<String, String> validationErrors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Timestamp when error occurred", example = "2024-01-01T12:00:00Z")
    private Instant timestamp;
}
