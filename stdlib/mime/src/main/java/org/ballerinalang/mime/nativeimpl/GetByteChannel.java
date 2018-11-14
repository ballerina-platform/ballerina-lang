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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;

import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_IO;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;

/**
 * Get the entity body as a byte channel.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getByteChannel",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.RECORD), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetByteChannel extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> byteChannelStruct;
        try {
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            byteChannelStruct = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_IO,
                    READABLE_BYTE_CHANNEL_STRUCT);
            Channel byteChannel = EntityBodyHandler.getByteChannel(entityStruct);
            if (byteChannel != null) {
                byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
                context.setReturnValues(byteChannelStruct);
            } else {
                if (EntityBodyHandler.getMessageDataSource(entityStruct) != null) {
                    context.setReturnValues(MimeUtil.createError(context, "Byte channel is not available but " +
                            "payload can be obtain either as xml, json, string or byte[] type"));
                } else if (EntityBodyHandler.getBodyPartArray(entityStruct) != null && EntityBodyHandler.
                        getBodyPartArray(entityStruct).size() != 0) {
                    context.setReturnValues(MimeUtil.createError(context,
                            "Byte channel is not available since payload contains a set of body parts"));
                } else {
                    context.setReturnValues(MimeUtil.createError(context,
                            "Byte channel is not available as payload"));
                }
            }
        } catch (Throwable e) {
            context.setReturnValues(MimeUtil.createError(context,
                    "Error occurred while constructing byte channel from entity body : " + e.getMessage()));
        }
    }
}
