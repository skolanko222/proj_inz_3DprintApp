package com.example.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketClientConfig {

    public WebSocketClient webSocketClient() {
        final WebSocketClient client = new StandardWebSocketClient();

        final WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSessionHandler sessionHandler = new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("New session established!!! : " + session.getSessionId());
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.out.println("Got an exception!!!" + exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.out.println("Got an error!!!" + exception);

            }

            @Override
            public Type getPayloadType(StompHeaders headers) {

                return null;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Received : " + payload);

            }
        };
        // register printer with ID "printer123"
        try {
            stompClient.connectAsync("ws://localhost:8080/ws", sessionHandler).get().send("/app/registerPrinter", "printer123");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        return client;
    }

    public static void main(String[] args) {
        WebSocketClientConfig webSocketClientConfig = new WebSocketClientConfig();
        webSocketClientConfig.webSocketClient();
    }
}