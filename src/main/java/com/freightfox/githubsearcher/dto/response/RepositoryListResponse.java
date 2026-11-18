package com.freightfox.githubsearcher.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response wrapper for retrieved stored repositories")
public class RepositoryListResponse {

    @Schema(description = "List of stored repositories matching query criteria")
    private List<StoredRepositoryResponse> repositories;
}
