/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.websub.advanced;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class is a dummy test to trigger before groups.
 */
@Test(groups = "websub-test") // not necessary, introduced as a workaround to get @BeforeGroup to run before other tests
public class WebSubDummyTestCase extends WebSubAdvancedBaseTest {
    @BeforeClass
    public void setup() throws BallerinaTestException {
    }

    @Test(description = "dummy test to trigger before groups")
    public void dummyTest() throws BallerinaTestException {
        Assert.assertTrue(true);
    }

    @AfterClass
    private void teardown() throws Exception {
    }
}
