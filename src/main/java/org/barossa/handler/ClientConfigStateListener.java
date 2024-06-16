package org.barossa.handler;

import dev.voroby.springframework.telegram.client.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.client.SingleTelegramClient;
import org.barossa.properties.TelegramClientProperties;

import java.nio.charset.StandardCharsets;

import static org.barossa.handler.AuthorizationState.WAIT_FOR_CONFIG;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@RequiredArgsConstructor
public class ClientConfigStateListener implements ClientStateUpdateListener {
    private final TelegramClientProperties properties;

    @Override
    public void onUpdate(SingleTelegramClient client) {
        if (WAIT_FOR_CONFIG.equals(client.getClientState())) {
            log.info("Setting up client-{} config", client.getNativeClientId());
            TdApi.SetTdlibParameters tdLibParameters = tdLibParameters();
            client.sendAsync(tdLibParameters);
        }
    }

    private TdApi.SetTdlibParameters tdLibParameters() {
        boolean useTestDc = properties.useTestDc();
        String databaseDirectory = checkStringOrEmpty(properties.databaseDirectory());
        String filesDirectory = checkStringOrEmpty(properties.filesDirectory());
        byte[] databaseEncryptionKey = properties.databaseEncryptionKey().getBytes(StandardCharsets.UTF_8);
        boolean useFileDatabase = properties.useFileDatabase();
        boolean useChatInfoDatabase = properties.useChatInfoDatabase();
        boolean useMessageDatabase = properties.useMessageDatabase();
        boolean useSecretChats = properties.useSecretChats();
        int apiId = properties.apiId();
        String apiHash = properties.apiHash();
        String systemLanguageCode = properties.systemLanguageCode();
        String deviceModel = properties.deviceModel();
        String systemVersion = checkStringOrEmpty(properties.systemVersion());
        String applicationVersion = properties.applicationVersion();
        return new TdApi.SetTdlibParameters(
                useTestDc,
                databaseDirectory,
                filesDirectory,
                databaseEncryptionKey,
                useFileDatabase,
                useChatInfoDatabase,
                useMessageDatabase,
                useSecretChats,
                apiId,
                apiHash,
                systemLanguageCode,
                deviceModel,
                systemVersion,
                applicationVersion
        );
    }

    private static String checkStringOrEmpty(String s) {
        return hasText(s) ? s : "";
    }
}
