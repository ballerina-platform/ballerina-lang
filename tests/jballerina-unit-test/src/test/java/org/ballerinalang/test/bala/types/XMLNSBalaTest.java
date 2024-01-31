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

package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
        result = BCompileUtil.compile("test-src/bala/test_bala/types/xmlns_bala_test.bal");
    }

    @Test
    public void testXMLNSDeclUsingModuleConstant() {
        BRunUtil.invoke(result, "testXMLNSDeclUsingModuleConstant");
    }

    @Test
    public void testXMLNSDeclStmtUsingModuleConstant() {
        BRunUtil.invoke(result, "testXMLNSDeclStmtUsingModuleConstant");
    }

    @Test
    public void testXMLNSNegativeDefinition() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/bala/test_bala/types/xmlns_bala_test_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 18, 7);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 22, 11);
        validateError(negativeResult, i++, "cannot bind prefix 'ns4' to the empty namespace name", 23, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
