package org.ballerinalang.nats.streaming.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.nats.Constants.STREAMING_DISPATCHER_LIST;

/**
 * Inits listener.
 *
 * @since 1.0.0
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "nats",
                   functionName = "init",
                   receiver = @Receiver(type = TypeKind.OBJECT,
                                        structType = "StreamingListener",
                                        structPackage = "ballerina/nats"),
                   isPublic = true)
public class Init implements NativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static void init(Strand strand, ObjectValue streamingListener) {
        ConcurrentHashMap<ObjectValue, StreamingListener> serviceListenerMap = new ConcurrentHashMap<>();
        streamingListener.addNativeData(STREAMING_DISPATCHER_LIST, serviceListenerMap);
    }
}
