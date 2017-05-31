/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BinaryExprEvalPrecedenceTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/binary-expr-precedence.bal");
    }

    @Test(description = "Test binary OR expression with " +
            "left most expr evaluated to true access expression")
    public void testBinaryOrExprWithLeftMostExprTrue() {
        boolean one = true;
        boolean two = false;
        boolean three = false;

        boolean expectedResult = true;

        BValue[] args = {new BBoolean(one), new BBoolean(two), new BBoolean(three)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "binaryOrExprwithLefMostSubExprTrue", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test binary AND expression with " +
            "left most expr evaluated to false access expression")
    public void testBinaryAndExprWithLeftMostExprTrue() {
        boolean one = false;
        boolean two = false;
        boolean three = false;

        boolean expectedResult = false;

        BValue[] args = {new BBoolean(one), new BBoolean(two), new BBoolean(three)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "binaryANDExprwithLefMostSubExprFalse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }

}
