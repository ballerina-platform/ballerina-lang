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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

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
public class GetByteArray extends AbstractGetPayloadHandler {

    public static Object getByteArray(Strand strand, ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        ArrayValue result = null;
        try {
            Object messageDataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (messageDataSource != null) {
                if (messageDataSource instanceof ArrayValue) {
                    result = (ArrayValue) messageDataSource;
                } else {
                    String contentTypeValue = HeaderUtil.getHeaderValue(entityObj,
                                                                        HttpHeaderNames.CONTENT_TYPE.toString());
                    if (isNotNullAndEmpty(contentTypeValue)) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (isNotNullAndEmpty(charsetValue)) {
                            result = new ArrayValueImpl(StringUtils.getJsonString(messageDataSource)
                                                            .getBytes(charsetValue));
                        } else {
                            result = new ArrayValueImpl(StringUtils.getJsonString(messageDataSource)
                                                            .getBytes(Charset.defaultCharset()));
                        }
                    }
                }
                return result != null ? result : new ArrayValueImpl(new byte[0]);
            }

            Object transportMessage = entityObj.getNativeData(TRANSPORT_MESSAGE);
            if (isStreamingRequired(entityObj) || transportMessage == null) {
                result = EntityBodyHandler.constructBlobDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(strand);
                constructNonBlockingDataSource(callback, entityObj, SourceType.BLOB);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                        "Error occurred while extracting blob data from entity", (ErrorValue) ex);
            }
            createParsingEntityBodyFailedErrorAndNotify(callback,
                    "Error occurred while extracting blob data from entity : " + getErrorMsg(ex), null);
        }
        return result;
    }
}
