package org.ballerinalang.nats.streaming.producer;

import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Closes a producer.
 *
 * @since 1.0.0
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "nats",
                   functionName = "detachFromNatsConnection",
                   receiver = @Receiver(type = TypeKind.OBJECT,
                                        structType = "StreamingProducer",
                                        structPackage = "ballerina/nats"),
                   isPublic = true)
public class CloseConnection implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static Object detachFromNatsConnection(Strand strand, ObjectValue streamingProducer, Object natsConnection) {
        StreamingConnection producersStreamingConnection = (StreamingConnection) streamingProducer
                .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        try {
            producersStreamingConnection.close();
            ObjectValue basicNatsConnection = (ObjectValue) natsConnection;
            ((AtomicInteger) basicNatsConnection.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();
            return null;
        } catch (IOException | TimeoutException e) {
            return Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            return Utils.createNatsError("Internal error while closing producer");
        }
    }
}
