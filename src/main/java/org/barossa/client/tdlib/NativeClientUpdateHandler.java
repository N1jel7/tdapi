package org.barossa.client.tdlib;

import dev.voroby.springframework.telegram.client.Client;
import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class NativeClientUpdateHandler implements Client.ResultHandler {

    private final Map<Integer, Consumer<TdApi.Object>> tdUpdateHandlers = new HashMap<>();

    private final Client.ResultHandler defaultHandler;

    public NativeClientUpdateHandler(Collection<UpdateNotificationListener<?>> notifications, Client.ResultHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        notifications.forEach(ntf -> {
            var handler = new NativeClientUpdateConsumer<>(ntf, ntf.notificationType());
            tdUpdateHandlers.putIfAbsent(getConstructorNumberOfType(ntf), handler);
        });
    }

    private int getConstructorNumberOfType(UpdateNotificationListener<?> updateNotification) {
        try {
            TdApi.Update tmp = updateNotification.notificationType().getConstructor().newInstance();
            return tmp.getConstructor();
        } catch (ReflectiveOperationException e) {
            throw new TelegramClientTdApiException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(TdApi.Object object) {
        tdUpdateHandlers.getOrDefault(object.getConstructor(), defaultHandler::onResult).
                accept(object);
    }

}