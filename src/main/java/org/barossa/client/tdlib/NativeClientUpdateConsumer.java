package org.barossa.client.tdlib;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.updates.UpdateNotificationListener;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
final class NativeClientUpdateConsumer<T extends TdApi.Update> implements Consumer<TdApi.Object> {

    private final UpdateNotificationListener<T> notificationListener;

    private final Class<T> type;

    public NativeClientUpdateConsumer(UpdateNotificationListener<?> notificationListener, Class<?> clazz) {
        this.notificationListener = (UpdateNotificationListener<T>) notificationListener;
        this.type = (Class<T>) clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(TdApi.Object object) {
        T notification = type.cast(object);
        notificationListener.handleNotification(notification);
    }

}