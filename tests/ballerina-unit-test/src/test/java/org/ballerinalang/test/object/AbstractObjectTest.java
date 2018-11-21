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
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for abstract object types in ballerina.
 */
public class AbstractObjectTest {

    CompileResult anonAbstractObjects = BCompileUtil.compile("test-src/object/abstract_anon_object.bal");
    CompileResult abstractObjects = BCompileUtil.compile("test-src/object/abstract_object.bal");

    @Test
    public void testAbstractObjectNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/abstract-object-negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person1'", 3, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person2'", 4, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person1'", 8, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person2'", 9, 18);
        BAssertUtil.validateError(negativeResult, index++, "abstract object 'Person2' cannot have a constructor method",
                28, 5);
        BAssertUtil.validateError(negativeResult, index++,
                "no implementation found for the function 'getName' of non-abstract object 'Person3'", 40, 5);
        BAssertUtil.validateError(negativeResult, index++,
                "function 'getName' in abstract object 'Person4' cannot have a body", 51, 5);
        BAssertUtil.validateError(negativeResult, index++,
                "cannot attach function 'getName' to abstract object 'Person5'", 67, 1);
    }

    @Test
    public void testAbstractAnonymousObjectNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/abstract_anon_object_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++,
                "abstract object '$anonType$0' cannot have a constructor method", 2, 54);
        BAssertUtil.validateError(compileResult, index++, "variable 'p1' is not initialized", 2, 1);
        BAssertUtil.validateError(compileResult, index++, "variable 'p2' is not initialized", 3, 1);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$2'", 4, 77);
        BAssertUtil.validateError(compileResult, index++,
                "abstract object '$anonType$3' cannot have a constructor method", 7, 58);
        BAssertUtil.validateError(compileResult, index, "cannot initialize abstract object '$anonType$5'", 9, 81);
    }

    @Test
    public void testAbstractAnonObjectInMatch() {
        BValue[] result = BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInMatch");
        Assert.assertEquals(result[0].stringValue(), "Person Name");
        Assert.assertEquals(result[1].stringValue(), "Employee Name");
    }

    @Test
    public void testAbstractAnonObjectInFunction() {
        BValue[] result = BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInFunction");
        Assert.assertEquals(result[0].stringValue(), "Person Name");
        Assert.assertEquals(result[1].stringValue(), "Employee Name");
    }

    @Test
    public void testAbstractAnonObjectInVarDef() {
        BValue[] result = BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInVarDef");
        Assert.assertEquals(result[0].stringValue(), "Person Name");
        Assert.assertEquals(result[1].stringValue(), "Employee Name");
    }

    @Test(description = "Test abstract object as an object field")
    public void testAbstractObjectInObject() {
        BValue[] result = BRunUtil.invoke(abstractObjects, "testAbstractObjectInObject");
        Assert.assertEquals(result[0].stringValue(), "{city:\"Colombo\", address:{city:\"Colombo\"}}");
    }
}
