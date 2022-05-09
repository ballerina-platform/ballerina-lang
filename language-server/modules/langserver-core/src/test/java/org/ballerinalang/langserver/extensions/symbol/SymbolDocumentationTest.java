/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.symbol;

import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.symbol.SymbolInfoResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test documentation info generated via getSymbol endpoint.
 */
public class SymbolDocumentationTest {
    private Endpoint serviceEndpoint;

    private final Path symbolDocumentBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("symbolDocumentation.bal");

    @BeforeClass
    public void startLangServer() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "documentation response generated for user defined documentation")
    public void testUserDefinedSymbolDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(15);
        functionPos.setCharacter(19);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(), "Adds two integers.");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "the sum of `x` and `y`");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).name, "x");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).description, "an integer");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(1).name, "y");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(1).description,
                "another integer");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "documentation response generated for module function")
    public void testStandardLibSymbolDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(16);
        functionPos.setCharacter(20);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "This is function3 with input parameters\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).name, "param1");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).description,
                "param1 Parameter Description ");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).kind, "REQUIRED");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).type, "int");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "Return Value Description");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedParams(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedDocumentation(), null);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);

    }
}
