package dev.demo.rag.core;

import dev.demo.rag.ai.OpenAIEmbeddingClient;
import dev.demo.rag.db.DocumentRepository;
import dev.demo.rag.dto.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

  private final OpenAIEmbeddingClient embeddings;
  private final DocumentRepository repo;
  private final RestClient chat;
  private final String chatModel;

  public RagService(OpenAIEmbeddingClient embeddings,
                    DocumentRepository repo,
                    @Value("${app.openai.apiKey}") String apiKey,
                    @Value("${app.openaiChatModel}") String chatModel) {
    this.embeddings = embeddings;
    this.repo = repo;
    this.chatModel = chatModel;
    this.chat = RestClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  public void addDocument(String source, String text) {
    var emb = embeddings.embed(text);
    repo.upsert(source, text, emb);
  }

  public List<SearchResponse> retrieve(String query, int k) {
    var qEmb = embeddings.embed(query);
    return repo.searchByCosine(qEmb, k);
  }

  public String answerWithContext(String question, int k) {
    List<SearchResponse> ctx = retrieve(question, k);
    StringBuilder contextBlock = new StringBuilder();
    for (int i = 0; i < ctx.size(); i++) {
      SearchResponse searchResponse = ctx.get(i);
      contextBlock.append("[").append(i+1).append("] (").append(searchResponse.source()).append(") ")
              .append(searchResponse.chunk())
              .append("\n");

      // [1] (report2022.html) ["o arquivo se encontra em ..."]
      // [2] (report2023.html) ["o arquivo de luz se encontra em ..."]
    }
    String system = """
      Você é um assistente técnico. Responda com base exclusivamente no CONTEXTO abaixo.
      Se não houver informação suficiente no contexto, diga honestamente.
      Mencione sempre as fontes como [n].
      CONTEXTO:
      """ + contextBlock;

    var body = Map.of(
      "model", chatModel,
      "messages", List.of(
        Map.of("role","system","content", system),
        Map.of("role","user","content", question)
      ),
      "temperature", 0.2
    );

    var resp = chat.post().uri("/chat/completions").body(body)
      .retrieve().toEntity(Map.class).getBody();

    var choices = (List<Map<String,Object>>) resp.get("choices");
    var msg = (Map<String,Object>) choices.get(0).get("message");
    return (String) msg.get("content");
  }
}
