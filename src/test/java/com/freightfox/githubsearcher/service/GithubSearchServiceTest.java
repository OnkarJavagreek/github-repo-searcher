package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.client.GithubApiClient;
import com.freightfox.githubsearcher.dto.github.GithubItemDto;
import com.freightfox.githubsearcher.dto.github.GithubOwnerDto;
import com.freightfox.githubsearcher.dto.github.GithubSearchResponseDto;
import com.freightfox.githubsearcher.dto.request.GithubSearchRequest;
import com.freightfox.githubsearcher.dto.response.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.response.SearchResponseWrapper;
import com.freightfox.githubsearcher.entity.StoredRepository;
import com.freightfox.githubsearcher.repository.StoredRepositoryRepository;
import com.freightfox.githubsearcher.service.impl.GithubSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubSearchServiceTest {

    @Mock
    private GithubApiClient githubApiClient;

    @Mock
    private StoredRepositoryRepository repositoryRepo;

    @InjectMocks
    private GithubSearchServiceImpl githubSearchService;

    private GithubItemDto sampleGithubItem;

    @BeforeEach
    void setUp() {
        sampleGithubItem = GithubItemDto.builder()
                .id(101L)
                .name("spring-boot-demo")
                .description("Demo Spring Boot project")
                .owner(GithubOwnerDto.builder().login("demo-user").build())
                .language("Java")
                .stargazersCount(500)
                .forksCount(120)
                .updatedAt(Instant.parse("2024-01-01T12:00:00Z"))
                .build();
    }

    @Test
    @DisplayName("searchAndSave should insert a new repository when it does not exist in DB")
    void searchAndSave_shouldInsertNewRepository() {
        GithubSearchRequest request = GithubSearchRequest.builder()
                .query("spring-boot")
                .language("Java")
                .sort("stars")
                .build();

        GithubSearchResponseDto githubResponse = GithubSearchResponseDto.builder()
                .totalCount(1)
                .items(List.of(sampleGithubItem))
                .build();

        when(githubApiClient.searchRepositories("spring-boot", "Java", "stars"))
                .thenReturn(githubResponse);
        when(repositoryRepo.findById(101L)).thenReturn(Optional.empty());
        when(repositoryRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SearchResponseWrapper result = githubSearchService.searchAndSave(request);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Repositories fetched and saved successfully");
        assertThat(result.getRepositories()).hasSize(1);
        assertThat(result.getRepositories().get(0).getId()).isEqualTo(101L);
        assertThat(result.getRepositories().get(0).getName()).isEqualTo("spring-boot-demo");
        assertThat(result.getRepositories().get(0).getOwner()).isEqualTo("demo-user");
        assertThat(result.getRepositories().get(0).getStars()).isEqualTo(500);

        verify(repositoryRepo, times(1)).findById(101L);
        verify(repositoryRepo, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("searchAndSave should update an existing repository (Upsert requirement)")
    void searchAndSave_shouldUpdateExistingRepository() {
        GithubSearchRequest request = GithubSearchRequest.builder()
                .query("spring-boot")
                .language("Java")
                .sort("stars")
                .build();

        StoredRepository existingEntity = StoredRepository.builder()
                .id(101L)
                .name("old-name")
                .description("old-desc")
                .owner("old-owner")
                .language("Java")
                .stars(100)
                .forks(10)
                .lastUpdated(Instant.parse("2023-01-01T00:00:00Z"))
                .build();

        GithubSearchResponseDto githubResponse = GithubSearchResponseDto.builder()
                .totalCount(1)
                .items(List.of(sampleGithubItem))
                .build();

        when(githubApiClient.searchRepositories("spring-boot", "Java", "stars"))
                .thenReturn(githubResponse);
        when(repositoryRepo.findById(101L)).thenReturn(Optional.of(existingEntity));
        when(repositoryRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SearchResponseWrapper result = githubSearchService.searchAndSave(request);

        assertThat(result.getRepositories()).hasSize(1);
        assertThat(existingEntity.getName()).isEqualTo("spring-boot-demo");
        assertThat(existingEntity.getStars()).isEqualTo(500);
        assertThat(existingEntity.getForks()).isEqualTo(120);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StoredRepository>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositoryRepo).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("searchAndSave should throw IllegalArgumentException when sort is invalid")
    void searchAndSave_shouldThrowExceptionForInvalidSort() {
        GithubSearchRequest request = GithubSearchRequest.builder()
                .query("spring")
                .sort("invalid_sort")
                .build();

        assertThatThrownBy(() -> githubSearchService.searchAndSave(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort field");

        verifyNoInteractions(githubApiClient);
        verifyNoInteractions(repositoryRepo);
    }

    @Test
    @DisplayName("getStoredRepositories should return list of repositories sorted by stars by default")
    void getStoredRepositories_shouldReturnSortedRepositories() {
        StoredRepository repo = StoredRepository.builder()
                .id(202L)
                .name("test-repo")
                .description("Test")
                .owner("owner1")
                .language("Java")
                .stars(150)
                .forks(20)
                .lastUpdated(Instant.now())
                .build();

        when(repositoryRepo.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(repo));

        RepositoryListResponse response = githubSearchService.getStoredRepositories("Java", 100, "stars");

        assertThat(response).isNotNull();
        assertThat(response.getRepositories()).hasSize(1);
        assertThat(response.getRepositories().get(0).getId()).isEqualTo(202L);
        assertThat(response.getRepositories().get(0).getName()).isEqualTo("test-repo");

        verify(repositoryRepo, times(1)).findAll(any(Specification.class), any(Sort.class));
    }
}
