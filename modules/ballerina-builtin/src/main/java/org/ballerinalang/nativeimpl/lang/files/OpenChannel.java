/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.files;


import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.BByteChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.channels.ByteChannel;
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
 * Gets the streams from a local file.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "openChannel",
        args = {@Argument(name = "file", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"), @Argument(name = "accessMode", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets streams from a local file")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File that should be opened")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "accessMode",
        value = "The mode the file should be opened in")})
public class OpenChannel extends AbstractNativeFunction {

    /**
     * represents the information related to the byte channel
     */
    private StructInfo byteChannelStructInfo;

    /**
     * The package path of the byte channel
     */
    private static final String BYTE_CHANNEL_PACKAGE = "ballerina.io";

    /**
     * The type of the byte channel
     */
    private static final String STRUCT_TYPE = "ByteChannel";

    /**
     * Represents the type of the channel
     */
    private static final String CHANNEL_TYPE = "file";

    /**
     * Gets the struct related to BByteChannel
     *
     * @param context invocation context
     * @return the struct related to BByteChannel
     */
    private StructInfo getByteChannelStructInfo(Context context) {
        StructInfo result = byteChannelStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(BYTE_CHANNEL_PACKAGE);
            byteChannelStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE);
        }
        return byteChannelStructInfo;
    }

    /**
     * Creates a directory at the specified path
     *
     * @param path the file location url
     */
    private void createDirs(Path path) {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new BallerinaException("Error in writing file");
            }
        }
    }

    @Override
    public BValue[] execute(Context context) {
        BStruct fileStruct = (BStruct) getRefArgument(context, 0);
        BStruct channelStruct = null;
        String accessMode = getStringArgument(context, 0);
        Path path = null;
        try {
            String accessLC = accessMode.toLowerCase(Locale.getDefault());

            path = Paths.get(fileStruct.getStringField(0));
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
            ByteChannel channel = Files.newByteChannel(path, opts);
            channelStruct = BLangVMStructs.createBStruct(getByteChannelStructInfo(context), CHANNEL_TYPE);
            BByteChannel byteChannel = new BByteChannel(channel);
            channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
        } catch (AccessDeniedException e) {
            throw new BallerinaException("Do not have access to write file: " + path, e);
        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return getBValues(channelStruct);
    }
}
