package org.barossa.config;

import dev.voroby.springframework.telegram.TelegramClientAutoConfiguration;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import dev.voroby.springframework.telegram.properties.TelegramProperties;
import org.barossa.handler.TelegramClientAuthorizationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class TelegramClientConfiguration extends TelegramClientAutoConfiguration {

    @Override
    public TelegramClientAuthorizationHandler updateAuthorizationNotification(TelegramProperties properties,@Lazy TelegramClient telegramClient, ClientAuthorizationState clientAuthorizationState) {
        return new TelegramClientAuthorizationHandler(properties, telegramClient);
    }
}
