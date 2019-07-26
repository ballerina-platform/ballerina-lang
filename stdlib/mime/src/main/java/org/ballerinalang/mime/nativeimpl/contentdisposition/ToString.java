/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.mime.nativeimpl.contentdisposition;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;

/**
 * Given a ContentDisposition object, get the string equivalent value that can be used with HTTP header.
 *
 * @since  0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "toString",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ContentDisposition",
                structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToString {

    public static String toString(Strand strand, ObjectValue contentDispositionObj) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (contentDispositionObj != null) {
            String disposition = String.valueOf(contentDispositionObj.get(DISPOSITION_FIELD));
            if (!disposition.isEmpty()) {
                dispositionBuilder.append(disposition);
                MimeUtil.convertDispositionObjectToString(dispositionBuilder, contentDispositionObj);
            }
        }
        return dispositionBuilder.toString();
    }
}
