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

import org.ballerinalang.testerina.coverage.LCovFNDA;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.test coverage package lcov data format classes.
 *
 * @since 0.985.0
 */
public class LCovFNDATest {

    private LCovFNDA lCovFNDA;

    @BeforeTest
    public void beforeSuite() {
        lCovFNDA = new LCovFNDA(1, "funcName");
    }

    @Test(description = "Getter for exec count with setter")
    public void getExeCountWithSetTest() {
        lCovFNDA.setExecutionCount(2);
        Assert.assertEquals(lCovFNDA.getExecutionCount(), 2);
    }

    @Test(description = "Getter for func name with setter")
    public void getFuncNameWithSetTest() {
        lCovFNDA.setFunctionName("funcName1");
        Assert.assertEquals(lCovFNDA.getFunctionName(), "funcName1");
    }

}
