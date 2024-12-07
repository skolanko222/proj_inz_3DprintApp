package com.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@Controller
public class ServerController {

    private final SimpMessagingTemplate messagingTemplate;

    // Track each printer's session ID
    private final Map<String, String> printerSessions = new ConcurrentHashMap<>();
    private final Map<String, String> clientSesions = new ConcurrentHashMap<>();


    @Autowired
    public ServerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/registerPrinter")
    public void registerPrinter(StompHeaderAccessor headerAccessor, String printerId) {
        //if the printer is already registered with a different session, remove the old session, print a message, and register the new session
        System.out.println(headerAccessor);
        String sessionId = headerAccessor.getSessionId();
        if (printerSessions.containsKey(printerId)) {
            String oldSessionId = printerSessions.get(printerId);
            printerSessions.remove(printerId);
            System.out.println("Printer with ID " + printerId + " reconnected with new session: " + sessionId);
        }

        printerSessions.put(printerId, sessionId); // Track the printer ID with session ID
        System.out.println("Registered printer with ID: " + printerId + " and session: " + sessionId);
    }

    @MessageMapping("/registerClient")
    public void registerClient(@RequestBody StatusPayload statusPayload) {
        String clientId = statusPayload.getPrinterId();
        String sessionId = statusPayload.getStatus();
        if (clientId == null || sessionId == null) {
            System.out.println("Invalid payload: printerId and status are required.");
            return;
        }
        clientSesions.put(clientId, sessionId);
        System.out.println("Registered client with ID: " + clientId + " and status xDD: " + sessionId);
    }

    @MessageMapping("/status")
    public void handleStatus(@RequestBody StatusPayload statusPayload) {
        System.out.println("Received status payload: " + statusPayload);
        // Extract printerId and status from the JSON payload
        String printerId = statusPayload.getPrinterId();

        if (printerId == null || statusPayload.getStatus() == null) {
            System.out.println("Invalid payload: printerId and status are required.");
            return;
        }
        // Send the status to the specified client
        sendStatusToClient(printerId, statusPayload);

    }

    // New endpoint to receive JSON commands and send to the appropriate printer
    @MessageMapping("/command")
    public void handleCommand(@RequestBody Map<String, String> commandPayload) {
        System.out.println("Received command payload: " + commandPayload);
        // Extract printerId and command from the JSON payload
        String printerId = commandPayload.get("printerId");
        String command = commandPayload.get("command");

        if (printerId == null || command == null) {
            System.out.println("Invalid payload: printerId and command are required.");
            return;
        }
        // Send the command to the specified printer
        sendCommandToPrinter(printerId, command);
    }

    // Method to send a command to a specific printer
    public void sendCommandToPrinter(String printerId, String command) {
        String sessionId = printerSessions.get(printerId);
        if (sessionId != null) {
            messagingTemplate.convertAndSend("/queue/command", command);

        } else {
            System.out.println("Printer with ID " + printerId + " is not connected.");
        }
    }

    // Method to send a status message to a specific client
    public void sendStatusToClient(String clientId, StatusPayload statusMessage) {
        String sessionId = clientSesions.get(clientId);
        if (sessionId != null) {
            messagingTemplate.convertAndSend("/queue/status", statusMessage);
        } else {
            System.out.println("Client with ID " + clientId + " is not connected.");
        }
    }



    public static void main(String[] args) {
        //run the server
        new SpringApplicationBuilder(ServerController.class).run(args);
    }
}
