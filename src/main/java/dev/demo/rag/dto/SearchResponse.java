package dev.demo.rag.dto;

public record SearchResponse(String chunk, String source, double distance) {}