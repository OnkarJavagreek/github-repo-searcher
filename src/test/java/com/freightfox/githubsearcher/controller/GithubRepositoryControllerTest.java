package com.freightfox.githubsearcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.githubsearcher.dto.request.GithubSearchRequest;
import com.freightfox.githubsearcher.dto.response.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.response.SearchResponseWrapper;
import com.freightfox.githubsearcher.dto.response.StoredRepositoryResponse;
import com.freightfox.githubsearcher.exception.GlobalExceptionHandler;
import com.freightfox.githubsearcher.exception.RateLimitExceededException;
import com.freightfox.githubsearcher.service.GithubSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubRepositoryController.class)
@Import(GlobalExceptionHandler.class)
class GithubRepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GithubSearchService githubSearchService;

    @Test
    @DisplayName("POST /api/github/search - Success returns 200 with persisted repositories")
    void searchRepositories_success() throws Exception {
        GithubSearchRequest request = GithubSearchRequest.builder()
                .query("spring boot")
                .language("Java")
                .sort("stars")
                .build();

        StoredRepositoryResponse repoResponse = StoredRepositoryResponse.builder()
                .id(123456L)
                .name("spring-boot-example")
                .description("An example repository for Spring Boot")
                .owner("user123")
                .language("Java")
                .stars(450)
                .forks(120)
                .lastUpdated(Instant.parse("2024-01-01T12:00:00Z"))
                .build();

        SearchResponseWrapper serviceResponse = SearchResponseWrapper.builder()
                .message("Repositories fetched and saved successfully")
                .repositories(List.of(repoResponse))
                .build();

        when(githubSearchService.searchAndSave(any(GithubSearchRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Repositories fetched and saved successfully")))
                .andExpect(jsonPath("$.repositories", hasSize(1)))
                .andExpect(jsonPath("$.repositories[0].id", is(123456)))
                .andExpect(jsonPath("$.repositories[0].name", is("spring-boot-example")))
                .andExpect(jsonPath("$.repositories[0].owner", is("user123")))
                .andExpect(jsonPath("$.repositories[0].stars", is(450)));
    }

    @Test
    @DisplayName("POST /api/github/search - Blank query returns 400 Bad Request with validation error")
    void searchRepositories_blankQuery_returns400() throws Exception {
        GithubSearchRequest invalidRequest = GithubSearchRequest.builder()
                .query("   ") // blank
                .language("Java")
                .build();

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VAL_400")))
                .andExpect(jsonPath("$.validationErrors.query", containsString("must not be blank")));
    }

    @Test
    @DisplayName("POST /api/github/search - Rate limit returns 429 Too Many Requests")
    void searchRepositories_rateLimit_returns429() throws Exception {
        GithubSearchRequest request = GithubSearchRequest.builder()
                .query("spring")
                .build();

        when(githubSearchService.searchAndSave(any(GithubSearchRequest.class)))
                .thenThrow(new RateLimitExceededException("GitHub rate limit exceeded"));

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode", is("GH_429")))
                .andExpect(jsonPath("$.message", containsString("rate limit exceeded")));
    }

    @Test
    @DisplayName("GET /api/github/repositories - Retrieves repositories with filter and sort parameters")
    void getRepositories_success() throws Exception {
        StoredRepositoryResponse repoResponse = StoredRepositoryResponse.builder()
                .id(123456L)
                .name("spring-boot-example")
                .description("An example repository for Spring Boot")
                .owner("user123")
                .language("Java")
                .stars(450)
                .forks(120)
                .lastUpdated(Instant.parse("2024-01-01T12:00:00Z"))
                .build();

        RepositoryListResponse response = RepositoryListResponse.builder()
                .repositories(List.of(repoResponse))
                .build();

        when(githubSearchService.getStoredRepositories("Java", 100, "stars")).thenReturn(response);

        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories", hasSize(1)))
                .andExpect(jsonPath("$.repositories[0].name", is("spring-boot-example")))
                .andExpect(jsonPath("$.repositories[0].stars", is(450)));
    }
}
