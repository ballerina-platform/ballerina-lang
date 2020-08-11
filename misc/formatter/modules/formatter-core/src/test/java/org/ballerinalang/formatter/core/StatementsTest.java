/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Test formatting for statements.
 *
 * @since 2.0.0
 */
public class StatementsTest {

    private void testFile(String sourceFilePath, String filePath) throws IOException {
        FormatterTestUtils.test(Paths.get("statements/", sourceFilePath), Paths.get("statements/", filePath));
    }

    @Test(description = "Test the formatting of action statements")
    public void testActionStatement() throws IOException {
        testFile("source/action-statement.bal", "expected/action-statement.bal");
    }

    @Test(description = "Test the formatting of assignment statements")
    public void testAssignmentStatement() throws IOException {
        testFile("source/assignment-statement.bal", "expected/assignment-statement.bal");
    }

    @Test(description = "Test the formatting of local type definition statements")
    public void testLocalTypeDefStatement() throws IOException {
        testFile("source/local-type-def-statement.bal", "expected/local-type-def-statement.bal");
    }

    @Test(description = "Test the formatting of block statements")
    public void testBlockStatement() throws IOException {
        testFile("source/block-statement.bal", "expected/block-statement.bal");
    }

    @Test(description = "Test the formatting of local variable declaration statements")
    public void testLocalVarDeclStatement() throws IOException {
        testFile("source/local-var-decl-statement.bal", "expected/local-var-decl-statement.bal");
    }

    @Test(description = "Test the formatting of XML namespace declaration statements")
    public void testXMLNSDeclStatement() throws IOException {
        testFile("source/xml-ns-decl-statement.bal", "expected/xml-ns-decl-statement.bal");
    }

    @Test(description = "Test the formatting of compound assignment statements")
    public void testCompoundAssignmentStatement() throws IOException {
        testFile("source/compound-assignment-statement.bal", "expected/compound-assignment-statement.bal");
    }

    @Test(description = "Test the formatting of destructuring assignment statements")
    public void testDestructuringAssignmentStatement() throws IOException {
        testFile("source/destructuring-assignment-statement.bal", "expected/destructuring-assignment-statement.bal");
    }

    @Test(description = "Test the formatting of call statements")
    public void testCallStatement() throws IOException {
        testFile("source/call-statement.bal", "expected/call-statement.bal");
    }

    @Test(description = "Test the formatting of if else statements")
    public void testIfElseStatement() throws IOException {
        testFile("source/if-else-statement.bal", "expected/if-else-statement.bal");
    }

    @Test(description = "Test the formatting of match statements")
    public void testMatchStatement() throws IOException {
        testFile("source/match-statement.bal", "expected/match-statement.bal");
    }

    @Test(description = "Test the formatting of foreach statements")
    public void testForeachStatement() throws IOException {
        testFile("source/foreach-statement.bal", "expected/foreach-statement.bal");
    }

    @Test(description = "Test the formatting of while statements")
    public void testWhileStatement() throws IOException {
        testFile("source/while-statement.bal", "expected/while-statement.bal");
    }

    @Test(description = "Test the formatting of break statements")
    public void testBreakStatement() throws IOException {
        testFile("source/break-statement.bal", "expected/break-statement.bal");
    }

    @Test(description = "Test the formatting of continue statements")
    public void testContinueStatement() throws IOException {
        testFile("source/continue-statement.bal", "expected/break-statement.bal");
    }

    @Test(description = "Test the formatting of fork statements")
    public void testForkStatement() throws IOException {
        testFile("source/fork-statement.bal", "expected/fork-statement.bal");
    }

    @Test(description = "Test the formatting of panic statements")
    public void testPanicStatement() throws IOException {
        testFile("source/panic-statement.bal", "expected/panic-statement.bal");
    }

    @Test(description = "Test the formatting of return statements")
    public void testReturnStatement() throws IOException {
        testFile("source/return-statement.bal", "expected/return-statement.bal");
    }

    @Test(description = "Test the formatting of lock statements")
    public void testLockStatement() throws IOException {
        testFile("source/lock-statement.bal", "expected/lock-statement.bal");
    }
}
