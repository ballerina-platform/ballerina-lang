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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import java.util.concurrent.ExecutionException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.ErrorCode.GenericError;

/**
 * Represents the util functions of IO operations.
 */
public class IOUtils {

    private static final String PACKAGE_IO = "ballerina/io";

    private IOUtils() {
    }

    /**
     * Creates an error message.
     *
     * @param values  the error details
     * @return an error which will be propagated to ballerina user
     */
    public static ErrorValue createError(Object... values) {
        return BallerinaErrors.createError(GenericError.errorCode(), createDetailRecord(values));
    }

    /**
     * Creates an error message.
     *
     * @param errorMsg  the error message
     * @return an error which will be propagated to ballerina user
     */
    public static ErrorValue createError(String errorMsg) {
        return BallerinaErrors.createError(GenericError.errorCode(), createDetailRecord(errorMsg, null));
    }

    /**
     * Creates an error message with given error code.
     *
     * @param code     the error code which represent the error type
     * @param errorMsg the error message
     * @return an error which will be propagated to ballerina user
     */
    public static ErrorValue createError(IOConstants.ErrorCode code, String errorMsg) {
        return BallerinaErrors.createError(code.errorCode(), createDetailRecord(errorMsg, null));
    }

    private static MapValue<String, Object> createDetailRecord(Object... values) {
        MapValue<String, Object> detail = BallerinaValues
                .createRecordValue(PACKAGE_IO, IOConstants.DETAIL_RECORD_TYPE_NAME);
        return BallerinaValues.createRecord(detail, values);
    }

    /**
     * Asynchronously writes bytes to a channel.
     *
     * @param channel the channel the bytes should be written.
     * @param content content which should be written.
     * @param offset  the start index of the bytes which should be written.
     * @return the number of bytes written to the channel.
     * @throws ExecutionException   errors which occur during execution.
     */
    public static int writeFull(Channel channel, byte[] content, int offset) throws ExecutionException {
        do {
            offset = offset + write(channel, content, offset);
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
     * @return the number of bytes written.
     * @throws ExecutionException   error while execution.
     */
    private static int write(Channel channel, byte[] content, int offset) throws ExecutionException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(content);
        writeBuffer.position(offset);
        int write;
        try {
            write = channel.write(writeBuffer);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        offset = offset + write;
        return offset;
    }

    /**
     * <p>
     * Writes the whole payload to the channel.
     * </p>
     *
     * @param characterChannel the character channel the payload should be written.
     * @param payload          the content.
     * @throws BallerinaException during i/o error.
     */
    public static void writeFull(CharacterChannel characterChannel, String payload)
            throws BallerinaException {
        try {
            int totalNumberOfCharsWritten = 0;
            int numberOfCharsWritten;
            final int lengthOfPayload = payload.getBytes().length;
            do {
                numberOfCharsWritten = characterChannel.write(payload, 0);
                totalNumberOfCharsWritten = totalNumberOfCharsWritten + numberOfCharsWritten;
            } while (totalNumberOfCharsWritten != lengthOfPayload && numberOfCharsWritten != 0);
            if (totalNumberOfCharsWritten != lengthOfPayload) {
                String message = "JSON payload was partially written expected: " + lengthOfPayload + ", written : " +
                        totalNumberOfCharsWritten;
                throw new BallerinaException(message);
            }
        } catch (IOException e) {
            throw new BallerinaException(e);
        }
    }

    /**
     * Asynchronously reads bytes from the channel.
     *
     * @param content the initialized array which should be filled with the content.
     * @param channel the channel the content should be read into.
     * @return the number of bytes read.
     * @throws IOException   errors which occurs while execution.
     */
    public static int readFull(Channel channel, byte[] content) throws IOException {
        int numberOfBytesToRead = content.length;
        int nBytesRead = 0;
        do {
            nBytesRead = nBytesRead + read(channel, content);
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
     * @return the number of bytes read.
     * @throws IOException   errors which occur during execution.
     */
    private static int read(Channel channel, byte[] content) throws IOException {
        if (channel.hasReachedEnd()) {
            throw new IOException(IOConstants.IO_EOF);
        } else {
            return channel.read(ByteBuffer.wrap(content));
        }
    }

    /**
     * Creates a directory at the specified path.
     *
     * @param path the file location url
     */
    private static void createDirsExtended(Path path) {
        Path parent = path.getParent();
        if (parent != null && !parent.toFile().exists()) {
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
    public static FileChannel openFileChannelExtended(Path path, String accessMode) throws IOException {
        String accessLC = accessMode.toLowerCase(Locale.getDefault());
        Set<OpenOption> opts = new HashSet<>();
        if (accessLC.contains("r")) {
            if (!path.toFile().exists()) {
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
            if (path.toFile().exists() && !Files.isWritable(path)) {
                throw new BallerinaException("file is not writable: " + path);
            }
            createDirsExtended(path);
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
    public static DelimitedRecordChannel createDelimitedRecordChannelExtended(String filePath, String encoding,
                                                                              String mode, Format format)
            throws IOException {
        Path path = Paths.get(filePath);
        FileChannel sourceChannel = openFileChannelExtended(path, mode);
        FileIOChannel fileIOChannel = new FileIOChannel(sourceChannel);
        CharacterChannel characterChannel = new CharacterChannel(fileIOChannel, Charset.forName(encoding).name());
        return new DelimitedRecordChannel(characterChannel, format);
    }
}
