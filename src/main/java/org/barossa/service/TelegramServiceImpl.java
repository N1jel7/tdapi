package org.barossa.service;

import dev.voroby.springframework.telegram.client.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.client.SingleTelegramClient;
import org.barossa.handler.*;
import org.barossa.properties.TelegramClientProperties;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private final Map<String, SingleTelegramClient> clients = new ConcurrentHashMap<>();
    private final TelegramClientProperties telegramProperties;

    static {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                System.loadLibrary("libcrypto-3-x64");
                System.loadLibrary("libssl-3-x64");
                System.loadLibrary("zlib1");
            }
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            log.error("Can't load platform lib: {}", e.getMessage(), e);
        }
    }

    @Override
    public AuthorizationState login(String phone) {
        Path directory = createClientFolder(telegramProperties.databaseDirectory());
        TelegramClientProperties clientProperties = TelegramClientProperties.of(telegramProperties)
                .databaseDirectory(directory.toString())
                .build();

        final CompletableFuture<AuthorizationState> loginFuture = new CompletableFuture<>();
        List<ClientStateUpdateListener> listeners = List.of(
                new ClientConfigStateListener(clientProperties),
                new ClientLoginStateListener(phone, loginFuture)
        );

        SingleTelegramClient client = new SingleTelegramClient(listeners);
        ClientUtil.preConfig(client);
        clients.put(phone, client);
        return getFuture(loginFuture);
    }

    @Override
    public AuthorizationState enterAuthCode(String phone, String code) {
        SingleTelegramClient client = clients.get(phone);
        if (Objects.isNull(client)) {
            throw new RuntimeException("No session with phone provided");
        }
        final CompletableFuture<AuthorizationState> enterAuthCodeFuture = new CompletableFuture<>();
        client.sendWithCallback(new TdApi.CheckAuthenticationCode(code), (r, e) -> {
            if(Objects.nonNull(r)){
                enterAuthCodeFuture.complete(AuthorizationState.of(r.getConstructor()));
            }
            if(Objects.nonNull(e)){
                log.info("Error for auth code {}: {}, phone: {}", code, e.message, phone);
            }
        });

        return getFuture(enterAuthCodeFuture);
    }

    @Override
    public void close(String phone) {
        SingleTelegramClient client = clients.get(phone);
        if(Objects.nonNull(client)){
            client.sendSync(new TdApi.Close());
        }
    }

    private static Path createClientFolder(String directory) {
        try {
            Path path = Path.of(directory, UUID.randomUUID().toString());
            return Files.createDirectory(path);
        } catch (Exception e) {
            log.info("Can't create client directory: {}", e.getMessage(), e);
            throw new RuntimeException("Can't create client directory", e);
        }
    }

    private static <T> T getFuture(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            log.info("Can't wait for future completion: {}", e.getMessage(), e);
            throw new RuntimeException("Can't wait for future completion", e);
        }
    }
}