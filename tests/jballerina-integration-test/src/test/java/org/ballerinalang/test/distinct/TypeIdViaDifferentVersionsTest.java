/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.distinct;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Tests type ID compatibility of the same construct via different versions of the same module.
 *
 * @since 2.0.1
 */
public class TypeIdViaDifferentVersionsTest extends BaseTest {

    private static final String testFileLocation =
            Paths.get("src", "test", "resources", "packaging", "distinct")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push down stream packages.
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "test_project_distinct_foo").toString(),
                                     "testorg-distinct_foo-any-1.0.0");
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "test_project_distinct_bar").toString(),
                                     "testorg-distinct_bar-any-1.0.0");
        // Triggers caching the BIR for foo 1.0.0.
        buildPackage("baz");
    }

    private void compilePackageAndPushToLocal(String packagePath, String balaFileName) throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher("target/bala/" + balaFileName + ".bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target/bala/" + balaFileName + ".bala to " +
                                                        "'local' repository.");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                              packagePath);
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                              packagePath);
        pushLeecher.waitForText(5000);
    }

    @Test
    public void testTypeIDsViaDifferentVersions() throws BallerinaTestException {
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "test_project_distinct_foo_patch").toString(),
                                     "testorg-distinct_foo-any-1.0.1");

        buildPackage("qux");
    }

    private void buildPackage(String name) throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher("target/bala/testorg-distinct_" + name + "-any-1.0.0.bala");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                              Paths.get(testFileLocation, "test_project_distinct_" + name).toString());
        buildLeecher.waitForText(5000);
    }
}
