package org.barossa.util;

import dev.voroby.springframework.telegram.properties.TelegramProperties;
import lombok.AllArgsConstructor;

public final class PropertiesBuilder {
    private PropertiesBuilder() {
    }

    @AllArgsConstructor
    public static class Builder {
        private boolean useTestDc;
        private String databaseDirectory;
        private String filesDirectory;
        private String databaseEncryptionKey;
        private boolean useFileDatabase;
        private boolean useChatInfoDatabase;
        private boolean useMessageDatabase;
        private boolean useSecretChats;
        private int apiId;
        private String apiHash;
        private String phone;
        private String systemLanguageCode;
        private String deviceModel;
        private String systemVersion;
        private String applicationVersion;
        private int logVerbosityLevel;
        private TelegramProperties.Proxy proxy;

        public Builder phone(String phone){
            this.phone = phone;
            return this;
        }

        public TelegramProperties build(){
            return new TelegramProperties(useTestDc,
                    databaseDirectory,
                    filesDirectory,
                    databaseEncryptionKey,
                    useFileDatabase,
                    useChatInfoDatabase,
                    useMessageDatabase,
                    useSecretChats,
                    apiId,
                    apiHash,
                    phone,
                    systemLanguageCode,
                    deviceModel,
                    systemVersion,
                    applicationVersion,
                    logVerbosityLevel,
                    proxy);
        }
    }

    public static Builder of(TelegramProperties properties) {
        return new Builder(properties.useTestDc(),
                properties.databaseDirectory(),
                properties.filesDirectory(),
                properties.databaseEncryptionKey(),
                properties.useFileDatabase(),
                properties.useChatInfoDatabase(),
                properties.useMessageDatabase(),
                properties.useSecretChats(),
                properties.apiId(),
                properties.apiHash(),
                properties.phone(),
                properties.systemLanguageCode(),
                properties.deviceModel(),
                properties.systemVersion(),
                properties.applicationVersion(),
                properties.logVerbosityLevel(),
                properties.proxy());
    }


}
