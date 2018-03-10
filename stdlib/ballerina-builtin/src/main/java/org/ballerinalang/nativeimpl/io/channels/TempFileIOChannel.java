/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.nativeimpl.io.channels;

import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A file channel for temp file resources. Once the channel is closed, temp file will be deleted.
 *
 * @since 0.964.0
 */
public class TempFileIOChannel extends FileIOChannel {

    private String tempFilePath;

    public TempFileIOChannel(FileChannel channel, int size, String tempFilePath) throws BallerinaIOException {
        super(channel, size);
        this.tempFilePath = tempFilePath;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (tempFilePath != null && !tempFilePath.isEmpty()) {
            File tempFile = new File(tempFilePath);
            boolean isFileDeleted = tempFile.delete();
            if (!isFileDeleted) {
                throw new BallerinaException("Temporary file deletion failure occurred while closing " +
                        "TempFileIOChannel! ");
            }
        }
    }
}
