/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.readonly;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;


/**
 * Tests intersection of the same construct via multiple modules.
 *
 * @since 2.0.0
 */
public class ReadOnlyIntersectionViaMultipleModulesTest extends BaseTest {

    private static final Path testFileLocation = Path.of("src/test/resources/packaging/readonly/one");
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push down stream packages.
        compilePackageAndPushToLocal(testFileLocation.resolve("test_project_immutable_foo"),
                                     "testorg-selectively_immutable_foo-any-1.0.0");
        compilePackageAndPushToLocal(testFileLocation.resolve("test_project_immutable_bar"),
                                     "testorg-selectively_immutable_bar-any-1.0.0");
        compilePackageAndPushToLocal(testFileLocation.resolve("test_project_immutable_baz"),
                                     "testorg-selectively_immutable_baz-any-1.0.0");
    }

    private void compilePackageAndPushToLocal(Path packagePath, String balaFileName) throws BallerinaTestException {
        String targetBala = Path.of("target/bala/" + balaFileName + ".bala").toString();
        LogLeecher buildLeecher = new LogLeecher(targetBala);
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed " + targetBala + " to 'local' repository.");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                              packagePath);
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                              packagePath);
        pushLeecher.waitForText(5000);
    }

    @Test
    public void testSameIntersectionViaDifferentModules() throws BallerinaTestException {
        buildQux();
        // Should load via the BIR.
        buildQux();
    }

    private void buildQux() throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher(
                Path.of("target/bala/testorg-selectively_immutable_qux-any-1.0.0.bala").toString());
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                testFileLocation.resolve("test_project_immutable_qux"));
        buildLeecher.waitForText(5000);
    }
}
