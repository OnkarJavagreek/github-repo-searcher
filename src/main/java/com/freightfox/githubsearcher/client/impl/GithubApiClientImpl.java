package com.freightfox.githubsearcher.client.impl;

import com.freightfox.githubsearcher.client.GithubApiClient;
import com.freightfox.githubsearcher.dto.github.GithubSearchResponseDto;
import com.freightfox.githubsearcher.exception.ErrorCode;
import com.freightfox.githubsearcher.exception.GithubApiException;
import com.freightfox.githubsearcher.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubApiClientImpl implements GithubApiClient {

    private final RestClient githubRestClient;

    @Override
    public GithubSearchResponseDto searchRepositories(String query, String language, String sort) {
        StringBuilder queryBuilder = new StringBuilder(query.trim());
        if (StringUtils.hasText(language)) {
            queryBuilder.append(" language:").append(language.trim());
        }

        String finalQuery = queryBuilder.toString();
        log.info("Querying GitHub API with q='{}', sort='{}'", finalQuery, sort);

        try {
            return githubRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/search/repositories")
                                .queryParam("q", finalQuery)
                                .queryParam("per_page", 30);
                        if (StringUtils.hasText(sort)) {
                            uriBuilder.queryParam("sort", sort.trim());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
                        if (status == HttpStatus.FORBIDDEN || status == HttpStatus.TOO_MANY_REQUESTS) {
                            throw new RateLimitExceededException(
                                    "GitHub API rate limit exceeded or access forbidden. Check credentials or try later."
                            );
                        }
                        throw new GithubApiException(
                                "GitHub API returned client error: " + response.getStatusText(),
                                status,
                                ErrorCode.GITHUB_API_ERROR
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
                        throw new GithubApiException(
                                "GitHub API returned server error: " + response.getStatusText(),
                                status,
                                ErrorCode.GITHUB_API_ERROR
                        );
                    })
                    .body(GithubSearchResponseDto.class);
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (GithubApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to execute GitHub API request", ex);
            throw new GithubApiException(
                    "Failed to communicate with GitHub API: " + ex.getMessage(),
                    ex,
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.GITHUB_API_ERROR
            );
        }
    }
}
