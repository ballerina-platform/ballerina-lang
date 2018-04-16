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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for readonly fields in user defined object types.
 */
public class ObjectReadonlyFieldTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/object-readonly-field.bal");
    }

    @Test(description = "Test object with readonly field which is in a different package")
    public void testReadOnlyAccessInDifferentPackage() {
        CompileResult compileResultNegative = BCompileUtil
                .compile("test-src/object/object-readonly-field-negative.bal");
        Assert.assertEquals(compileResultNegative.getErrorCount(), 4);
        BAssertUtil.validateError(compileResultNegative, 0, "cannot assign a value to readonly 'p.age'", 9, 5);
        BAssertUtil.validateError(compileResultNegative, 1, "cannot assign a value to readonly 'bar:globalInt'", 15, 5);
        BAssertUtil.validateError(compileResultNegative, 2,
                "annotation 'ballerina/builtin:readonly' is not allowed in function", 20, 1);
        BAssertUtil.validateError(compileResultNegative, 3,
                "annotation 'ballerina/builtin:readonly' is not allowed in service", 24, 1);
    }

    @Test(description = "Test object with readonly field which is in a same package")
    public void testReadOnlyObjFieldAccessInSamePackage() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testReadOnlyObjFieldAccessInSamePackage");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 44);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "john");
    }

    @Test(description = "Test object with readonly global variable which is in a same package")
    public void testReadOnlyGlobalVarAccessInSamePackage() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testReadOnlyGlobalVarAccessInSamePackage");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);
    }
}
