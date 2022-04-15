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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
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
@Test()
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
        Object arr = BRunUtil.invoke(result, "getVarsInOtherPkg");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof Double);
        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(0), 800L);
        Assert.assertEquals(returns.get(1).toString(), "value");
        Assert.assertEquals(returns.get(2), 99.34323);
        Assert.assertEquals(returns.get(3), 88343L);
    }

    @Test(description = "Test accessing global vars with identifier literals defined in other packages")
    public void testAccessStructGlobalVarWithIdentifierLiteralInOtherPackage() {
        Object arr = BRunUtil.invoke(result, "accessStructWithIL");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof Long);

        Assert.assertEquals(returns.get(0).toString(), "Harry");
        Assert.assertEquals(returns.get(1), 25L);

    }

    @Test(description = "Test access fields of record types with type label")
    public void testAccessTypeLabelWithIL() {
        Object arr = BRunUtil.invoke(result, "accessTypeLabelWithIL");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof Long);

        Assert.assertEquals(returns.get(0).toString(), "John");
        Assert.assertEquals(returns.get(1), 20L);

    }

    @Test(description = "Test get nested anonymous record arrays with element type of record having quoted identifier")
    public void testGetNestedAnonymousRecordArray() {
        Object arr = BRunUtil.invoke(result, "getAnonFromFoo");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 1);
        Assert.assertTrue(returns.get(0) instanceof BMap);
        BMap bmap = (BMap) returns.get(0);
        Assert.assertEquals(bmap.get(StringUtils.fromString("name")).toString(), "Waruna");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
