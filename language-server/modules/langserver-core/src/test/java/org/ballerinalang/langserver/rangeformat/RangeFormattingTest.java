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
package org.ballerinalang.langserver.rangeformat;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Test suit for range-formatting.
 */
public class RangeFormattingTest {
    private final Path formattingDirectory = FileUtils.RES_DIR.resolve("rangeformat");
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void loadLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "test formatting functionality on functions")
    public void formatTestSuit() throws IOException {
        Path expectedFilePath = formattingDirectory.resolve("expected.bal");
        Path inputFilePath = formattingDirectory.resolve("source.bal");

        String expected = new String(Files.readAllBytes(expectedFilePath));
        expected = expected.replaceAll("\\r\\n", "\n");
        DocumentRangeFormattingParams params = new DocumentRangeFormattingParams();
        Position start = new Position(1, 4);
        Position end = new Position(3, 29);
        Range range = new Range(start, end);

        TextDocumentIdentifier textDocumentIdentifier1 = new TextDocumentIdentifier();
        textDocumentIdentifier1.setUri(Path.of(inputFilePath.toString()).toUri().toString());

        FormattingOptions formattingOptions = new FormattingOptions();
        formattingOptions.setInsertSpaces(true);
        formattingOptions.setTabSize(4);

        params.setOptions(formattingOptions);
        params.setTextDocument(textDocumentIdentifier1);
        params.setRange(range);

        TestUtil.openDocument(this.serviceEndpoint, inputFilePath);

        String result = TestUtil.getRangeFormatResponse(params, this.serviceEndpoint);
        Gson gson = new Gson();
        ResponseMessage responseMessage = gson.fromJson(result, ResponseMessage.class);
        String actual = (String) ((LinkedTreeMap<?, ?>) ((List<?>) responseMessage.getResult()).get(0)).get("newText");
        actual = actual.replaceAll("\\r\\n", "\n");
        TestUtil.closeDocument(this.serviceEndpoint, inputFilePath);
        Assert.assertEquals(actual, expected);
    }

    @AfterClass
    public void shutdownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
