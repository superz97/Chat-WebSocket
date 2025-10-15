package com.github.superz97.chat.dto.response;

import com.github.superz97.chat.enums.WebSocketMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {

    private WebSocketMessageType type;
    private Object payload;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static WebSocketMessageDTO message(Object payload) {
        return WebSocketMessageDTO.builder()
                .type(WebSocketMessageType.MESSAGE)
                .payload(payload)
                .build();
    }

    public static WebSocketMessageDTO typed(WebSocketMessageType type, Object payload) {
        return WebSocketMessageDTO.builder()
                .type(type)
                .payload(payload)
                .build();
    }

}
