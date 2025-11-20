package dev.demo.rag.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIEmbeddingClient {

  private final RestClient http;
  private final String model;

  public OpenAIEmbeddingClient(
      @Value("${app.openai.apiKey}") String apiKey,
      @Value("${app.openai.embeddingModel}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  public List<Double> embed(String text) {
    Map<String,Object> body = Map.of("input", text, "model", model);
    var resp = http.post().uri("/embeddings").body(body).retrieve()
        .toEntity(EmbeddingsResponse.class).getBody();
    if (resp == null || resp.data == null || resp.data.isEmpty()) {
      throw new RuntimeException("Falha ao obter embeddings");
    }
    return resp.data.get(0).embedding;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class EmbeddingsResponse {
    public List<EmbeddingData> data;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class EmbeddingData {
    public List<Double> embedding;
  }
}
