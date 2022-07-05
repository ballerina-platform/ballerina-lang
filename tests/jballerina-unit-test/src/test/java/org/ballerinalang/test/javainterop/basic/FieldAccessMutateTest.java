/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.nativeimpl.jvm.tests.JavaFieldAccessMutate;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

/**
 * Test cases for java interoperability field access and mutations.
 *
 * @since 1.0.0
 */
public class FieldAccessMutateTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/field_access_mutate_tests.bal");
    }

    @Test(description = "Test static field access")
    public void testStaticFieldAccess() {
        Object returns = BRunUtil.invoke(result, "testStaticFieldAccess");
        Assert.assertEquals(((HandleValue) returns).getValue(), JavaFieldAccessMutate.contractId);
    }

    @Test(description = "Test static field mutate")
    public void testStaticFieldMutate() {
        Object[] args = new Object[1];
        String argValue = "NewValue";
        args[0] = StringUtils.fromString(argValue);
        BRunUtil.invoke(result, "testStaticFieldMutate", args);
        Assert.assertEquals(JavaFieldAccessMutate.contractId.getValue(), argValue);
    }

    @Test(description = "Test static field access")
    public void testStaticPrimitiveFieldAccess() {
        Object returns = BRunUtil.invoke(result, "testStaticPrimitiveFieldAccess");
        Assert.assertEquals(returns, (long) JavaFieldAccessMutate.age);
    }

    @Test(description = "Test static field mutate")
    public void testStaticPrimitiveFieldMutate() {
        Object[] args = new Object[1];
        args[0] = (345);
        BRunUtil.invoke(result, "testStaticPrimitiveFieldMutate", args);
        Assert.assertEquals(JavaFieldAccessMutate.aShort, 345);
    }

    @Test(description = "Test static boolean field mutate")
    public void testStaticBooleanFieldMutate() {
        BRunUtil.invoke(result, "testStaticBooleanFieldMutate");
    }

    @Test(description = "Test ballerina finite to java boolean cast")
    public void testBFiniteToJBooleanCast() {
        BRunUtil.invoke(result, "testBFiniteToJBooleanCast");
    }

    @Test(description = "Test instance field access")
    public void testInstanceFieldAccess() {
        Object[] args = new Object[1];
        Date createdAt = new Date();
        JavaFieldAccessMutate receiver = new JavaFieldAccessMutate(createdAt);
        args[0] = new HandleValue(receiver);
        Object returns = BRunUtil.invoke(result, "testInstanceFieldAccess", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), createdAt);
    }

    @Test(description = "Test instance field mutate")
    public void testInstanceFieldMutate() {
        Object[] args = new Object[2];
        UUID uuid = UUID.randomUUID();
        JavaFieldAccessMutate receiver = new JavaFieldAccessMutate();
        args[0] = new HandleValue(receiver);
        args[1] = new HandleValue(uuid);
        BRunUtil.invoke(result, "testInstanceFieldMutate", args);
        Assert.assertEquals(receiver.uuid, uuid);
    }

    @Test(description = "Test instance field access")
    public void testInstancePrimitiveFieldAccess() {
        Object[] args = new Object[1];
        JavaFieldAccessMutate receiver = new JavaFieldAccessMutate();
        args[0] = new HandleValue(receiver);
        Object returns = BRunUtil.invoke(result, "testInstancePrimitiveFieldAccess", args);
        Assert.assertEquals(returns, false);
    }

    @Test(description = "Test instance field mutate")
    public void testInstancePrimitiveFieldMutate() {
        Object[] args = new Object[2];
        JavaFieldAccessMutate receiver = new JavaFieldAccessMutate();
        args[0] = new HandleValue(receiver);
        args[1] = (123.0f);
        BRunUtil.invoke(result, "testInstancePrimitiveFieldMutate", args);
        Assert.assertEquals(receiver.lkr, 123.0f);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
