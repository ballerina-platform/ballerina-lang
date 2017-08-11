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
 * Create a file or folder
 */
@BallerinaAction(
        packageName = "ballerina.net.ftp",
        actionName = "createFile",
        connectorName = FileConstants.CONNECTOR_NAME,
        args = { @Argument(name = "ftpClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                         structPackage = "ballerina.lang.files"),
                 @Argument(name = "fileType", type = TypeEnum.STRING)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Create a file or folder") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "ftp client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "File struct containing path information") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "fileType",
        value = "Specification whether file or folder") })
public class CreateFile extends AbstractFtpAction {
    @Override
    public BValue execute(Context context) {

        // Extracting Argument values
        BStruct file = (BStruct) getRefArgument(context, 1);
        String type = getStringArgument(context, 0);
        if (!validateProtocol(file.getStringField(0))) {
            throw new BallerinaException("Only FTP, SFTP and FTPS protocols are supported by this connector");
        }
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        String pathString = file.getStringField(0);
        propertyMap.put(FileConstants.PROPERTY_URI, pathString);
        propertyMap.put(FileConstants.PROPERTY_ACTION, FileConstants.ACTION_CREATE);
        if (type.equalsIgnoreCase(FileConstants.TYPE_FOLDER)) { //TODO: Refactor the file/dir type recognition
            propertyMap.put(FileConstants.PROPERTY_FOLDER, Boolean.TRUE.toString());
        }
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
