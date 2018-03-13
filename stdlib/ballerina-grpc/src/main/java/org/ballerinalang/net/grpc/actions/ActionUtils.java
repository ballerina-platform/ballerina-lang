package org.ballerinalang.net.grpc.actions.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;

import static org.ballerinalang.net.grpc.MessageUtils.createStruct;

/**
 * Action util class.
 */
public class ActionUtils {
    public static void notifyErrorReply(Context context, String errorMessage) {
        BStruct outboundError = createStruct(context, "ConnectorError");
        outboundError.setStringField(0, errorMessage);
        context.setReturnValues(null, outboundError);
    }
    
    public static void notifyReply(Context context, BValue responseBValue) {
        context.setReturnValues(responseBValue, null);
    }
}
