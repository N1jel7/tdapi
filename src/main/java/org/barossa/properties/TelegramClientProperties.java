package org.barossa.properties;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@ConfigurationProperties(prefix = "spring.telegram.client")
public record TelegramClientProperties(
        boolean useTestDc,
        String databaseDirectory,
        String filesDirectory,
        String databaseEncryptionKey,
        boolean useFileDatabase,
        boolean useChatInfoDatabase,
        boolean useMessageDatabase,
        boolean useSecretChats,
        int apiId,
        String apiHash,
        String systemLanguageCode,
        String deviceModel,
        String systemVersion,
        String applicationVersion,
        int logVerbosityLevel,
        Proxy proxy
) {

    public static TelegramClientPropertiesBuilder of(TelegramClientProperties properties){
        return builder()
                .useTestDc(properties.useTestDc())
                .databaseDirectory(properties.databaseDirectory())
                .filesDirectory(properties.filesDirectory())
                .databaseEncryptionKey(properties.databaseEncryptionKey())
                .useFileDatabase(properties.useFileDatabase())
                .useChatInfoDatabase(properties.useChatInfoDatabase())
                .useMessageDatabase(properties.useMessageDatabase())
                .useSecretChats(properties.useSecretChats())
                .apiId(properties.apiId())
                .apiHash(properties.apiHash())
                .systemLanguageCode(properties.systemLanguageCode())
                .deviceModel(properties.deviceModel())
                .systemVersion(properties.systemVersion())
                .applicationVersion(properties.applicationVersion())
                .logVerbosityLevel(properties.logVerbosityLevel())
                .proxy(properties.proxy());
    }

    public record Proxy(
            String server,
            int port,
            ProxyHttp http,
            ProxySocks5 socks5,
            ProxyMtProto mtproto
    ) {
        public record ProxyHttp(String username, String password, boolean httpOnly) {
        }

        public record ProxySocks5(String username, String password) {
        }

        public record ProxyMtProto(String secret) {
        }
    }

}