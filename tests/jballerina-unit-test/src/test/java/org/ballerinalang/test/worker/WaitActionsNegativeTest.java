/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

//import org.ballerinalang.test.util.BAssertUtil;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative wait action related tests.
 *
 * @since 0.985.0
 */
@Test(groups = { "disableOnOldParser" })
public class WaitActionsNegativeTest {

//    private CompileResult resultNegative;
//
//    @BeforeClass
//    public void setup() {
//        this.resultNegative = BCompileUtil.compile("test-src/workers/wait-actions-negative.bal");
//    }
//
//    @Test(description = "Test negative scenarios of worker actions")
//    public void testNegativeWorkerActions() {
//        int index = 0;
//        Assert.assertEquals(resultNegative.getErrorCount(), 20, "Wait actions negative test error count");
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<string>', found 'future<int>'", 56, 22);
//        BAssertUtil.validateError(resultNegative, index++,
//                "variable assignment is required", 57, 5);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<int>', found 'future<string>'", 66, 31);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<(int|boolean)>', found 'future<string>'", 67, 36);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<int>'", 68, 37);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<string>'", 68, 41);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<boolean>'", 68, 44);
//        // Commented errors won't get captured after (typeChecker, semanticAnalyzer) & codeAnalyzer separation.
//        //        BAssertUtil.validateError(resultNegative, index++,
//        //                "operator '|' cannot be applied to type 'future'", 69, 27);
//        //        BAssertUtil.validateError(resultNegative, index++,
//        //                "operator '|' cannot be applied to type 'future'", 70, 33);
//        BAssertUtil.validateError(resultNegative, index++,
//                "operator '|' not defined for 'future<int>' and 'future<string>'", 70, 33);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<future<(int|string)>>', found 'future<int>'", 71, 40);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<future<(int|string)>>', found 'future<string>'", 71, 43);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<int>', found 'future<string>'", 81, 34);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<(boolean|string)>', found 'future<int>'", 82, 41);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<(boolean|string)>', found 'future<int>'", 82, 45);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<int>', found 'future<string>'", 83, 51);
//        BAssertUtil.validateError(resultNegative, index++,
//                "missing non-defaultable required record field 'f3'", 85, 50);
//        BAssertUtil.validateError(resultNegative, index++,
//                "missing non-defaultable required record field 'f2'", 86, 30);
//        BAssertUtil.validateError(resultNegative, index++,
//                                  "incompatible types: expected 'sealedRec', found 'record {| int id; string name; " +
//                                          "boolean status; |}'", 87, 31);
//        BAssertUtil.validateError(resultNegative, index++,
//                "invalid field name 'status' in type 'sealedRec'", 88, 31);
//        BAssertUtil.validateError(resultNegative, index++,
//                "incompatible types: expected 'future<int>', found 'future<string>'", 89, 55);
//        BAssertUtil.validateError(resultNegative, index,
//                "incompatible types: expected 'future<string>', found 'future<int>'", 90, 54);
//    }
}
