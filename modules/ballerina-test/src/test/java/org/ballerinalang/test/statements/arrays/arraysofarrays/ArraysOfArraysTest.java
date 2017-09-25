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
package org.ballerinalang.test.statements.arrays.arraysofarrays;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user arrays of arrays in ballerina.
 */
public class ArraysOfArraysTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/statements/arrays/arraysofarrays/arraysOfArrays.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testValueAssignmentAndRetrieval() {
//        BValue[] args = new BValue[]{new BInteger(1), new BInteger(2)};
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "valueAssignmentAndRetrieval", args);
        Assert.assertEquals(returns[0].stringValue(), "3");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayInitializationAndRetrieval() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "arrayInitializationAndRetrieval", args);
        Assert.assertEquals(returns[0].stringValue(), "1");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayToArrayAssignment() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "arrayToArrayAssignment", args);
        Assert.assertEquals(returns[0].stringValue(), "9");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayTest() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "threeDarray", args);
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayValueAccess() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "threeDarrayValueAccess", args);
        Assert.assertEquals(returns[0].stringValue(), "99");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayStringValueAccess() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "threeDarrayStringValueAccess", args);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayFunctionCalltest() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "twoDarrayFunctionCalltest", args);
        Assert.assertEquals(returns[0].stringValue(), "4");
    }

    @Test(description = "Test setting incorrect type",
          expectedExceptions = {BallerinaException.class})
    public void testAssignIncorrectValue() {
        BTestUtils.compile("test-src/statements/arrays/arraysofarrays/arraysOfArraysFailures.bal");
        // TODO: Check if this is correct!!!
       // BTestUtils.parseBalFile("test-src/statements/arrays/arraysofarrays/arraysOfArraysFailures.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayStruct() {
//        BValue[] returns = BLangFunctions.invokeNew(programFile, "twoDarrayStructTest");
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "twoDarrayStructTest", args);
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Nested array initializations")
    public void testVNestedArrayInit() {
//        BValue[] returns = BLangFunctions.invokeNew(programFile, "nestedArrayInit");
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "nestedArrayInit", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 6);
    }
}
