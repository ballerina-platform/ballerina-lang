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

package org.ballerinalang.mime.nativeimpl.mimebase64;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.ballerinalang.mime.util.Constants.CHARSET_INDEX;
import static org.ballerinalang.mime.util.Constants.STRING_INDEX;

/**
 * Mime base64 encoder to encode string values.
 *
 * @since 0.96
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
                   functionName = "encodeString",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "MimeBase64Encoder",
                                        structPackage = "ballerina.mime"),
                   args = { @Argument(name = "content", type = TypeKind.STRING), @Argument(name = "charset",
                                                                        type = TypeKind.STRING) },
                   returnType = { @ReturnType(type = TypeKind.STRING) },
                   isPublic = true)
public class EncodeString extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        String content = context.getStringArgument(STRING_INDEX);
        String charset = context.getStringArgument(CHARSET_INDEX);
        byte[] mimeBytes;
        String mimeEncodedString;
        try {
            if (charset != null) {
                mimeBytes = content.getBytes(charset);
                mimeEncodedString = new String(Base64.getMimeEncoder().encode(mimeBytes), charset);
            } else {
                mimeBytes = content.getBytes(StandardCharsets.UTF_8);
                mimeEncodedString = new String(Base64.getMimeEncoder().encode(mimeBytes), StandardCharsets.UTF_8);
            }
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Error occured while converting given string to bytes: " + e.getMessage());
        }
        context.setReturnValues(new BString(mimeEncodedString));
    }
}
