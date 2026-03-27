package com.slothlife.chat.api.infrastructure.adapter.in.web;

import com.slothlife.chat.api.domain.port.in.ChatbotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Chatbot", description = "Endpoints for interacting with the chatbot")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotUseCase chatbotUseCase;

    @Operation(summary = "Send a message to the chatbot", description = "Receives a text message and returns a response")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Response generated successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid message parameter")
    })
    @GetMapping
    public String chat(
            @Parameter(description = "Message to send to the chatbot", required = true, example = "Hello!")
            @RequestParam String message) {
        return chatbotUseCase.chat(message);
    }
}
