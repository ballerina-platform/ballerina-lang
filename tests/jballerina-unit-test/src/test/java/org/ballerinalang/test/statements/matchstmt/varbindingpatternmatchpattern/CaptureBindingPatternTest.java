/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.matchstmt.varbindingpatternmatchpattern;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the var binding-pattern capture-pattern.
 *
 * @since 2.0.0
 */
public class CaptureBindingPatternTest {
    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture-binding" +
                "-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture" +
                "-binding-pattern-negative.bal");
    }

    @Test(dataProvider = "captureBindingPatternTests")
    public void testCaptureBindingPattern(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] captureBindingPatternTests() {
        return new Object[]{
                "testCaptureBindingPattern1",
                "testCaptureBindingPattern2",
                "testCaptureBindingPattern3",
                "testCaptureBindingPattern4",
                "testCaptureBindingPattern5",
                "testCaptureBindingPattern6",
                "testCaptureBindingPattern7",
                "testCaptureBindingPattern8"
        };
    }

    @Test
    public void testCaptureBindingPatternNegative1() {
        Assert.assertEquals(resultNegative.getWarnCount(), 2);

        BAssertUtil.validateWarning(resultNegative, 0, "unused variable 'a'", 19, 9);
        BAssertUtil.validateWarning(resultNegative, 1, "unreachable pattern", 22, 9);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
