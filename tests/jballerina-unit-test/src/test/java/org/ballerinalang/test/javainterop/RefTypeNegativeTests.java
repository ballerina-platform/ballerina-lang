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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for java interop with ballerina ref types.
 *
 * @since 1.1.0
 */
public class RefTypeNegativeTests {

    @Test(expectedExceptions = BLangCompilerException.class)
    public void testInvalidMethodSignaturesForRefTypes() {
        CompileResult compileResult =
                BCompileUtil.compileInProc("test-src/javainterop/ballerina_ref_types_as_interop_negative.bal");
        Diagnostic[] diagnostics = compileResult.getDiagnostics();
        Assert.assertNotNull(diagnostics);
        Assert.assertEquals(diagnostics.length, 2);
        Assert.assertEquals(diagnostics[0].getMessage(), "error: .:ballerina_ref_types_as_interop_negative.bal:13:1: " +
                "{ballerinax/java}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible return type for method " +
                "'getAllFloats' in class 'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type 'float'" +
                " will not be matched to ballerina type 'ALL_INT'");
        Assert.assertEquals(diagnostics[1].getMessage(), "error: .:ballerina_ref_types_as_interop_negative.bal:28:1: " +
                "{ballerinax/java}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible param type for method " +
                "'acceptAllInts' in class 'org.ballerinalang.test.javainterop.RefTypeNegativeTests': Java type 'int' " +
                "will not be matched to ballerina type 'MIX_TYPE'");
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
}
