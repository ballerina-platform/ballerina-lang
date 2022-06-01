/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.record;

import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for anonymous open records.
 *
 * @since 0.970.0
 */
public class AnonymousOpenRecordTest {

    private CompileResult compileResult, result;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/anon_record.bal");
        result = BCompileUtil.compile("test-src/record/anon_record_code_analysis.bal");
    }

    @Test(description = "Test Anonymous record in a function parameter declaration")
    public void testAnonStructAsFuncParam() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonStructAsFuncParam");

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 24L);
    }

    @Test(description = "Test Anonymous record in a local variable declaration")
    public void testAnonStructAsLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonStructAsLocalVar");

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 11L);
    }

    @Test(description = "Test Anonymous record in a package variable declaration")
    public void testAnonStructAsPkgVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonStructAsPkgVar");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "sameera:jayasoma:100");
    }

    @Test(description = "Test Anonymous record in a record field")
    public void testAnonStructAsStructField() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonStructAsStructField");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "JAN:12 Gemba St APT 134:CA:sam");
    }

    @Test
    public void testAnonRecordWithRestField() {
        Object returns = BRunUtil.invoke(compileResult, "testRestField");
        Assert.assertTrue((Boolean) returns);

        returns = BRunUtil.invoke(compileResult, "testAnonRecWithExplicitRestField");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAnonRecordAsRestFieldOfAnonRecord() {
        BRunUtil.invoke(compileResult, "testAnonRecordAsRestFieldOfAnonRecord");
    }

    @Test(description = "Test Code analyzer execution on Anonymous records")
    public void testCodeAnalyzerRunningOnAnonymousRecordsForDeprecatedFunctionAnnotation() {
        BAssertUtil.validateWarning(result, 0, "usage of construct 'Test' is deprecated", 3, 17);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        result = null;
    }
}
