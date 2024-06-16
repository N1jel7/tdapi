package org.barossa.client.tdlib;

import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.exception.TelegramClientConfigurationException;
import lombok.Getter;
import org.barossa.handler.AuthorizationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.barossa.client.tdlib.NativeClientUtil.*;

/**
 * Main class for interaction with the TDLib.
 */
public class SingleNativeClient {

    private static final Logger log = LoggerFactory.getLogger(SingleNativeClient.class);

    /**
     * Sends a request to the TDLib.
     *
     * @param query            Object representing a query to the TDLib.
     * @param resultHandler    Result handler with onResult method which will be called with result
     *                         of the query or with TdApi.Error as parameter. If it is null, nothing
     *                         will be called.
     * @param exceptionHandler Exception handler with onException method which will be called on
     *                         exception thrown from resultHandler. If it is null, then
     *                         defaultExceptionHandler will be called.
     */
    public void send(TdApi.Function query, dev.voroby.springframework.telegram.client.Client.ResultHandler resultHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler exceptionHandler) {
        long queryId = currentQueryId.incrementAndGet();
        if (resultHandler != null) {
            handlers.put(queryId, new Handler(resultHandler, exceptionHandler));
        }
        log.info("Native client {} send message", nativeClientId);
        nativeClientSend(nativeClientId, queryId, query);
    }

    /**
     * Sends a request to the TDLib with an empty ExceptionHandler.
     *
     * @param query         Object representing a query to the TDLib.
     * @param resultHandler Result handler with onResult method which will be called with result
     *                      of the query or with TdApi.Error as parameter. If it is null, then
     *                      defaultExceptionHandler will be called.
     */
    public void send(TdApi.Function query, dev.voroby.springframework.telegram.client.Client.ResultHandler resultHandler) {
        send(query, resultHandler, null);
    }

    /**
     * Synchronously executes a TDLib request. Only a few marked accordingly requests can be executed synchronously.
     *
     * @param query Object representing a query to the TDLib.
     * @param <T>   Automatically deduced return type of the query.
     * @return request result.
     * @throws SingleClientExecutionException if query execution fails.
     */
    @SuppressWarnings("unchecked")
    public static <T extends TdApi.Object> T execute(TdApi.Function<T> query) throws SingleClientExecutionException {
        TdApi.Object object = nativeClientExecute(query);
        if (object instanceof TdApi.Error) {
            throw new SingleClientExecutionException((TdApi.Error) object);
        }
        return (T) object;
    }

    /**
     * Creates new Client.
     *
     * @param updateHandler           Handler for incoming updates.
     * @param updateExceptionHandler  Handler for exceptions thrown from updateHandler. If it is null, exceptions will be ignored.
     * @param defaultExceptionHandler Default handler for exceptions thrown from all ResultHandler. If it is null, exceptions will be ignored.
     * @return created Client
     * @throws TelegramClientConfigurationException if a Client instance has already been created for this JVM process.
     *                                              If you want to create the instance again, you should close the running instance and get AuthorizationStateClosed.
     *                                              After this, a new instance of the {@link TelegramClient} bean must be registered in context.
     */
    public synchronized static SingleNativeClient create(dev.voroby.springframework.telegram.client.Client.ResultHandler updateHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler updateExceptionHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler defaultExceptionHandler) {
        SingleNativeClient client = new SingleNativeClient(updateHandler, updateExceptionHandler, defaultExceptionHandler);
        if (!responseReceiver.isRun) {
            responseReceiver.isRun = true;
            Thread receiverThread = new Thread(responseReceiver, "TDLib thread-" + client.getNativeClientId());
            receiverThread.setDaemon(true);
            receiverThread.start();
        }
        return client;
    }

    /**
     * Sets the handler for messages that are added to the internal TDLib log.
     * None of the TDLib methods can be called from the callback.
     *
     * @param maxVerbosityLevel The maximum verbosity level of messages for which the callback will be called.
     * @param logMessageHandler Handler for messages that are added to the internal TDLib log. Pass null to remove the handler.
     */
    public static void setLogMessageHandler(int maxVerbosityLevel, dev.voroby.springframework.telegram.client.Client.LogMessageHandler logMessageHandler) {
        nativeClientSetLogMessageHandler(maxVerbosityLevel, logMessageHandler);
    }

    public static class SingleClientExecutionException extends Exception {
        /**
         * Original TDLib error occurred when performing one of the synchronous functions.
         */
        public final TdApi.Error error;

        /**
         * @param error TDLib error occurred while performing {@link #execute(TdApi.Function)}.
         */
        SingleClientExecutionException(TdApi.Error error) {
            super(error.code + ": " + error.message);
            this.error = error;
        }
    }

    private static class ResponseReceiver implements Runnable {
        public boolean isRun = false;

        @Override
        public void run() {
            while (true) {
                int resultN = nativeClientReceive(clientIds, eventIds, events, 10000.0 /*seconds*/);
                for (int i = 0; i < resultN; i++) {
                    processResult(clientIds[i], eventIds[i], events[i]);
                    events[i] = null;
                }
            }
        }

        private void processResult(int clientId, long id, TdApi.Object object) {
            boolean isClosed = false;
            if (id == 0 && object instanceof TdApi.UpdateAuthorizationState) {
                TdApi.AuthorizationState authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
                log.info("New auth for client {}, constructor: {}, state: {}", clientId, authorizationState.getConstructor(), AuthorizationState.of(authorizationState.getConstructor()));
                if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
                    isClosed = true;
                }
            }

            Handler handler = id == 0 ? updateHandlers.get(clientId) : handlers.remove(id);
            if (handler != null) {
                try {
                    handler.resultHandler.onResult(object);
                } catch (Throwable cause) {
                    dev.voroby.springframework.telegram.client.Client.ExceptionHandler exceptionHandler = handler.exceptionHandler;
                    if (exceptionHandler == null) {
                        exceptionHandler = defaultExceptionHandlers.get(clientId);
                    }
                    if (exceptionHandler != null) {
                        try {
                            exceptionHandler.onException(cause);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            if (isClosed) {
                updateHandlers.remove(clientId);           // there will be no more updates
                defaultExceptionHandlers.remove(clientId); // ignore further exceptions
                clientCount.decrementAndGet();
            }
        }

        private static final int MAX_EVENTS = 1000;
        private final int[] clientIds = new int[MAX_EVENTS];
        private final long[] eventIds = new long[MAX_EVENTS];
        private final TdApi.Object[] events = new TdApi.Object[MAX_EVENTS];
    }

    @Getter
    private final int nativeClientId;

    private static final ConcurrentHashMap<Integer, Client.ExceptionHandler> defaultExceptionHandlers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Handler> updateHandlers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Handler> handlers = new ConcurrentHashMap<>();
    private static final AtomicLong currentQueryId = new AtomicLong();
    private static final AtomicLong clientCount = new AtomicLong();

    private static final ResponseReceiver responseReceiver = new ResponseReceiver();

    private static class Handler {
        final dev.voroby.springframework.telegram.client.Client.ResultHandler resultHandler;
        final dev.voroby.springframework.telegram.client.Client.ExceptionHandler exceptionHandler;

        Handler(dev.voroby.springframework.telegram.client.Client.ResultHandler resultHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler exceptionHandler) {
            this.resultHandler = resultHandler;
            this.exceptionHandler = exceptionHandler;
        }
    }

    private SingleNativeClient(Client.ResultHandler updateHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler updateExceptionHandler, dev.voroby.springframework.telegram.client.Client.ExceptionHandler defaultExceptionHandler) {
        clientCount.incrementAndGet();
        nativeClientId = createNativeClient();
        if (updateHandler != null) {
            updateHandlers.put(nativeClientId, new Handler(updateHandler, updateExceptionHandler));
        }
        if (defaultExceptionHandler != null) {
            defaultExceptionHandlers.put(nativeClientId, defaultExceptionHandler);
        }
        send(new TdApi.GetOption("version"), null, null);
    }
}
