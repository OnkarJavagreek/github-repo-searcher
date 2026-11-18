package com.freightfox.githubsearcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubBaseUrl;

    @Value("${github.api.token:}")
    private String githubToken;

    @Value("${github.api.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${github.api.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Bean
    public RestClient githubRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(githubBaseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "FreightFox-GitHub-Searcher/1.0");

        if (StringUtils.hasText(githubToken)) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken.trim());
        }

        return builder.build();
    }
}
