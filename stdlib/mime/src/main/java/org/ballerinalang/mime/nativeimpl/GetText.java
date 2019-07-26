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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.PARSING_ENTITY_BODY_FAILED;

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

    public static Object getText(Strand strand, ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        String result = null;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return MimeUtil.getMessageAsString(dataSource);
            }

            if (isStreamingRequired(entityObj)) {
                result = EntityBodyHandler.constructStringDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(strand);
                constructNonBlockingDataSource(callback, entityObj, SourceType.TEXT);
            }
        } catch (Exception ex) {
            return createErrorAndNotify(PARSING_ENTITY_BODY_FAILED, callback,
                                        "Error occurred while extracting text data from entity : " + ex.getMessage());
        }
        return result;
    }
}
