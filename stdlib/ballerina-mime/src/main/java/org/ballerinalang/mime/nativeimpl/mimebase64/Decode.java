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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Base64;

import static org.ballerinalang.mime.util.Constants.BLOB_INDEX;

/**
 * Mime Base64 Decoder.
 *
 * @since 0.96
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
                   functionName = "decode",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "MimeBase64Decoder",
                                        structPackage = "ballerina.mime"),
                   args = { @Argument(name = "content", type = TypeKind.BLOB) },
                   returnType = { @ReturnType(type = TypeKind.BLOB) },
                   isPublic = true)
public class Decode extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        byte[] encodedContent = context.getBlobArgument(BLOB_INDEX);
        Base64.Decoder decoder = Base64.getMimeDecoder();
        byte[] decodedContent = decoder.decode(encodedContent);
        context.setReturnValues(new BBlob(decodedContent));
    }
}
