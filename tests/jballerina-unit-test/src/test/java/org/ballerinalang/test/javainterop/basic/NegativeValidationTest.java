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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.Test;

/**
 * Negative test cases for java interop.
 *
 * @since 1.0.0
 */
public class NegativeValidationTest {
    private final String expectedMsg1 = ".*error: .:class_not_found.bal:25:1: " +
            "\\{ballerinax/java\\}CLASS_NOT_FOUND message=org.ballerinalang.nativeimpl.jvm.tests.PublicStaticMethods.*";
    private final String expectedMsg2 = ".*error: .:method_not_found1.bal:8:1: " +
            "\\{ballerinax/java\\}METHOD_NOT_FOUND message=No such public method 'acceptStringOrErrorReturn' " +
            "found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg3 = ".*error: .:method_not_found2.bal:22:1: " +
            "\\{ballerinax/java\\}METHOD_NOT_FOUND message=No such public method 'acceptObjectAndObjectReturn' " +
            "with '3' parameter\\(s\\) found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg4 = ".*error: .:method_not_found3.bal:21:1: " +
            "\\{ballerinax/java\\}METHOD_NOT_FOUND message=No such public method 'acceptRecordAndRecordReturn' " +
            "with '3' parameter\\(s\\) found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg5 = ".*error: .:method_not_found4.bal:23:1: " +
            "\\{ballerinax/java\\}METHOD_NOT_FOUND message=No such public method 'acceptIntAndUnionReturn' " +
            "found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg6 = ".*error: .:method_not_found5.bal:23:1: " +
            "\\{ballerinax/java\\}METHOD_NOT_FOUND message=No such public method 'acceptIntStringAndUnionReturn' " +
            "found in class 'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg7 = ".*error: .:method_sig_not_match1.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'acceptIntReturnIntThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg8 = ".*error: .:method_sig_not_match2.bal:7:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'acceptRecordAndRecordReturnWhichThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg9 = ".*error: .:method_sig_not_match3.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'acceptIntUnionReturnWhichThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg10 = ".*error: .:method_sig_not_match4.bal:14:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'acceptRefTypesAndReturnMapWhichThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg11 = ".*error: .:method_sig_not_match5.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'acceptStringErrorReturnWhichThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg12 = ".*error: .:method_sig_not_match6.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=No such Java method " +
            "'getArrayValueFromMapWhichThrowsCheckedException' which throws checked exception found in class " +
            "'class org.ballerinalang.nativeimpl.jvm.tests.StaticMethods'.*";
    private final String expectedMsg13 = "error: .:method_sig_not_match7.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible param type for method 'split'" +
            " in class 'java.lang.String': Java type 'java.lang.String' will not be matched to ballerina type " +
            "'string'.*";
    private final String expectedMsg14 = "error: .:method_sig_not_match8.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Parameter count does not match with Java " +
            "method 'split' found in class 'java.lang.String'";
    private final String expectedMsg15 = "error: .:method_sig_not_match9.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible param type for method " +
            "'decimalParamAsObjectAndReturn' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
            "Java type 'java.lang.Object' will not be matched to ballerina type 'decimal'";
    private final String expectedMsg16 = "error: .:method_sig_not_match10.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible return type for method " +
            "'decimalParamAndReturnAsObject' in class 'org.ballerinalang.nativeimpl." +
            "jvm.tests.StaticMethods': Java type 'java.lang.Object' will not be matched to ballerina type 'decimal'";
    private final String expectedMsg17 = "error: .:method_sig_not_match11.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible return type for method " +
            "'returnStringForBUnionFromJava' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
            "Java type 'java.lang.String' will not be matched to ballerina type 'int\\|float\\|string'";
    private final String expectedMsg18 = "error: .:method_sig_not_match12.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible param type for method " +
            "'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'int' " +
            "will not be matched to ballerina type 'json'";
    private final String expectedMsg19 = "error: .:method_sig_not_match13.bal:3:1: " +
            "\\{ballerinax/java\\}METHOD_SIGNATURE_DOES_NOT_MATCH message=Incompatible param type for method " +
            "'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'int' " +
            "will not be matched to ballerina type 'int\\|string'";

    @Test(expectedExceptions = BLangCompilerException.class)
    public void testAcceptNothing() {
        String path = "test-src/javainterop/ballerina_types_as_interop_types_negative.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg1)
    public void testClassNotFound() {
        String path = "test-src/javainterop/negative/class_not_found.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg2)
    public void testMethodNotFound1() {
        String path = "test-src/javainterop/negative/method_not_found1.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg3)
    public void testMethodNotFound2() {
        String path = "test-src/javainterop/negative/method_not_found2.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg4)
    public void testMethodNotFound3() {
        String path = "test-src/javainterop/negative/method_not_found3.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg5)
    public void testMethodNotFound4() {
        String path = "test-src/javainterop/negative/method_not_found4.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg6)
    public void testMethodNotFound5() {
        String path = "test-src/javainterop/negative/method_not_found5.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg7)
    public void testMethodSignatureNotMatch1() {
        String path = "test-src/javainterop/negative/method_sig_not_match1.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg8)
    public void testMethodSignatureNotMatch2() {
        String path = "test-src/javainterop/negative/method_sig_not_match2.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg9)
    public void testMethodSignatureNotMatch3() {
        String path = "test-src/javainterop/negative/method_sig_not_match3.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg10)
    public void testMethodSignatureNotMatch4() {
        String path = "test-src/javainterop/negative/method_sig_not_match4.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg11)
    public void testMethodSignatureNotMatch5() {
        String path = "test-src/javainterop/negative/method_sig_not_match5.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg12)
    public void testMethodSignatureNotMatch6() {
        String path = "test-src/javainterop/negative/method_sig_not_match6.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg13)
    public void testMethodSignatureNotMatch7() {
        String path = "test-src/javainterop/negative/method_sig_not_match7.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg14)
    public void testMethodSignatureNotMatch8() {
        String path = "test-src/javainterop/negative/method_sig_not_match8.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg15)
    public void testMethodSignatureNotMatch9() {
        String path = "test-src/javainterop/negative/method_sig_not_match9.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg16)
    public void testMethodSignatureNotMatch10() {
        String path = "test-src/javainterop/negative/method_sig_not_match10.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg17)
    public void testReturnStringForBUnionFromJava() {
        String path = "test-src/javainterop/negative/method_sig_not_match11.bal";
        BCompileUtil.compileInProc(path);
    }

    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg18)
    public void testJavaPrimitiveForBJsonParam() {
        String path = "test-src/javainterop/negative/method_sig_not_match12.bal";
        BCompileUtil.compileInProc(path);
    }
    
    @Test(expectedExceptions = BLangCompilerException.class, expectedExceptionsMessageRegExp = expectedMsg19)
    public void testJavaPrimitiveForBUnionParam() {
        String path = "test-src/javainterop/negative/method_sig_not_match13.bal";
        BCompileUtil.compileInProc(path);
    }
}
