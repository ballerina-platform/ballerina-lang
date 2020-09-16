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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the var binding-pattern capture-pattern.
 *
 * @since Swan Lake
 */
@Test(groups = { "disableOnOldParser" })
public class CaptureBindingPatternTest {
    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture-binding" +
                "-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture" +
                "-binding-pattern-negative.bal");
    }

    @Test
    public void testCaptureBindingPattern1() {
        BRunUtil.invoke(result, "testCaptureBindingPattern1");
    }

    @Test
    public void testCaptureBindingPattern2() {
        BRunUtil.invoke(result, "testCaptureBindingPattern2");
    }

    @Test
    public void testCaptureBindingPattern3() {
        BRunUtil.invoke(result, "testCaptureBindingPattern3");
    }

    @Test
    public void testCaptureBindingPattern4() {
        BRunUtil.invoke(result, "testCaptureBindingPattern4");
    }

    @Test
    public void testCaptureBindingPatternNegative1() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "unreachable pattern", 22, 9);
    }
}
