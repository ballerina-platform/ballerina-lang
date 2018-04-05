package org.ballerinalang.net.jms.nativeimpl.endpoint.session;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.Connection;
import javax.jms.Session;

/**
 * Connection init function for JMS connection endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Session", structPackage = "ballerina.jms"),
        args = {@Argument(name = "connectionConnector", type = TypeKind.STRUCT, structType = "ConnectionConnector")
        },
        isPublic = true
)
public class InitEndpoint implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct sessionEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct sessionConfig = sessionEndpoint.getStructField(Constants.SESSION_CONFIG);
        BStruct connectionConnector = (BStruct) context.getRefArgument(1);
        Object nativeData = connectionConnector.getNativeData(Constants.JMS_CONNECTION);
        if (nativeData instanceof Connection) {
            Session session = JMSUtils.createSession((Connection) nativeData, sessionConfig);
            Struct sessionConnector = sessionEndpoint.getStructField(Constants.SESSION_FIELD_CONNECTOR);
            sessionConnector.addNativeData(Constants.JMS_SESSION, session);
        } else {
            throw new BallerinaException("JMS Connection is not properly established.");
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
