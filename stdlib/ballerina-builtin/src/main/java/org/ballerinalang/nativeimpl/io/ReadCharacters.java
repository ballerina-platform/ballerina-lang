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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.characters.ReadCharactersEvent;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Native function ballerina.io#readCharacters.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readCharacters",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "numberOfChars", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class ReadCharacters extends AbstractNativeFunction {

    /**
     * Specifies the index which contains the character channel in ballerina.lo#readCharacters.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Specifies the index which holds number of characters in ballerina.lo#readCharacters.
     */
    private static final int NUMBER_OF_CHARS_INDEX = 0;

    /**
     * Will be the I/O event handler.
     */
    private EventManager eventManager = EventManager.getInstance();


    /*
     * Callback method of the read characters response.
     *
     * @param result the result returned as the response.
     * @return the processed event result.
     *//*
    private static EventResult readCharactersResponse(EventResult<Boolean, EventContext> result) {
        BStruct errorStruct;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        Boolean numberOfBytes = result.getResponse();
        return result;
    }
    */

    /**
     * Reads characters asynchronously.
     *
     * @param numberOfCharacters number of characters which should be read.
     * @param characterChannel   the channel which the characters will be read.
     * @return the content which is read.
     * @throws ExecutionException   if an error occurs in the async framework.
     * @throws InterruptedException during interrupt.
     */
    private String asyncReadCharacters(int numberOfCharacters, CharacterChannel characterChannel) throws
            ExecutionException, InterruptedException {
        ReadCharactersEvent event = new ReadCharactersEvent(characterChannel, numberOfCharacters);
        CompletableFuture<EventResult> future = eventManager.publish(event);
        EventResult eventResult = future.get();
        return (String) eventResult.getResponse();
    }

    /**
     * <p>
     * Reads characters from the channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        long numberOfCharacters;
        BString content;
        try {
            channel = (BStruct) getRefArgument(context, CHAR_CHANNEL_INDEX);
            numberOfCharacters = getIntArgument(context, NUMBER_OF_CHARS_INDEX);
            CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            // IOUtils.read(characterChannel,numberOfCharacters,ReadCharacters::readCharactersResponse);
            String readCharacters = asyncReadCharacters((int) numberOfCharacters, characterChannel);
            content = new BString(readCharacters);
        } catch (Throwable e) {
            String message = "Error occurred while reading characters:" + e.getMessage();
            throw new BallerinaException(message, context);

        }
        return getBValues(content, null);
    }
}
