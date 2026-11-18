package com.freightfox.githubsearcher.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of a stored GitHub repository")
public class StoredRepositoryResponse {

    @Schema(description = "Unique GitHub Repository ID", example = "123456")
    private Long id;

    @Schema(description = "Repository name", example = "spring-boot-example")
    private String name;

    @Schema(description = "Repository description", example = "An example repository for Spring Boot")
    private String description;

    @Schema(description = "Repository owner login username", example = "user123")
    private String owner;

    @Schema(description = "Primary programming language", example = "Java")
    private String language;

    @Schema(description = "Total star count", example = "450")
    private Integer stars;

    @Schema(description = "Total fork count", example = "120")
    private Integer forks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Last updated timestamp in ISO-8601 UTC", example = "2024-01-01T12:00:00Z")
    private Instant lastUpdated;
}
