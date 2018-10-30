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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.characters.WriteCharactersEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina/io#writeCharacters.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableCharacterChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "content", type = TypeKind.STRING),
                @Argument(name = "startOffset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class WriteCharacters implements NativeCallableUnit {
    /**
     * Index of the content provided in ballerina/io#writeCharacters.
     */
    private static final int CONTENT_INDEX = 0;

    /**
     * Index of the character channel in ballerina/io#writeCharacters.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Index of the start offset in ballerina/io#writeCharacters.
     */
    private static final int START_OFFSET_INDEX = 0;

    /**
     * Processors the response after reading characters.
     *
     * @param result the response returned after reading characters.
     * @return the response returned from the event.
     */
    private static EventResult writeResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        CallableUnitCallback callback = eventContext.getCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            Integer numberOfCharactersWritten = result.getResponse();
            context.setReturnValues(new BInteger(numberOfCharactersWritten));
        }
        callback.notifySuccess();
        return result;
    }

    /**
     * Writes characters to a given file.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> channel = (BMap<String, BValue>) context.getRefArgument(CHAR_CHANNEL_INDEX);
        String content = context.getStringArgument(CONTENT_INDEX);
        long startOffset = context.getIntArgument(START_OFFSET_INDEX);
        CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(IOConstants
                .CHARACTER_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        WriteCharactersEvent event = new WriteCharactersEvent(characterChannel, content, (int) startOffset,
                eventContext);
        Register register = EventRegister.getFactory().register(event, WriteCharacters::writeResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
