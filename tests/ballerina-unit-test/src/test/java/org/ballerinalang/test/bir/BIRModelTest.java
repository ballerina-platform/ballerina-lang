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
package org.ballerinalang.test.bir;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This class contains unit tests to cover AST to BIR lowering.
 *
 * @since 0.980.0
 */
public class BIRModelTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeClass
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @Test(description = "Test AST to BIR lowering")
    public void testBIRGen() {
        CompileResult result = BCompileUtil.compileAndDumpBir("test-src/bir/bir_model_dump.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertEquals(outContent.toString(), "    bir_model_dump.bal\n"
                + "################################# Begin bir program #################################\n"
                + "\n"
                + "package $anon/.;\n"
                + "\n"
                + "// Import Declarations\n"
                + "\n"
                + "// Type Definitions\n"
                + "\n"
                + "// Global Variables\n"
                + "PACKAGE_PRIVATE map<any> #0;\n"
                + "\n"
                + "// Function Definitions\n"
                + "PUBLIC function ..<init> () -> () {\n"
                + "\tmap<any> %ret\t// TEMP\n"
                + "\n"
                + "\tbb0 {\n"
                + "\t\t%ret = NEW_MAP map<any>;\n"
                + "\t\t#0 = MOVE %ret;\n"
                + "\t\tgoto bb1;\n"
                + "\t}\n"
                + "\n"
                + "\tbb1 {\n"
                + "\t\treturn;\n"
                + "\t}\n"
                + "\n"
                + "}\n"
                + "\n"
                + "PACKAGE_PRIVATE function ifSimple () -> int {\n"
                + "\tint %ret\t// RETURN\n"
                + "\tint %1\t// LOCAL\n"
                + "\tint %2\t// TEMP\n"
                + "\tint %3\t// LOCAL\n"
                + "\tint %4\t// TEMP\n"
                + "\tint %5\t// TEMP\n"
                + "\tint %6\t// TEMP\n"
                + "\tboolean %7\t// TEMP\n"
                + "\tint %8\t// TEMP\n"
                + "\tint %9\t// TEMP\n"
                + "\tint %10\t// TEMP\n"
                + "\tint %11\t// TEMP\n"
                + "\tint %12\t// TEMP\n"
                + "\tint %13\t// TEMP\n"
                + "\tint %14\t// TEMP\n"
                + "\n"
                + "\tbb0 {\n"
                + "\t\t%2 = CONST_LOAD <int> 100;\n"
                + "\t\t%1 = MOVE %2;\n"
                + "\t\t%4 = CONST_LOAD <int> 10;\n"
                + "\t\t%3 = MOVE %4;\n"
                + "\t\t%5 = MOVE %1;\n"
                + "\t\t%6 = MOVE %3;\n"
                + "\t\t%7 = EQUAL %5 %6;\n"
                + "\t\tbranch %7 [true:bb1, false:bb2];\n"
                + "\t}\n"
                + "\n"
                + "\tbb1 {\n"
                + "\t\t%8 = MOVE %3;\n"
                + "\t\t%ret = MOVE %8;\n"
                + "\t\tgoto bb4;\n"
                + "\t}\n"
                + "\n"
                + "\tbb2 {\n"
                + "\t\t%9 = MOVE %3;\n"
                + "\t\t%10 = CONST_LOAD <int> 1;\n"
                + "\t\t%11 = ADD %9 %10;\n"
                + "\t\t%1 = MOVE %11;\n"
                + "\t\tgoto bb3;\n"
                + "\t}\n"
                + "\n"
                + "\tbb3 {\n"
                + "\t\t%12 = MOVE %1;\n"
                + "\t\t%13 = MOVE %3;\n"
                + "\t\t%14 = MUL %12 %13;\n"
                + "\t\t%ret = MOVE %14;\n"
                + "\t\tgoto bb4;\n"
                + "\t}\n"
                + "\n"
                + "\tbb4 {\n"
                + "\t\treturn;\n"
                + "\t}\n"
                + "\n"
                + "}\n"
                + "\n"
                + "PUBLIC function main (int) -> () {\n"
                + "\tint %ret\t// ARG\n"
                + "\tint %1\t// LOCAL\n"
                + "\tint %2\t// TEMP\n"
                + "\tboolean %3\t// LOCAL\n"
                + "\tint %4\t// TEMP\n"
                + "\tint %5\t// TEMP\n"
                + "\tboolean %6\t// TEMP\n"
                + "\n"
                + "\tbb0 {\n"
                + "\t\t%2 = CONST_LOAD <int> 10;\n"
                + "\t\t%1 = MOVE %2;\n"
                + "\t\t%4 = MOVE %1;\n"
                + "\t\t%5 = CONST_LOAD <int> 100;\n"
                + "\t\t%6 = EQUAL %4 %5;\n"
                + "\t\t%3 = MOVE %6;\n"
                + "\t\tgoto bb1;\n"
                + "\t}\n"
                + "\n"
                + "\tbb1 {\n"
                + "\t\treturn;\n"
                + "\t}\n"
                + "\n"
                + "}\n"
                + "\n"
                + "PACKAGE_PRIVATE function genComplex (int, int) -> int {\n"
                + "\tint %ret\t// RETURN\n"
                + "\tint %1\t// ARG\n"
                + "\tint %2\t// ARG\n"
                + "\tint %3\t// LOCAL\n"
                + "\tint %4\t// TEMP\n"
                + "\tint %5\t// LOCAL\n"
                + "\tint %6\t// TEMP\n"
                + "\tint %7\t// TEMP\n"
                + "\tint %8\t// TEMP\n"
                + "\tint %9\t// LOCAL\n"
                + "\tint %10\t// TEMP\n"
                + "\tint %11\t// TEMP\n"
                + "\tint %12\t// TEMP\n"
                + "\tint %13\t// TEMP\n"
                + "\tint %14\t// TEMP\n"
                + "\tint %15\t// TEMP\n"
                + "\tint %16\t// TEMP\n"
                + "\tint %17\t// TEMP\n"
                + "\tint %18\t// TEMP\n"
                + "\tint %19\t// TEMP\n"
                + "\tint %20\t// TEMP\n"
                + "\n"
                + "\tbb0 {\n"
                + "\t\t%4 = CONST_LOAD <int> 10;\n"
                + "\t\t%3 = MOVE %4;\n"
                + "\t\t%6 = MOVE %3;\n"
                + "\t\t%7 = MOVE %1;\n"
                + "\t\t%8 = ADD %6 %7;\n"
                + "\t\t%5 = MOVE %8;\n"
                + "\t\t%10 = MOVE %3;\n"
                + "\t\t%11 = MOVE %5;\n"
                + "\t\t%12 = SUB %10 %11;\n"
                + "\t\t%13 = MOVE %2;\n"
                + "\t\t%14 = ADD %12 %13;\n"
                + "\t\t%9 = MOVE %14;\n"
                + "\t\t%15 = MOVE %5;\n"
                + "\t\t%16 = MOVE %9;\n"
                + "\t\t%17 = ADD %15 %16;\n"
                + "\t\t%5 = MOVE %17;\n"
                + "\t\t%18 = MOVE %3;\n"
                + "\t\t%19 = MOVE %5;\n"
                + "\t\t%20 = ADD %18 %19;\n"
                + "\t\t%ret = MOVE %20;\n"
                + "\t\tgoto bb1;\n"
                + "\t}\n"
                + "\n"
                + "\tbb1 {\n"
                + "\t\treturn;\n"
                + "\t}\n"
                + "\n"
                + "}\n"
                + "\n"
                + "################################## End bir program ##################################\n");
    }

    @AfterClass
    public void restoreStreams() {
        System.setOut(originalOut);
    }
}


