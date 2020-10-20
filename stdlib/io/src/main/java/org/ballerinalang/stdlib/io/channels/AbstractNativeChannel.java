/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.channels;

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;

import static org.ballerinalang.stdlib.io.utils.IOConstants.IO_PACKAGE_ID;

/**
 * <p>
 * For I/O APIs to work it requires each I/O source (file,tcp socket) to return a generic ByteChannel.
 * </p>
 * <p>
 * This will prepare the ByteChannel to perform I/O operations.
 * </p>
 */
public abstract class AbstractNativeChannel {
    /**
     * The struct type of the byte channel.
     */
    private static final String READ_BYTE_CHANNEL_STRUCT = "ReadableByteChannel";
    /**
     * Writable byte channel struct.
     */
    private static final String WRITE_BYTE_CHANNEL_STRUCT = "WritableByteChannel";

    protected static BObject createChannel(Channel channel) {
        BObject channelObj;
        if (channel.isReadable()) {
            channelObj = ValueCreator.createObjectValue(IO_PACKAGE_ID, READ_BYTE_CHANNEL_STRUCT);
        } else {
            channelObj = ValueCreator.createObjectValue(IO_PACKAGE_ID, WRITE_BYTE_CHANNEL_STRUCT);
        }
        channelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, channel);
        return channelObj;
    }
}
