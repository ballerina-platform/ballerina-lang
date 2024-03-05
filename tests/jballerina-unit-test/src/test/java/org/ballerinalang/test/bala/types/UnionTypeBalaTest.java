/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Bala tests for the union type.
 *
 * @since 2.0.0
 */
public class UnionTypeBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/types/test_union_type.bal");
    }

    @Test(dataProvider = "unionTestFunctions")
    public void testUnionType(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "unionTestFunctions")
    public Object[] unionTestFunctions() {
        return new Object[]{
                "testUnionPositive",
                "testUnionNegative",
                "testUnionRuntimeToString",
                "testTernaryWithQueryForModuleImportedVariable"
        };
    }

    @Test
    public void testUnionTypeNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/bala/test_bala/types/test_union_type_negative.bal");
        int i = 0;
        validateError(negativeResult, i++,
                "incompatible types: expected 'boolean', found 'testorg/foo:1.0.0:IntOrString'", 21, 17);
        validateError(negativeResult, i++,
                "incompatible types: expected 'testorg/foo:1.0.0:FooBar', found 'string'", 23, 20);
        validateError(negativeResult, i++,
                "incompatible types: expected 'testorg/foo:1.0.0:BazQux', found 'string'", 25, 20);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
