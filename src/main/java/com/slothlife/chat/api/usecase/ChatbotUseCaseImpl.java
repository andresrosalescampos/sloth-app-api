package com.slothlife.chat.api.usecase;

import com.slothlife.chat.api.domain.port.in.ChatbotUseCase;
import com.slothlife.chat.api.domain.port.out.GeminiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotUseCaseImpl implements ChatbotUseCase {

    private final GeminiPort geminiPort;

    @Override
    public String chat(String message) {
        log.info("Processing chat message");
        return geminiPort.generateResponse(message);
    }
}
