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
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;

/**
 * Get the entity body as a blob.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getByteArray",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetByteArray extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BByteArray result = null;
        try {
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            BValue messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (messageDataSource != null) {
                if (messageDataSource instanceof BByteArray) {
                    result = (BByteArray) messageDataSource;
                } else {
                    String contentTypeValue = HeaderUtil.getHeaderValue(entityStruct,
                            HttpHeaderNames.CONTENT_TYPE.toString());
                    if (contentTypeValue != null && !contentTypeValue.isEmpty()) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (charsetValue != null && !charsetValue.isEmpty()) {
                            result = new BByteArray(messageDataSource.stringValue().getBytes(charsetValue));
                        } else {
                            result = new BByteArray(messageDataSource.stringValue().getBytes(
                                    Charset.defaultCharset()));
                        }
                    }
                }
            } else {
                result = EntityBodyHandler.constructBlobDataSource(entityStruct);
                EntityBodyHandler.addMessageDataSource(entityStruct, result);
                //Set byte channel to null, once the message data source has been constructed
                entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
            }
            context.setReturnValues(result != null ? result : new BByteArray(new byte[0]));
        } catch (Throwable e) {
            context.setReturnValues(MimeUtil.createError(context, "Error occurred while extracting blob data " +
                    "from entity : " + e.getMessage()));
        }
    }
}
