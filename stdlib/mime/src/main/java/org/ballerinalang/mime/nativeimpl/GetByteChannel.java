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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.PARSING_ENTITY_BODY_FAILED;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_IO;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;

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
public class GetByteChannel {

    public static Object getByteChannel(Strand strand, ObjectValue entityObj) {
        ObjectValue byteChannelObj;
        try {
            byteChannelObj = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_IO, READABLE_BYTE_CHANNEL_STRUCT);
            populateEntityWithByteChannel(entityObj);
            Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
            if (byteChannel != null) {
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
                return byteChannelObj;
            } else {
                if (EntityBodyHandler.getMessageDataSource(entityObj) != null) {
                    return MimeUtil.createError(PARSING_ENTITY_BODY_FAILED, "Byte channel is not available but " +
                                                        "payload can be obtain either as xml, json, string or byte[] " +
                                                        "type");
                } else if (EntityBodyHandler.getBodyPartArray(entityObj) != null && EntityBodyHandler.
                        getBodyPartArray(entityObj).size() != 0) {
                    return MimeUtil.createError(PARSING_ENTITY_BODY_FAILED,
                            "Byte channel is not available since payload contains a set of body parts");
                } else {
                    return MimeUtil.createError(PARSING_ENTITY_BODY_FAILED, "Byte channel is not available as payload");
                }
            }
        } catch (Throwable e) {
            return MimeUtil.createError(PARSING_ENTITY_BODY_FAILED,
                    "Error occurred while constructing byte channel from entity body : " + e.getMessage());
        }
    }

    private static void populateEntityWithByteChannel(ObjectValue entity) {
        HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) entity.getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage == null) {
            return;
        }
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);

        long contentLength = MimeUtil.extractContentLength(httpCarbonMessage);
        if (contentLength > 0) {
            entity.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(
                    new EntityBodyChannel(httpMessageDataStreamer.getInputStream())));
        }
    }
}
