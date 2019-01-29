/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventExecutor;
import org.ballerinalang.stdlib.io.events.EventManager;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.stdlib.io.events.bytes.WriteBytesEvent;
import org.ballerinalang.stdlib.io.events.characters.WriteCharactersEvent;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents the util functions of IO operations.
 */
public class IOUtils {

    /**
     * IO package name.
     */
    private static final String PROTOCOL_PACKAGE_IO = "ballerina/io";
    /**
     * IO error struct name.
     */
    private static final String IO_ERROR = "IOError";
    /**
     * message which will be propagated.
     */
    private static final String MESSAGE = "message";
/*
    /**
     * Returns the error struct for the corresponding message.
     *
     * @param context context of the extern function.
     * @param message error message.
     * @return error message struct.
     *//*

    public static BMap<String, BValue> createError(Context context, String message) {
        PackageInfo ioPkg = context.getProgramFile().getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo error = ioPkg.getStructInfo(BLangVMErrors.STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(error, message);
    }
*/

    /**
     * Creates an error message.
     *
     * @param context context which is invoked.
     * @param errCode the error code.
     * @param errMsg  the cause for the error.
     * @return an error which will be propagated to ballerina user.
     */
    public static BError createError(Context context, String errCode, String errMsg) {
        BMap<String, BValue> ioErrorRecord = BLangConnectorSPIUtil.createBStruct(context,
                PROTOCOL_PACKAGE_IO,
                IO_ERROR);
        ioErrorRecord.put(MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, errCode, ioErrorRecord);
    }

    /**
     * Asynchronously writes bytes to a channel.
     *
     * @param channel the channel the bytes should be written.
     * @param content content which should be written.
     * @param context context of the extern function call.
     * @param offset  the start index of the bytes which should be written.
     * @return the number of bytes written to the channel.
     * @throws ExecutionException   errors which occur during execution.
     * @throws InterruptedException during interrupt error.
     */
    public static int writeFull(Channel channel, byte[] content, int offset, EventContext context)
            throws ExecutionException, InterruptedException {
        do {
            offset = offset + write(channel, content, offset, context);
        } while (offset < content.length);
        return offset;
    }

    /**
     * <p>
     * Writes bytes to a channel.
     * </p>
     * <p>
     * This will be a blocking call.
     * </p>
     *
     * @param channel channel which should be used to write bytes.
     * @param content content which should be written.
     * @param offset  offset which should be set when writing bytes.
     * @param context context obtained from the extern function call.
     * @return the number of bytes written.
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException   error while execution.
     */
    private static int write(Channel channel, byte[] content, int offset, EventContext context)
            throws InterruptedException, ExecutionException {
        WriteBytesEvent writeBytesEvent = new WriteBytesEvent(channel, content, offset, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(writeBytesEvent);
        EventResult eventResponse = future.get();
        offset = offset + (Integer) eventResponse.getResponse();
        Throwable error = ((EventContext) eventResponse.getContext()).getError();
        if (null != error) {
            throw new ExecutionException(error);
        }
        return offset;
    }

    /**
     * <p>
     * Validates whether the channel has reached it's end or the channel is closed.
     * </p>
     * <p>
     * At an event the channel has reached it's end, the corresponding discard will be called.
     * Discard will clean the existing state.
     * </p>
     *
     * @param event the event which was executed.
     * @return true if the channel has reached it's end.
     */
    public static boolean validateChannelState(EventContext event) {
        Register register = event.getRegister();
        EventExecutor exec = register.getExec();
        Channel channel = exec.getChannel();
        if (!channel.getByteChannel().isOpen()) {
            register.discard();
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Writes the whole payload to the channel.
     * </p>
     *
     * @param characterChannel the character channel the payload should be written.
     * @param payload          the content.
     * @param eventContext     the context of the event.
     * @throws BallerinaException during i/o error.
     */
    public static void writeFull(CharacterChannel characterChannel, String payload, EventContext eventContext) throws
            BallerinaException {
        try {
            int totalNumberOfCharsWritten = 0;
            int numberOfCharsWritten;
            final int lengthOfPayload = payload.length();
            do {
                WriteCharactersEvent event = new WriteCharactersEvent(characterChannel, payload, 0, eventContext);
                CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
                EventResult eventResult = future.get();
                numberOfCharsWritten = (Integer) eventResult.getResponse();
                totalNumberOfCharsWritten = totalNumberOfCharsWritten + numberOfCharsWritten;
            } while (totalNumberOfCharsWritten != lengthOfPayload && numberOfCharsWritten != 0);
            if (totalNumberOfCharsWritten != lengthOfPayload) {
                String message = "JSON payload was partially written expected:" + lengthOfPayload + ",written : " +
                        totalNumberOfCharsWritten;
                throw new BallerinaException(message);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new BallerinaException(e);
        }
    }

    /**
     * Asynchronously reads bytes from the channel.
     *
     * @param content the initialized array which should be filled with the content.
     * @param context context of the extern function call.
     * @param channel the channel the content should be read into.
     * @return the number of bytes read.
     * @throws InterruptedException during interrupt error.
     * @throws ExecutionException   errors which occurs while execution.
     */
    public static int readFull(Channel channel, byte[] content, EventContext context)
            throws InterruptedException, ExecutionException {
        int numberOfBytesToRead = content.length;
        int nBytesRead = 0;
        do {
            nBytesRead = nBytesRead + read(channel, content, context);
        } while (nBytesRead < numberOfBytesToRead && !channel.hasReachedEnd());
        return nBytesRead;
    }

    /**
     * <p>
     * Reads bytes from a channel and will obtain the response.
     * </p>
     * <p>
     * This operation will be blocking.
     * </p>
     *
     * @param channel channel the bytes should be read from.
     * @param content byte [] which will hold the content which is read.
     * @param context context obtained from the extern function.
     * @return the number of bytes read.
     * @throws InterruptedException errors which occur if the thread is interrupted.
     * @throws ExecutionException   errors which occur during execution.
     */
    private static int read(Channel channel, byte[] content, EventContext context)
            throws InterruptedException, ExecutionException {
        ReadBytesEvent event = new ReadBytesEvent(channel, content, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        EventResult eventResponse = future.get();
        int numberOfBytesRead = (Integer) eventResponse.getResponse();
        Throwable error = ((EventContext) eventResponse.getContext()).getError();
        if (null != error) {
            throw new ExecutionException(error);
        }
        return numberOfBytesRead;
    }

    /**
     * Creates a directory at the specified path.
     *
     * @param path the file location url
     */
    private static void createDirs(Path path) {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new BallerinaException("Error in creating directory.", e);
            }
        }
    }

    /**
     * Open a file channel from the given path.
     *
     * @param path       path to the file.
     * @param accessMode file access mode.
     * @return the filechannel which will hold the reference.
     * @throws IOException during i/o error.
     */
    public static FileChannel openFileChannel(Path path, String accessMode) throws IOException {
        String accessLC = accessMode.toLowerCase(Locale.getDefault());
        Set<OpenOption> opts = new HashSet<>();
        if (accessLC.contains("r")) {
            if (!Files.exists(path)) {
                throw new BallerinaException("file not found: " + path);
            }
            if (!Files.isReadable(path)) {
                throw new BallerinaException("file is not readable: " + path);
            }
            opts.add(StandardOpenOption.READ);
        }
        boolean write = accessLC.contains("w");
        boolean append = accessLC.contains("a");
        if (write || append) {
            if (Files.exists(path) && !Files.isWritable(path)) {
                throw new BallerinaException("file is not writable: " + path);
            }
            createDirs(path);
            opts.add(StandardOpenOption.CREATE);
            if (append) {
                opts.add(StandardOpenOption.APPEND);
            } else {
                opts.add(StandardOpenOption.WRITE);
            }
        }
        return FileChannel.open(path, opts);
    }

    /**
     * Creates a delimited record channel to read from CSV file.
     *
     * @param filePath path to the CSV file.
     * @param encoding the encoding of CSV file.
     * @param mode     permission to access the file.
     * @param format   format of the CSV file.
     * @return delimited record channel to read from CSV.
     * @throws IOException during I/O error.
     */
    public static DelimitedRecordChannel createDelimitedRecordChannel(String filePath, String encoding, String mode,
                                                                      Format format)
            throws IOException {
        Path path = Paths.get(filePath);
        FileChannel sourceChannel = openFileChannel(path, mode);
        FileIOChannel fileIOChannel = new FileIOChannel(sourceChannel);
        CharacterChannel characterChannel = new CharacterChannel(fileIOChannel, Charset.forName(encoding).name());
        return new DelimitedRecordChannel(characterChannel, format);
    }

}
