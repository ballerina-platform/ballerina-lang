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
package org.ballerinalang.test.service.grpc.tool;

import com.google.common.io.Files;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.exception.BalGenToolException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_PREFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_URL_SUFFIX;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.grantPermission;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.saveFile;

/**
 * Test util class contains util functions to retrieve proto descriptor.
 */
public class ProtoDescriptorUtils {

    public static Path getProtocCompiler() {
        String compilerFilename = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
        if (exePath == null) {
            exePath = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
            File exeFile = new File(exePath);
            exePath = exeFile.getAbsolutePath(); // if file already exists will do nothing
            if (!exeFile.isFile()) {
                outStream.println("Downloading proc executor ...");
                try {
                    boolean newFile = exeFile.createNewFile();
                    if (newFile) {
                        LOG.debug("Successfully created new protoc exe file" + exePath);
                    }
                } catch (IOException e) {
                    throw new BalGenToolException("Exception occurred while creating new file for protoc exe. ", e);
                }
                String url = PROTOC_PLUGIN_EXE_URL_SUFFIX + protocVersion + "/protoc-" + protocVersion + "-" +
                        OSDetector.getDetectedClassifier() + PROTOC_PLUGIN_EXE_PREFIX;
                try {
                    saveFile(new URL(url), exePath);
                    File file = new File(exePath);
                    //set application user permissions to 455
                    grantPermission(file);
                } catch (IOException e) {
                    throw new BalGenToolException("Exception occurred while writing protoc executable to file. ", e);
                }
                outStream.println("Download successfully completed!");
            } else {
                grantPermission(exeFile);
                outStream.println("Continue with existing protoc executor.");
            }
        } else {
            outStream.println("Pre-Downloaded descriptor detected ...");
        }
    }
}
