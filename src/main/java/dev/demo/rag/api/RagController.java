package dev.demo.rag.api;

import dev.demo.rag.core.RagService;
import dev.demo.rag.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins="*", allowedHeaders="*", methods={})
@RestController
@RequestMapping("/api")
public class RagController {
  private final RagService rag;

  public RagController(RagService rag) { this.rag = rag; }

  @PostMapping("/docs")
  public void upsert(@RequestBody UpsertDocRequest req) {
    rag.addDocument(req.source(), req.text());
  }

  @GetMapping("/search")
  public List<SearchResponse> search(@RequestParam String q,
                                     @RequestParam(defaultValue = "5") int k) {
    return rag.retrieve(q, k);
  }

  @PostMapping("/ask")
  public AskResponse ask(@RequestBody AskRequest req) {
    var ctx = rag.retrieve(req.question(), req.k());
    var answer = rag.answerWithContext(req.question(), req.k());
    return new AskResponse(req.question(), ctx, answer);
  }
}
