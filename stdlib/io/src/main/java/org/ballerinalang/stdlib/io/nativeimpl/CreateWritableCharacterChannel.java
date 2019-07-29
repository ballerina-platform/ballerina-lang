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
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function ballerina/io#createWritableCharacterChannel.
 *
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableCharacterChannel", structPackage =
                "ballerina/io"),
        args = {@Argument(name = "byteChannel", type = TypeKind.OBJECT, structType = "WritableByteChannel",
                structPackage = "ballerina/io"),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        isPublic = true
)
public class CreateWritableCharacterChannel {

    private static final Logger log = LoggerFactory.getLogger(CreateWritableCharacterChannel.class);

    public static void init(Strand strand, ObjectValue characterChannel, ObjectValue byteChannelInfo, String encoding) {
        try {
            Channel byteChannel = (Channel) byteChannelInfo.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            CharacterChannel bCharacterChannel = new CharacterChannel(byteChannel, encoding);
            characterChannel.addNativeData(IOConstants.CHARACTER_CHANNEL_NAME, bCharacterChannel);
        } catch (Throwable e) {
            String message = "Error occurred while converting byte channel to character channel:" + e.getMessage();
            log.error(message, e);
            throw new BallerinaException(message, e);
        }
    }
}
