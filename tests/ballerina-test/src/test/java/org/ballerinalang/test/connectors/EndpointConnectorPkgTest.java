/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for endpoint and connector combination in packages.
 */
@Test(enabled = false)
public class EndpointConnectorPkgTest {
    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass()
    public void setup() {
        result = BCompileUtil.compile(this, "test-src/connectors", "pkg.ab");
        resultNegative = BCompileUtil.compile(this, "test-src/connectors", "pkg.gh");
    }

    @Test(description = "Test simple endpoint creation")
    public void testEndpointWithConnector() {
        BValue[] returns = BRunUtil.invoke(result, "testEndpointWithConnector");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Foo-val1");
    }

    @Test(description = "Test create statement as connector parameter")
    public void testCreateAsConnectorParam() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateAsConnectorParam");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Bar-val1-val2-Foo-val1");
    }

    @Test(description = "Test using connector as variable reference in endpoint")
    public void testConnectorAsVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorAsVarRef");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Bar-val1-val2-Foo-val1");
    }

    @Test(description = "Test using endpoint type as base connector")
    public void testBaseConnectorEndpointType() {
        BValue[] returns = BRunUtil.invoke(result, "testBaseConnectorEndpointType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "FooFilter1-val1-val2-Foo-val1");
    }

    @Test(description = "Test casting connectors to base type")
    public void testCastingConnectorToBaseType() {
        BValue[] returns = BRunUtil.invoke(result, "testCastingConnectorToBaseType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "FooFilter2-val1-para3-" +
                "FooFilter1-val1-para2-FooFilter2-val1-para1-Foo-val1-");
    }

    @Test(description = "Test bind connection with endpoint")
    public void testBindConnectionToEndpoint() {
        BValue[] returns = BRunUtil.invoke(result, "testBindConnectionToEndpoint");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "FooFilter1-val1-para2-FooFilter2-val1-para1-Foo-val1-");
    }

    @Test(description = "Test endpoint, connectors with errors")
    public void testConnectorNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "incompatible types: expected " +
                        "'endpoint<pkg.ij:Foo>', found 'pkg.gh:Bar'", 16, 9);
    }

}
