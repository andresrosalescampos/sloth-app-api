package com.slothlife.chat.api.domain.port.out;

public interface GeminiPort {

    String generateResponse(String message);
}
