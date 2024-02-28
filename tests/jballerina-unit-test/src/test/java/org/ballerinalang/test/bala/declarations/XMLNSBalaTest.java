/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.bala.declarations;

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
 * Bala test cases for xmlns declaration.
 *
 * @since 2201.9.0
 */
public class XMLNSBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/declarations/xmlns_bala_test.bal");
    }

    @Test (dataProvider = "balaXMLNSDeclFunctions")
    public void testBalaXMLNSDeclaration(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "balaXMLNSDeclFunctions")
    private Object[] balaXMLNSDeclFunctions() {
        return new String[]{
                "testXMLNSDeclUsingModuleConstant",
                "testXMLNSDeclStmtUsingModuleConstant"
        };
    }

    @Test
    public void testXMLNSDefinitionNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/bala/test_bala/declarations/xmlns_bala_test_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 19, 7);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 23, 11);
        validateError(negativeResult, i++, "cannot bind prefix 'ns4' to the empty namespace name", 24, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
