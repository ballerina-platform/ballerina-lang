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
package org.ballerinalang.nativeimpl.actions.ftp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ftp.util.FileConstants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Checks the existence of a file.
 */
@BallerinaAction(
        packageName = "ballerina.net.ftp",
        actionName = "exists",
        connectorName = FileConstants.CONNECTOR_NAME,
        args = { @Argument(name = "ftpClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                         structPackage = "ballerina.lang.files") },
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Checks the existence of a file") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "ftp client connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "File struct containing path information") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "isExist",
        value = "Boolean representing  whether the file exists") })
public class Exists extends AbstractFtpAction {
    @Override
    public BValue execute(Context context) {

        // Extracting Argument values
        BStruct file = (BStruct) getRefArgument(context, 1);
        if (!validateProtocol(file.getStringField(0))) {
            throw new BallerinaException("Only FTP, SFTP and FTPS protocols are supported by this connector");
        }
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        String pathString = file.getStringField(0);
        propertyMap.put(FileConstants.PROPERTY_URI, pathString);
        propertyMap.put(FileConstants.PROPERTY_ACTION, FileConstants.ACTION_EXISTS);
        CarbonMessage responseMessage = executeCallbackAction(null, propertyMap, context);
        boolean b = Boolean.parseBoolean(((TextCarbonMessage) responseMessage).getText());
        context.getControlStackNew().currentFrame.returnValues[0] = new BBoolean(b);
        return null;
    }
}
