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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Extern function ballerina.io#readString.
 *
 * @since 0.974.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "readString",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableDataChannel",
                structPackage = "ballerina.io"),
        args = {@Argument(name = "nBytes", type = TypeKind.INT),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        isPublic = true
)
public class ReadString {

    private static final Logger log = LoggerFactory.getLogger(ReadString.class);

    public static Object readString(Strand strand, ObjectValue dataChannelObj, long nBytes, String encoding) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        if (channel.hasReachedEnd()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Channel %d reached it's end", channel.hashCode()));
            }
            return IOUtils.createError(IOConstants.IO_EOF);
        } else {
            try {
                return channel.readString((int) nBytes, encoding);
            } catch (IOException e) {
                log.error("Error occurred while reading string.", e);
                return IOUtils.createError(e);
            }
        }
    }

}
