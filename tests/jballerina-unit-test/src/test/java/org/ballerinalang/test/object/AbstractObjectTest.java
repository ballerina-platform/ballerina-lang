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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for abstract object types in ballerina.
 */
public class AbstractObjectTest {

    private CompileResult anonAbstractObjects;
    private CompileResult abstractObjects;

    @BeforeClass
    public void setup() {
        anonAbstractObjects = BCompileUtil.compile("test-src/object/abstract_anon_object.bal");
        abstractObjects = BCompileUtil.compile("test-src/object/abstract_object.bal");
    }

    @Test(groups = "brokenOnClassChange")
    public void testAbstractObjectNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/abstract-object-negative.bal");
        int index = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 13);
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
        BAssertUtil.validateError(negativeResult, index++, "abstract object field: 'age' can not be declared as " +
                "private", 58, 5);
        BAssertUtil.validateError(negativeResult, index++, "interface function: 'getName' of abstract object " +
                "'Person6' can not be declared as private", 61, 5);
        BAssertUtil.validateError(negativeResult, index++, "interface function: 'getName' of abstract object 'Foo' " +
                "can not be declared as private", 65, 5);
        BAssertUtil.validateError(negativeResult, index++,
                                  "external function: 'getName' not allowed in abstract object 'Person7'", 78, 5);
        BAssertUtil.validateError(negativeResult, index++,
                                  "function 'getName' in abstract object 'Person7' cannot have a body", 78, 5);
        BAssertUtil.validateError(negativeResult, index,
                                  "fields with default values are not yet supported with abstract objects", 83, 18);
    }

    @Test
    public void testAbstractAnonymousObjectNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/abstract_anon_object_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++,
                "abstract object '$anonType$_1' cannot have a constructor method", 2, 45);
        BAssertUtil.validateError(compileResult, index++, "missing object keyword", 2, 81);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 2, 81);
        BAssertUtil.validateError(compileResult, index++, "missing identifier", 2, 83);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 2, 83);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$_1'", 2, 90);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$_2'", 3, 68);
        BAssertUtil.validateError(compileResult, index++,
                "abstract object '$anonType$_6' cannot have a constructor method", 6, 49);
        BAssertUtil.validateError(compileResult, index++, "missing object keyword", 6, 85);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 6, 85);
        BAssertUtil.validateError(compileResult, index++, "invalid token '}'", 6, 89);
        BAssertUtil.validateError(compileResult, index++, "missing close brace token", 8, 70);
        BAssertUtil.validateError(compileResult, index++, "missing identifier", 8, 70);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 8, 70);
        BAssertUtil.validateError(compileResult, index, "invalid usage of 'new' with type 'any'", 8, 72);
    }

    @Test
    public void testAbstractAnonObjectInMatch() {
        BValue[] result = BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInTypeTest");
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
