/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.ballerinalang.runtime.test;

import io.ballerina.runtime.util.CompatibilityChecker;
import io.ballerina.runtime.util.RuntimeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test cases for {@link RuntimeUtils class}.
 */
public class RuntimeUtilTests {

    private static final String JAVA_VERSION_PROPERTY = "java.version";
    private static final PrintStream STD_ERR = System.err;
    private static final String JAVA_VERSION = System.getProperty(JAVA_VERSION_PROPERTY);
    private ByteArrayOutputStream baos;

    @BeforeClass
    public void setup() {
        this.baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(this.baos));
    }

    @Test
    public void testVerifyJavaCompatibilityNegative() {
        // versions in the format" {compiledVersion, runtimeVersion, supportedVersionRange}
        String[][] versions = { { "1.8.0_144", "1.7.0_1", "1.8.*" }, { "1.8.0_144", "9", "1.8.*" },
                { "9.0.1", "10.0.2.1", "9.0.*" }, { "9", "10.0.2.1", "9.0.*" }, { "10", "9", "10.0.*" } };
        for (int i = 0; i < versions.length; i++) {
            testVersionsNegative(versions[i][0], versions[i][1], versions[i][2]);
        }
    }

    @Test
    public void testVerifyJavaCompatibilityPositive() {
        // versions in the format" {compiledVersion, runtimeVersion}
        String[][] versions = { { "1.8.0_144", "1.8.0_144" }, { "1.8.0_144", "1.8.0_221" }, { "9.0_221", "9" },
                { "9", "9.0_221" }, { "10.0.1", "10.0.2.1" }, { "10", "10" } };
        for (int i = 0; i < versions.length; i++) {
            testVersionsPositive(versions[i][0], versions[i][1]);
        }
    }

    private void testVersionsNegative(String compiledVersion, String runtimeVersion, String supportedRange) {
        this.baos.reset();
        System.setProperty(JAVA_VERSION_PROPERTY, runtimeVersion);
        CompatibilityChecker.verifyJavaCompatibility(compiledVersion);
        Assert.assertEquals(new String(this.baos.toByteArray()).trim(),
                "WARNING: Incompatible JRE version '" + runtimeVersion +
                        "' found. This ballerina program supports running on JRE version '" + supportedRange + "'");
    }

    private void testVersionsPositive(String compiledVersion, String runtimeVersion) {
        this.baos.reset();
        System.setProperty(JAVA_VERSION_PROPERTY, runtimeVersion);
        CompatibilityChecker.verifyJavaCompatibility(compiledVersion);
        Assert.assertTrue(this.baos.toByteArray().length == 0,
                "version '" + compiledVersion + "' was incompatible to run on version '" + runtimeVersion + "'");
    }

    @AfterClass
    public void reset() {
        System.setProperty(JAVA_VERSION_PROPERTY, JAVA_VERSION);
        System.setErr(STD_ERR);
    }
}
