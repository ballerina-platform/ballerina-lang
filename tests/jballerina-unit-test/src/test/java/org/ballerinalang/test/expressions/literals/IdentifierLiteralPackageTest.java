/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Identifier literal test cases with package.
 */
@Test(groups = {"disableOnOldParser"})
public class IdentifierLiteralPackageTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {

        result = BCompileUtil.compile("test-src/expressions/literals/identifierliteral/testproject/" +
                                        "identifier-literal-pkg.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test(description = "Test accessing variable in other packages defined with identifier literal")
    public void testAccessingVarsInOtherPackage() {
        BValue[] returns = BRunUtil.invoke(result, "getVarsInOtherPkg");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 800);
        Assert.assertEquals(returns[1].stringValue(), "value");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 99.34323);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 88343);
    }

    @Test(description = "Test accessing global vars with identifier literals defined in other packages")
    public void testAccessStructGlobalVarWithIdentifierLiteralInOtherPackage() {
        BValue[] returns = BRunUtil.invoke(result, "accessStructWithIL");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        Assert.assertEquals(returns[0].stringValue(), "Harry");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);

    }

    @Test(description = "Test access fields of record types with type label")
    public void testAccessTypeLabelWithIL() {
        BValue[] returns = BRunUtil.invoke(result, "accessTypeLabelWithIL");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        Assert.assertEquals(returns[0].stringValue(), "John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 20);

    }

    @Test(description = "Test get nested anonymous record arrays with element type of record having quoted identifier")
    public void testGetNestedAnonymousRecordArray() {
        BValue[] returns = BRunUtil.invoke(result, "getAnonFromFoo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        BMap bmap = (BMap) returns[0];
        Assert.assertEquals(bmap.get("name").stringValue(), "Waruna");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
