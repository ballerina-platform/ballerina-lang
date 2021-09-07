/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for connector info.
 */
public class BallerinaConnectorInfoTest {

    private BallerinaConnectorInfo connectorInfo;

    @BeforeClass
    public void initConnectorInfo() {
        connectorInfo = new BallerinaConnectorInfo(
                "1",
                "ballerinax",
                "slack",
                "0.1.0",
                "Client",
                "any",
                "slbeta2"
        );
    }

    @Test(description = "Test version from connector info object")
    public void getVersionTest() {
        String version = "0.1.0";
        Assert.assertEquals(version, connectorInfo.getVersion());
    }

    @Test(description = "Test overloaded constructor with display name")
    public void createConnectorInfoWithDisplayName() {
        String displayName = "Slack";
        connectorInfo = new BallerinaConnectorInfo(
                "1",
                "ballerinax",
                "slack",
                "0.1.0",
                "Client",
                "any",
                "slbeta2",
                "Slack"
        );
        Assert.assertEquals(displayName, connectorInfo.getDisplayName());
    }
}
