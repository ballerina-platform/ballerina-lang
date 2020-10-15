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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BError;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.ballerinalang.stdlib.io.utils.IOConstants.ErrorCode.AccessDeniedError;
import static org.ballerinalang.stdlib.io.utils.IOConstants.ErrorCode.EoF;
import static org.ballerinalang.stdlib.io.utils.IOConstants.ErrorCode.FileNotFoundError;
import static org.ballerinalang.stdlib.io.utils.IOConstants.ErrorCode.GenericError;
import static org.ballerinalang.stdlib.io.utils.IOConstants.IO_PACKAGE_ID;

/**
 * Represents the util functions of IO operations.
 */
public class IOUtils {

    private IOUtils() {
    }

    /**
     * Creates an error message.
     *
     * @param errorMsg  the error message
     * @return an error which will be propagated to ballerina user
     */
    public static BError createError(String errorMsg) {
        return ErrorCreator.createDistinctError(GenericError.errorCode(), IO_PACKAGE_ID,
                                                StringUtils.fromString(errorMsg));
    }

    /**
     * Creates an error message.
     *
     * @param error Java throwable instance
     * @return an error which will be propagated to ballerina user
     */
    public static BError createError(Throwable error) {
        return createError(error.getMessage());
    }

    /**
     * Creates an error message with given error type.
     *
     * @param code     the error code which represent the error type
     * @param errorMsg the error message
     * @return an error which will be propagated to ballerina user
     */
    public static BError createError(IOConstants.ErrorCode code, String errorMsg) {
        return ErrorCreator.createDistinctError(code.errorCode(), IO_PACKAGE_ID, StringUtils.fromString(errorMsg));
    }

    /**
     * Create an EoF error instance.
     *
     * @return EoF Error
     */
    public static BError createEoFError() {
        return IOUtils.createError(EoF, "EoF when reading from the channel");
    }

    /**
     * Asynchronously writes bytes to a channel.
     *
     * @param channel the channel the bytes should be written.
     * @param content content which should be written.
     * @param offset  the start index of the bytes which should be written.
     * @return the number of bytes written to the channel.
     * @throws IOException   errors which occur during execution.
     */
    public static int writeFull(Channel channel, byte[] content, int offset) throws IOException {
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
     * @throws IOException   error while execution.
     */
    private static int write(Channel channel, byte[] content, int offset) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(content);
        writeBuffer.position(offset);
        int write = channel.write(writeBuffer);
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
     * @throws BallerinaIOException during i/o error.
     */
    public static void writeFull(CharacterChannel characterChannel, String payload) throws BallerinaIOException {
        try {
            int totalNumberOfCharsWritten = 0;
            int numberOfCharsWritten;
            final int lengthOfPayload = payload.getBytes().length;
            do {
                numberOfCharsWritten = characterChannel.write(payload, 0);
                totalNumberOfCharsWritten = totalNumberOfCharsWritten + numberOfCharsWritten;
            } while (totalNumberOfCharsWritten != lengthOfPayload && numberOfCharsWritten != 0);
            if (totalNumberOfCharsWritten != lengthOfPayload) {
                String message = String
                        .format("JSON payload was partially written expected: %d, written : %d", lengthOfPayload,
                                totalNumberOfCharsWritten);
                throw new BallerinaIOException(message);
            }
        } catch (IOException e) {
            throw new BallerinaIOException("unable to write the content fully", e);
        }
    }

    /**
     * Asynchronously reads bytes from the channel.
     *
     * @param content the initialized array which should be filled with the content.
     * @param channel the channel the content should be read into.
     * @return the number of bytes read.
     * @throws IOException   errors which occurs while execution.
     * @throws BError instance of {ballerina/io}EoF when channel reach the EoF.
     */
    public static int readFull(Channel channel, byte[] content) throws IOException, BError {
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
     * @throws BError instance of {ballerina/io}EoF when channel reach the EoF.
     */
    private static int read(Channel channel, byte[] content) throws IOException, BError {
        if (channel.hasReachedEnd()) {
            throw createEoFError();
        } else {
            return channel.read(ByteBuffer.wrap(content));
        }
    }

    /**
     * Creates a directory at the specified path.
     *
     * @param path the file location url
     */
    private static void createDirsExtended(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !parent.toFile().exists()) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Open a file channel from the given path.
     *
     * @param path       path to the file.
     * @param accessMode file access mode.
     * @return the file channel which will hold the reference.
     * @throws BallerinaIOException during i/o error.
     */
    public static FileChannel openFileChannelExtended(Path path, String accessMode) throws BallerinaIOException {
        String accessLC = accessMode.toLowerCase(Locale.getDefault());
        Set<OpenOption> opts = new HashSet<>();
        if (accessLC.contains("r")) {
            if (!path.toFile().exists()) {
                String msg = "no such file or directory: " + path.toFile().getAbsolutePath();
                throw createError(FileNotFoundError, msg);
            }
            if (!Files.isReadable(path)) {
                throw new BallerinaIOException("file is not readable: " + path);
            }
            opts.add(StandardOpenOption.READ);
        }
        boolean write = accessLC.contains("w");
        boolean append = accessLC.contains("a");
        try {
            if (write || append) {
                if (path.toFile().exists() && !Files.isWritable(path)) {
                    throw new BallerinaIOException("file is not writable: " + path);
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
        } catch (AccessDeniedException e) {
            String msg = "do not have necessary permissions to access: " + e.getMessage();
            throw createError(AccessDeniedError, msg);
        } catch (IOException | UnsupportedOperationException e) {
            throw new BallerinaIOException("fail to open file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a delimited record channel to read from CSV file.
     *
     * @param filePath path to the CSV file.
     * @param encoding the encoding of CSV file.
     * @param mode     permission to access the file.
     * @param format   format of the CSV file.
     * @return delimited record channel to read from CSV.
     * @throws BallerinaIOException during I/O error.
     */
    public static DelimitedRecordChannel createDelimitedRecordChannelExtended(String filePath, String encoding,
                                                                              String mode, Format format)
            throws BallerinaIOException {
        Path path = Paths.get(filePath);
        FileChannel sourceChannel = openFileChannelExtended(path, mode);
        FileIOChannel fileIOChannel = new FileIOChannel(sourceChannel);
        CharacterChannel characterChannel = new CharacterChannel(fileIOChannel, Charset.forName(encoding).name());
        return new DelimitedRecordChannel(characterChannel, format);
    }
}
