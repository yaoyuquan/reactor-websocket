package io.yao.reactor.websocket.config;

import io.yao.reactor.websocket.handler.CustomHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoyuquan
 */
@Configuration
public class WebConfig {

    @Autowired
    private CustomHandler customHandler;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>(25);
        map.put("/websocket", customHandler);
        var order = -1;

        return new SimpleUrlHandlerMapping(map, order);
    }
}
