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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.io#readCharacters.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "readCharacters",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "numberOfChars", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class ReadCharacters implements NativeCallableUnit {

    /**
     * Specifies the index which contains the character channel in ballerina.lo#readCharacters.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Specifies the index which holds number of characters in ballerina.lo#readCharacters.
     */
    private static final int NUMBER_OF_CHARS_INDEX = 0;

    /*
     * Callback method of the read characters response.
     *
     * @param result the result returned as the response.
     * @return the processed event result.
     */
    private static EventResult readCharactersResponse(EventResult<String, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        CallableUnitCallback callback = eventContext.getCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            BStruct errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            String readChars = result.getResponse();
            context.setReturnValues(new BString(readChars));
        }
        callback.notifySuccess();
        return result;
    }

    /**
     * <p>
     * Reads characters from the channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct channel = (BStruct) context.getRefArgument(CHAR_CHANNEL_INDEX);
        long numberOfCharacters = context.getIntArgument(NUMBER_OF_CHARS_INDEX);
        CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(IOConstants
                .CHARACTER_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        IOUtils.read(characterChannel, (int) numberOfCharacters, eventContext, ReadCharacters::readCharactersResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
