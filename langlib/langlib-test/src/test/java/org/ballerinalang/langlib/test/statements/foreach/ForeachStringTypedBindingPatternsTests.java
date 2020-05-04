/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for typed binding patterns in foreach.
 *
 * @since 0.985.0
 */
@Test(enabled = false)
public class ForeachStringTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-string-typed-binding-patterns.bal");
    }

    @Test(enabled = false)
    public void testStringWithSimpleVariableWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testStringWithSimpleVariableWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ");
    }

    @Test(enabled = false)
    public void testStringWithSimpleVariableWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testStringWithSimpleVariableWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ");
    }

    @Test(enabled = false)
    public void testStringWithSimpleVariableWithAnydataType() {
        BValue[] returns = BRunUtil.invoke(program, "testStringWithSimpleVariableWithAnydataType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ");
    }

    @Test(enabled = false)
    public void testStringWithSimpleVariableWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testStringWithSimpleVariableWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ");
    }

    @Test(enabled = false)
    public void testIterationOnEmptyString() {
        BValue[] returns = BRunUtil.invoke(program, "testIterationOnEmptyString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
}
