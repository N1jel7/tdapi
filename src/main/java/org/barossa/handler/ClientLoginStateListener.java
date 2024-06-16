package org.barossa.handler;

import dev.voroby.springframework.telegram.client.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.client.SingleTelegramClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.barossa.handler.AuthorizationState.WAIT_FOR_LOGIN;

@Slf4j
@RequiredArgsConstructor
public class ClientLoginStateListener implements ClientStateUpdateListener {
    private final String phone;
    private final CompletableFuture<AuthorizationState> login;

    @Override
    public void onUpdate(SingleTelegramClient client) {
        if (WAIT_FOR_LOGIN.equals(client.getClientState())) {
            log.info("Pre-login config...");
            ClientUtil.preAuth(client);
            log.info("Logging to client-{}", client.getNativeClientId());
            client.sendWithCallback(new TdApi.SetAuthenticationPhoneNumber(phone, new TdApi.PhoneNumberAuthenticationSettings(true, true, true, false, true, null, new String[]{})), (r, e) -> {
                if (Objects.nonNull(e)) {
                    log.info("Client-{} login error: {}", client.getNativeClientId(), e.message);
                    AuthorizationState newState = AuthorizationState.valueOf(e.message);
                    client.updateState(newState);
                    login.complete(newState);
                }else {
                    login.complete(client.getClientState());
                }
            });
        }
    }
}
