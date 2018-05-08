/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.io#createCharacterChannel.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "byteChannel", type = TypeKind.STRUCT, structType = "ByteChannel",
                structPackage = "ballerina.io"),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        isPublic = true
)
public class CreateCharacterChannel extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(CreateCharacterChannel.class);
    /**
     * Specifies the index of the character channel in ballerina.io#CharacterChannel.init.
     */
    private static final int BYTE_CHANNEL_INDEX = 1;

    /**
     * Specifies the index of the character channel.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Specifies the index of the encoding in ballerina.io#createCharacterChannel.
     */
    private static final int ENCODING_INDEX = 0;

    /**
     * The package path of the byte channel.
     */
    private static final String CHAR_CHANNEL_PACKAGE = "ballerina.io";

    /**
     * The type of the byte channel struct.
     */
    private static final String STRUCT_TYPE = "CharacterChannel";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct byteChannelInfo;
        BStruct characterChannel;
        String encoding;
        try {
            //File which holds access to the channel information
            byteChannelInfo = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
            encoding = context.getStringArgument(ENCODING_INDEX);
            characterChannel = (BStruct) context.getRefArgument(CHAR_CHANNEL_INDEX);
            Channel byteChannel = (Channel) byteChannelInfo.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            CharacterChannel bCharacterChannel = new CharacterChannel(byteChannel, encoding);
            characterChannel.addNativeData(IOConstants.CHARACTER_CHANNEL_NAME, bCharacterChannel);
        } catch (Throwable e) {
            String message = "Error occurred while converting byte channel to character channel:" + e.getMessage();
            log.error(message, e);
            throw new BallerinaIOException(message, e);
        }
    }
}
