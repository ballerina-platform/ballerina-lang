/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.klass;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for class in ballerina.
 */
public class ClassTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/klass/simple_class.bal");
    }

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObject() {
        BRunUtil.invoke(compileResult, "testSimpleObjectAsStruct");
    }

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObjectObjectInitExprWithoutName() {
        BRunUtil.invoke(compileResult, "testSimpleObjectAsStructWithNew");
    }

    @Test(description = "Test Basic object as struct")
    public void testUsingClassValueAsRecordField() {
        BRunUtil.invoke(compileResult, "testUsingClassValueAsRecordField");
    }

    @Test(description = "Test Basic object as struct")
    public void testTypeRef() {
        BRunUtil.invoke(compileResult, "testTypeRefInClass");
    }

    @Test(description = "Class definition negative")
    public void classDefNegative() {
        CompileResult negative = BCompileUtil.compile("test-src/klass/class-def-negative.bal");
        int i = 0;
        String expectedErrorMsgPrefix = "cyclic type reference in ";
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, A]'", 2, 5);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, C, A]'", 2, 5);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[B, A, B]'", 7, 5);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[B, C, A, B]'", 7, 5);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, A]'", 12, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[C, A, B, C]'", 12, 1);
        Assert.assertEquals(negative.getErrorCount(), i);
    }
}
