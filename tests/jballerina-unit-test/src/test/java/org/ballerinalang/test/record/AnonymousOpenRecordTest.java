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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for anonymous open records.
 *
 * @since 0.970.0
 */
public class AnonymousOpenRecordTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/anon_record.bal");
    }

    @Test(description = "Test Anonymous record in a function parameter declaration")
    public void testAnonStructAsFuncParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsFuncParam");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 24);
    }

    @Test(description = "Test Anonymous record in a local variable declaration")
    public void testAnonStructAsLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsLocalVar");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 11);
    }

    @Test(description = "Test Anonymous record in a package variable declaration")
    public void testAnonStructAsPkgVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsPkgVar");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "sameera:jayasoma:100");
    }

    @Test(description = "Test Anonymous record in a record field")
    public void testAnonStructAsStructField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonStructAsStructField");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "JAN:12 Gemba St APT 134:CA:sam");
    }

    @Test
    public void testAnonRecordWithRestField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRestField");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

        returns = BRunUtil.invoke(compileResult, "testAnonRecWithExplicitRestField");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAnonRecordAsRestFieldOfAnonRecord() {
        BRunUtil.invoke(compileResult, "testAnonRecordAsRestFieldOfAnonRecord");
    }

    @Test(description = "Test Code analyzer execution on Anonymous records")
    public void testCodeAnalyzerRunningOnAnonymousRecordsForDeprecatedFunctionAnnotation() {
        BAssertUtil.validateWarning(compileResult, 0, "usage of construct 'Test()' is deprecated", 69, 17);
    }
}
