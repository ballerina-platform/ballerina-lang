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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.stdlib.io.channels.base.Channel;

import java.io.IOException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.BYTE_CHANNEL_NAME;

/**
 * Native function to create a Blob to use as the content when a ByteChannel is specified to indicate the content to
 * be sent as the WebSub notification.
 *
 * @since 0.973.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "constructBlob",
        args = {@Argument(name = "byteChannel", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.BLOB)}
)
public class ConstructBlob extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> byteChannel = (BMap<String, BValue>) context.getRefArgument(0);
        Channel channel = (Channel) byteChannel.getNativeData(BYTE_CHANNEL_NAME);
        if (channel == null) {
            context.setReturnValues(new BBlob(new byte[0]));
        } else {
            try {
                byte[] byteData = MimeUtil.getByteArray(channel.getInputStream());
                channel.close();
                context.setReturnValues(new BBlob((new BlobDataSource(byteData)).getValue()));
            } catch (IOException e) {
                context.setReturnValues(new BBlob(new byte[0]));
            }
        }
    }
}
