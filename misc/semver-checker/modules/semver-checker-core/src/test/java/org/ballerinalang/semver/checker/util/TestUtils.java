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

package org.ballerinalang.semver.checker.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.PackageDiff;
import org.ballerinalang.semver.checker.exception.SemverTestException;
import org.testng.Assert;

import java.util.Optional;

/**
 * Utilities to execute/test semver unit test cases.
 *
 * @since 2201.2.0
 */
public class TestUtils {

    private static final String JSON_ATTR_OLD_CODE = "oldCode";
    private static final String JSON_ATTR_NEW_CODE = "newCode";
    private static final String JSON_ATTR_EXPECTED_RESULTS = "expectedOutput";

    /**
     * Executes test cases using the provided data set.
     *
     * @param testData test data as a JSON element
     */
    public static void executeTestData(JsonElement testData) throws SemverTestException {
        try {
            JsonObject testDataObject = (JsonObject) testData;
            String oldCode = testDataObject.get(JSON_ATTR_OLD_CODE).getAsString();
            String newCode = testDataObject.get(JSON_ATTR_NEW_CODE).getAsString();
            JsonObject expectedOutput = (JsonObject) testDataObject.get(JSON_ATTR_EXPECTED_RESULTS);
            BuildProject oldProject = ProjectUtils.createProject(oldCode);
            BuildProject currentProject = ProjectUtils.createProject(newCode);
            Package oldPackage = oldProject.currentPackage();
            Package currentPackage = currentProject.currentPackage();
            assertPackageDiff(oldPackage, currentPackage, expectedOutput);
        } catch (Exception e) {
            throw new SemverTestException("failed to load Ballerina package using test data");
        }
    }

    /**
     * Compares the two given packages and asserts the package diff against the provided expected result.
     *
     * @param oldPackage     Package instance of previous code
     * @param currentPackage package instance of current code
     * @param expectedOutput Expected json output for the test case
     */
    public static void assertPackageDiff(Package oldPackage, Package currentPackage, JsonObject expectedOutput) {
        PackageComparator packageComparator = new PackageComparator(currentPackage, oldPackage);
        Optional<PackageDiff> packageDiff = packageComparator.computeDiff();
        if (expectedOutput.equals(new JsonObject())) {
            // disabled test cases
        } else {
            packageDiff.ifPresent(diff -> Assert.assertEquals(diff.getAsJson(), expectedOutput));
        }
    }
}
