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

package org.ballerinalang.nativeimpl.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.nativeimpl.io.channels.AbstractNativeChannel;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

/**
 * Returns a new byte channel from the path.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "newByteChannel",
        args = {
                @Argument(name = "path", type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file"),
                @Argument(name = "accessMode", type = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")
        },
        isPublic = true
)
public class NewByteChannel extends AbstractNativeChannel {
    /**
     * Specifies the access mode to the file.
     */
    private static final int FILE_ACCESS_MODE_INDEX = 0;
    /**
     * Specifies the index of the path.
     */
    private static final int PATH_INDEX = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel inFlow(Context context) throws BallerinaException {
        BStruct pathStruct = (BStruct) context.getRefArgument(PATH_INDEX);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        String accessMode = context.getStringArgument(FILE_ACCESS_MODE_INDEX);
        Channel channel;
        try {
            FileChannel byteChannel = IOUtils.openFileChannel(path, accessMode);
            channel = new FileIOChannel(byteChannel);
        } catch (AccessDeniedException e) {
            throw new BallerinaException("Do not have access to write file: " + path, e);
        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return channel;
    }
}
