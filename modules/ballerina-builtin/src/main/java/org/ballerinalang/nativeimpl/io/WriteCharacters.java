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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#writeCharacters.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "writeCharacters",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.STRING),
                @Argument(name = "startOffset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class WriteCharacters extends AbstractNativeFunction {
    /**
     * Index of the content provided in ballerina.io#writeCharacters
     */
    private static final int CONTENT_INDEX = 0;

    /**
     * Index of the character channel in ballerina.io#writeCharacters
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Index of the start offset in ballerina.io#writeCharacters
     */
    private static final int START_OFFSET_INDEX = 0;

    /**
     * Writes characters to a given file.
     *
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        String content;
        long startOffset;
        int numberOfCharactersWritten;
        try {
            channel = (BStruct) getRefArgument(context, CHAR_CHANNEL_INDEX);
            content = getStringArgument(context, CONTENT_INDEX);
            startOffset = getIntArgument(context, START_OFFSET_INDEX);

            CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);

            numberOfCharactersWritten = characterChannel.write(content, (int) startOffset);
        } catch (Throwable e) {
            String message = "Error occurred while writing characters .";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return getBValues(new BInteger(numberOfCharactersWritten));
    }
}
