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

package org.ballerinalang.test.imports;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for imports.
 */
public class ImportsTest {

    @Test(description = "Test self import")
    public void testSelfImport() {
        CompileResult result = BCompileUtil.compile("test-src/imports/self-import", "foo");
        assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "cyclic module imports detected 'self-import/foo:1.0.0 -> self-import/foo:1.0.0'", 2,
                1);
    }

    @Test(description = "Test cyclic imports")
    public void testCyclicImports() {
        CompileResult result = BCompileUtil.compile("test-src/imports/cyclic-imports", "abc");
        assertEquals(result.getErrorCount(), 3);
        validateError(result, 0, "cyclic module imports detected " +
                                 "'cyclic-imports/def:1.0.0 -> cyclic-imports/ghi:1.0.0 -> cyclic-imports/def:1.0.0'",
                2, 1);
        validateError(result, 1, "cyclic module imports detected " +
                                 "'cyclic-imports/abc:1.0.0 -> cyclic-imports/def:1.0.0 -> " +
                                 "cyclic-imports/ghi:1.0.0 -> cyclic-imports/jkl:1.0.0 -> cyclic-imports/abc:1.0.0'",
                2, 1);
        validateError(result, 2, "cyclic module imports detected 'cyclic-imports/abc:1.0.0 -> " +
                                 "cyclic-imports/def:1.0.0 -> cyclic-imports/ghi:1.0.0 -> cyclic-imports/abc:1.0.0'",
                3, 1);
    }

    @Test(description = "Test importing same module name but with different org names")
    public void testSameModuleNameDifferentOrgImports() {
        CompileResult result = BCompileUtil.compile("test-src/imports/same-module-different-org-import", "math");
        BValue[] returns = BRunUtil.invoke(result, "getStringValueOfPI");
        Assert.assertTrue((returns[0]).stringValue().startsWith("3.14"));
    }

    @Test(description = "Test auto imports")
    public void testPredeclaredModules() {
        CompileResult result = BCompileUtil.compile("test-src/imports/predeclared-imports", "bar");
        BValue[] returns = BRunUtil.invoke(result, "testPredeclaredModules");
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(), BigDecimal.valueOf(22.0));
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 70.35);
        Assert.assertEquals((returns[2]).stringValue(), "NaN");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 110);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 15);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }

    @Test(description = "Test overridden predeclared modules")
    public void testOverriddenPredeclaredModules() {
        CompileResult result = BCompileUtil.compile("test-src/imports/predeclared-imports", "foo");
        BValue[] returns = BRunUtil.invoke(result, "testOverriddenPredeclaredModules");
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(), BigDecimal.valueOf(22.0));
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 70.35);
        Assert.assertEquals((returns[2]).stringValue(), "NaN");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 110);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 15);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
}
