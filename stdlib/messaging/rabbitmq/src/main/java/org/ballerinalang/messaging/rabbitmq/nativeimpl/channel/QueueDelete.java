package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CHANNEL_NATIVE_OBJECT;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.ORG_NAME;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.STRUCT_PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CHANNEL_STRUCT;

/**
 *
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "rabbitmq",
        functionName = "queueDelete",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Channel", structPackage = "ballerina/rabbitmq"),
        isPublic = true
)
public class QueueDelete implements NativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
