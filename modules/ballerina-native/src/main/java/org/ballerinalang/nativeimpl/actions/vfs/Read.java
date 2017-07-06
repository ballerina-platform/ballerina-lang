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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.vfs.util.FileConstants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
* Read.
*/
@BallerinaAction(
        packageName = "ballerina.net.vfs",
        actionName = "read",
        connectorName = FileConstants.CONNECTOR_NAME,
        args = { @Argument(name = "vfsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                         structPackage = "ballerina.lang.files"),
                 @Argument(name = "bytes", type = TypeEnum.INT) },
        returnType = {@ReturnType(type = TypeEnum.BLOB)}
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Read byte data from a file") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "vfsClientConnector",
        value = "Vfs client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File struct") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "bytes",
        value = "The number of bytes to be read") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "blob",
        value = "The blob containing files read") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "numberRead",
        value = "The number of bytes actually read") })
public class Read extends AbstractVfsAction {
    @Override
    public BValue execute(Context context) {

        // Extracting Argument values
        BStruct file = (BStruct) getRefArgument(context, 1);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        String pathString = file.getStringField(0);
        propertyMap.put(FileConstants.PROPERTY_URI, pathString);
        propertyMap.put(FileConstants.PROPERTY_ACTION, FileConstants.ACTION_READ);
        CarbonMessage responseMessage = executeCallbackAction(null, propertyMap, context);
        context.getControlStackNew().currentFrame.returnValues[0] =
                new BBlob(((BinaryCarbonMessage) responseMessage).readBytes().array());
        return null;
    }
}

