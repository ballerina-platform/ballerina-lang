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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Locale;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_AS_PRIMARY_TYPE;

/**
 * Get the entity body as a string.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getText",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.STRING), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetText extends AbstractGetPayloadHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BString result;
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            String baseType = HeaderUtil.getBaseType(entityStruct);
            if (!isTextContentType(baseType)) {
                createErrorAndNotify(context, callback, "Entity body is not text " + COMPATIBLE_SINCE_CONTENT_TYPE +
                        baseType);
                return;
            }

            BValue dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (dataSource != null) {
                result = MimeUtil.getMessageAsString(dataSource);
                setReturnValuesAndNotify(context, callback, result);
                return;
            }

            if (isStreamingRequired(entityStruct)) {
                result = EntityBodyHandler.constructStringDataSource(entityStruct);
                updateDataSourceAndNotify(context, callback, entityStruct, result);
            } else {
                constructNonBlockingDataSource(context, callback, entityStruct, SourceType.TEXT);
            }
        } catch (Exception ex) {
            createErrorAndNotify(context, callback,
                                 "Error occurred while extracting text data from entity : " + ex.getMessage());
        }
    }

    private boolean isTextContentType(String baseType) {
        return baseType != null && (baseType.toLowerCase(Locale.getDefault()).startsWith(TEXT_AS_PRIMARY_TYPE) ||
                baseType.equalsIgnoreCase(APPLICATION_FORM));
    }
}
