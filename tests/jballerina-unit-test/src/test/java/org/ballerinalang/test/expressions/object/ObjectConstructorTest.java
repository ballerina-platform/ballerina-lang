/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.object;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for object-constructor-expr types in ballerina.
 */
@Test
public class ObjectConstructorTest {

    private CompileResult compiledConstructedObjects;

    @BeforeClass
    public void setup() {
        compiledConstructedObjects = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_expression.bal");
    }

    @Test
    public void testObjectCreationViaObjectConstructor() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectCreationViaObjectConstructor");
    }

    @Test
    public void testObjectConstructorAnnotationAttachment() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorAnnotationAttachment");
    }

    @Test
    public void testObjectConstructorObjectFunctionInvocation() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorObjectFunctionInvocation");
    }

    @Test
    public void testObjectConstructorIncludedMethod() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorIncludedMethod");
    }

    @Test
    public void testObjectConstructorNegative() {

        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_expression_negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: 'SampleRec' is not an object", 19, 39);
        BAssertUtil.validateError(negativeResult, index++, "a remote function in a non client object", 22, 5);
        BAssertUtil.validateError(negativeResult, index++, "object constructor 'init' method cannot have parameters",
                26, 5);
        BAssertUtil.validateError(negativeResult, index++, "object initializer function can not be declared as " +
                "private", 30, 5);
        BAssertUtil.validateError(negativeResult, index++, "invalid token 'public'", 34, 29);
        BAssertUtil.validateError(negativeResult, index++, "type inclusions are not allowed in object constructor",
                40, 1);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }
}
