/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.mime.nativeimpl.mimebase64;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.utils.Utils;

import static org.ballerinalang.mime.util.MimeConstants.UTF_8;

/**
 * Extern function 'mime:base64Encode' that can encode a a given string, blob or a byte channel.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "base64Encode",
        args = {@Argument(name = "contentToBeEncoded", type = TypeKind.UNION), @Argument(name = "charset",
                type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class Base64Encode {

    public static Object base64Encode(Strand strand, Object contentToBeDecoded, String charset) {
        return Utils.encode(contentToBeDecoded, charset != null ? charset : UTF_8, true);
    }
}
