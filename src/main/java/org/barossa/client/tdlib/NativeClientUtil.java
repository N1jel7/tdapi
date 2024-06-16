package org.barossa.client.tdlib;

import dev.voroby.springframework.telegram.client.TdApi;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Optional;

public class NativeClientUtil {
    private static final Method createNativeClientMethod;
    private static final Method nativeClientSendMethod;
    private static final Method nativeClientReceiveMethod;
    private static final Method nativeClientExecuteMethod;
    private static final Method nativeClientSetLogMessageHandlerMethod;


    static {
        try{
            createNativeClientMethod = dev.voroby.springframework.telegram.client.Client.class.getDeclaredMethod("createNativeClient");
            nativeClientSendMethod = dev.voroby.springframework.telegram.client.Client.class.getDeclaredMethod("nativeClientSend", int.class, long.class, TdApi.Function.class);
            nativeClientReceiveMethod = dev.voroby.springframework.telegram.client.Client.class.getDeclaredMethod("nativeClientReceive", Array.newInstance(int.class, 0).getClass(), Array.newInstance(long.class, 0).getClass(), Array.newInstance(TdApi.Object.class, 0).getClass(), double.class);
            nativeClientExecuteMethod = dev.voroby.springframework.telegram.client.Client.class.getDeclaredMethod("nativeClientExecute", TdApi.Function.class);
            nativeClientSetLogMessageHandlerMethod = dev.voroby.springframework.telegram.client.Client.class.getDeclaredMethod("nativeClientSetLogMessageHandler", int.class, dev.voroby.springframework.telegram.client.Client.LogMessageHandler.class);

            createNativeClientMethod.setAccessible(true);
            nativeClientSendMethod.setAccessible(true);
            nativeClientReceiveMethod.setAccessible(true);
            nativeClientExecuteMethod.setAccessible(true);
            nativeClientSetLogMessageHandlerMethod.setAccessible(true);

        }catch (NoSuchMethodException e){
            throw new RuntimeException(e);
        }
    }

    public static int createNativeClient() {
        try {
            return (int) createNativeClientMethod.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int nativeClientSend(int nativeClientId, long eventId, TdApi.Function function) {
        try {
            return Optional.ofNullable(nativeClientSendMethod.invoke(null, nativeClientId, eventId, function))
                    .map(o -> (int) o)
                    .orElse(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int nativeClientReceive(int[] clientIds, long[] eventIds, TdApi.Object[] events, double timeout) {
        try {
            return (int) nativeClientReceiveMethod.invoke(null, clientIds, eventIds, events, timeout);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TdApi.Object nativeClientExecute(TdApi.Function function) {
        try {
            return (TdApi.Object) nativeClientExecuteMethod.invoke(null, function);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int nativeClientSetLogMessageHandler(int maxVerbosityLevel, dev.voroby.springframework.telegram.client.Client.LogMessageHandler logMessageHandler) {
        try {
            return (int) nativeClientSetLogMessageHandlerMethod.invoke(null, maxVerbosityLevel, logMessageHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
