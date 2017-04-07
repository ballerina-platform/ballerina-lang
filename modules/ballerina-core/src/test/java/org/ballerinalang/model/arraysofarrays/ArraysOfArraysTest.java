/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.arraysofarrays;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user arrays of arrays in ballerina.
 */
public class ArraysOfArraysTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/arraysofarrays/arraysOfArrays.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testValueAssignmentAndRetrieval() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "valueAssignmentAndRetrieval");
        Assert.assertEquals(returns[0].stringValue(), "3");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayInitializationAndRetrieval() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayInitializationAndRetrieval");
        Assert.assertEquals(returns[0].stringValue(), "1");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayToArrayAssignment() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayToArrayAssignment");
        Assert.assertEquals(returns[0].stringValue(), "9");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayTest() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarray");
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayValueAccess() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarrayValueAccess");
        Assert.assertEquals(returns[0].stringValue(), "99");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayStringValueAccess() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarrayStringValueAccess");
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayFunctionCalltest() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "twoDarrayFunctionCalltest");
        Assert.assertEquals(returns[0].stringValue(), "4");
    }

    @Test(description = "Test setting incorrect type",
          expectedExceptions = {BallerinaException.class})
    public void testAssignIncorrectValue() {
        bLangProgram = BTestUtils.parseBalFile("lang/arraysofarrays/arraysOfArraysFailures.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "twoDarrayStructTest");
        Assert.assertEquals(returns[0].stringValue(), "2");
    }
}
