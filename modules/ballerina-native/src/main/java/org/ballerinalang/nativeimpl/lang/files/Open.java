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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
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
        functionName = "open",
        args = {@Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"), @Argument(name = "accessMode", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets streams from a local file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File that should be opened") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "accessMode",
        value = "The mode the file should be opened in") })
public class Open extends AbstractNativeFunction {
    
    @Override 
    public BValue[] execute(Context context) {
        BStruct struct = (BStruct) getRefArgument(context, 0);
        String accessMode = getStringArgument(context, 0);
        try {
            String accessLC = accessMode.toLowerCase(Locale.getDefault());

            Path path = Paths.get(struct.getStringField(0));
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
            SeekableByteChannel channel = Files.newByteChannel(path, opts);
            struct.addNativeData("channel", channel);

        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return VOID_RETURN;
    }

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
}
