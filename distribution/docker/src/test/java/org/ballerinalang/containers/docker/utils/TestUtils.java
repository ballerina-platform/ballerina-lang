/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ballerinalang.containers.docker.utils;

import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.client.DockerClient;
import org.ballerinalang.containers.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for Container support Tests.
 */
public class TestUtils {
    public static void deleteDockerImage(String imageName) {
        if (!imageName.contains(":")) {
            imageName += ":" + Constants.IMAGE_VERSION_LATEST;
        }
        DockerClient client = new io.fabric8.docker.client.DefaultDockerClient();
        List<ImageDelete> imageDeleteList = client.image().withName(imageName)
                .delete()
                .force()
                .andPrune(false);

        for (ImageDelete imageDelete : imageDeleteList) {
            imageDelete.getDeleted();
            imageDelete.getUntagged();
        }
    }

    public static List<Path> getTestServiceAsPathList() throws FileNotFoundException {
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestService.bsz").getPath());
        List<Path> packagePaths = new ArrayList<>();
        packagePaths.add(ballerinaPackage);
        return packagePaths;
    }

    public static List<Path> getTestFunctionAsPathList() throws FileNotFoundException {
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bmz").getPath());
        List<Path> packagePaths = new ArrayList<>();
        packagePaths.add(ballerinaPackage);
        return packagePaths;
    }

    public static String getTestServiceAsString() throws IOException {
        return new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("ballerina/TestService.bal").getPath())));
    }

    public static String getTestFunctionAsString() throws IOException {
        return new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("ballerina/TestFunction.bal").getPath())));
    }
}
