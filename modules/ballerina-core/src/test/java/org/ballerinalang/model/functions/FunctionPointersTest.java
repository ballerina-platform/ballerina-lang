/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.model.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda.
 *
 * @since 0.90
 */
public class FunctionPointersTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/functions/function-pointers.bal");

    }

    @Test
    public void testFunctionPointerAsVariable() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testFunctionPointerAsLambda() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "sum is 3");
    }

    @Test
    public void testFunctionPointerAsParameter() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testLambdaAsReturnParameter() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test4", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "hello world.");
    }

    @Test
    public void testFunctionPointerAsReturnParameter() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test5", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test5 string1.0");
    }


    @Test
    public void testNestedFunctionPointersAsParameters() {
        BValue[] args = new BValue[0];
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test6", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test6 test6 1.0");
    }
}
