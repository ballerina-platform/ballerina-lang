/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

import java.util.Arrays;

/**
 * Extern function ballerina.lo#readBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableByteChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "nBytes", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class ReadBytes implements NativeCallableUnit {

    private static final BTupleType readTupleType =
            new BTupleType(Arrays.asList(new BArrayType(BTypes.typeByte), BTypes.typeInt));

    private static final org.ballerinalang.jvm.types.BTupleType readTupleJvmType =
            new org.ballerinalang.jvm.types.BTupleType(Arrays.asList(new org.ballerinalang.jvm.types.BArrayType(
                    org.ballerinalang.jvm.types.BTypes.typeByte), org.ballerinalang.jvm.types.BTypes.typeInt));


    /**
     * Specifies the index which holds the number of bytes in ballerina.lo#readBytes.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 0;
    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /*
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    private static EventResult readResponse(EventResult<Integer, EventContext> result) {
        BValueArray contentTuple = new BValueArray(readTupleType);
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        CallableUnitCallback callback = eventContext.getCallback();
        byte[] content = (byte[]) eventContext.getProperties().get(ReadBytesEvent.CONTENT_PROPERTY);
        if (null != error) {
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            Integer numberOfBytes = result.getResponse();
            contentTuple.add(0, new BValueArray(content));
            contentTuple.add(1, new BInteger(numberOfBytes));
            context.setReturnValues(contentTuple);
        }
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }

    /**
     * <p>
     * Reads bytes from a given channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> channel = (BMap<String, BValue>) context.getRefArgument(BYTE_CHANNEL_INDEX);
        int nBytes = (int) context.getIntArgument(NUMBER_OF_BYTES_INDEX);
        int arraySize = nBytes <= 0 ? IOConstants.CHANNEL_BUFFER_SIZE : nBytes;
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        byte[] content = new byte[arraySize];
        EventContext eventContext = new EventContext(context, callback);
        ReadBytesEvent event = new ReadBytesEvent(byteChannel, content, eventContext);
        Register register = EventRegister.getFactory().register(event, ReadBytes::readResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static Object read(Strand strand, ObjectValue channel, long nBytes) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);

        int arraySize = nBytes <= 0 ? IOConstants.CHANNEL_BUFFER_SIZE : (int) nBytes;
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        byte[] content = new byte[arraySize];
        EventContext eventContext = new EventContext(callback);
        ReadBytesEvent event = new ReadBytesEvent(byteChannel, content, eventContext);
        Register register = EventRegister.getFactory().register(event, ReadBytes::readChannelResponse);
        eventContext.setRegister(register);
        register.submit();
        //TODO : Remove callback once strand non-blocking support is given
        callback.sync();
        return callback.getReturnValue();
    }

    /**
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    private static EventResult readChannelResponse(EventResult<Integer, EventContext> result) {
        ArrayValue contentTuple = new ArrayValue(readTupleJvmType);
        EventContext eventContext = result.getContext();
        Throwable error = eventContext.getError();
        //TODO : Remove callback once strand non-blocking support is given
        NonBlockingCallback callback = eventContext.getNonBlockingCallback();
        byte[] content = (byte[]) eventContext.getProperties().get(ReadBytesEvent.CONTENT_PROPERTY);
        if (null != error) {
            callback.setReturnValues(IOUtils.createError(error.getMessage()));
        } else {
            Integer numberOfBytes = result.getResponse();
            contentTuple.add(0, new ArrayValue(content));
            contentTuple.add(1, numberOfBytes);
            callback.setReturnValues(contentTuple);
        }
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }
}
