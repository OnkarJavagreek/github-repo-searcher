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
@Schema(description = "Response wrapper for search and store operation")
public class SearchResponseWrapper {

    @Schema(description = "Operation status message", example = "Repositories fetched and saved successfully")
    private String message;

    @Schema(description = "List of saved/updated repositories")
    private List<StoredRepositoryResponse> repositories;
}
