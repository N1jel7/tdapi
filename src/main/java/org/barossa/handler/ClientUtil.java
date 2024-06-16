package org.barossa.handler;

import dev.voroby.springframework.telegram.client.TdApi;
import lombok.AllArgsConstructor;
import org.barossa.client.SingleTelegramClient;

public final class ClientUtil {
    private static final ConnectionProperties PRE_CONFIG_CONNECTION_PROPERTIES = new ConnectionProperties("FIREBASE_INITIALIZING", "org.thunderdog.challegram", "com.google.android.packageinstaller", 10800,
            new GitProperties("TGX-Android/Telegram-X", "7bc96dbf", "b102c3a", 1717988850));

    private ClientUtil() {
    }

    public static void preConfig(SingleTelegramClient client) {
        client.sendAsync(new TdApi.GetOption("version"));

        client.sendAsync(new TdApi.SetOption("use_quick_ack", new TdApi.OptionValueBoolean(true)));

        client.sendAsync(new TdApi.SetOption("use_pfs", new TdApi.OptionValueBoolean(true)));

        client.sendAsync(new TdApi.SetOption("is_emulator", new TdApi.OptionValueBoolean(false)));

        client.sendAsync(new TdApi.SetOption("language_pack_database_path", new TdApi.OptionValueString("/data/user/0/org.example.tgx/files/langpack/main")));

        client.sendAsync(new TdApi.SetOption("localization_target", new TdApi.OptionValueString("android_x")));

        client.sendAsync(new TdApi.SetOption("language_pack_id", new TdApi.OptionValueString("en")));

        client.sendAsync(new TdApi.SetOption("notification_group_count_max", new TdApi.OptionValueInteger(25)));

        client.sendAsync(new TdApi.SetOption("notification_group_size_max", new TdApi.OptionValueInteger(7)));

        client.sendAsync(new TdApi.SetOption("storage_max_files_size", new TdApi.OptionValueInteger(2147483647)));

        client.sendAsync(new TdApi.SetOption("ignore_default_disable_notification", new TdApi.OptionValueBoolean(true)));

        client.sendAsync(new TdApi.SetOption("ignore_platform_restrictions", new TdApi.OptionValueBoolean(true)));

        client.sendAsync(new TdApi.SetOption("process_pinned_messages_as_metions", new TdApi.OptionValueBoolean(true)));

        client.sendAsync(new TdApi.SetOption("connection_parameters", new TdApi.OptionValueString(PRE_CONFIG_CONNECTION_PROPERTIES.toString())));

        client.sendAsync(new TdApi.SetNetworkType(new TdApi.NetworkTypeMobile()));
    }

    public static void preAuth(SingleTelegramClient client){
        client.sendAsync(new TdApi.SetOption("connection_parameters", new TdApi.OptionValueString(PRE_CONFIG_CONNECTION_PROPERTIES.toString())));
    }

    @AllArgsConstructor
    static class ConnectionProperties {
        private String deviceToken;
        private String packageId;
        private String installer;
        private long tzOffset;
        private GitProperties git;

        @Override
        public String toString() {
            return "{\"device_token\":\"" + deviceToken + "\",\"package_id\":\"" + packageId + "\",\"installer\":\"" + installer + "\",\"tz_offset\":" + tzOffset + ".000000," + git.toString() + "}\n";
        }
    }

    @AllArgsConstructor
    static class GitProperties {
        private String remote;
        private String commit;
        private String tdlib;
        private long date;

        @Override
        public String toString() {
            return "\"git\":{\"remote\":\"" + remote + "\",\"commit\":\"" + commit + "\",\"tdlib\":\"" + tdlib + "\",\"date\":" + date + ".000000}";
        }
    }
}
