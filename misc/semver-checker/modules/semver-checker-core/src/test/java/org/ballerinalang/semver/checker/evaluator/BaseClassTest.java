/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.semver.checker.evaluator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.projects.directory.BuildProject;
import org.ballerinalang.semver.checker.ProjectUtil;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Base class for testing all class definition related test cases.
 */
public class BaseClassTest {

    /**
     * @param fileName Name of the testcase file
     */
    protected static void testEvaluate(String fileName) throws Exception {

        Object obj;
        JsonArray fileData = null;
        try (FileReader reader = new FileReader(fileName)) {
            obj = JsonParser.parseReader(reader);
            fileData = (JsonArray) obj;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < Objects.requireNonNull(fileData).size(); i++) {
            JsonObject element = (JsonObject) fileData.get(i);
            String oldCode = String.valueOf(element.get("oldCode"));
            String newCode = String.valueOf(element.get("newCode"));

            /*BuildProject project1 = ProjectUtil.createProject(oldCode);
            BuildProject project2 = ProjectUtil.createProject(newCode);*/
            // call the logic

        }

    }
}


