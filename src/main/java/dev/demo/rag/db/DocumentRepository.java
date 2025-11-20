package dev.demo.rag.db;

import dev.demo.rag.dto.SearchResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DocumentRepository {

  private final JdbcTemplate jdbc;

  public DocumentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void upsert(String source, String chunk, List<Double> embedding) {
    String vec = toPgVectorLiteral(embedding);
    String sql = """
      INSERT INTO documents (source, chunk, embedding)
      VALUES (?, ?, ?::vector)
    """;
    jdbc.update(sql, ps -> {
      ps.setString(1, source);
      ps.setString(2, chunk);
      ps.setString(3, vec);
    });
  }

  public List<SearchResponse> searchByCosine(List<Double> queryEmbedding, int k) {
    String vec = toPgVectorLiteral(queryEmbedding);
    String sql = """
      SELECT chunk, source, (embedding <=> ?::vector) AS distance
      FROM documents
      ORDER BY embedding <=> ?::vector
      LIMIT ?
    """;
    return jdbc.query(sql,
        ps -> { ps.setString(1, vec); ps.setInt(2, k); },
        (rs, i) -> new SearchResponse(
            rs.getString("chunk"),
            rs.getString("source"),
            rs.getDouble("distance")
        ));
  }

  private String toPgVectorLiteral(List<Double> v) {
    return "[" + v.stream().map(d -> {
      String s = Double.toString(d);
      if (s.contains("E") || s.contains("e")) {
        return String.format("%.8f", d);
      }
      return s;
    }).collect(Collectors.joining(",")) + "]";
  }
}
