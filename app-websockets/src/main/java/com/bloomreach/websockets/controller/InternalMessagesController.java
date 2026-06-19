package com.bloomreach.websockets.controller;

import com.bloomreach.websockets.generated.api.InternalMessagesApi;
import com.bloomreach.websockets.generated.model.SendMessageRequest;
import com.bloomreach.websockets.generated.model.SendMessageResponse;
import com.bloomreach.websockets.service.WebSocketMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for internal WebSocket message delivery operations.
 */
@RestController
@Tag(name = "Internal Messages")
public class InternalMessagesController implements InternalMessagesApi {

    private final WebSocketMessageService webSocketMessageService;

    /**
     * @param webSocketMessageService service used to deliver messages to connected clients
     */
    public InternalMessagesController(final WebSocketMessageService webSocketMessageService) {
        this.webSocketMessageService = webSocketMessageService;
    }

    @Override
    @Operation(summary = "Send a message to a user's active WebSocket connections")
    @ApiResponse(responseCode = "200", description = "Message delivery attempted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<SendMessageResponse> sendMessage(final SendMessageRequest sendMessageRequest) {
        final SendMessageResponse response = webSocketMessageService.send(
                sendMessageRequest.getUserId(),
                sendMessageRequest.getPayload()
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
