package br.com.fiap.orderservice.controller;

import br.com.fiap.orderservice.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final ChatService chatService;

    public AssistantController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> ask(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String answer = chatService.answer(question);

        return Map.of("answer", answer);
    }
}