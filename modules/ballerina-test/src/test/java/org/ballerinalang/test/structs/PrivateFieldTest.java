/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.structs;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for testing private fields in structs.
 */
public class PrivateFieldTest {

    CompileResult resultSamePackage, resultDifferentPkg, resultTestequal;

    @BeforeClass
    public void setup() {
        resultSamePackage = BCompileUtil.compile(this, "test-src/structs/fields/", "foo");
        Assert.assertEquals(resultSamePackage.getErrorCount(), 0);
        resultDifferentPkg = BCompileUtil.compile(this, "test-src/structs/fields/", "bar");
        Assert.assertEquals(resultDifferentPkg.getErrorCount(), 0);
        resultTestequal = BCompileUtil.compile(this, "test-src/structs/fields/", "testequal");
        Assert.assertEquals(resultTestequal.getErrorCount(), 0);
    }

    @Test
    public void testSamePackage1PrivateFieldWithDefaultValue() {
        BValue[] values = BRunUtil.invoke(resultSamePackage, "test1");
        Assert.assertEquals(values.length, 5);
        Assert.assertEquals(values[0].stringValue(), "a");
        Assert.assertEquals(((BInteger) values[1]).intValue(), 1);
        Assert.assertEquals(((BBoolean) values[2]).booleanValue(), true);
        Assert.assertEquals(((BFloat) values[3]).floatValue(), 1.1);
        Assert.assertEquals(values[4].stringValue(), "value");
    }

    @Test
    public void testSamePackage1PrivateFieldWithModifiedValue() {
        BValue[] values = BRunUtil.invoke(resultSamePackage, "test2");
        Assert.assertEquals(values.length, 5);
        Assert.assertEquals(values[0].stringValue(), "b");
        Assert.assertEquals(((BInteger) values[1]).intValue(), 2);
        Assert.assertEquals(((BBoolean) values[2]).booleanValue(), false);
        Assert.assertEquals(((BFloat) values[3]).floatValue(), 1.2);
        Assert.assertEquals(values[4].stringValue(), "new");
    }

    @Test
    public void testSamePackage3PrivateFieldWithBindFunction() {
        BValue[] values = BRunUtil.invoke(resultSamePackage, "test3");
        Assert.assertEquals(values.length, 5);
        Assert.assertEquals(values[0].stringValue(), "a");
        Assert.assertEquals(((BInteger) values[1]).intValue(), 1);
        Assert.assertEquals(((BBoolean) values[2]).booleanValue(), true);
        Assert.assertEquals(((BFloat) values[3]).floatValue(), 1.1);
        Assert.assertEquals(values[4].stringValue(), "value");
    }

    @Test
    public void testSamePackage4AnonymousStrut() {
        BValue[] values = BRunUtil.invoke(resultSamePackage, "test4");
        Assert.assertEquals(values.length, 4);
        Assert.assertEquals(values[0].stringValue(), "z");
        Assert.assertEquals(values[1].stringValue(), "a");
        Assert.assertEquals(values[2].stringValue(), "b");
        Assert.assertEquals(values[3].stringValue(), "c");
    }

    @Test
    public void testDifferentPackage1AccessValuesWithBindFunction() {
        BValue[] values = BRunUtil.invoke(resultDifferentPkg, "test1");
        Assert.assertEquals(values.length, 1);
        Assert.assertNotNull(values[0]);
        BStruct value = (BStruct) values[0];
        Assert.assertEquals(value.getType().getName(), "NullReferenceException");
        Assert.assertNotNull(value.getRefField(1), "NullReferenceException");
        BRefValueArray valueArray = (BRefValueArray) value.getRefField(1);
        Assert.assertEquals(valueArray.size(), 2);
    }

    @Test
    public void testDifferentPackage2AccessValuesAnonymousStruct() {
        BValue[] values = BRunUtil.invoke(resultDifferentPkg, "test2");
        Assert.assertEquals(values.length, 1);
        Assert.assertEquals(values[0].stringValue(), "c");
    }

    @Test
    public void testStructCastWithPrivateFields() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test1");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0].stringValue(), "bob");
        Assert.assertEquals(((BFloat) values[1]).floatValue(), 1000.5);
    }

    @Test
    public void testStructAnyCastWithPrivateFields() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test2");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0].stringValue(), "bob");
        Assert.assertEquals(((BFloat) values[1]).floatValue(), 1000.5);
    }

    @Test
    public void testStructUnsafeCastWithPrivateFields1() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test3");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0], null);
        Assert.assertEquals(((BStruct) values[1]).getStringField(0), "'person:Person' cannot be cast to " +
                "'abc:ABCEmployee'");
    }

    @Test
    public void testStructUnsafeCastWithPrivateFields2() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test4");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0], null);
        Assert.assertEquals(((BStruct) values[1]).getStringField(0), "'person:Person' cannot be cast to " +
                "'fake:FakeEmployee'");
    }

    @Test
    public void testStructUnsafeCastWithPrivateFields3() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test5");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0], null);
        Assert.assertEquals(((BStruct) values[1]).getStringField(0), "'abc:ABCEmployee' cannot be cast to " +
                "'fake:FakeEmployee'");
    }

    @Test
    public void testStructUnsafeCastWithPrivateFields4() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test6");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0], null);
        Assert.assertEquals(((BStruct) values[1]).getStringField(0), "'abc:ABCEmployee' cannot be cast to " +
                "'xyz:XYZEmployee'");
    }

    @Test
    public void testStructUnsafeCastWithPrivateFields5() {
        BValue[] values = BRunUtil.invoke(resultTestequal, "test7");
        Assert.assertEquals(values.length, 2);
        Assert.assertEquals(values[0], null);
        Assert.assertEquals(((BStruct) values[1]).getStringField(0), "'xyz:XYZEmployee' cannot be cast to " +
                "'abc:ABCEmployee'");
    }

}
