package com.freightfox.githubsearcher.controller;

import com.freightfox.githubsearcher.dto.request.GithubSearchRequest;
import com.freightfox.githubsearcher.dto.response.ErrorResponse;
import com.freightfox.githubsearcher.dto.response.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.response.SearchResponseWrapper;
import com.freightfox.githubsearcher.service.GithubSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub Repositories", description = "Endpoints for searching GitHub repositories and managing stored results")
public class GithubRepositoryController {

    private final GithubSearchService githubSearchService;

    @PostMapping("/search")
    @Operation(
            summary = "Search GitHub repositories and persist results",
            description = "Calls the GitHub REST API, fetches repository details, performs an upsert in the database (updates existing, inserts new), and returns the results."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Repositories successfully fetched and stored",
                    content = @Content(schema = @Schema(implementation = SearchResponseWrapper.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "GitHub API rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "External GitHub API error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SearchResponseWrapper> searchRepositories(
            @Valid @RequestBody GithubSearchRequest request
    ) {
        log.info("Received search request: query='{}', language='{}', sort='{}'",
                request.getQuery(), request.getLanguage(), request.getSort());
        SearchResponseWrapper response = githubSearchService.searchAndSave(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/repositories")
    @Operation(
            summary = "Retrieve stored repositories",
            description = "Returns stored repositories from the database with optional filters (programming language, minimum stars) and sorting (stars, forks, or updated date)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Repositories successfully retrieved",
                    content = @Content(schema = @Schema(implementation = RepositoryListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter or sort parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RepositoryListResponse> getRepositories(
            @Parameter(description = "Filter by programming language (e.g. Java, Python)", example = "Java")
            @RequestParam(name = "language", required = false) String language,

            @Parameter(description = "Filter by minimum star count", example = "100")
            @RequestParam(name = "minStars", required = false) Integer minStars,

            @Parameter(description = "Sort results by stars, forks, or updated (default is stars)", example = "stars")
            @RequestParam(name = "sort", required = false, defaultValue = "stars") String sort
    ) {
        log.info("Received retrieve request: language='{}', minStars={}, sort='{}'", language, minStars, sort);
        RepositoryListResponse response = githubSearchService.getStoredRepositories(language, minStars, sort);
        return ResponseEntity.ok(response);
    }
}
