/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for xml attribute access using module constants.
 *
 * @since 2201.9.0
 */
public class XMLAttrAccessWithModulePrefixTest {
    CompileResult compileResult;
    CompileResult negative;

    @BeforeClass
    public void setup() {
        CompileResult projectCompileResult =
                BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        if (projectCompileResult.getErrorCount() > 0) {
            Arrays.stream(projectCompileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains errors");
        }
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/types/xml_attribute_access.bal");
        negative = BCompileUtil.compile("test-src/bala/test_bala/types/xml_attribute_access_negative.bal");
    }

    @Test
    public void testXMLAttributeAccessWithModulePrefix() {
        BRunUtil.invoke(compileResult, "testXMLAttributeAccessWithModulePrefix");
    }

    @Test
    public void testOptionalXMLAttributeAccessWithModulePrefix() {
        BRunUtil.invoke(compileResult, "testOptionalXMLAttributeAccessWithModulePrefix");
    }

    @Test
    public void testXMLAttributeAccessErrors() {
        BRunUtil.invoke(compileResult, "testXMLAttributeAccessErrors");
    }

    @Test
    public void testXMLAttributeAccessCompileNegative() {
        int i = 0;
        validateError(negative, i++, "attempt to refer to non-accessible symbol 'XMLD'", 24, 22);
        validateError(negative, i++, "undefined symbol 'foo:XMLD'", 24, 22);
        validateError(negative, i++, "undefined symbol 'foo:XMLE'", 25, 22);
        validateError(negative, i++, "undefined symbol 'foo:XMLG'", 26, 22);
        validateError(negative, i++, "cannot find the prefix 'bar'", 27, 24);
        validateError(negative, i++, "attempt to refer to non-accessible symbol 'XMLD'", 33, 23);
        validateError(negative, i++, "undefined symbol 'foo:XMLD'", 33, 23);
        validateError(negative, i++, "undefined symbol 'foo:XMLE'", 34, 23);
        validateError(negative, i++, "undefined symbol 'foo:XMLG'", 35, 23);
        validateError(negative, i++, "cannot find the prefix 'bar'", 36, 26);
        assertEquals(negative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negative = null;
    }

}
