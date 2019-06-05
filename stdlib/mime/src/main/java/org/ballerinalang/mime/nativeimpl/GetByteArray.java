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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
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

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BValueArray result = null;
            BMap<String, BValue> entityObj = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            BValue messageDataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (messageDataSource != null) {
                if (messageDataSource instanceof BValueArray) {
                    result = (BValueArray) messageDataSource;
                } else {
                    String contentTypeValue = HeaderUtil.getHeaderValue(entityObj,
                                                                        HttpHeaderNames.CONTENT_TYPE.toString());
                    if (isNotNullAndEmpty(contentTypeValue)) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (isNotNullAndEmpty(charsetValue)) {
                            result = new BValueArray(messageDataSource.stringValue().getBytes(charsetValue));
                        } else {
                            result = new BValueArray(messageDataSource.stringValue().getBytes(
                                    Charset.defaultCharset()));
                        }
                    }
                }
                setReturnValuesAndNotify(context, callback, result != null ? result : new BValueArray(new byte[0]));
                return;
            }

            Object transportMessage = entityObj.getNativeData(TRANSPORT_MESSAGE);
            if (isStreamingRequired(entityObj) || transportMessage == null) {
                result = EntityBodyHandler.constructBlobDataSource(entityObj);
                updateDataSourceAndNotify(context, callback, entityObj, result);
            } else {
                constructNonBlockingDataSource(context, callback, entityObj, SourceType.BLOB);
            }
        } catch (Exception ex) {
            createErrorAndNotify(context, callback,
                                 "Error occurred while extracting blob data from entity : " + ex.getMessage());
        }
    }
}
