/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Send
 */
@BallerinaAction(
        packageName = "ballerina.net.vfs",
        actionName = "write",
        connectorName = Constants.CONNECTOR_NAME,
        args = { @Argument(name = "vfsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "blob", type = TypeEnum.BLOB),
                 @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                         structPackage = "ballerina.lang.files") },
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function writes a file using the given byte data") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Vfs client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "content",
        value = "Blob content to be written") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "The file which the blob should be written to") })
public class Write extends AbstractVfsAction {
    @Override public BValue execute(Context context) {

        byte[] content = getBlobArgument(context, 1);
        BStruct destination = (BStruct) getRefArgument(context, 2);
        CarbonMessage byteMessage = new BinaryCarbonMessage(ByteBuffer.wrap(content), true);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(Constants.PROPERTY_URI, destination.getStringField(0));
        propertyMap.put(Constants.PROPERTY_ACTION, Constants.ACTION_WRITE);
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector(Constants.VFS_CONNECTOR_NAME)
                                     .send(byteMessage, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Exception occurred while sending message.", e, context);
        }
        return null;
    }
}
