package io.yao.reactor.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;


/**
 * @author yaoyuquan
 */
@Component
@Slf4j
public class CustomHandler implements WebSocketHandler {

    public static final Map<String, Consumer<String>> MAP = new HashMap<>();


    @Override
    public Mono<Void> handle(WebSocketSession session) {

        log.info("receive connection");

        URI uri = session.getHandshakeInfo().getUri();
        String query = uri.getQuery();


        String[] pair = StringUtils.split(query, "&");

        if(pair != null) {
            Optional<String> optional = Arrays.stream(pair)
                    .filter(kv -> StringUtils.startsWith(kv, "sessionId"))
                    .map(kv -> StringUtils.split(kv, "="))
                    .filter(Objects::nonNull)
                    .filter(params -> params.length > 1)
                    .map(params -> params[1])
                    .findFirst();

            if(optional.isPresent()) {
                String sessionId = optional.get();

                Mono<Void> input = session.receive()
                        .doOnNext(this::handleMessage)
                        .doOnError(throwable -> log.error(throwable.getMessage()))
                        .doOnComplete(() -> log.info("input complete {}", sessionId))
                        .doOnTerminate(() -> log.info("input terminate {}", sessionId))
                        .doOnCancel(() -> log.info("input cancel {}", sessionId))
                        .then();

                Mono<Void> output = session
                        .send(Flux.create(sink -> MAP.put(sessionId, text -> sink.next(session.textMessage(text)))))
                        .doOnError(throwable -> log.info(throwable.getMessage()))
                        .doOnCancel(() -> log.info("output cancel {}", sessionId))
                        .doOnTerminate(() -> log.info("output terminate"));

                return Mono.zip(input, output).then();

            }
        }

        return session.send(Flux.just(session.textMessage("sessionId 不能为空")));


    }

    private void handleMessage(WebSocketMessage message) {
        String payload = message.getPayloadAsText();
        log.info("receive websocket message {}", payload);
    }
}
