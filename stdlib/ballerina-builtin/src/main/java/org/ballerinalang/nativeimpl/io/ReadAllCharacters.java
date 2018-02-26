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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#readAllCharacters.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readAllCharacters",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ReadAllCharacters extends BlockingNativeCallableUnit {
    /**
     * Specifies the index which contains the character channel in ballerina.lo#readCharacters.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * <p>
     * Reads characters from the channel.
     * </p>
     *
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct channel;
        BString content;
        try {
            channel = (BStruct) context.getRefArgument(CHAR_CHANNEL_INDEX);
            CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            String readBytes = characterChannel.readAll();
            content = new BString(readBytes);
        } catch (Throwable e) {
            String message = "Error occurred while reading characters:" + e.getMessage();
            throw new BallerinaException(message, context);
        }
        context.setReturnValues(content);
    }
}
