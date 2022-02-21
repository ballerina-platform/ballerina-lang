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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

    @Test
    public void testAbstractObjectNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/object/abstract-object-negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person1'", 3, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person2'", 4, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person1'", 8, 18);
        BAssertUtil.validateError(negativeResult, index++, "cannot initialize abstract object 'Person2'", 9, 18);
        BAssertUtil.validateError(negativeResult, index++, "object type descriptor 'Person2' cannot have an init " +
                        "method", 27, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test
    public void testAbstractAnonymousObjectNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/abstract_anon_object_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++,
                "object type descriptor '$anonType$_1' cannot have an init method", 2, 45);
        BAssertUtil.validateError(compileResult, index++, "missing object keyword", 2, 81);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 2, 81);
        BAssertUtil.validateError(compileResult, index++, "missing identifier", 2, 83);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 2, 83);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$_1'", 2, 90);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$_2'", 3, 68);
        BAssertUtil.validateError(compileResult, index++,
                "object type descriptor '$anonType$_4' cannot have an init method", 6, 49);
        BAssertUtil.validateError(compileResult, index++, "missing object keyword", 6, 85);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 6, 85);
        BAssertUtil.validateError(compileResult, index++, "missing identifier", 6, 87);
        BAssertUtil.validateError(compileResult, index++, "missing semicolon token", 6, 87);
        BAssertUtil.validateError(compileResult, index++, "cannot initialize abstract object '$anonType$_6'", 8, 72);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testAbstractAnonObjectInMatch() {
        BArray result = (BArray) BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInTypeTest");
        Assert.assertEquals(result.get(0).toString(), "Person Name");
        Assert.assertEquals(result.get(1).toString(), "Employee Name");
    }

    @Test
    public void testAbstractAnonObjectInFunction() {
        BArray result = (BArray) BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInFunction");
        Assert.assertEquals(result.get(0).toString(), "Person Name");
        Assert.assertEquals(result.get(1).toString(), "Employee Name");
    }

    @Test
    public void testAbstractAnonObjectInVarDef() {
        BArray result = (BArray) BRunUtil.invoke(anonAbstractObjects, "testAbstractAnonObjectInVarDef");
        Assert.assertEquals(result.get(0).toString(), "Person Name");
        Assert.assertEquals(result.get(1).toString(), "Employee Name");
    }

    @Test(description = "Test abstract object as an object field")
    public void testAbstractObjectInObject() {
        Object result = BRunUtil.invoke(abstractObjects, "testAbstractObjectInObject");
        Assert.assertEquals(result.toString(), "{city:Colombo, address:{city:Colombo}}");
    }

    @AfterClass
    public void tearDown() {
        anonAbstractObjects = null;
        abstractObjects = null;
    }
}
