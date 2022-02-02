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

package org.ballerinalang.test.functions;

import io.ballerina.runtime.api.types.ObjectType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Test nil return from functions.
 */
public class FunctionNilReturnTest {

    private CompileResult compileResult;
    private PrintStream original;
    private static final String EXPECTED_OUTPUT = "Hello\n\nBallerina\n";
    private static final String WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE =
            "this function should explicitly return a value";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = System.out;
        compileResult = BCompileUtil.compile("test-src/functions/function-nil-return.bal");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        System.setOut(original);
    }

    @Test
    public void testPrint1() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrint1", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint2() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrint2", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint3() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrint3", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrint4() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrint4", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintInMatchBlock() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrintInMatchBlock", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testPrintInWorkers() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testPrintInWorkers", new ObjectType[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), EXPECTED_OUTPUT);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testNoReturnFuncInvocnInNilReturnFuncRetStmt() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testNoReturnFuncInvocnInNilReturnFuncRetStmt", new Object[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), "nil returns here\nno returns here\n\n");
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testNilReturnFuncInvocnInNilReturnFuncRetStmt() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            JvmRunUtil.invoke(compileResult, "testNilReturnFuncInvocnInNilReturnFuncRetStmt", new Object[0]);
            Assert.assertEquals(outputStream.toString().replace("\r", ""), "nil returns here\nexplicit nil returns" +
                    " here\n\n");
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testReturnsDuringValidCheck() {
        Object returns = JvmRunUtil.invoke(compileResult, "testReturnsDuringValidCheck");
        Assert.assertNull(returns);
    }

    @Test
    public void testMissingReturnInIfElse() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testMissingReturnInIfElse"));
    }

    @Test
    public void testMissingReturnInNestedIfElse() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testMissingReturnInNestedIfElse"));
    }

    @Test
    public void testReturningInMatch() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testReturningInMatch"));
    }

    @Test
    public void testValidCheckWithExplicitReturn() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testValidCheckWithExplicitReturn"));
    }

    @Test
    public void testEmptyFunctionsWithNilableReturns() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableInt"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableFloat"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableDecimal"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableString"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableBoolean"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableByte"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableJSON"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableXML"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableMap"));

        // Arrays
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableIntArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableFloatArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableDecimalArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableStringArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableBooleanArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableByteArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableJSONArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableXMLArray"));
    }

    @Test
    public void testEmptyFunctionsWithComplexNilableReturns() {
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableOpenRecord"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableClosedRecord"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableObject"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableUnion"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableTuple"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableTypedesc"));

        // Arrays
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableOpenRecordArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableClosedRecordArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableObjectArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableUnionArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableTupleArray"));
        Assert.assertNull(JvmRunUtil.invoke(compileResult, "testNilableTypedescArray"));
    }

    @Test
    public void testWarningsForNotExplicitlyReturningAValue() {
        int i = 0;
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 97, 46);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 105, 52);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 120, 41);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 138, 35);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 141, 37);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 144, 39);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 147, 38);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 150, 39);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 153, 36);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 156, 36);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 162, 35);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 165, 35);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 171, 40);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 174, 42);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 177, 44);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 180, 43);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 183, 44);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 186, 41);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 189, 41);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 192, 40);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 203, 42);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 206, 47);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 214, 44);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 217, 49);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 224, 38);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 227, 43);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 230, 37);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 233, 42);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 236, 37);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 239, 42);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 242, 40);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 245, 45);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 253, 28);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 257, 59);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 265, 35);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 274, 35);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 291, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 294, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 300, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 303, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 306, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 312, 84);
        validateWarning(compileResult, i++, WARN_SHOULD_EXPLICITLY_RETURN_A_VALUE, 315, 84);
        Assert.assertEquals(compileResult.getWarnCount(), i);

    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
