package org.ballerinalang.nativeimpl.actions.vfs;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Copy a file from a given location to another
 */
@BallerinaAction(
        packageName = "ballerina.net.vfs",
        actionName = "copy",
        connectorName = Constants.CONNECTOR_NAME,
        args = {@Argument(name = "vfsClientConnector", type = TypeEnum.CONNECTOR),
                @Argument(name = "source", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"),
                @Argument(name = "destination", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function copies a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Vfs client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File that should be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File should be pasted") })
public class Copy extends AbstractVfsAction {
    @Override public BValue execute(Context context) {

        // Extracting Argument values
        BStruct source = (BStruct) getRefArgument(context, 1);
        BStruct destination = (BStruct) getRefArgument(context, 2);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(Constants.PROPERTY_URI, source.getStringField(0));
        propertyMap.put(Constants.PROPERTY_DESTINATION, destination.getStringField(0));
        propertyMap.put(Constants.PROPERTY_ACTION, Constants.ACTION_COPY);
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector(Constants.VFS_CONNECTOR_NAME)
                                     .send(null, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Exception occurred while sending message.", e, context);
        }
        return null;
    }
}
