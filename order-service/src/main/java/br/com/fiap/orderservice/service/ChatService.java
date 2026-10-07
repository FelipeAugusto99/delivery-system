package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final DishRepository dishRepository;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            DishRepository dishRepository
    ) {
        this.chatClient = chatClientBuilder.build();
        this.dishRepository = dishRepository;
    }

    public String answer(String question) {

        List<Dish> dishes = dishRepository.findAll();

        StringBuilder menu = new StringBuilder();

        for (Dish dish : dishes) {
            menu.append("Name: ").append(dish.getName())
                    .append(", Price: R$ ").append(dish.getPrice())
                    .append(", Stock: ").append(dish.getStock())
                    .append("\n");
        }

        return chatClient.prompt()
                .system("""
                        Você é um atendente de um restaurante de delivery.

                        Responda sempre em português, de forma curta e educada.

                        Responda perguntas sobre o cardápio, preços,
                        disponibilidade e sugestões de pratos.

                        Utilize somente as informações do cardápio fornecido.
                        Não invente pratos, preços ou disponibilidade.

                        Se a pergunta não estiver relacionada ao restaurante,
                        recuse educadamente e redirecione a conversa
                        para o cardápio.

                        Cardápio atual:
                        """ + menu)
                .user(question)
                .call()
                .content();
    }
}