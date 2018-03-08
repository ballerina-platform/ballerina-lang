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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.lo#writeBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.BLOB),
                @Argument(name = "startOffset", type = TypeKind.INT),
                @Argument(name = "numberOfBytes", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class Write extends AbstractNativeFunction {

    /**
     * Index which holds the byte channel in ballerina.io#writeBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * Index which holds the content in ballerina.io#writeBytes.
     */
    private static final int CONTENT_INDEX = 0;

    /*
     * Index which holds the start offset in ballerina.io#writeBytes.
     */
    private static final int START_OFFSET_INDEX = 0;

    /*
     * Index which holds the number of bytes which should be written.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 1;

    private static final Logger log = LoggerFactory.getLogger(Write.class);

    /*
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    /*private static EventResult writeResponse(EventResult<Integer, EventContext> result) {
        *//*
         * Async task response goes here
         *//*
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        Integer numberOfBytesWritten = result.getResponse();
        BStruct errorStruct;
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        return result;
    }*/


    /**
     * Writes bytes to a given channel.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        byte[] content;
        int numberOfBytes;
        int offset;
        int numberOfBytesWritten = 0;
        BStruct errorStruct = null;
        try {
            channel = (BStruct) getRefArgument(context, BYTE_CHANNEL_INDEX);
            content = getBlobArgument(context, CONTENT_INDEX);
            numberOfBytes = (int) getIntArgument(context, NUMBER_OF_BYTES_INDEX);
            offset = (int) getIntArgument(context, START_OFFSET_INDEX);
            Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            numberOfBytesWritten = IOUtils.writeFull(byteChannel, content, offset, numberOfBytes, eventContext);
//            IOUtils.write(byteChannel, content, offset, numberOfBytes, eventContext, Write::writeResponse);
        } catch (Throwable e) {
            String message = "Error occurred while writing bytes:" + e.getMessage();
            log.error(message);
            errorStruct = IOUtils.createError(context, message);
        }
        return getBValues(new BInteger(numberOfBytesWritten), errorStruct);
    }
}
