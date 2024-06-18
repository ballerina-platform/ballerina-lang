/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.document;

import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.SyntaxApiCallsResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test syntaxApiCallsGen methods.
 *
 * @since 2.0.0
 */
public class SyntaxApiCallsGenTest {
    private final Path syntaxApiCallsDir = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxApiCalls");
    private final Path emptyFile = syntaxApiCallsDir.resolve("empty.bal");
    private final Path mainFile = syntaxApiCallsDir.resolve("main.bal");
    private final Path mainFileWoMinutiaeResult = syntaxApiCallsDir.resolve("main.no.minutiae.txt");
    private final Path mainFileResult = syntaxApiCallsDir.resolve("main.result.txt");
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Request for an empty Ballerina file")
    public void testEmptySourceFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, emptyFile);
        SyntaxApiCallsResponse response = LSExtensionTestUtil
                .getBallerinaSyntaxApiCalls(emptyFile.toString(), false, this.serviceEndpoint);
        Assert.assertTrue(response.isParseSuccess());
        Assert.assertEquals(response.getSource(), "");
        Assert.assertEquals(response.getCode(), """
                NodeFactory.createModulePartNode(
                    NodeFactory.createNodeList(),
                    NodeFactory.createNodeList(),
                    NodeFactory.createToken(SyntaxKind.EOF_TOKEN)
                )""");
        TestUtil.closeDocument(this.serviceEndpoint, emptyFile);
    }

    @Test(description = "Request for a sample Ballerina file")
    public void testSampleBallerinaFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        SyntaxApiCallsResponse response = LSExtensionTestUtil
                .getBallerinaSyntaxApiCalls(mainFile.toString(), false, this.serviceEndpoint);
        String mainFileContent = new String(Files.readAllBytes(mainFile));
        String mainFileResultContent = new String(Files.readAllBytes(mainFileResult));
        Assert.assertTrue(response.isParseSuccess());
        Assert.assertEquals(replaceLineEnds(response.getSource()), replaceLineEnds(mainFileContent));
        Assert.assertEquals(replaceLineEnds(response.getCode().trim()), replaceLineEnds(mainFileResultContent.trim()));
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @Test(description = "Request for a sample Ballerina file without minutiae")
    public void testSampleBallerinaFileWithoutMinutiae() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        SyntaxApiCallsResponse response = LSExtensionTestUtil
                .getBallerinaSyntaxApiCalls(mainFile.toString(), true, this.serviceEndpoint);
        String mainFileContent = new String(Files.readAllBytes(mainFile));
        String mainFileResultContent = new String(Files.readAllBytes(mainFileWoMinutiaeResult));
        Assert.assertTrue(response.isParseSuccess());
        Assert.assertEquals(replaceLineEnds(response.getSource()), replaceLineEnds(mainFileContent));
        Assert.assertEquals(replaceLineEnds(response.getCode().trim()), replaceLineEnds(mainFileResultContent.trim()));
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    private String replaceLineEnds(String source) {
        return source.replace("\r\n", "\n").replace("\\r\\n", "\\n");
    }
}
