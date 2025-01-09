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
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
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
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 12);
    }

    @Test
    public void testClassNotFound() {
        String path = "test-src/javainterop/negative/class_not_found.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}CLASS_NOT_FOUND 'org.ballerinalang.nativeimpl.jvm." +
                        "tests.PublicStaticMethods'", "class_not_found.bal", 25, 1);
    }

    @Test
    public void testMethodNotFound1() {
        String path = "test-src/javainterop/negative/method_not_found1.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public method " +
                        "'acceptStringOrErrorReturn' found in class 'org.ballerinalang.nativeimpl." +
                        "jvm.tests.StaticMethods''", "method_not_found1.bal", 8, 1);
    }

    @Test
    public void testMethodNotFound2() {
        String path = "test-src/javainterop/negative/method_not_found2.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public static method " +
                        "'acceptObjectAndObjectReturn' with '3' " + "parameter(s) found in class '" +
                        "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found2.bal", 22, 1);
    }

    @Test
    public void testMethodNotFound3() {
        String path = "test-src/javainterop/negative/method_not_found3.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public static method " +
                        "'acceptRecordAndRecordReturn' with '3' " + "parameter(s) found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''",
                "method_not_found3.bal", 21, 1);
    }

    @Test
    public void testMethodNotFound4() {
        String path = "test-src/javainterop/negative/method_not_found4.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public method " +
                        "'acceptIntAndUnionReturn' found in class 'org.ballerinalang.nativeimpl.jvm." +
                        "tests.StaticMethods''", "method_not_found4.bal", 23, 1);
    }

    @Test
    public void testMethodNotFound5() {
        String path = "test-src/javainterop/negative/method_not_found5.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public method " +
                        "'acceptIntStringAndUnionReturn' found in class 'org.ballerinalang.nativeimpl." +
                        "jvm.tests.StaticMethods''", "method_not_found5.bal", 23, 1);
    }

    @Test
    public void testMethodNotFound6() {
        String testFileName = "method_not_found6.bal";
        String path = "test-src/javainterop/negative/" + testFileName;
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 4);
        String message = "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public method '%s' that matches with " +
                "parameter types '(%s)' found in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods''";

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
    public void testMethodNotFound7() {
        String path = "test-src/javainterop/negative/method_not_found7.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 4);
        String message = "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public static method '%s' with " +
                "'%s' parameter(s) found in class '%s''";
        BAssertUtil.validateError(compileResult, 0, String.format(message, "getPrintableStackTrace", "1",
                        "io.ballerina.runtime.api.values.BError"), "method_not_found7.bal", 19, 1);
        BAssertUtil.validateError(compileResult, 1, String.format(message, "concat", "2",
                        "io.ballerina.runtime.api.values.BString"), "method_not_found7.bal", 23, 1);
        BAssertUtil.validateError(compileResult, 2,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public method 'concat' " +
                        "with '3' parameter(s) found in class 'io.ballerina.runtime.api.values.BString''",
                "method_not_found7.bal", 27, 1);
        BAssertUtil.validateError(compileResult, 3,
                "{ballerina/jballerina.java}METHOD_NOT_FOUND 'No such public static method 'indexOf' " +
                        "with '0' parameter(s) found in class 'java.lang.String''",
                "method_not_found7.bal", 32, 1);
    }

    @Test
    public void testConstructorNotFound() {
        String path = "test-src/javainterop/negative/constructor_not_found.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 3);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}CONSTRUCTOR_NOT_FOUND 'No such public constructor found " +
                        "in class 'org.ballerinalang.nativeimpl.jvm.tests.ClassWithPrivateConstructor''",
                "constructor_not_found.bal", 19, 1);
        BAssertUtil.validateError(compileResult, 1,
                "{ballerina/jballerina.java}CONSTRUCTOR_NOT_FOUND " +
                "'No such public constructor with '2' parameter(s) found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.ClassWithOneParamConstructor''",
                "constructor_not_found.bal", 23, 1);
        BAssertUtil.validateError(compileResult, 2,
                "{ballerina/jballerina.java}CONSTRUCTOR_NOT_FOUND " +
                "'No such public constructor that matches with parameter types '(int)' found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.ClassWithDefaultConstructor''",
                "constructor_not_found.bal", 27, 1);
    }

    @Test
    public void testMethodSignatureNotMatch1() {
        String path = "test-src/javainterop/negative/method_sig_not_match1.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptIntReturnIntThrowsCheckedException' which throws checked exception found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': expected 'int|error', found 'int''",
                "method_sig_not_match1.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch2() {
        String path = "test-src/javainterop/negative/method_sig_not_match2.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptRecordAndRecordReturnWhichThrowsCheckedException' which throws checked exception " +
                        "found in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'Employee|error', found 'Employee''",
                "method_sig_not_match2.bal", 7, 1);
    }

    @Test
    public void testMethodSignatureNotMatch3() {
        String path = "test-src/javainterop/negative/method_sig_not_match3.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptIntUnionReturnWhichThrowsCheckedException' which throws checked exception found in " +
                        "class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected '(int|string|float|boolean)|error', found '(int|string|float|boolean)''",
                "method_sig_not_match3.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch4() {
        String path = "test-src/javainterop/negative/method_sig_not_match4.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptRefTypesAndReturnMapWhichThrowsCheckedException' which throws checked exception " +
                        "found in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'map|error', found 'map''",
                "method_sig_not_match4.bal", 14, 1);
    }

    @Test
    public void testMethodSignatureNotMatch5() {
        String path = "test-src/javainterop/negative/method_sig_not_match5.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptStringErrorReturnWhichThrowsCheckedException' which throws checked exception found " +
                        "in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'error', found '()''",
                "method_sig_not_match5.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch6() {
        String path = "test-src/javainterop/negative/method_sig_not_match6.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'getArrayValueFromMapWhichThrowsCheckedException' which throws checked exception found in " +
                        "class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'int[]|error', found 'int[]''",
                "method_sig_not_match6.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch7() {
        String path = "test-src/javainterop/negative/method_sig_not_match15.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptIntErrorUnionReturnWhichThrowsCheckedException' which throws checked exception " +
                        "found in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'int|error', found 'int''",
                "method_sig_not_match15.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch8() {
        CompileResult compileResult = BCompileUtil.compile("test-src/javainterop/negative/distinct_error");
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateError(compileResult, 0,
        "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                "'Incompatible ballerina return type for Java method " +
                "'returnDistinctErrorUnionWhichThrowsCheckedException' which throws checked exception " +
                "found in class 'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                "expected 'int|error', found '(int|testorg/distinct_error.errors:1.0.0:DistinctError)''",
                21, 1);
    }

    @Test
    public void testMethodSignatureNotMatch9() {
        String path = "test-src/javainterop/negative/method_sig_not_match7.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param " +
                        "type for method 'split' in class 'java.lang.String': Java type 'java.lang.String' will " +
                        "not be matched to ballerina type 'string''", "method_sig_not_match7.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch10() {
        String path = "test-src/javainterop/negative/method_sig_not_match8.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Parameter count does " +
                        "not match with Java method 'split' found in class 'java.lang.String''",
                "method_sig_not_match8.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch11() {
        String path = "test-src/javainterop/negative/method_sig_not_match9.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param " +
                        "type for method 'decimalParamAsObjectAndReturn' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.Object' " +
                        "will not be matched to ballerina type 'decimal''",
                "method_sig_not_match9.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch12() {
        String path = "test-src/javainterop/negative/method_sig_not_match10.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return " +
                        "type for method 'decimalParamAndReturnAsObject' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.Object' " +
                        "will not be matched to ballerina type 'decimal''",
                "method_sig_not_match10.bal", 3, 1);
    }

    @Test
    public void testReturnStringForBUnionFromJava() {
        String path = "test-src/javainterop/negative/method_sig_not_match11.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible return " +
                        "type for method 'returnStringForBUnionFromJava' in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': Java type 'java.lang.String' " +
                        "will not be matched to ballerina type '(int|float|string)''",
                "method_sig_not_match11.bal", 3, 1);
    }

    @Test
    public void testJavaPrimitiveForBJsonParam() {
        String path = "test-src/javainterop/negative/method_sig_not_match12.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param " +
                        "type for method 'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm." +
                        "tests.StaticMethods': Java type 'int' will not be matched to ballerina type 'json''",
                "method_sig_not_match12.bal", 3, 1);
    }

    @Test
    public void testJavaPrimitiveForBUnionParam() {
        String path = "test-src/javainterop/negative/method_sig_not_match13.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param " +
                        "type for method 'getIntFromJsonInt' in class 'org.ballerinalang.nativeimpl.jvm.tests." +
                        "StaticMethods': Java type 'int' will not be matched to ballerina type '(int|string)''",
                "method_sig_not_match13.bal", 3, 1);
    }

    @Test
    public void testMethodSignatureNotMatch14() {
        String path = "test-src/javainterop/negative/method_sig_not_match14.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(
                compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH 'Incompatible param type for method " +
                        "'decimalParamAndWithBigDecimal' in class 'org.ballerinalang.nativeimpl.jvm.tests" +
                        ".StaticMethods': Java type 'java.math.BigDecimal' will not be matched to ballerina type " +
                        "'decimal''", "method_sig_not_match14.bal", 3, 1);
    }

    @Test(description = "When there are instance and static methods with same name and parameters that differ by one")
    public void testResolveWithInstanceAndStatic() {
        String path = "test-src/javainterop/negative/method_resolve_error.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 8);
        BAssertUtil.validateError(compileResult, 0,
                                  "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods " +
                                          "cannot be differentiated. Please specify the parameter types for each " +
                                          "parameter in 'paramTypes' field in the annotation'",
                "method_resolve_error.bal", 19, 1);
        BAssertUtil.validateError(compileResult, 1,
                                  "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods " +
                                          "cannot be differentiated. Please specify the parameter types for each " +
                                          "parameter in 'paramTypes' field in the annotation'",
                "method_resolve_error.bal", 24, 1);
        BAssertUtil.validateError(compileResult, 2,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods cannot be " +
                        "differentiated. Please specify the parameter types for each parameter in 'paramTypes' " +
                        "field in the annotation'", "method_resolve_error.bal", 29, 1);
        BAssertUtil.validateError(compileResult, 3,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods cannot be " +
                        "differentiated. Please specify the parameter types for each parameter in 'paramTypes' " +
                        "field in the annotation'", "method_resolve_error.bal", 33, 1);
        BAssertUtil.validateError(compileResult, 4,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods 'getDescription' " +
                        "with '1' parameter(s) in class " +
                        "'org.ballerinalang.test.javainterop.overloading.pkg.SportsCar', please specify the " +
                        "parameter types for each parameter in 'paramTypes' field in the annotation'",
                "method_resolve_error.bal", 38, 1);
        BAssertUtil.validateError(compileResult, 5,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded constructors with '1' " +
                        "parameter(s) in class 'java.lang.Byte', please specify the parameter types for each " +
                        "parameter in 'paramTypes' field in the annotation'",
                "method_resolve_error.bal", 42, 1);
        BAssertUtil.validateError(compileResult, 6,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods " +
                        "'getCategorization' with '2' parameter(s) in class " +
                        "'org.ballerinalang.test.javainterop.overloading.pkg.SportsCar', please specify the " +
                        "parameter types for each parameter in 'paramTypes' field in the annotation'",
                "method_resolve_error.bal", 46, 1);
    }

    @Test
    public void testOverloadedMethods() {
        String path = "test-src/javainterop/negative/overloaded_methods.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 3);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods cannot be " +
                        "differentiated. Please specify the parameter types for each parameter in 'paramTypes' field " +
                        "in the annotation'", "overloaded_methods.bal", 24, 5);
        BAssertUtil.validateError(compileResult, 1,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods cannot be " +
                        "differentiated. Please specify the parameter types for each parameter in 'paramTypes' field " +
                        "in the annotation'", "overloaded_methods.bal", 30, 1);
        BAssertUtil.validateError(compileResult, 2,
                "{ballerina/jballerina.java}OVERLOADED_METHODS 'Overloaded methods 'getPrice' with '1' " +
                        "parameter(s) in class 'org.ballerinalang.test.javainterop.overloading.pkg.SportsCar', " +
                        "please specify the parameter types for each parameter in 'paramTypes' " +
                        "field in the annotation'", "overloaded_methods.bal", 35, 1);
    }

    @Test
    public void testNoClassDefFoundError() {
        String path = "test-src/javainterop/negative/project_no_class_def_found";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}NO_CLASS_DEF_FOUND 'Class definition 'javalibs/app/Bar' " +
                        "not found'", 19, 1);
    }

    @Test
    public void testNoClassDefFoundErrorForConstructorCalls() {
        String path = "test-src/javainterop/negative/project_no_class_def_found_constructor";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}NO_CLASS_DEF_FOUND 'Class definition 'javalibs/app/Bar' " +
                        "not found'", 19, 1);
    }

    @Test(description = "Test error in instance field set without exactly two parameters")
    public void testInstanceFieldSetWithoutTwoParameters() {
        String path = "test-src/javainterop/negative/fieldset_error1.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0, "{ballerina/jballerina.java}INVALID NUMBER OF PARAMETERS "
                + "'Two parameters are required to set the value to the instance field 'isEmpty' in class " +
                "'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test(description = "Test error in instance field set with no handle type first parameter")
    public void testNotHandleTypeFirstParameterForInstanceFieldSet() {
        String path = "test-src/javainterop/negative/fieldset_error2.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0, "{ballerina/jballerina.java}INVALID " +
                "PARAMETER TYPE 'First parameter needs to be of the handle type to set the value to the instance " +
                "field 'isEmpty' in class 'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test(description = "Test error in instance field get without exactly one parameter")
    public void testInstanceFieldGetWithoutOneParameter() {
        String path = "test-src/javainterop/negative/fieldget_error1.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateWarning(compileResult, 0, "unused variable 'ans'", 4, 5);
        BAssertUtil.validateError(compileResult, 1, "{ballerina/jballerina.java}INVALID NUMBER OF PARAMETERS "
                + "'One parameter is required to get the value of the instance field 'isEmpty' in class " +
                "'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test(description = "Test error in instance field get with no handle type parameter")
    public void testNoHandleTypeParameterForInstanceFieldGet() {
        String path = "test-src/javainterop/negative/fieldget_error2.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateWarning(compileResult, 0, "unused variable 'ans'", 4, 5);
        BAssertUtil.validateError(compileResult, 1, "{ballerina/jballerina.java}" +
                "INVALID PARAMETER TYPE 'The parameter needs to be of the handle type to get the value of the instance "
                + "field 'isEmpty' in class 'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test(description = "Test error in static field set without exactly one parameter")
    public void testStaticFieldSetWithoutOneParameter() {
        String path = "test-src/javainterop/negative/fieldset_error3.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0, "{ballerina/jballerina.java}INVALID NUMBER OF PARAMETERS "
                + "'One parameter is required to set the value to the static field 'isAvailable' in class " +
                "'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test(description = "Test error in static field get with any parameter")
    public void testStaticFieldGetWithAnyParameter() {
        String path = "test-src/javainterop/negative/fieldget_error3.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateWarning(compileResult, 0, "unused variable 'ans'", 4, 5);
        BAssertUtil.validateError(compileResult, 1, "{ballerina/jballerina.java}INVALID NUMBER OF PARAMETERS "
                + "'No parameter is required to get the value of the static field 'isAvailable' in class " +
                "'org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate''", 7, 1);
    }

    @Test
    public void testMethodSignatureNotMatch16() {
        String path = "test-src/javainterop/negative/method_sig_not_match16.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptIntErrorUnionReturnWhichThrowsUncheckedException' which throws " +
                        "'java.lang.RuntimeException' found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "expected 'int', found '(int|error)''",
                "method_sig_not_match16.bal", 19, 1);
    }

    @Test
    public void testMethodSignatureNotMatch17() {
        String path = "test-src/javainterop/negative/method_sig_not_match17.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        compileResult.getDiagnostics();
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0,
                "{ballerina/jballerina.java}METHOD_SIGNATURE_DOES_NOT_MATCH " +
                        "'Incompatible ballerina return type for Java method " +
                        "'acceptIntErrorUnionReturnWhichThrowsUncheckedException' which throws " +
                        "'java.lang.RuntimeException' found in class " +
                        "'org.ballerinalang.nativeimpl.jvm.tests.StaticMethods': " +
                        "no return type expected but found 'error''",
                "method_sig_not_match17.bal", 19, 1);
    }

    @Test
    public void testAbstractClassInstantiationError() {
        String path = "test-src/javainterop/negative/instantiation_error.bal";
        CompileResult compileResult = BCompileUtil.compile(path);
        Assert.assertEquals(compileResult.getDiagnostics().length, 2);
        BAssertUtil.validateError(compileResult, 0, "{ballerina/jballerina.java}INSTANTIATION_ERROR" +
                        " ''org.ballerinalang.nativeimpl.jvm.tests.AbstractClass' is abstract, " +
                        "and cannot be instantiated'", 19, 1);
        BAssertUtil.validateError(compileResult, 1, "{ballerina/jballerina.java}INSTANTIATION_ERROR" +
                        " ''org.ballerinalang.nativeimpl.jvm.tests.Interface' is abstract, and cannot be instantiated'",
                23, 1);
    }
}
