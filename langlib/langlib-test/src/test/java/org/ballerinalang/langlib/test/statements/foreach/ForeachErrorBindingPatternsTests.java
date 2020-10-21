/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with error binding pattern.
 *
 * @since 0.990.4
 */
public class ForeachErrorBindingPatternsTests {

    private CompileResult program, negative;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach_errors.bal");
        negative = BCompileUtil.compile("test-src/statements/foreach/foreach_errors_negative.bal");
    }

    @Test
    public void testArrayWithErrors() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithErrors");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(),
                "Error One:msgOne:true:Error Two:msgTwo:false:Error Three:msgThree:true:");
        Assert.assertEquals(returns[1].stringValue(),
                "Error One:msgOne:true:Error Two:msgTwo:false:Error Three:msgThree:true:");
        Assert.assertEquals(returns[2].stringValue(),
                "Error One:Error Two:Error Three:Error One:Error Two:Error Three:");
    }

    @Test
    public void testMapWithErrors() {
        BValue[] returns = BRunUtil.invoke(program, "testMapWithErrors");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(),
                "Error One:msgOne:true:Error Two:msgTwo:false:Error Three:msgThree:true:");
        Assert.assertEquals(returns[1].stringValue(),
                "Error One:msgOne:true:Error Two:msgTwo:false:Error Three:msgThree:true:");
        Assert.assertEquals(returns[2].stringValue(),
                "Error One:Error Two:Error Three:Error One:Error Two:Error Three:");
    }

    @Test(enabled = false)
    public void testNegativeForEachWithErrors() {
        Assert.assertEquals(negative.getErrorCount(), 8);
        int i = 0;
        BAssertUtil.validateError(negative, i++,
                "invalid error variable; expecting an error type but found 'DError?' in type definition",
                33, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'map<string>', found 'anydata'", 62, 25);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(string|boolean)', found 'anydata'",
                66, 28);
        BAssertUtil.validateError(negative, i++,
                "invalid error variable; expecting an error type but found 'DError?' in type definition",
                78, 17);
        BAssertUtil.validateError(negative, i++,
                "incompatible types: expected 'map<string>', found 'anydata'", 109, 25);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(string|boolean)', found 'anydata'",
                113, 28);
        BAssertUtil.validateError(negative, i++, "invalid error binding pattern with type 'ReasonError'",
                131, 17);
        BAssertUtil.validateError(negative, i++, "undefined symbol 'otherVar'", 134, 17);
    }
}
