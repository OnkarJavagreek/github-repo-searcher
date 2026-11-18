package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.dto.request.GithubSearchRequest;
import com.freightfox.githubsearcher.dto.response.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.response.SearchResponseWrapper;

public interface GithubSearchService {

    /**
     * Searches GitHub repositories via GitHub REST API, upserts the results in the database,
     * and returns the persisted repositories.
     *
     * @param request Search criteria (query, language, sort)
     * @return Confirmation message and list of saved repositories
     */
    SearchResponseWrapper searchAndSave(GithubSearchRequest request);

    /**
     * Retrieves stored repositories from database filtered by language and minStars,
     * sorted by stars, forks, or updated.
     *
     * @param language Programming language filter (optional)
     * @param minStars Minimum star count filter (optional)
     * @param sort     Sort attribute: 'stars', 'forks', or 'updated' (default is 'stars')
     * @return List of stored repositories matching criteria
     */
    RepositoryListResponse getStoredRepositories(String language, Integer minStars, String sort);
}
