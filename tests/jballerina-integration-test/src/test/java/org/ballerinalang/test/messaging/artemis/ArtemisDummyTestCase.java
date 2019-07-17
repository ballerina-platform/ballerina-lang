/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.messaging.artemis;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This is a dummy test that ensures @BeforeGroups runs before the actual test classes. When there is @BeforeClass in a
 * test, it happens to run before @BeforeGroups. This class will run before any other test classes ensuring that
 * the annotation @BeforeGroups runs before other tests.
 */
@Test(groups = "artemis-test")
public class ArtemisDummyTestCase {
    @BeforeClass
    public void setup() throws BallerinaTestException {
    }

    @Test(description = "dummy test to trigger before groups")
    public void dummyTest() {
        Assert.assertTrue(true);
    }

    @AfterClass
    private void teardown() throws Exception {
    }
}
