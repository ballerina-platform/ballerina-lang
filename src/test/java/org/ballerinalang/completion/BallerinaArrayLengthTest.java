/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.completion;

public class BallerinaArrayLengthTest extends BallerinaCompletionTestBase {

    // * Local array.
    public void testArrayLengthInSingleDimensionLocalArray() {
        doTest("function test(){ int[] a=[]; a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionLocalArrayAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[] a=[]; a.len<caret> }",
                "function test(){ int[] a=[]; a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOne() {
        doTest("function test(){ int[][] a=[]; a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; a.len<caret> }",
                "function test(){ int[][] a=[]; a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwo() {
        doTest("function test(){ int[][] a=[]; a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; a[0].len<caret> }",
                "function test(){ int[][] a=[]; a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThree() {
        doTest("function test(){ int[][] a=[]; a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; a[0][0].len<caret> }",
                "function test(){ int[][] a=[]; a[0][0].len }", null);
    }

    public void testArrayLengthInSingleDimensionLocalArrayOnRHS() {
        doTest("function test(){ int[] a=[]; int len = a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionLocalArrayOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[] a=[]; int len = a.len<caret> }",
                "function test(){ int[] a=[]; int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHS() {
        doTest("function test(){ int[][] a=[]; int len = a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a.len<caret> }",
                "function test(){ int[][] a=[]; int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHS() {
        doTest("function test(){ int[][] a=[]; int len = a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a[0].len<caret> }",
                "function test(){ int[][] a=[]; int len = a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHS() {
        doTest("function test(){ int[][] a=[]; int len = a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a[0][0].len<caret> }",
                "function test(){ int[][] a=[]; int len = a[0][0].len }", null);
    }

    // After element.
    public void testArrayLengthInSingleDimensionLocalArrayOnRHSAfterElement() {
        doTest("function test(){ int[] a=[]; int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionLocalArrayOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[] a=[]; int len = 1 + a.len<caret> }",
                "function test(){ int[] a=[]; int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSAfterElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a.len<caret> }",
                "function test(){ int[][] a=[]; int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSAfterElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a[0].len<caret> }",
                "function test(){ int[][] a=[]; int len = 1 + a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSAfterElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a[0][0].len<caret> }",
                "function test(){ int[][] a=[]; int len = 1 + a[0][0].len }", null);
    }

    // Before element.
    public void testArrayLengthInSingleDimensionLocalArrayOnRHSBeforeElement() {
        doTest("function test(){ int[] a=[]; int len = a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionLocalArrayOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[] a=[]; int len = a.len<caret>  + 1}",
                "function test(){ int[] a=[]; int len = a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSBeforeElement() {
        doTest("function test(){ int[][] a=[]; int len = a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a.len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSBeforeElement() {
        doTest("function test(){ int[][] a=[]; int len = a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a[0].len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSBeforeElement() {
        doTest("function test(){ int[][] a=[]; int len = a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = a[0][0].len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = a[0][0].len + 1 }", null);
    }

    // Between elements.
    public void testArrayLengthInSingleDimensionLocalArrayOnRHSBetweenElement() {
        doTest("function test(){ int[] a=[]; int len = 1 + a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionLocalArrayOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[] a=[]; int len = 1 + a.len<caret>  + 1}",
                "function test(){ int[] a=[]; int len = 1 + a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSBetweenElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionOneOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a.len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = 1 + a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSBetweenElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionTwoOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a[0].len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = 1 + a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSBetweenElement() {
        doTest("function test(){ int[][] a=[]; int len = 1 + a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionLocalArrayDimensionThreeOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(){ int[][] a=[]; int len = 1 + a[0][0].len<caret> + 1 }",
                "function test(){ int[][] a=[]; int len = 1 + a[0][0].len + 1 }", null);
    }
    
    // * Global array.
    public void testArrayLengthInSingleDimensionGlobalArray() {
        doTest("int[] a=[]; function test(){ a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionGlobalArrayAutoCompletion() {
        doCheckResult("test.bal", "int[] a=[]; function test(){ a.len<caret> }",
                "int[] a=[]; function test(){ a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOne() {
        doTest("int[][] a=[]; function test(){ a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ a.len<caret> }",
                "int[][] a=[]; function test(){ a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwo() {
        doTest("int[][] a=[]; function test(){ a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ a[0].len<caret> }",
                "int[][] a=[]; function test(){ a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThree() {
        doTest("int[][] a=[]; function test(){ a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ a[0][0].len<caret> }",
                "int[][] a=[]; function test(){ a[0][0].len }", null);
    }

    public void testArrayLengthInSingleDimensionGlobalArrayOnRHS() {
        doTest("int[] a=[]; function test(){ int len = a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSAutoCompletion() {
        doCheckResult("test.bal", "int[] a=[]; function test(){ int len = a.len<caret> }",
                "int[] a=[]; function test(){ int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHS() {
        doTest("int[][] a=[]; function test(){ int len = a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a.len<caret> }",
                "int[][] a=[]; function test(){ int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHS() {
        doTest("int[][] a=[]; function test(){ int len = a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a[0].len<caret> }",
                "int[][] a=[]; function test(){ int len = a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHS() {
        doTest("int[][] a=[]; function test(){ int len = a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a[0][0].len<caret> }",
                "int[][] a=[]; function test(){ int len = a[0][0].len }", null);
    }

    // After element.
    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSAfterElement() {
        doTest("int[] a=[]; function test(){ int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "int[] a=[]; function test(){ int len = 1 + a.len<caret> }",
                "int[] a=[]; function test(){ int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSAfterElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a.len<caret> }",
                "int[][] a=[]; function test(){ int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSAfterElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a[0].len<caret> }",
                "int[][] a=[]; function test(){ int len = 1 + a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSAfterElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a[0][0].len<caret> }",
                "int[][] a=[]; function test(){ int len = 1 + a[0][0].len }", null);
    }

    // Before element.
    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSBeforeElement() {
        doTest("int[] a=[]; function test(){ int len = a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "int[] a=[]; function test(){ int len = a.len<caret>  + 1}",
                "int[] a=[]; function test(){ int len = a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSBeforeElement() {
        doTest("int[][] a=[]; function test(){ int len = a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a.len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSBeforeElement() {
        doTest("int[][] a=[]; function test(){ int len = a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a[0].len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSBeforeElement() {
        doTest("int[][] a=[]; function test(){ int len = a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = a[0][0].len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = a[0][0].len + 1 }", null);
    }

    // Between elements.
    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSBetweenElement() {
        doTest("int[][] a=[]; function test(){ int[] a=[]; int len = 1 + a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionGlobalArrayOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int[] a=[]; int len = 1 + a.len<caret>  + 1}",
                "int[][] a=[]; function test(){ int[] a=[]; int len = 1 + a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSBetweenElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionOneOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a.len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = 1 + a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSBetweenElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionTwoOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a[0].len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = 1 + a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSBetweenElement() {
        doTest("int[][] a=[]; function test(){ int len = 1 + a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionGlobalArrayDimensionThreeOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "int[][] a=[]; function test(){ int len = 1 + a[0][0].len<caret> + 1 }",
                "int[][] a=[]; function test(){ int len = 1 + a[0][0].len + 1 }", null);
    }

    // * Parameter array.
    public void testArrayLengthInSingleDimensionParameterArray() {
        doTest("function test(int[] a){ a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionParameterArrayAutoCompletion() {
        doCheckResult("test.bal", "function test(int[] a){ a.len<caret> }",
                "function test(int[] a){ a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOne() {
        doTest("function test(int[][] a){ a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ a.len<caret> }",
                "function test(int[][] a){ a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwo() {
        doTest("function test(int[][] a){ a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ a[0].len<caret> }",
                "function test(int[][] a){ a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThree() {
        doTest("function test(int[][] a){ a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ a[0][0].len<caret> }",
                "function test(int[][] a){ a[0][0].len }", null);
    }

    public void testArrayLengthInSingleDimensionParameterArrayOnRHS() {
        doTest("function test(int[] a){ int len = a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionParameterArrayOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(int[] a){ int len = a.len<caret> }",
                "function test(int[] a){ int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHS() {
        doTest("function test(int[][] a){ int len = a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a.len<caret> }",
                "function test(int[][] a){ int len = a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHS() {
        doTest("function test(int[][] a){ int len = a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a[0].len<caret> }",
                "function test(int[][] a){ int len = a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHS() {
        doTest("function test(int[][] a){ int len = a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a[0][0].len<caret> }",
                "function test(int[][] a){ int len = a[0][0].len }", null);
    }

    // After element.
    public void testArrayLengthInSingleDimensionParameterArrayOnRHSAfterElement() {
        doTest("function test(int[] a){ int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInSingleDimensionParameterArrayOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[] a){ int len = 1 + a.len<caret> }",
                "function test(int[] a){ int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSAfterElement() {
        doTest("function test(int[][] a){ int len = 1 + a.<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a.len<caret> }",
                "function test(int[][] a){ int len = 1 + a.length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSAfterElement() {
        doTest("function test(int[][] a){ int len = 1 + a[0].<caret> }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a[0].len<caret> }",
                "function test(int[][] a){ int len = 1 + a[0].length }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSAfterElement() {
        doTest("function test(int[][] a){ int len = 1 + a[0][0].<caret> }");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSAfterElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a[0][0].len<caret> }",
                "function test(int[][] a){ int len = 1 + a[0][0].len }", null);
    }

    // Before element.
    public void testArrayLengthInSingleDimensionParameterArrayOnRHSBeforeElement() {
        doTest("function test(int[] a){ int len = a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionParameterArrayOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[] a){ int len = a.len<caret>  + 1}",
                "function test(int[] a){ int len = a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSBeforeElement() {
        doTest("function test(int[][] a){ int len = a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a.len<caret> + 1 }",
                "function test(int[][] a){ int len = a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSBeforeElement() {
        doTest("function test(int[][] a){ int len = a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a[0].len<caret> + 1 }",
                "function test(int[][] a){ int len = a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSBeforeElement() {
        doTest("function test(int[][] a){ int len = a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSBeforeElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = a[0][0].len<caret> + 1 }",
                "function test(int[][] a){ int len = a[0][0].len + 1 }", null);
    }

    // Between elements.
    public void testArrayLengthInSingleDimensionParameterArrayOnRHSBetweenElement() {
        doTest("function test(int[][] a){ int[] a=[]; int len = 1 + a.<caret> + 1 }", "length");
    }

    public void testArrayLengthInSingleDimensionParameterArrayOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int[] a=[]; int len = 1 + a.len<caret>  + 1}",
                "function test(int[][] a){ int[] a=[]; int len = 1 + a.length  + 1}", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSBetweenElement() {
        doTest("function test(int[][] a){ int len = 1 + a.<caret> + 1}", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionOneOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a.len<caret> + 1 }",
                "function test(int[][] a){ int len = 1 + a.length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSBetweenElement() {
        doTest("function test(int[][] a){ int len = 1 + a[0].<caret> + 1 }", "length");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionTwoOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a[0].len<caret> + 1 }",
                "function test(int[][] a){ int len = 1 + a[0].length + 1 }", null);
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSBetweenElement() {
        doTest("function test(int[][] a){ int len = 1 + a[0][0].<caret> + 1 }");
    }

    public void testArrayLengthInMultiDimensionParameterArrayDimensionThreeOnRHSBetweenElementAutoCompletion() {
        doCheckResult("test.bal", "function test(int[][] a){ int len = 1 + a[0][0].len<caret> + 1 }",
                "function test(int[][] a){ int len = 1 + a[0][0].len + 1 }", null);
    }
}
