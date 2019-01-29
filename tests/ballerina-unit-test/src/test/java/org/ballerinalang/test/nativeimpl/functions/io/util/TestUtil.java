/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.io.util;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the util functions related to I/O test cases.
 */
public class TestUtil {

    /**
     * Opens a channel from a specified file.
     *
     * @param filePath the path to the file.
     * @return the file channel.
     * @throws IOException during I/O error.
     * @throws URISyntaxException during failure to validate uri syntax.
     */
    public static ByteChannel openForReading(String filePath) throws IOException, URISyntaxException {
        Set<OpenOption> opts = new HashSet<>();
        opts.add(StandardOpenOption.READ);
        URL fileResource = TestUtil.class.getClassLoader().getResource(filePath);
        ByteChannel channel = null;
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            channel = Files.newByteChannel(path, opts);
        }
        return channel;
    }

    /**
     * Opens a file for writing.
     *
     * @param filePath the path the file should be opened for writing.
     * @return the writable channel.
     * @throws IOException during I/O error.
     */
    public static ByteChannel openForReadingAndWriting(String filePath) throws IOException {
        Set<OpenOption> opts = new HashSet<>();
        opts.add(StandardOpenOption.CREATE);
        opts.add(StandardOpenOption.WRITE);
        opts.add(StandardOpenOption.READ);
        Path path = Paths.get(filePath);
        return Files.newByteChannel(path, opts);
    }
}
