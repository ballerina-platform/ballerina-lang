package org.ballerinalang.nativeimpl.connectors.file.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.StreamingCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Append
 */
@BallerinaAction(
        packageName = "ballerina.net.file",
        actionName = "writeCSV",
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = { @Argument(name = "fileClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "arr", type = TypeEnum.ARRAY, elementType = TypeEnum.STRING),
                 @Argument(name = "path", type = TypeEnum.STRING)/*,
                 @Argument(name = "properties", type = TypeEnum.MAP)*/ },
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "APPEND action implementation of the File Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "File Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "array",
        value = "Any") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "path",
        value = "Path of the file") })
//@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "properties",
//        value = "Properties") })
public class WriteCSV extends AbstractFileAction {
    private static final Logger log = LoggerFactory.getLogger(WriteCSV.class);
    @Override public BValue execute(Context context) {

        // Extracting Argument values
        BConnector bConnector = (BConnector) getArgument(context, 0);
        Connector connector = bConnector.value();
        if (!(connector instanceof ClientConnector)) {
            throw new BallerinaException("Need to use a FileConnector as the first argument", context);
        }
        //Getting ballerina message and extract carbon message.
//        BMessage bMessage = new BMessage();
//        if (bMessage == null) {
//            throw new BallerinaException("Ballerina message not found", context);
//        }
        BString path = (BString) getArgument(context, 2);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        //Getting the map of properties.
//        BMap properties = (BMap) getArgument(context, 3);
        BArray<BString> arr = (BArray<BString>) getArgument(context, 1);
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(arr.get(0).stringValue());
        for (int i = 1; i != arr.size(); i++) {
            stringBuffer.append(",");
            stringBuffer.append(arr.get(i).stringValue());
        }
        String content = stringBuffer.toString();
        content += "\n";
        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(content).array());
        CarbonMessage message = new StreamingCarbonMessage(is);

        String pathString = path.stringValue();
        propertyMap.put("uri", pathString);
        propertyMap.put("action", "append");
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector("file")
                                     .send(message, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Exception occurred while sending message.", e, context);
        }
        return null;
    }
}
