package org.ballerinalang.net.jms.nativeimpl.endpoint.queue.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.JmsMessageListenerImpl;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * Register JMS listener for a consumer endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "registerListener",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "QueueConsumer", structPackage = "ballerina.jms"),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC),
                @Argument(name = "connector", type = TypeKind.STRUCT, structType = "QueueConsumerConnector")
        },
        isPublic = true
)
public class RegisterMessageListener implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        BStruct consumerConnector = (BStruct) context.getRefArgument(2);

        Resource resource = JMSUtils.extractJMSResource(service);

        Object nativeData = consumerConnector.getNativeData(Constants.JMS_CONSUMER_OBJECT);
        if (nativeData instanceof MessageConsumer) {
            MessageListener listener = new JmsMessageListenerImpl(resource);
            try {
                ((MessageConsumer) nativeData).setMessageListener(listener);
            } catch (JMSException e) {
                throw new BallerinaException("Error registering the message listener for service"
                                                     + service.getPackage() + service.getName());
            }
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
