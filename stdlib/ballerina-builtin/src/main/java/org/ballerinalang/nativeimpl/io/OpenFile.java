/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.nativeimpl.io.channels.AbstractNativeChannel;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Native function to obtain channel from File.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "openFile",
        args = {@Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "accessMode", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io")},
        isPublic = true
)
public class OpenFile extends AbstractNativeChannel {

    private static final int PATH_FIELD_INDEX = 0;
    private static final int FILE_ACCESS_MODE_INDEX = 1;

    /**
     * Creates a directory at the specified path.
     *
     * @param path the file location url
     */
    private void createDirs(Path path) {
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
     * {@inheritDoc}
     */
    @Override
    public Channel inFlow(Context context) throws BallerinaException {
        String pathUrl = context.getStringArgument(PATH_FIELD_INDEX);
        String accessMode = context.getStringArgument(FILE_ACCESS_MODE_INDEX);
        Path path = null;
        Channel channel;
        try {
            String accessLC = accessMode.toLowerCase(Locale.getDefault());
            path = Paths.get(pathUrl);
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
            FileChannel byteChannel = FileChannel.open(path, opts);
            // channel = new FileIOChannel(byteChannel, IOConstants.CHANNEL_BUFFER_SIZE);
            channel = new FileIOChannel(byteChannel);
        } catch (AccessDeniedException e) {
            throw new BallerinaException("Do not have access to write file: " + path, e);
        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return channel;
    }
}
