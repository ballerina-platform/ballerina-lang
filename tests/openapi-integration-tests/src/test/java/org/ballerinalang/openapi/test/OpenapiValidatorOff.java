/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.openapi.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OpenapiValidatorOff extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = basicTestsProjectPath.toString();
    }
    @Test
    public void testOpenapiValidatorOff() throws BallerinaTestException {
        String msg1 = "warning: openapi-test/openapi-validator-off:0.0.0::openapi-validator-off.bal:15:17:" +
                " Couldn't find a Ballerina service resource for the path '/{param1}/{param2}'" +
                " which is documented in the OpenAPI contract";
        String msg2 = "warning: openapi-test/openapi-validator-off:0.0.0::openapi-validator-off.bal:19:9:" +
                " Ballerina service contains a Resource that is not documented in the OpenAPI contract." +
                " Error Resource path '/{param1}/{param3}'";

        LogLeecher clientLeecher1 = new LogLeecher(msg1, LogLeecher.LeecherType.ERROR);
        LogLeecher clientLeecher2 = new LogLeecher(msg2, LogLeecher.LeecherType.ERROR);
        balClient.runMain("test", new String[]{ "openapi-validator-off"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }
}
