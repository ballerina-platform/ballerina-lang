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
public class ReadOnlyIntersectionViaMultipleModulesTest3 extends BaseTest {

    private static final String testFileLocation = Path.of("src/test/resources/packaging/readonly/three")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push down stream packages.
        compilePackageAndPushToLocal(Path.of(testFileLocation, "test_project_immutable_foo").toString(),
                                     "testorg-selectively_immutable_foo3-any-1.0.0");
        compilePackageAndPushToLocal(Path.of(testFileLocation, "test_project_immutable_bar").toString(),
                                     "testorg-selectively_immutable_bar3-any-1.0.0");
        compilePackageAndPushToLocal(Path.of(testFileLocation, "test_project_immutable_baz").toString(),
                                     "testorg-selectively_immutable_baz3-any-1.0.0");
    }

    private void compilePackageAndPushToLocal(String packagePath, String balaFileName) throws BallerinaTestException {
        String targetBalaFilename = Path.of("target/bala/" + balaFileName + ".bala").toString();
        LogLeecher buildLeecher = new LogLeecher(targetBalaFilename);
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed " + targetBalaFilename + " to " +
                                                        "'local' repository.");
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
                Path.of("target/bala/testorg-selectively_immutable_qux3-any-1.0.0.bala").toString());
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                              Path.of(testFileLocation, "test_project_immutable_qux").toString());
        buildLeecher.waitForText(5000);
    }
}
