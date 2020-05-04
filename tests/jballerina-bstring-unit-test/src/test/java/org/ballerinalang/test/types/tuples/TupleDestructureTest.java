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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(result, "tupleDestructureTest1", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "a");

        returns = BRunUtil.invoke(result, "tupleDestructureTest2", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[2, 3, 4]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest3", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[2, 3, 4]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3, 4]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest5", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[2, 3, 4]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest6", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[\"a\", \"b\"]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest7", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[\"a\", \"b\"]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest8", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "a");
        Assert.assertEquals(returns[2].stringValue(), "[\"b\"]");

        returns = BRunUtil.invoke(result, "tupleDestructureTest9", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "true");
        Assert.assertEquals(returns[1].stringValue(), "string value");
        Assert.assertEquals(returns[2].stringValue(), "[25, 12.5]");
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

}
