package com.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
        //if the printer is already registered with a different session, remove the old session, print a message, and register the new session
        if (printerSessions.containsKey(printerId)) {
            String oldSessionId = printerSessions.get(printerId);
            printerSessions.remove(printerId);
            System.out.println("Printer with ID " + printerId + " reconnected with new session: " + headerAccessor.getSessionId());
        }

        String sessionId = headerAccessor.getSessionId();
        printerSessions.put(printerId, sessionId); // Track the printer ID with session ID
        System.out.println("Registered printer with ID: " + printerId + " and session: " + sessionId);
    }

    @MessageMapping("/status")
    public void receiveStatus(String statusMessage) {
        // Handle status message received from printer applications
        System.out.println("Received printer status: " + statusMessage);

    }

    // New endpoint to receive JSON commands and send to the appropriate printer
    @MessageMapping("/command")
    public String handleCommand(@RequestBody Map<String, String> commandPayload) {
        System.out.println("Received command payload: " + commandPayload);
        // Extract printerId and command from the JSON payload
        String printerId = commandPayload.get("printerId");
        String command = commandPayload.get("command");
        System.out.println("Sent command to printer with ID: " + printerId);

        if (printerId == null || command == null) {
            return "Invalid payload: printerId and command are required.";
        }

        // Send the command to the specified printer
        sendCommandToPrinter(printerId, command);

        return "Command sent to printer with ID: " + printerId;
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
