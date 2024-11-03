package com.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@Controller
@RequestMapping("/app")
public class ServerController {

    private final SimpMessagingTemplate messagingTemplate;

    // Track each printer's session ID
    private final Map<String, String> printerSessions = new ConcurrentHashMap<>();

    @Autowired
    public ServerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/registerPrinter")
    public void registerPrinter(SimpMessageHeaderAccessor headerAccessor, String printerId) {
        String sessionId = headerAccessor.getSessionId();
        printerSessions.put(printerId, sessionId); // Track the printer ID with session ID
        System.out.println("Registered printer with ID: " + printerId + " and session: " + sessionId);
    }

    @MessageMapping("/status")
    public void receiveStatus(String statusMessage) {
        // Handle status message received from printer applications
        System.out.println("Received printer status: " + statusMessage);
    }

    // Method to send a command to a specific printer
    public void sendCommandToPrinter(String printerId, String command) {
        String sessionId = printerSessions.get(printerId);
        if (sessionId != null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/command", command);
            System.out.println("Sent command to printer with ID: " + printerId);
        } else {
            System.out.println("Printer with ID " + printerId + " is not connected.");
        }
    }

    public static void main(String[] args) {
        //run the server
        new SpringApplicationBuilder(ServerController.class).run(args);

        
    }
}
