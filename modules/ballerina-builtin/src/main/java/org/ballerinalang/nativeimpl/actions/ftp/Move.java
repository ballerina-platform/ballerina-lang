package org.ballerinalang.nativeimpl.actions.ftp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ftp.util.FileConstants;
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
 * Move a file or a folder from one place to another
 */
@BallerinaAction(
        packageName = "ballerina.net.ftp",
        actionName = "move",
        connectorName = FileConstants.CONNECTOR_NAME,
        args = {@Argument(name = "ftpClientConnector", type = TypeEnum.CONNECTOR),
                @Argument(name = "source", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files"),
                @Argument(name = "destination", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function copies a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "ftp client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File that should be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File should be copied to") })
public class Move extends AbstractFtpAction {
    @Override
    public BValue execute(Context context) {
        // Extracting Argument values
        BStruct source = (BStruct) getRefArgument(context, 1);
        BStruct destination = (BStruct) getRefArgument(context, 2);
        if (!validateProtocol(source.getStringField(0)) && !validateProtocol(destination.getStringField(0))) {
            throw new BallerinaException("Only FTP, SFTP and FTPS protocols are supported by this connector");
        }
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(FileConstants.PROPERTY_URI, source.getStringField(0));
        propertyMap.put(FileConstants.PROPERTY_DESTINATION, destination.getStringField(0));
        propertyMap.put(FileConstants.PROPERTY_ACTION, FileConstants.ACTION_MOVE);
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector(FileConstants.FTP_CONNECTOR_NAME)
                                     .send(null, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        }
        return null;
    }
}
