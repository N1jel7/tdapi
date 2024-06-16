package org.barossa.client;

import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.QueryResultHandler;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.barossa.client.tdlib.NativeClientUpdateHandler;
import org.barossa.client.tdlib.SingleNativeClient;
import org.barossa.handler.AuthorizationState;
import org.barossa.handler.ClientStateUpdateListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SingleTelegramClient implements UpdateNotificationListener<TdApi.UpdateAuthorizationState> {
    private static final int LOG_VERBOSITY_LEVEL = 0;

    private final SingleNativeClient client;
    private final Collection<ClientStateUpdateListener> stateUpdateListeners;
    @Getter
    private AuthorizationState clientState;

    public SingleTelegramClient(Collection<ClientStateUpdateListener> stateUpdateListeners) {
        this.client = initializeNativeClient(Collections.singleton(this));
        this.stateUpdateListeners = stateUpdateListeners;
    }

    private SingleNativeClient initializeNativeClient(Collection<UpdateNotificationListener<?>> notificationHandlers) {
        var logVerbosityLevel = new TdApi.SetLogVerbosityLevel(LOG_VERBOSITY_LEVEL);
        try {
            SingleNativeClient.execute(logVerbosityLevel);
        } catch (SingleNativeClient.SingleClientExecutionException e) {
            logError(logVerbosityLevel, e.error);
            throw new RuntimeException(e);
        }
        Client.LogMessageHandler logMessageHandler = (level, message) -> {
            switch (level) {
                case 0, 1 -> log.error(message);
                case 2 -> log.warn(message);
                case 3 -> log.info(message);
                default -> log.debug(message);
            }
        };
        Client.setLogMessageHandler(LOG_VERBOSITY_LEVEL, logMessageHandler);

        Client.ResultHandler defaultHandler = o -> log.debug("Default handler: {}", o);

        return SingleNativeClient.create(new NativeClientUpdateHandler(notificationHandlers, defaultHandler), null, null);
    }

    void close() {
        sendSync(new TdApi.Close());
    }

    public int getNativeClientId() {
        return client.getNativeClientId();
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @return response from TDLib.
     * @throws NullPointerException         if query is null.
     * @throws TelegramClientTdApiException for TDLib request timeout or returned {@link TdApi.Error}.
     */
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> T sendSync(TdApi.Function<T> query) {
        Objects.requireNonNull(query);
        var ref = new AtomicReference<TdApi.Object>();
        client.send(query, ref::set);
        var sent = Instant.now();
        while (ref.get() == null &&
                sent.plus(60, ChronoUnit.SECONDS).isAfter(Instant.now())) {
            /*wait for result*/
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            }
        }

        TdApi.Object obj = ref.get();
        if (obj == null) {
            throw new TelegramClientTdApiException("TDLib request timeout.");
        }
        if (obj instanceof TdApi.Error err) {
            logError(query, err);
            throw new TelegramClientTdApiException("Received an error from TDLib.", err, query);
        }

        return (T) obj;
    }

    /**
     * Sends a request to the TDLib asynchronously.
     * If this stage completes exceptionally you can handle cause {@link TelegramClientTdApiException}
     *
     * @param query object representing a query to the TDLib.
     * @return {@link CompletableFuture<Response>} response from TDLib.
     * @throws NullPointerException if query is null.
     */
    public <T extends TdApi.Object> CompletableFuture<Response<T>> sendAsync(TdApi.Function<T> query) {
        Objects.requireNonNull(query);
        var future = new CompletableFuture<Response<T>>();
        sendWithCallback(query, ((obj, error) -> {
            if (error != null) {
                logError(query, error);
            }
            future.complete(new Response<>(obj, error));
        }));
        return future;
    }

    private static void logError(TdApi.Function<?> query, TdApi.Error error) {
        String errorLogString = String.format("""
                TDLib error:
                [
                    code: %d,
                    message: %s
                    queryIdentifier: %d
                ]
                """, error.code, error.message, query.getConstructor());
        log.error(errorLogString);
    }


    /**
     * Sends a request to the TDLib with callback.
     *
     * @param query         object representing a query to the TDLib
     * @param resultHandler Result handler for results of queries with callback to TDLib
     * @param <T>           The object type that is returned by the function
     */
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> void sendWithCallback(TdApi.Function<T> query,
                                                          QueryResultHandler<T> resultHandler) {
        Objects.requireNonNull(query);
        client.send(query, object -> {
            if (object instanceof TdApi.Error err) {
                resultHandler.onResult(null, err);
            } else {
                resultHandler.onResult((T) object, null);
            }
        });
    }

    @Override
    public void handleNotification(TdApi.UpdateAuthorizationState notification) {
        int stateConstructor = notification.authorizationState.getConstructor();
        updateState(AuthorizationState.of(stateConstructor));
    }

    @Override
    public Class<TdApi.UpdateAuthorizationState> notificationType() {
        return TdApi.UpdateAuthorizationState.class;
    }

    public void updateState(AuthorizationState newState) {
        log.info("Client-{}: State updated to {}", client.getNativeClientId(), newState);
        clientState = newState;
        stateUpdateListeners.forEach(listener -> listener.onUpdate(this));
    }
}
