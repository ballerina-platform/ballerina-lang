/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.EntityBody;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import static org.ballerinalang.mime.util.Constants.BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_IO;

/**
 * Get the entity body as a byte channel.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getByteChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetByteChannel extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct byteChannelStruct;
        try {
            BStruct entityStruct = (BStruct) this.getRefArgument(context, FIRST_PARAMETER_INDEX);
            EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
            Channel byteChannel = null;
            if (entityBody != null) {
                if (entityBody.isStream()) {
                    byteChannel = entityBody.getEntityWrapper();
                } else {
                    byteChannel = entityBody.getFileIOChannel();
                }
            }
            byteChannelStruct = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_IO, BYTE_CHANNEL_STRUCT);
            byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
        } catch (Throwable e) {
            throw new BallerinaException("Error occurred while constructing byte channel from entity body : "
                    + e.getMessage());
        }
        return this.getBValues(byteChannelStruct);
    }
}
