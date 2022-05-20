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
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.PackageDiff;
import org.ballerinalang.semver.checker.ProjectUtil;
import org.testng.Assert;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for testing all function definition related test cases.
 */
public class BaseFunctionTest {

    /**
     * @param testFileName Name of the testcase file name
     */
    protected static void testEvaluate(String testFileName) throws Exception {
        Object obj;
        JsonArray fileData = null;
        try (FileReader reader = new FileReader(testFileName)) {
            obj = JsonParser.parseReader(reader);
            fileData = (JsonArray) obj;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < Objects.requireNonNull(fileData).size(); i++) {
            JsonObject element = (JsonObject) fileData.get(i);
            String oldCode =element.get("oldCode").getAsString();
            String newCode = element.get("newCode").getAsString();
            JsonObject expectedOutput = (JsonObject) element.get("expectedOutput");
            BuildProject oldProject = ProjectUtil.createProject(oldCode);
            BuildProject currentProject = ProjectUtil.createProject(newCode);
            Package oldPackage = oldProject.currentPackage();
            Package currentPackage = currentProject.currentPackage();
            compare(oldPackage , currentPackage , expectedOutput );
        }
    }

    public static void compare(Package oldPackage , Package currentPackage , JsonObject expectedOutput ){
        PackageComparator packageComparator = new PackageComparator(currentPackage,oldPackage);
        Optional<PackageDiff> packageDiff = packageComparator.computeDiff();
        if (expectedOutput.equals(new JsonObject())){
            //For now disabled test cases.
        }
        else{
            packageDiff.ifPresent(diff -> Assert.assertEquals(diff.getAsJson(), expectedOutput));
        }
    }
}






