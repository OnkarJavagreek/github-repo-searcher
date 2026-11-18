package com.freightfox.githubsearcher.service.impl;

import com.freightfox.githubsearcher.client.GithubApiClient;
import com.freightfox.githubsearcher.dto.github.GithubItemDto;
import com.freightfox.githubsearcher.dto.github.GithubSearchResponseDto;
import com.freightfox.githubsearcher.dto.request.GithubSearchRequest;
import com.freightfox.githubsearcher.dto.response.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.response.SearchResponseWrapper;
import com.freightfox.githubsearcher.dto.response.StoredRepositoryResponse;
import com.freightfox.githubsearcher.entity.StoredRepository;
import com.freightfox.githubsearcher.repository.StoredRepositoryRepository;
import com.freightfox.githubsearcher.repository.specification.RepositorySpecification;
import com.freightfox.githubsearcher.service.GithubSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubSearchServiceImpl implements GithubSearchService {

    private static final Set<String> ALLOWED_SORTS = Set.of("stars", "forks", "updated");

    private final GithubApiClient githubApiClient;
    private final StoredRepositoryRepository repositoryRepo;

    @Override
    @Transactional
    public SearchResponseWrapper searchAndSave(GithubSearchRequest request) {
        String sort = request.getSort();
        if (StringUtils.hasText(sort) && !ALLOWED_SORTS.contains(sort.trim().toLowerCase())) {
            throw new IllegalArgumentException("Invalid sort field: '" + sort + "'. Allowed values: stars, forks, updated");
        }

        GithubSearchResponseDto searchResult = githubApiClient.searchRepositories(
                request.getQuery(),
                request.getLanguage(),
                request.getSort()
        );

        List<GithubItemDto> items = searchResult.getItems();
        log.info("Received {} repositories from GitHub for query='{}'", items.size(), request.getQuery());

        List<StoredRepository> entitiesToPersist = new ArrayList<>();

        for (GithubItemDto item : items) {
            if (item.getId() == null) {
                continue;
            }

            Optional<StoredRepository> existingOpt = repositoryRepo.findById(item.getId());
            StoredRepository entity;

            if (existingOpt.isPresent()) {
                // Upsert logic: Update existing entity details instead of duplicating
                entity = existingOpt.get();
                entity.setName(item.getName());
                entity.setDescription(item.getDescription());
                entity.setOwner(item.getOwner() != null ? item.getOwner().getLogin() : "unknown");
                entity.setLanguage(item.getLanguage());
                entity.setStars(item.getStargazersCount() != null ? item.getStargazersCount() : 0);
                entity.setForks(item.getForksCount() != null ? item.getForksCount() : 0);
                entity.setLastUpdated(item.getUpdatedAt());
                log.debug("Updating existing repository with id={}", entity.getId());
            } else {
                // New repository: Insert
                entity = StoredRepository.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .owner(item.getOwner() != null ? item.getOwner().getLogin() : "unknown")
                        .language(item.getLanguage())
                        .stars(item.getStargazersCount() != null ? item.getStargazersCount() : 0)
                        .forks(item.getForksCount() != null ? item.getForksCount() : 0)
                        .lastUpdated(item.getUpdatedAt())
                        .build();
                log.debug("Creating new repository record with id={}", entity.getId());
            }

            entitiesToPersist.add(entity);
        }

        List<StoredRepository> savedEntities = repositoryRepo.saveAll(entitiesToPersist);

        List<StoredRepositoryResponse> responses = savedEntities.stream()
                .map(this::mapToResponse)
                .toList();

        return SearchResponseWrapper.builder()
                .message("Repositories fetched and saved successfully")
                .repositories(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryListResponse getStoredRepositories(String language, Integer minStars, String sort) {
        Sort sortOrder = resolveSortOrder(sort);
        Specification<StoredRepository> specification = RepositorySpecification.withFilters(language, minStars);

        List<StoredRepository> repositories = repositoryRepo.findAll(specification, sortOrder);
        log.info("Found {} stored repositories matching filters [language={}, minStars={}, sort={}]",
                repositories.size(), language, minStars, sort);

        List<StoredRepositoryResponse> responseList = repositories.stream()
                .map(this::mapToResponse)
                .toList();

        return RepositoryListResponse.builder()
                .repositories(responseList)
                .build();
    }

    private Sort resolveSortOrder(String sort) {
        if (!StringUtils.hasText(sort)) {
            // Default sort is stars descending as per specification
            return Sort.by(Sort.Direction.DESC, "stars");
        }

        String normalizedSort = sort.trim().toLowerCase();
        return switch (normalizedSort) {
            case "stars" -> Sort.by(Sort.Direction.DESC, "stars");
            case "forks" -> Sort.by(Sort.Direction.DESC, "forks");
            case "updated" -> Sort.by(Sort.Direction.DESC, "lastUpdated");
            default -> throw new IllegalArgumentException(
                    "Invalid sort field: '" + sort + "'. Allowed values: stars, forks, updated"
            );
        };
    }

    private StoredRepositoryResponse mapToResponse(StoredRepository entity) {
        return StoredRepositoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .owner(entity.getOwner())
                .language(entity.getLanguage())
                .stars(entity.getStars())
                .forks(entity.getForks())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
