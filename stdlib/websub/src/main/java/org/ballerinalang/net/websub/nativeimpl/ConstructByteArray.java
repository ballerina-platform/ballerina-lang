/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;

import java.io.IOException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.BYTE_CHANNEL_NAME;

/**
 * Extern function to create a byte array to use as the content when a ByteChannel is specified to indicate the
 * content to be sent as the WebSub notification.
 *
 * @since 0.973.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "constructByteArray",
        args = {@Argument(name = "byteChannel", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)}
)
public class ConstructByteArray {

    public static ArrayValue constructByteArray(Strand strand, ObjectValue byteChannel) {
        Channel channel = (Channel) byteChannel.getNativeData(BYTE_CHANNEL_NAME);
        if (channel == null) {
            return new ArrayValueImpl(new byte[0]);
        }
        try {
            byte[] byteData = MimeUtil.getByteArray(channel.getInputStream());
            channel.close();
            return new ArrayValueImpl(byteData);
        } catch (IOException e) {
            return new ArrayValueImpl(new byte[0]);
        }
    }
}
