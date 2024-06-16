package org.barossa.handler;

import org.barossa.client.SingleTelegramClient;

public interface ClientStateUpdateListener {
    void onUpdate(SingleTelegramClient client);
}
