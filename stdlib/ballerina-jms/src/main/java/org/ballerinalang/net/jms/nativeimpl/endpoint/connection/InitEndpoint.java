package org.ballerinalang.net.jms.nativeimpl.endpoint.connection;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * Connection init function for JMS connection endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection", structPackage = "ballerina.jms"),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ConnectionConfiguration")
        },
        isPublic = true
)
public class InitEndpoint implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct connectionEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct connectionConfig = connectionEndpoint.getStructField(Constants.CONNECTION_CONFIG);

        Connection connection = JMSUtils.createConnection(connectionConfig);
        try {
            connection.start();
        } catch (JMSException e) {
            throw new BallerinaException("Error occurred while starting connection", e);
        }
        Struct connector = connectionEndpoint.getStructField(Constants.CONNECTION_CONNECTOR);
        connector.addNativeData(Constants.JMS_CONNECTION, connection);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
