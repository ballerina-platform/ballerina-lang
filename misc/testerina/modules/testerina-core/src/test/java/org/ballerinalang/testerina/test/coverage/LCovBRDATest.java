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

import org.ballerinalang.testerina.coverage.LCovBRDA;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.test coverage package lcov data format classes.
 *
 * @since 0.985.0
 */
public class LCovBRDATest {

    private LCovBRDA lCovBRDA;

    @BeforeTest
    public void beforeSuite() {
        lCovBRDA = new LCovBRDA(1, 1, 1, "-");
    }

    @Test(description = "Getter for line num with setter")
    public void getLineNumberWithSetTest() {
        lCovBRDA.setLineNumber(2);
        Assert.assertEquals(lCovBRDA.getLineNumber(), 2);
    }

    @Test(description = "Getter for block num with setter")
    public void getBlockNumberWithSetTest() {
        lCovBRDA.setBlockNumber(2);
        Assert.assertEquals(lCovBRDA.getBlockNumber(), 2);
    }

    @Test(description = "Getter for branch num with setter")
    public void getBranchNumberWithSetTest() {
        lCovBRDA.setBranchNumber(2);
        Assert.assertEquals(lCovBRDA.getBranchNumber(), 2);
    }

    @Test(description = "Getter for taken field with setter")
    public void getTakenWithSetTest() {
        lCovBRDA.setTaken("2");
        Assert.assertEquals(lCovBRDA.getTaken(), "2");
    }

}
