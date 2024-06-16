package org.barossa;

import org.barossa.client.SingleTelegramClient;
import org.barossa.handler.AuthorizationState;
import org.barossa.handler.ClientConfigStateListener;
import org.barossa.handler.ClientLoginStateListener;
import org.barossa.handler.ClientStateUpdateListener;
import org.barossa.properties.TelegramClientProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication(excludeName = {"dev.voroby.springframework.telegram.TelegramClientAutoConfiguration"})
@ConfigurationPropertiesScan(basePackages = "org.barossa.properties")
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    CommandLineRunner runner(TelegramClientProperties properties){
        return args -> {
            Path directory = createClientFolder(properties.databaseDirectory());
            TelegramClientProperties clientProperties = TelegramClientProperties.of(properties)
                    .databaseDirectory(directory.toString())
                    .build();

            final CompletableFuture<AuthorizationState> loginFuture = new CompletableFuture<>();
            List<ClientStateUpdateListener> listeners = List.of(
                    new ClientConfigStateListener(clientProperties),
                    new ClientLoginStateListener("+12399556336", loginFuture)
            );
            SingleTelegramClient client = new SingleTelegramClient(listeners);
        };
    }

    private static Path createClientFolder(String directory) {
        try {
            Path path = Path.of(directory, UUID.randomUUID().toString());
            return Files.createDirectory(path);
        } catch (Exception e) {
            throw new RuntimeException("Can't create client directory", e);
        }
    }
}