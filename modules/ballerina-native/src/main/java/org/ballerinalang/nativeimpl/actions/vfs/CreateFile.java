package org.ballerinalang.nativeimpl.actions.vfs;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Create a file or folder
 */
@BallerinaAction(
        packageName = "ballerina.net.vfs",
        actionName = "createFile",
        connectorName = Constants.CONNECTOR_NAME,
        args = { @Argument(name = "vfsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                         structPackage = "ballerina.lang.files"),
                 @Argument(name = "type", type = TypeEnum.STRING)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Create a file or folder") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Vfs client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "File struct containing path information") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "type",
        value = "Specification whether file or folder") })
public class CreateFile extends AbstractVfsAction {
    @Override
    public BValue execute(Context context) {

        // Extracting Argument values
        BStruct file = (BStruct) getRefArgument(context, 1);
        String type = getStringArgument(context, 0);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        String pathString = file.getStringField(0);
        propertyMap.put(Constants.PROPERTY_URI, pathString);
        propertyMap.put(Constants.PROPERTY_ACTION, Constants.ACTION_CREATE);
        if (type.equalsIgnoreCase(Constants.TYPE_FOLDER)) {
            propertyMap.put(Constants.PROPERTY_FOLDER, Boolean.TRUE.toString());
        }
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector(Constants.VFS_CONNECTOR_NAME)
                                     .send(null, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        }
        return null;
    }
}
