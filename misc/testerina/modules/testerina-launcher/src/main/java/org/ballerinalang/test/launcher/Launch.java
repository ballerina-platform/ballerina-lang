/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.launcher;

import com.google.gson.Gson;
import org.ballerinalang.jvm.launch.LaunchUtils;
import org.ballerinalang.test.launcher.entity.TestSuite;
import org.ballerinalang.test.launcher.util.TesterinaConstants;
import org.ballerinalang.test.launcher.util.TesterinaUtils;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Main class to init the test suit.
 */
public class Launch {

    public static void main(String[] args) throws Exception {
        Path jsonCachePath = Paths.get(args[0], TesterinaConstants.TESTERINA_TEST_SUITE);
        System.out.println(jsonCachePath);
        String[] configArgs = Arrays.copyOfRange(args, 1, args.length);
        LaunchUtils.initConfigurations(configArgs);
        BufferedReader br = Files.newBufferedReader(jsonCachePath, StandardCharsets.UTF_8);

        //convert the json string back to object
        Gson gson = new Gson();
        TestSuite response = gson.fromJson(br, TestSuite.class);
        startTestSuit(Paths.get(response.getSourceRootPath()), response);
    }


    private static void startTestSuit(Path sourceRootPath, TestSuite testSuite) throws ClassNotFoundException {
        TesterinaUtils.executeTests(sourceRootPath, testSuite);
    }
}
