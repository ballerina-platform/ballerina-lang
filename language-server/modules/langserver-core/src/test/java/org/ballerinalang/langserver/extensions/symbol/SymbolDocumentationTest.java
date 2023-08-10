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

import io.ballerina.compiler.api.symbols.SymbolKind;
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
        functionPos.setLine(16);
        functionPos.setCharacter(19);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(), "Adds two integers.");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "the sum of `x` and `y`");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "x");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "an integer");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(1).getName(), "y");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(1).getDescription(),
                "another integer");
        Assert.assertEquals(symbolInfoResponse.getSymbolKind(), SymbolKind.FUNCTION);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "documentation response generated for module function")
    public void testStandardLibSymbolDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(17);
        functionPos.setCharacter(20);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "This is function3 with input parameters\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "param1");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "param1 Parameter Description ");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getKind(), "REQUIRED");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getType(), "int");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "Return Value Description");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedParams(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedDocumentation(), null);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);

    }

    @Test(description = "empty documentation response")
    public void testEmptyDocumentationResponse() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(18);
        functionPos.setCharacter(20);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(), null);
        Assert.assertEquals(symbolInfoResponse.getSymbolKind(), SymbolKind.FUNCTION);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test deprecated params and description")
    public void testDeprecatedParams() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(19);
        functionPos.setCharacter(15);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedDocumentation(),
                "This function is deprecated in favour of `Person` type.");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedParams().get(0).getName(), "street");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDeprecatedParams().get(0).getDescription(),
                "deprecated for removal");
        Assert.assertEquals(symbolInfoResponse.getSymbolKind(), SymbolKind.FUNCTION);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test implicit-new-expression documentation")
    public void testImplicitNewExprDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(20);
        functionPos.setCharacter(27);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "Counter constructor.\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "num");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "Number to increment");
        Assert.assertEquals(symbolInfoResponse.getSymbolKind(), SymbolKind.METHOD);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test explicit-new-expression documentation")
    public void testExplicitNewExprDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(21);
        functionPos.setCharacter(28);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "File constructor.\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "path");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "Path of the file");
        Assert.assertEquals(symbolInfoResponse.getSymbolKind(), SymbolKind.METHOD);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test implicit-new-expression documentation without init()")
    public void testImplicitNewExprDocumentationWithoutInit() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(22);
        functionPos.setCharacter(19);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertEquals(symbolInfoResponse.getDocumentation(), null);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test lang-lib documentation with filtered first param")
    public void testLangLibDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(23);
        functionPos.setCharacter(22);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "Returns the length of the string.\n" +
                        "\n" +
                        "```ballerina\n" +
                        "\"Hello, World!\".length() ⇒ 13;\n" +
                        "```\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "the number of characters (code points) in parameter `str`");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters(), null);
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test lang-lib documentation with qualified identifier")
    public void testLangLibDocumentationWithQualifiedIdentifier() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(24);
        functionPos.setCharacter(26);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "Returns the length of the string.\n" +
                        "\n" +
                        "```ballerina\n" +
                        "\"Hello, World!\".length() ⇒ 13;\n" +
                        "```\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "the number of characters (code points) in parameter `str`");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "str");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "the string");
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test documentation for remote-method-calls")
    public void testDocumentationForRemoteActionMethodCall() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(26);
        functionPos.setCharacter(30);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertNotEquals(symbolInfoResponse.getDocumentation(), null);
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getDescription(),
                "Set the Id of the student\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getReturnValueDescription(),
                "Student id");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getName(), "id");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().getParameters().get(0).getDescription(),
                "Id of the student");
        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

}
