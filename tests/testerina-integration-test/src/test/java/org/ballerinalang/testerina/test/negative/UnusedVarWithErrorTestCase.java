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
package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.assertEquals;

/**
 * Test to validate number of times the unused var error is logged when there are tests in a module.
 *
 * @since 2.0.0
 */
public class UnusedVarWithErrorTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("unused-var-with-error-test").toString();
    }

    @Test
    public void testUnusedVarWithError() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("build", new String[0], new HashMap<>(), projectPath, true);
        assertEquals(output.split("ERROR \\[main.bal:\\(17:1,17:26\\)] unused variable 'unused1' with inferred type " +
                "including error", -1).length - 1, 1);
        assertEquals(output.split("ERROR \\[main.bal:\\(20:5,20:30\\)] unused variable 'unused2' with inferred type " +
                "including error", -1).length - 1, 1);
        assertEquals(output.split("main_test.bal:\\(21:5,21:30\\)] unused variable 'unused3' with " +
                "inferred type including error", -1).length - 1, 1);
    }
}
