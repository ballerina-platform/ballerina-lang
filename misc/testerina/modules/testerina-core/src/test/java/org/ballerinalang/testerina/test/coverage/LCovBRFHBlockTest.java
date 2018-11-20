/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.test.coverage;

import org.ballerinalang.testerina.coverage.LCovBRFHBlock;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.test coverage package lcov data format classes.
 *
 * @since 0.985.0
 */
public class LCovBRFHBlockTest {

    private LCovBRFHBlock lCovBRFHBlock;

    @BeforeTest
    public void beforeSuite() {
        lCovBRFHBlock = new LCovBRFHBlock(1, 1);
    }

    @Test(description = "Getter for branch num found with setter")
    public void getBranchNumberFoundWithSetTest() {
        lCovBRFHBlock.setNumOfBranchesFound(2);
        Assert.assertEquals(lCovBRFHBlock.getNumOfBranchesFound(), 2);
    }

    @Test(description = "Getter for branch num hit with setter")
    public void getBranchNumberHitWithSetTest() {
        lCovBRFHBlock.setNumOfBranchesHit(2);
        Assert.assertEquals(lCovBRFHBlock.getNumOfBranchesHit(), 2);
    }

}
