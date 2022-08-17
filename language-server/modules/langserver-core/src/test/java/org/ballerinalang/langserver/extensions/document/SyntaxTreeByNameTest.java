/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.document;

import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test syntaxTreeByName call.
 * @since 2201.3.0
 */
public class SyntaxTreeByNameTest {
    private Endpoint serviceEndpoint;

    private final Path projectFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("balProject")
            .resolve("main.bal");

    private final Path sameFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainFunction.bal");

    private final Path incorrectFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("non-exist.bal");

    @BeforeClass
    public void startLanguageServer() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Find ST by range in a file")
    public void testFindFunctionInSameFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sameFile);
        Range range = new Range(new Position(4, 13), new Position(4, 26));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                sameFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSource());
        Assert.assertNotNull(syntaxTreeByNameResponse.getDefFilePath());
        TestUtil.closeDocument(this.serviceEndpoint, sameFile);
    }

    @Test(description = "Find ST by range in a project")
    public void testFindFunctionInProject() throws Exception {
        TestUtil.openDocument(serviceEndpoint, projectFile);
        Range range = new Range(new Position(3, 12), new Position(3, 21));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                projectFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSource());
        Assert.assertNotNull(syntaxTreeByNameResponse.getDefFilePath());
        TestUtil.closeDocument(this.serviceEndpoint, projectFile);
    }

    @Test(description = "Find ST by range in a module")
    public void testFindFunctionInModule() throws Exception {
        TestUtil.openDocument(serviceEndpoint, projectFile);
        Range range = new Range(new Position(4, 11), new Position(4, 19));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                projectFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSource());
        Assert.assertNotNull(syntaxTreeByNameResponse.getDefFilePath());
        TestUtil.closeDocument(this.serviceEndpoint, projectFile);
    }

    @Test(description = "Find ST by incorrect range in a file")
    public void testNotFindFunctionInSameFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sameFile);
        Range range = new Range(new Position(6, 13), new Position(6, 26));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                sameFile.toString(), range, this.serviceEndpoint);
        Assert.assertFalse(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNull(syntaxTreeByNameResponse.getSource());
        Assert.assertNull(syntaxTreeByNameResponse.getDefFilePath());
        TestUtil.closeDocument(this.serviceEndpoint, sameFile);
    }

    @Test(description = "Exception for incorrect file path")
    public void testExceptionForIncorrectFilePath() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sameFile);
        Range range = new Range(new Position(6, 13), new Position(6, 26));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                incorrectFile.toString(), range, this.serviceEndpoint);
        Assert.assertFalse(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNull(syntaxTreeByNameResponse.getSource());
        Assert.assertNull(syntaxTreeByNameResponse.getDefFilePath());
        TestUtil.closeDocument(this.serviceEndpoint, sameFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
