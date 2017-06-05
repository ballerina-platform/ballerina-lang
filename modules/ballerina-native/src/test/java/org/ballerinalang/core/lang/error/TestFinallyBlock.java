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
package org.ballerinalang.core.lang.error;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for finally block.
 */
public class TestFinallyBlock {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/errors/test_finally.bal");

    }

    @Test
    public void testThrowErrorFromFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block error");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "assigned");
    }

    @Test
    public void testReturnFromFinallyBlock() {
        BValue[] args = {new BInteger(11)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block");
    }

    @Test(description = "check finally while returning.")
    public void testReturnWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "try");
    }

    @Test(description = "check finally while returning for reference type.")
    public void testReturnReferenceTypeWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test4", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally outerFinally");
    }

    @Test(description = "check while condition in a finally block")
    public void testWhileInaFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test5", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "start123end");
    }


}
