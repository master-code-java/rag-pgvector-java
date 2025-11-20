package dev.demo.rag.dto;

import java.util.List;

public record AskResponse(String question, List<SearchResponse> context, String answer) {}
