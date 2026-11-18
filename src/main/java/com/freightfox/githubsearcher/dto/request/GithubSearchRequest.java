package com.freightfox.githubsearcher.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to search GitHub repositories and persist results")
public class GithubSearchRequest {

    @NotBlank(message = "Search query must not be blank")
    @Schema(description = "Keywords or repository name to search for", example = "spring boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "Target programming language filter", example = "Java")
    private String language;

    @Schema(description = "Sort criteria: 'stars', 'forks', or 'updated'", example = "stars", defaultValue = "stars")
    private String sort;
}
