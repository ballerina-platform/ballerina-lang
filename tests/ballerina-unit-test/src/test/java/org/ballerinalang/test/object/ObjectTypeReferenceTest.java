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
package org.ballerinalang.test.object;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for object reference type in ballerina.
 */
public class ObjectTypeReferenceTest {

    CompileResult compileResult = BCompileUtil.compile("test-src/object/object-type-reference.bal");

    @Test
    public void testSimpleObjectTypeReferenceNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/object-type-reference-negative.bal");
        System.out.println(negativeResult);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'Employee1' is not an abstract object", 16,
                6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'salary'", 32, 6);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference in '[Foo, A, B, C, D, E]'", 36, 1);
        BAssertUtil.validateError(negativeResult, i++,
                "no implementation found for the function 'getSalary' of non-abstract object 'Manager2'", 80, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "no implementation found for the function 'getName' of non-abstract object 'Manager2'", 80, 5);
    }

    @Test
    public void testSimpleObjectTypeReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectTypeReference");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(returns[1].stringValue(), " from inner function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 0.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testInitTypeReferenceObjectWithNew() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInitTypeReferenceObjectWithNew");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
        Assert.assertEquals(returns[1].stringValue(), "John from inner function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 1000.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testObjectWithChainedTypeReferences() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithChainedTypeReferences");
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
        Assert.assertEquals(returns[1].stringValue(), "John from outer function");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 2500.0);
        Assert.assertEquals(returns[3].stringValue(), "HR");
    }

    @Test
    public void testAbstractObjectFuncWithDefaultVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbstractObjectFuncWithDefaultVal");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Hello Jane");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1800.0);
    }
}
