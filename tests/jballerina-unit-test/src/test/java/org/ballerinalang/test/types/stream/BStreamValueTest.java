/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.stream;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test stream type.
 *
 * @since 1.2.0
 */
public class BStreamValueTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/stream/stream-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/stream/stream-negative.bal");
    }

    @Test(description = "Test global stream construct")
    public void testGlobalStreamConstruct() {
        BValue[] values = BRunUtil.invoke(result, "testGlobalStreamConstruct", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream construct within a function")
    public void testStreamConstruct() {
        BValue[] values = BRunUtil.invoke(result, "testStreamConstruct", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream construct with stream filter")
    public void testStreamConstructWithFilter() {
        BValue[] values = BRunUtil.invoke(result, "testStreamConstructWithFilter", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream return type")
    public void testStreamReturnType() {
        BValue[] values = BRunUtil.invoke(result, "testStreamReturnType", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test negative test scenarios of stream type")
    public void testStreamTypeNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', found" +
                " 'int'", 26, 12);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'val' in record 'record {| int value; |}'", 31, 14);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 35, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'boolean'", 39, 21);
    }

}
