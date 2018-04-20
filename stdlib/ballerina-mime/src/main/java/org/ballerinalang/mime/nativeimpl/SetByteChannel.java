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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.runtime.message.MessageDataSource;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.Constants.SECOND_PARAMETER_INDEX;

/**
 * Set byte channel as entity body.
 *
 * @since 0.963.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "setByteChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        args = {@Argument(name = "byteChannel", type = TypeKind.STRUCT), @Argument(name = "contentType",
                type = TypeKind.STRING)},
        isPublic = true
)
public class SetByteChannel extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct entityStruct = (BStruct) context.getRefArgument(FIRST_PARAMETER_INDEX);
        BStruct byteChannel = (BStruct) context.getRefArgument(SECOND_PARAMETER_INDEX);
        String contentType = context.getStringArgument(FIRST_PARAMETER_INDEX);
        entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel.getNativeData
                (IOConstants.BYTE_CHANNEL_NAME));
        MessageDataSource dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
        if (dataSource != null) { //Clear message data source when the user set a byte channel to entity
            entityStruct.addNativeData(MESSAGE_DATA_SOURCE, null);
        }
        HeaderUtil.setHeaderToEntity(entityStruct, HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        context.setReturnValues();
    }
}
