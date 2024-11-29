package connection.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.lang.reflect.Type;

@Configuration
public class WebSocketClientConfig implements StompSessionHandler {
    final WebSocketClient client = new StandardWebSocketClient();
    final WebSocketStompClient stompClient = new WebSocketStompClient(client);
    StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println(connectedHeaders);

        System.out.println("New session established!!! : " + session.getSessionId());
        this.session = session;
        session.subscribe("/queue/command", this);
    }

    //create

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
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("Received : " + payload);

    }

    public WebSocketClientConfig() {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.connectAsync("ws://localhost:8080/ws", this);

    }

    public void registerPrinter(String printerId) {
        session.send("/app/registerPrinter", printerId);
    }

    public void sendStatus() {
        StatusPayload payload = new StatusPayload();
        payload.setPrinterId("printer123");
        payload.setStatus("ready");

        StompHeaders headers = new StompHeaders();
        headers.setDestination("/app/status");
        headers.set("content-type", "application/json");

        // Send the StatusPayload object directly, not a JSON string
        System.out.println("Sending status payload: " + payload);
        session.send(headers, payload);
    }

    public static void main(String[] args) throws InterruptedException {
        //connect
        System.out.println("Connecting to server...");
        WebSocketClientConfig client = new WebSocketClientConfig();
        Thread.sleep(1000);
        //register printer
        System.out.println("Registering printer...");
        client.registerPrinter("printer123");
        Thread.sleep(1000);
        client.sendStatus();


        //await termination
        synchronized (client) {
            client.wait();
        }


    }
}