package org.barossa.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum AuthorizationState {
    WAIT_FOR_CONFIG(904720988),
    WAIT_FOR_LOGIN(306402531),
    WAIT_FOR_DEVICE_CONFIRMATION(860166378),
    WAIT_FOR_AUTH_CODE(52643073),
    WAIT_FOR_PASSWORD(112238030),
    WAIT_FOR_EMAIL(1040478663),
    WAIT_FOR_EMAIL_CODE(-1868627365),
    LOGGED_IN(-1834871737),
    LOGGING_OUT(154449270),
    CLOSING(445855311),
    CLOSED(1526047584),
    PHONE_NUMBER_BANNED(-1);
    private final int stateId;

    public static AuthorizationState of(int stateId){
        return Arrays.stream(AuthorizationState.values())
                .filter(state -> state.getStateId() == stateId)
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(AuthorizationState.class, Integer.toString(stateId)));
    }
}
