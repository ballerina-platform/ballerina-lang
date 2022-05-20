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

public class BaseVariableDeclarationTest {

    protected static void testEvaluate(String fileName) throws Exception {

        Object obj;
        JsonArray fileData = null;
        try (FileReader reader = new FileReader(fileName)) {
            obj = JsonParser.parseReader(reader);
            fileData = (JsonArray) obj;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < fileData.size(); i++) {
            JsonObject element = (JsonObject) fileData.get(i);
            String oldCode = String.valueOf(element.get("oldCode"));
            String newCode = String.valueOf(element.get("newCode"));
            ProjectUtil project = new ProjectUtil();
            BuildProject project1 = project.createProject(oldCode);
            BuildProject project2 = project.createProject(newCode);
            // call the logic

        }

    }

}
