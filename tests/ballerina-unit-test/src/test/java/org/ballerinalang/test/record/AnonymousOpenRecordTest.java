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
package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for anonymous open records.
 *
 * @since 0.970.0
 */
public class AnonymousOpenRecordTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/anon_record.bal");
    }

    @Test(description = "Test Anonymous record in a function parameter declaration")
    public void testAnonStructAsFuncParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsFuncParam");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 24);
    }

    @Test(description = "Test Anonymous record in a local variable declaration")
    public void testAnonStructAsLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsLocalVar");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 11);
    }

    @Test(description = "Test Anonymous record in a package variable declaration")
    public void testAnonStructAsPkgVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsPkgVar");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "sameera:jayasoma:100");
    }

    @Test(description = "Test Anonymous record in a record field")
    public void testAnonStructAsStructField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsStructField");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "JAN:12 Gemba St APT 134:CA:sam");
    }

    @Test(description = "Test rest field in anonymous records")
    public void testRestFieldInAnonRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRestField");
        BMap person = (BMap) returns[0];

        Assert.assertTrue(person.get("fname") instanceof BString);
        Assert.assertEquals(person.get("fname").stringValue(), "John");

        Assert.assertTrue(person.get("lname") instanceof BString);
        Assert.assertEquals(person.get("lname").stringValue(), "Doe");

        Assert.assertTrue(person.get("location") instanceof BString);
        Assert.assertEquals(person.get("location").stringValue(), "Colombo");

        Assert.assertTrue(person.get("age") instanceof BInteger);
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 20);

        Assert.assertTrue(person.get("height") instanceof BFloat);
        Assert.assertEquals(((BFloat) person.get("height")).floatValue(), 5.5);
    }

    @Test(description = "Test explicit rest field in anonymous records")
    public void testExplicitRestFieldInAnonRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonRecWithExplicitRestField");
        BMap animal = (BMap) returns[0];

        Assert.assertTrue(animal.get("kind") instanceof BString);
        Assert.assertEquals(animal.get("kind").stringValue(), "Cat");

        Assert.assertTrue(animal.get("name") instanceof BString);
        Assert.assertEquals(animal.get("name").stringValue(), "Miaw");

        Assert.assertTrue(animal.get("legs") instanceof BInteger);
        Assert.assertEquals(((BInteger) animal.get("legs")).intValue(), 4);
    }
}
