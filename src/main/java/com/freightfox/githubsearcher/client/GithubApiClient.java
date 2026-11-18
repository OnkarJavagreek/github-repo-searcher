package com.freightfox.githubsearcher.client;

import com.freightfox.githubsearcher.dto.github.GithubSearchResponseDto;

public interface GithubApiClient {

    /**
     * Executes repository search on GitHub REST API.
     *
     * @param query    Keywords or repository query term
     * @param language Programming language filter (optional)
     * @param sort     Sort field (stars, forks, updated)
     * @return Search response from GitHub
     */
    GithubSearchResponseDto searchRepositories(String query, String language, String sort);
}
