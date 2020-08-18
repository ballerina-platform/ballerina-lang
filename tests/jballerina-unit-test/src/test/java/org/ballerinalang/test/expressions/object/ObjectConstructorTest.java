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

/**
 * Test cases for object-constructor-expr types in ballerina.
 */
public class ObjectConstructorTest {

    private CompileResult compiledConstructedObjects;

    @BeforeClass
    public void setup() {
        compiledConstructedObjects = BCompileUtil.compile("test-src/expressions/object/object_constructor.bal");
    }

    public void testObjectCreationViaObjectConstructor() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectCreationViaObjectConstructor");
    }

    public void testObjectConstructorAnnotationAttachment() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorAnnotationAttachment");
    }

    public void testObjectConstructorObjectFunctionInvocation() {
        BRunUtil.invoke(compiledConstructedObjects, "testObjectConstructorObjectFunctionInvocation");
    }

    public void testObjectConstructorNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/object/object_constructor_negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: 'SampleRec' is not an object", 21, 39);
        BAssertUtil.validateError(negativeResult, index++, "a remote function in a non client object", 24, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), index+1);
    }
}
