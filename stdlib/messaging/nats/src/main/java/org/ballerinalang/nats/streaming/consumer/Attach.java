package org.ballerinalang.nats.streaming.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.nats.Constants.STREAMING_DISPATCHER_LIST;

/**
 * Create a listener and attach service.
 *
 * @since 1.0.0
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "nats",
                   functionName = "attach",
                   receiver = @Receiver(type = TypeKind.OBJECT,
                                        structType = "StreamingListener",
                                        structPackage = "ballerina/nats"),
                   isPublic = true)
public class Attach implements NativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static void attach(Strand strand, ObjectValue streamingListener, ObjectValue service,
            Object connection) {
        List<ObjectValue> serviceList = (List<ObjectValue>) ((ObjectValue) connection)
                .getNativeData(Constants.SERVICE_LIST);
        serviceList.add(service);
        ConcurrentHashMap<ObjectValue, StreamingListener> serviceListenerMap =
                (ConcurrentHashMap<ObjectValue, StreamingListener>) streamingListener
                .getNativeData(STREAMING_DISPATCHER_LIST);
        serviceListenerMap.put(service, new StreamingListener(service, strand.scheduler));
    }
}
