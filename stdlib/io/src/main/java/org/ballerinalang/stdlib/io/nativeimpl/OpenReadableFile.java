/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.io.channels.AbstractNativeChannel;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOUtils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function to obtain channel from File.
 *
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "openReadableFile",
        args = {@Argument(name = "path", type = TypeKind.STRING)},
        isPublic = true
)
public class OpenReadableFile extends AbstractNativeChannel {

    /**
     * Read only access mode.
     */
    private static final String READ_ACCESS_MODE = "r";

    public static Object openReadableFile(Strand strand, String pathUrl) {
        Object channel;
        try {
            channel = createChannel(inFlow(pathUrl));
        } catch (AccessDeniedException e) {
            channel = IOUtils.createError("Do not have access to read file: " + e.getMessage());
        } catch (Throwable e) {
            channel = IOUtils.createError("Failed to open file: " + e.getMessage());
        }
        return channel;
    }

    private static Channel inFlow(String pathUrl) throws IOException {
        Path path = Paths.get(pathUrl);
        FileChannel fileChannel = IOUtils.openFileChannelExtended(path, READ_ACCESS_MODE);
        Channel channel = new FileIOChannel(fileChannel);
        channel.setReadable(true);
        return channel;
    }
}
