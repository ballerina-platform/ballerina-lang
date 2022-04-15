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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.tuples;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple destructure.
 */
public class TupleDestructureTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/tuples/tuple_destructure_test.bal");
        resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple_destructure_negative.bal");
    }

    @Test(description = "Test positive tuple destructure scenarios")
    public void testTupleDestructure() {
        BArray returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest1", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "a");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest2", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "[2,3,4]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest3", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "[2,3,4]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest4", new Object[]{});
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.toString(), "[1,2,3,4]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest5", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "[2,3,4]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest6", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "[\"a\",\"b\"]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest7", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "[\"a\",\"b\"]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest8", new Object[]{});
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "1");
        Assert.assertEquals(returns.get(1).toString(), "a");
        Assert.assertEquals(returns.get(2).toString(), "[\"b\"]");

        returns = (BArray) BRunUtil.invoke(result, "tupleDestructureTest9", new Object[]{});
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "true");
        Assert.assertEquals(returns.get(1).toString(), "string value");
        Assert.assertEquals(returns.get(2).toString(), "[25,12.5]");
    }

    @Test(description = "Test positive tuple destructure scenarios")
    public void testNegativeTupleDestructure() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[int,string,string]', found " +
                "'[int,string...]'", 22, 17);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[int,int...]', found '[int," +
                "string...]'", 30, 17);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[int,string[]]', found '[int," +
                "string...]'", 38, 14);
        BAssertUtil.validateError(resultNegative, i, "incompatible types: expected '[int,int]', found '[int,int," +
                "int]'", 46, 14);
    }

    @Test(description = "Test tuple destructure invalid syntax")
    public void testTupleDestructureInvalidSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/types/tuples/tuple_destructure_invalid_syntax.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "invalid binding pattern", 18, 6);
        BAssertUtil.validateError(result, i++, "incompatible types: expected '[other]', " +
                "found 'string'", 18, 11);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
