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
package org.ballerinalang.test.javainterop.basic;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina/java functions related to Java casting.
 *
 * @since 1.2.3
 */
public class JavaCastTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/java_cast_test.bal");
    }

    @Test(description = "Test java:cast method in ballerina/java")
    public void testJavaCastMethod() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastFunction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "cast this object");
    }

    @Test(description = "Test java:cast method in ballerina/java for an incorrect Java cast")
    public void testIncorrectJavaCast() {
        BValue[] returns = BRunUtil.invoke(result, "testIncorrectJavaCast");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Cannot cast String to ArrayList"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an typedesc argument which is not an object")
    public void testJavaCastForInvalidTypedesc1() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidTypedesc1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while processing the typedesc " +
                "parameter: java.lang.ClassCastException: org.ballerinalang.jvm.types.BStringType"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an typedesc argument which is not a JObject")
    public void testJavaCastForInvalidTypedesc2() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidTypedesc2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while retrieving details of the " +
                "`@java:Binding` annotation from NotAJObject typedesc: java.lang.NullPointerException"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an incorrect class in typedesc object annotation")
    public void testJavaCastForInvalidClass1() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidClass1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while casting Object object " +
                "to the typedesc provided: java.lang.ClassNotFoundException: java.lang.Str"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an incorrect class in parameter object annotation")
    public void testJavaCastForInvalidClass2() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidClass2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while casting Object3 object " +
                "to the typedesc provided: java.lang.ClassNotFoundException: java.lang.Objecte"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an empty `jObj` value in parameter object")
    public void testJavaCastFunctionNulljObj() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastFunctionNulljObj");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Empty handle reference found for " +
                "`jObj` field in Object"));
    }

    @Test(description = "Test java:cast method in ballerina/java for missing `@java:Binding` annotation in typedesc")
    public void testJavaCastMissingAnnotation1() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastMissingAnnotation1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while retrieving details of " +
                "the `@java:Binding` annotation from String2 typedesc: java.lang.NullPointerException"));
    }

    @Test(description = "Test java:cast method in ballerina/java for missing `@java:Binding` annotation in parameter")
    public void testJavaCastMissingAnnotation2() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastMissingAnnotation2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} Error while retrieving details of " +
                "the `@java:Binding` annotation from Object2 object: java.lang.NullPointerException"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an input parameter which is not an object")
    public void testJavaCastForInvalidObject1() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidObject1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} java.lang.String cannot be cast " +
                "to org.ballerinalang.jvm.values.api.BObject"));
    }

    @Test(description = "Test java:cast method in ballerina/java for an input parameter which is not a JObject")
    public void testJavaCastForInvalidObject2() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaCastForInvalidObject2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerina/java} No such field or method: jObj " +
                "in NotAJObject"));
    }
}
