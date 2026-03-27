package com.slothlife.chat.api.infrastructure.adapter.out.gemini.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        return candidates.stream()
                .findFirst()
                .map(Candidate::content)
                .map(Content::parts)
                .flatMap(parts -> parts.stream().findFirst())
                .map(Part::text)
                .orElse("");
    }
}
