package com.example.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyWebsocketMessageMessageConverter extends AbstractMessageConverter {
    private static final JsonMapper jsonMapper = new JsonMapper();

    public MyWebsocketMessageMessageConverter() {
        super(MimeTypeUtils.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return MyWebsocketMessage.class == clazz;
    }

    @Nullable
    protected Object convertFromInternal(Message<?> message, @Nullable Class<?> targetClass, @Nullable Object conversionHint) {
        try {
            if (message.getPayload() instanceof byte[] bytes) {
                return jsonMapper.readValue(bytes, MyWebsocketMessage.class);
            } else {
                throw new RuntimeException("Unknown message type " + message.getPayload().getClass().getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unmarshal " + message, e);
        }
    }

    @Nullable
    protected Object convertToInternal(Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint) {
        try {
            return jsonMapper.writeValueAsString(payload).getBytes(UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to marshal " + payload, e);
        }
    }
}
