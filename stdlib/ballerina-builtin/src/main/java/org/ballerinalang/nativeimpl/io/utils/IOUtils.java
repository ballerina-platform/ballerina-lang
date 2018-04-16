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

package org.ballerinalang.nativeimpl.io.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.csv.Format;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.bytes.CloseByteChannelEvent;
import org.ballerinalang.nativeimpl.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.nativeimpl.io.events.bytes.WriteBytesEvent;
import org.ballerinalang.nativeimpl.io.events.characters.CloseCharacterChannelEvent;
import org.ballerinalang.nativeimpl.io.events.characters.ReadCharactersEvent;
import org.ballerinalang.nativeimpl.io.events.characters.WriteCharactersEvent;
import org.ballerinalang.nativeimpl.io.events.records.CloseDelimitedRecordEvent;
import org.ballerinalang.nativeimpl.io.events.records.DelimitedRecordReadEvent;
import org.ballerinalang.nativeimpl.io.events.records.DelimitedRecordWriteEvent;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
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
import java.util.function.Function;

import static org.ballerinalang.nativeimpl.io.IOConstants.IO_ERROR_STRUCT;
import static org.ballerinalang.nativeimpl.io.IOConstants.IO_PACKAGE;

/**
 * Represents the util functions of IO operations.
 */
public class IOUtils {
    /**
     * Returns the error struct for the corresponding message.
     *
     * @param context context of the native function.
     * @param message error message.
     * @return error message struct.
     */
    public static BStruct createError(Context context, String message) {
        PackageInfo ioPkg = context.getProgramFile().getPackageInfo(IO_PACKAGE);
        StructInfo error = ioPkg.getStructInfo(IO_ERROR_STRUCT);
        return BLangVMStructs.createBStruct(error, message);
    }

    /**
     * Asynchronously writes bytes to a channel.
     *
     * @param channel the channel the bytes should be written.
     * @param content content which should be written.
     * @param context context of the native function call.
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
     * @param context context obtained from the native function call.
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
     * Writes bytes to a channel asynchronously.
     * </p>
     *
     * @param channel  channel which will be used to write bytes.
     * @param content  content which will be written.
     * @param offset   the offset which will be set to write bytes.
     * @param context  context of the native function call.
     * @param function callback function which should be called upon completion.
     */
    public static void write(Channel channel, byte[] content, int offset, EventContext context,
                             Function<EventResult, EventResult> function) {
        WriteBytesEvent writeBytesEvent = new WriteBytesEvent(channel, content, offset, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(writeBytesEvent);
        future.thenApply(function);
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
     * @param context context of the native function call.
     * @param channel the channel the content should be read into.
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
     * Reads characters from the channel.
     *
     * @param characterChannel   channel the characters should be read.
     * @param numberOfCharacters the number of characters to read.
     * @param context            context of the event.
     * @param function           the callback function which will be triggered after reading characters.
     */
    public static void read(CharacterChannel characterChannel, int numberOfCharacters, EventContext context
            , Function<EventResult, EventResult> function) {
        ReadCharactersEvent event = new ReadCharactersEvent(characterChannel, numberOfCharacters, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        future.thenApply(function);
    }

    /**
     * Writes characters to a channel.
     *
     * @param characterChannel the channel the characters will be written
     * @param content          the content which will be written.
     * @param offset           if an offset should be specified while writing.
     * @param context          context of the event.
     * @param function         callback function which should be triggered
     */
    public static void write(CharacterChannel characterChannel, String content, int offset,
                             EventContext context, Function<EventResult, EventResult> function) {
        WriteCharactersEvent event = new WriteCharactersEvent(characterChannel, content, offset, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        future.thenApply(function);
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
     * @param context context obtained from the native function.
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
     * <p>
     * Reads bytes asynchronously and trigger the callback.
     * </p>
     *
     * @param channel  the channel which the bytes should be read from.
     * @param content  the byte[] which will holds the content which will be read.
     * @param context  context which will be obtained from the native function call.
     * @param function the callback function which will be triggered.
     */
    public static void read(Channel channel, byte[] content, EventContext context,
                            Function<EventResult, EventResult> function) {
        ReadBytesEvent event = new ReadBytesEvent(channel, content, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        future.thenApply(function);
    }

    /**
     * Reads delimited records asynchronously.
     *
     * @param recordChannel channel the bytes should be read from.
     * @param context       event context.
     * @param function      callback function which will be triggered.
     */
    public static void read(DelimitedRecordChannel recordChannel, EventContext context,
                            Function<EventResult, EventResult> function) {
        DelimitedRecordReadEvent event = new DelimitedRecordReadEvent(recordChannel, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        future.thenApply(function);
    }

    /**
     * Asynchronously writes delimited records to the channel.
     *
     * @param recordChannel channel the records should be written.
     * @param records       the record content.
     * @param context       event context.
     * @param function      callback function which will be triggered.
     */
    public static void write(DelimitedRecordChannel recordChannel, BStringArray records, EventContext context,
                             Function<EventResult, EventResult> function) {
        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(recordChannel, records, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(recordWriteEvent);
        future.thenApply(function);
    }

    /**
     * Closes the channel asynchronously.
     *
     * @param byteChannel  channel which should be closed.
     * @param eventContext context of the event.
     * @param function     callback function which will be triggered.
     */
    public static void close(Channel byteChannel, EventContext eventContext,
                             Function<EventResult, EventResult> function) {
        CloseByteChannelEvent closeEvent = new CloseByteChannelEvent(byteChannel, eventContext);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(closeEvent);
        future.thenApply(function);
    }

    /**
     * Closes the character channel asynchronously.
     *
     * @param charChannel  channel which should be closed.
     * @param eventContext context of the event.
     * @param function     callback function which will be triggered.
     */
    public static void close(CharacterChannel charChannel, EventContext eventContext,
                             Function<EventResult, EventResult> function) {
        CloseCharacterChannelEvent closeEvent = new CloseCharacterChannelEvent(charChannel, eventContext);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(closeEvent);
        future.thenApply(function);
    }

    /**
     * Closes the delimited record channel asynchronously.
     *
     * @param charChannel  channel which should be closed.
     * @param eventContext context of the event.
     * @param function     callback function which will be triggered.
     */
    public static void close(DelimitedRecordChannel charChannel, EventContext eventContext,
                             Function<EventResult, EventResult> function) {
        CloseDelimitedRecordEvent closeEvent = new CloseDelimitedRecordEvent(charChannel, eventContext);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(closeEvent);
        future.thenApply(function);
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

    /**
     * Creates a delimited record channel to read from CSV file.
     *
     * @param filePath path to the CSV file.
     * @param encoding the encoding of CSV file.
     * @param rs       record separator.
     * @param fs       field separator.
     * @return delimited record channel to read from CSV.
     * @throws IOException during I/O error.
     */
    public static DelimitedRecordChannel createDelimitedRecordChannel(String filePath, String encoding, String rs,
                                                                      String fs) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            String msg = "Unable to find a file in given path: " + filePath;
            throw new IOException(msg);
        }
        FileChannel sourceChannel = FileChannel.open(path, StandardOpenOption.READ);
        FileIOChannel fileIOChannel = new FileIOChannel(sourceChannel);
        CharacterChannel characterChannel = new CharacterChannel(fileIOChannel, Charset.forName(encoding).name());
        return new DelimitedRecordChannel(characterChannel, rs, fs);
    }

}
