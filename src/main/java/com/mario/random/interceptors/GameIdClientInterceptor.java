package com.mario.random.interceptors;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameIdClientInterceptor implements ClientInterceptor {

    public static final String GAME_ID_HEADER = "game_id";

    @VisibleForTesting
    private static final Metadata.Key<String> GAME_ID_HEADER_KEY = Metadata.Key.of(GAME_ID_HEADER, Metadata.ASCII_STRING_MARSHALLER);
    private final String gameId;

    public GameIdClientInterceptor(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {

        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(GAME_ID_HEADER_KEY, gameId);
                log.info("Game Id added to header: {}", gameId);
                super.start(responseListener, headers);
            }
        };
    }
}