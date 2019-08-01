/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.AbstractNativeChannel;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function to obtain channel from File.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "openWritableFile",
        args = {@Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "append", type = TypeKind.BOOLEAN)},
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "WritableByteChannel", structPackage = "ballerina/io")
        },
        isPublic = true
)
public class OpenWritableFile extends AbstractNativeChannel {

    /**
     * Read only access mode.
     */
    private static final String WRITE_ACCESS_MODE = "w";
    /**
     * Append content.
     */
    private static final String APPEND_ACCESS_MODE = "a";


    public static Object openWritableFile(Strand strand, String pathUrl, boolean accessMode) {
        return createChannel(inFlow(pathUrl, accessMode));
    }

    private static Channel inFlow(String pathUrl, boolean accessMode) throws BallerinaException {
        Channel channel;
        try {
            Path path = Paths.get(pathUrl);
            FileChannel fileChannel;
            if (accessMode) {
                fileChannel = IOUtils.openFileChannelExtended(path, APPEND_ACCESS_MODE);
            } else {
                fileChannel = IOUtils.openFileChannelExtended(path, WRITE_ACCESS_MODE);
            }
            channel = new FileIOChannel(fileChannel);
        } catch (AccessDeniedException e) {
            throw new BallerinaException("Do not have access to write file: ", e);
        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return channel;
    }
}
