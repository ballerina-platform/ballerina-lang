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
package org.ballerinalang.test.javainterop.basic;

import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BTypedesc;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for java interop.
 *
 * @since 1.0.0
 */
public class NegativeValidationTest {

    @Test
    public void testAcceptNothing() {

        String path = "test-src/javainterop/ballerina_types_as_interop_types_negative.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 12);
    }

    @Test
    public void testClassNotFound() {

        String path = "test-src/javainterop/negative/class_not_found.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}CLASS_NOT_FOUND 'org.ballerinalang.nativeimpl.jvm.tests.PublicStaticMethods'",
                "class_not_found.bal", 25, 1);
    }

    @Test
    public void testMethodNotFound1() {

        String path = "test-src/javainterop/negative/method_not_found1.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_NOT_FOUND 'No such public method 'acceptStringOrErrorReturn' found in " +
                        "class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found1.bal", 8, 1);
    }

    @Test
    public void testMethodNotFound2() {

        String path = "test-src/javainterop/negative/method_not_found2.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_NOT_FOUND 'No such public method 'acceptObjectAndObjectReturn' with '3' " +
                        "parameter(s) found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found2.bal", 22, 1);
    }

    @Test
    public void testMethodNotFound3() {

        String path = "test-src/javainterop/negative/method_not_found3.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_NOT_FOUND 'No such public method 'acceptRecordAndRecordReturn' with '3' " +
                        "parameter(s) found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found3.bal", 21, 1);
    }

    @Test
    public void testMethodNotFound4() {

        String path = "test-src/javainterop/negative/method_not_found4.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_NOT_FOUND 'No such public method 'acceptIntAndUnionReturn' found in class " +
                        "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found4.bal", 23, 1);
    }

    @Test
    public void testMethodNotFound5() {

        String path = "test-src/javainterop/negative/method_not_found5.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_NOT_FOUND 'No such public method 'acceptIntStringAndUnionReturn' found in " +
                        "class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found5.bal", 23, 1);
    }

    @Test
    public void testMethodNotFound6() {

        String testFileName = "method_not_found6.bal";
        String path = "test-src/javainterop/negative/" + testFileName;

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 4);

        String message = "{ballerina/java}METHOD_NOT_FOUND 'No such public method '%s' that matches with " +
                "parameter types '(%s)' found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''";

        String bTypeDescClassName = BTypedesc.class.getName();
        String bFutureClassName = BFuture.class.getName();

        BAssertUtil.validateError(compileResult, 0, String.format(message, "getFuture", bTypeDescClassName),
                testFileName, 3, 1);

        BAssertUtil.validateError(compileResult, 1, String.format(message, "getTypeDesc", bFutureClassName),
                testFileName, 9, 1);

        BAssertUtil.validateError(compileResult, 2,
                String.format(message, "getFutureOnly", bFutureClassName + "," + bTypeDescClassName),
                testFileName, 15, 1);

        BAssertUtil.validateError(compileResult, 3,
                String.format(message, "getTypeDescOnly", bTypeDescClassName + "," + bFutureClassName),
                testFileName, 21, 1);
    }

    @Test
    public void testMethodSignatureNotMatch1() {

        String path = "test-src/javainterop/negative/method_sig_not_match1.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'acceptIntReturnIntThrowsCheckedException' which throws checked exception found in class " +
                        "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match1.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch2() {

        String path = "test-src/javainterop/negative/method_sig_not_match2.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'acceptRecordAndRecordReturnWhichThrowsCheckedException' which throws checked exception " +
                        "found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match2.bal", 7, 1);
    }

    @Test
    public void testMethodSignatureNotMatch3() {

        String path = "test-src/javainterop/negative/method_sig_not_match3.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'acceptIntUnionReturnWhichThrowsCheckedException' which throws checked exception found in " +
                        "class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match3.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch4() {

        String path = "test-src/javainterop/negative/method_sig_not_match4.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'acceptRefTypesAndReturnMapWhichThrowsCheckedException' which throws checked exception " +
                        "found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match4.bal", 14, 1);
    }

    @Test
    public void testMethodSignatureNotMatch5() {

        String path = "test-src/javainterop/negative/method_sig_not_match5.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'acceptStringErrorReturnWhichThrowsCheckedException' which throws checked exception found " +
                        "in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match5.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch6() {

        String path = "test-src/javainterop/negative/method_sig_not_match6.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'No such Java method " +
                        "'getArrayValueFromMapWhichThrowsCheckedException' which throws checked exception found in " +
                        "class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_sig_not_match6.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch7() {

        String path = "test-src/javainterop/negative/method_sig_not_match7.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method 'split' in " +
                        "class 'java.lang.String': Java type 'java.lang.String' will not be matched to ballerina " +
                        "type 'string''",
                "method_sig_not_match7.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch8() {

        String path = "test-src/javainterop/negative/method_sig_not_match8.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Parameter count does not match with Java method " +
                        "'split' found in class 'java.lang.String''",
                "method_sig_not_match8.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch9() {

        String path = "test-src/javainterop/negative/method_sig_not_match9.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'decimalParamAsObjectAndReturn' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.Object' " +
                        "will not be matched to ballerina type 'decimal''",
                "method_sig_not_match9.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch10() {

        String path = "test-src/javainterop/negative/method_sig_not_match10.bal";

        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return type for method " +
                        "'decimalParamAndReturnAsObject' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.Object' " +
                        "will not be matched to ballerina type 'decimal''",
                "method_sig_not_match10.bal", 3, 1);
    }

    @Test
    public void testReturnStringForBUnionFromJava() {

        String path = "test-src/javainterop/negative/method_sig_not_match11.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return type for method " +
                        "'returnStringForBUnionFromJava' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.String' " +
                        "will not be matched to ballerina type '(int|float|string)''",
                "method_sig_not_match11.bal", 3, 1);
    }

    @Test
    public void testJavaPrimitiveForBJsonParam() {

        String path = "test-src/javainterop/negative/method_sig_not_match12.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "Java type 'int' will not be matched to ballerina type 'json''",
                "method_sig_not_match12.bal", 3, 1);
    }

    @Test
    public void testJavaPrimitiveForBUnionParam() {

        String path = "test-src/javainterop/negative/method_sig_not_match13.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "Java type 'int' will not be matched to ballerina type '(int|string)''",
                "method_sig_not_match13.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch14() {

        String path = "test-src/javainterop/negative/method_sig_not_match14.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(
                compileResult, 0,
                "{ballerina/java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'decimalParamAndWithBigDecimal' in class 'org.ballerinalang.nativeimpl.jvm.tests" +
                        ".StaticMethods': Java type 'java.math.BigDecimal' will not be matched to ballerina type " +
                        "'decimal''", "method_sig_not_match14.bal", 3, 1);
    }

    @Test(description = "When there are instance and static methods with same name and parameters that differ by one")
    public void testResolveWithInstanceAndStatic() {
        String path = "test-src/javainterop/negative/method_resolve_error.bal";
        CompileResult compileResult = BCompileUtil.compileInProc(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateError(compileResult, 0,
                                  "{ballerina/java}OVERLOADED_METHODS 'Overloaded methods cannot be differentiated. " +
                                          "Please specify the parameterTypes for each parameter in 'paramTypes' field" +
                                          " in the annotation'", "method_resolve_error.bal", 19, 1);
        BAssertUtil.validateError(compileResult, 1,
                                  "{ballerina/java}OVERLOADED_METHODS 'Overloaded methods cannot be differentiated. " +
                                          "Please specify the parameterTypes for each parameter in 'paramTypes' field" +
                                          " in the annotation'", "method_resolve_error.bal", 24, 1);

    }
}
