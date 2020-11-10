/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.javainterop;

import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.FPValue;
import io.ballerina.runtime.values.FutureValue;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for java interop with ballerina ref types.
 *
 * @since 1.1.0
 */
public class RefTypeNegativeTests {

    @Test
    public void testInvalidMethodSignaturesForRefTypes() {
        CompileResult compileResult =
                BCompileUtil.compileInProc("test-src/javainterop/ballerina_ref_types_as_interop_negative.bal");
        Diagnostic[] diagnostics = compileResult.getDiagnostics();
        Assert.assertNotNull(diagnostics);
        Assert.assertEquals(diagnostics.length, 6);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return type for method " +
                        "'getAllFloats' in class 'org.ballerinalang.test.javainterop.RefTypeNegativeTests': " +
                        "Java type 'float' will not be matched to ballerina type 'ALL_INT''",
                "ballerina_ref_types_as_interop_negative.bal", 13, 1);
        BAssertUtil.validateError(compileResult, 1,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'acceptAllInts' in class 'org.ballerinalang.test.javainterop.RefTypeNegativeTests': " +
                        "Java type 'int' will not be matched to ballerina type 'MIX_TYPE''",
                "ballerina_ref_types_as_interop_negative.bal", 28, 1);
        BAssertUtil.validateError(compileResult, 2,
                                  "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for " +
                                          "method 'acceptImmutableValue' in class " +
                                          "'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type 'int'" +
                                          " will not be matched to ballerina type '(map<int> & readonly)''", 33, 1);
        BAssertUtil.validateError(compileResult, 3,
                                  "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return type for " +
                                          "method 'acceptAndReturnImmutableArray' in class " +
                                          "'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type " +
                                          "'java.lang.Object' will not be matched to ballerina type " +
                                          "'(int[] & readonly)''", 39, 1);
        BAssertUtil.validateError(compileResult, 4,
                                  "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for " +
                                          "method 'acceptReadOnlyValue' in class " +
                                          "'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type " +
                                          "'long' will not be matched to ballerina type 'readonly''", 45, 1);
        BAssertUtil.validateError(compileResult, 5,
                                  "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return type for " +
                                          "method 'returnReadOnlyValue' in class " +
                                          "'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type " +
                                          "'io.ballerina.runtime.api.values.BFuture' will not be matched to " +
                                          "ballerina type 'readonly''", 51, 1);
    }

    // static methods

    public static int getAllInts() {
        return 3;
    }

    public static int acceptAllInts(int x) {
        return x;
    }

    public static float getAllFloats() {
        return (float) 3.54;
    }

    public static int acceptAllFloats(float x) {
        return (int) x;
    }

    public static void acceptImmutableValue(int m) {
    }

    public static Object acceptAndReturnImmutableArray(ArrayValue a) {
        return a;
    }

    public static void acceptReadOnlyValue(long r) {
    }

    public static BFuture returnReadOnlyValue(FPValue f) {
        return new FutureValue(null, null, null); // OK since not used anywhere.
    }
}
