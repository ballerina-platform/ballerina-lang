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

package org.ballerinalang.test.expressions.identifierliteral;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Identifier literal test cases with package.
 */
public class IdentifierLiteralPackageTest {

    @Test(description = "Test accessing variable in other packages defined with identifier literal", enabled = false)
    public void testAccessingVarsInOtherPackage() {
        CompileResult result = BTestUtils.compile("test-src/expressions/identifierliteral", "pkg.main");
        BValue[] returns = BTestUtils.invoke(result, "pkg.main", "getVarsInOtherPkg");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 800);
        Assert.assertEquals(((BString) returns[1]).stringValue(), "value");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 99.34323);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 88343);
    }

    @Test(description = "Test accessing global vars with identifier literals defined in other packages")
    public void testAccessStructGlobalVarWithIdentifierLiteralInOtherPackage() {
        CompileResult result = BTestUtils.compile("test-src/expressions/identifierliteral", "pkg.main");
        BValue[] returns = BTestUtils.invoke(result, "pkg.main", "accessStructWithIL");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        Assert.assertEquals(((BString) returns[0]).stringValue(), "Harry");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);

    }
}
