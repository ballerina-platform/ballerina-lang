/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for Connector actions with optional and named params.
 */
public class ConnectorActionWithOptionalParamsTest {
    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/connector-actions-with-optional-params.bal");
    }

    @Test
    public void testInvokeActionInOrder1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionInOrder1");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeActionInOrder2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionInOrder2");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeActionInMixOrder1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionInMixOrder1");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeActionInMixOrder2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionInMixOrder2");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeActionWithoutRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithoutRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeActionWithoutSomeNamedArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithoutSomeNamedArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeActionWithRequiredArgsOnly() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithRequiredArgsOnly");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[]");
    }

    @Test
    public void testInvokeActionWithRequiredAndRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithRequiredAndRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 5);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[40, 50, 60]");
    }

    @Test
    public void testInvokeActionWithoutRestParams() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithoutRestParams");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeActionWithOnlyNamedParams1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyNamedParams1");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeActionWithOnlyNamedParams2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyNamedParams2");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");
    }

    @Test
    public void testInvokeActionWithOnlyNamedParams3() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyNamedParams3");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "John");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 7);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Doe");
    }

    @Test
    public void testInvokeActionWithOnlyRestParam1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyRestParam1");
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(returns[0].stringValue(), "[]");
    }

    @Test
    public void testInvokeActionWithOnlyRestParam2() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyRestParam2");
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(returns[0].stringValue(), "[10, 20, 30]");
    }

    @Test
    public void testInvokeActionWithOnlyRestParam3() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithOnlyRestParam3");
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(returns[0].stringValue(), "[10, 20, 30]");
    }

    @Test
    public void testInvokeActionWithAnyRestParam1() {
        BValue[] returns = BRunUtil.invoke(result, "testInvokeActionWithAnyRestParam1");
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[[10, 20, 30], {\"name\":\"John\"}]");
    }

    @Test
    public void funcInvocAsRestArgs() {
        BValue[] returns = BRunUtil.invoke(result, "funcInvocAsRestArgs");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 20.0);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Alex");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 30);

        Assert.assertTrue(returns[4] instanceof BString);
        Assert.assertEquals(returns[4].stringValue(), "Bob");

        Assert.assertTrue(returns[5] instanceof BIntArray);
        Assert.assertEquals(returns[5].stringValue(), "[1, 2, 3, 4]");
    }
}
