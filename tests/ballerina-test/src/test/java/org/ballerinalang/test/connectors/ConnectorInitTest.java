/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the hidden 'init' function invocation in connectors.
 *
 * @since 0.8.0
 */
@Test(enabled = false)
public class ConnectorInitTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(this, "test-src/", "connectors.init");
    }

    @Test(description = "Test Connector int functionality")
    public void testConnectorInit() {
        BValue[] args = {new BString("Apple"), new BInteger(13)};
        BValue[] returns = BRunUtil.invoke(result, "connectors.init",
                "testConnectorInit", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 151);
        Assert.assertEquals(returns[1].stringValue(), "151:Applesameera");
    }



    @Test(description = "Test connector init using parameters with implicitly castable types")
    public void testConnectorInitWithImplicitCastableTypes() {
        BValue[] returns = BRunUtil.invoke(result, "connectors.init",
                "testConnectorInitWithImplicitCastableTypes", new BString[] {});
        Assert.assertEquals(returns.length, 2);

        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        Assert.assertEquals(returns[0].stringValue(), "John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 40);
    }

    @Test(description = "Test connector init with invalid parameter count")
    public void testConnectorInitWithInvalidArgTypes() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/connectors/init/invalid/connector-init-arg-type-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'int', found 'string'", 14, 28);
    }

    @Test(description = "Test connector init with invalid parameter count")
    public void testConnectorInitWithInvalidArgCount() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/connectors/init/invalid/connector-init-arg-count-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "not enough arguments in call to 'Foo()'", 14, 9);
    }
}
