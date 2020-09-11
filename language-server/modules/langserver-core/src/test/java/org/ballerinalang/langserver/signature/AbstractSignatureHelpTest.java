/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Signature help.
 *
 * @since 0.982.0
 */
public abstract class AbstractSignatureHelpTest {

    private final String configDir = "config";

    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();

    private Path testRoot = new File(getClass().getClassLoader().getResource("signature").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(AbstractSignatureHelpTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "signature-help-data-provider", description = "Test Signature Help")
    public void test(String config, String source)
            throws WorkspaceDocumentException, IOException, InterruptedException {
        String configJsonPath =
                "signature" + File.separator + source + File.separator + configDir + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        expected.remove("id");
        String response = this.getSignatureResponse(configJsonObject, sourcePath).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        boolean result = expected.equals(responseJson);
        if (!result) {
            // Fix test cases replacing expected using responses
//            JsonObject obj = new JsonObject();
//            obj.add("position", configJsonObject.get("position"));
//            obj.add("expected", responseJson);
//            java.nio.file.Files.write(org.ballerinalang.langserver.util.FileUtils.RES_DIR.resolve(configJsonPath),
//                                      obj.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

            Assert.fail("Failed Test for: " + configJsonPath);
        }
    }

    @DataProvider(name = "signature-help-data-provider")
    public abstract Object[][] dataProvider();

    public Object[][] oldDataProvider() {
        log.info("Test textDocument/signatureHelp");
        return new Object[][]{
                {"annotations1.json", "annotations.bal"},
                {"annotations2.json", "annotations.bal"},
                {"exprListConstructor.json", "functionsAndTypeDefs.bal"},
                {"exprMappingConstructor1.json", "functionsAndTypeDefs.bal"},
                {"exprMappingConstructor2.json", "functionsAndTypeDefs.bal"},
                //TODO Table remove - Fix
//                {"exprTableConstructor.json", "functionsAndTypeDefs.bal"},
                {"exprStringTemplate.json", "functionsAndTypeDefs.bal"},
                {"exprXML.json", "functionsAndTypeDefs.bal"},
                {"exprNewImplicit.json", "functionsAndTypeDefs.bal"},
                {"exprNewExplicit.json", "functionsAndTypeDefs.bal"},
                {"exprVariableReference.json", "functionsAndTypeDefs.bal"},
                {"exprFieldAccess1.json", "functionsAndTypeDefs.bal"},
                {"exprFieldAccess2.json", "functionsAndTypeDefs.bal"},
                {"exprFieldAccess3.json", "functionsAndTypeDefs.bal"},
                {"exprOptionalFieldAccess.json", "functionsAndTypeDefs.bal"},
                {"exprAnnotationAccess.json", "functionsAndTypeDefs.bal"},
                {"exprMemberAccess1.json", "functionsAndTypeDefs.bal"},
                {"exprMemberAccess2.json", "functionsAndTypeDefs.bal"},
                {"exprXMLAttributes.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCall.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCallNested1.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCallNested2.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCallNamed.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCallRestArgs1.json", "functionsAndTypeDefs.bal"},
                {"exprFunctionCallRestArgs2.json", "functionsAndTypeDefs.bal"},
                {"exprMethodCall.json", "functionsAndTypeDefs.bal"},
                //TODO: Fix #24839
//                {"exprErrorConstructorDirect.json", "functionsAndTypeDefs.bal"},
                {"exprErrorConstructorIndirect.json", "functionsAndTypeDefs.bal"},
                {"exprAnonFunction.json", "functionsAndTypeDefs.bal"},
                {"exprArrowFunction.json", "functionsAndTypeDefs.bal"},
                {"exprTypeCast.json", "functionsAndTypeDefs.bal"},
                {"exprTypeOf.json", "functionsAndTypeDefs.bal"},
                {"exprUnary1.json", "functionsAndTypeDefs.bal"},
                {"exprUnary2.json", "functionsAndTypeDefs.bal"},
                {"exprUnary3.json", "functionsAndTypeDefs.bal"},
                {"exprUnary4.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative1.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative2.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative3.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative4.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative5.json", "functionsAndTypeDefs.bal"},
                {"exprMultiplicative6.json", "functionsAndTypeDefs.bal"},
                {"exprAdditive1.json", "functionsAndTypeDefs.bal"},
                {"exprAdditive2.json", "functionsAndTypeDefs.bal"},
                {"exprShift1.json", "functionsAndTypeDefs.bal"},
                {"exprShift2.json", "functionsAndTypeDefs.bal"},
                {"exprShift3.json", "functionsAndTypeDefs.bal"},
                {"exprRange1.json", "functionsAndTypeDefs.bal"},
                {"exprRange2.json", "functionsAndTypeDefs.bal"},
                {"exprRange3.json", "functionsAndTypeDefs.bal"},
                {"exprRange4.json", "functionsAndTypeDefs.bal"},
                {"exprNumericalComparison1.json", "functionsAndTypeDefs.bal"},
                {"exprNumericalComparison2.json", "functionsAndTypeDefs.bal"},
                {"exprNumericalComparison3.json", "functionsAndTypeDefs.bal"},
                {"exprNumericalComparison4.json", "functionsAndTypeDefs.bal"},
                {"exprIs.json", "functionsAndTypeDefs.bal"},
                {"exprEquality1.json", "functionsAndTypeDefs.bal"},
                {"exprEquality2.json", "functionsAndTypeDefs.bal"},
                {"exprEquality3.json", "functionsAndTypeDefs.bal"},
                {"exprEquality4.json", "functionsAndTypeDefs.bal"},
                {"exprBinaryBitwise1.json", "functionsAndTypeDefs.bal"},
                {"exprBinaryBitwise2.json", "functionsAndTypeDefs.bal"},
                {"exprBinaryBitwise3.json", "functionsAndTypeDefs.bal"},
                {"exprLogical1.json", "functionsAndTypeDefs.bal"},
                {"exprLogical2.json", "functionsAndTypeDefs.bal"},
                {"exprConditional1.json", "functionsAndTypeDefs.bal"},
                {"exprConditional2.json", "functionsAndTypeDefs.bal"},
                {"exprConditional3.json", "functionsAndTypeDefs.bal"},
                {"exprConditional4.json", "functionsAndTypeDefs.bal"},
                {"exprConditional5.json", "functionsAndTypeDefs.bal"},
                {"exprChecking1.json", "functionsAndTypeDefs.bal"},
                {"exprChecking2.json", "functionsAndTypeDefs.bal"},
                {"exprTrap.json", "functionsAndTypeDefs.bal"},
                {"exprServiceConstructor1.json", "functionsAndTypeDefs.bal"},
                {"exprServiceConstructor2.json", "functionsAndTypeDefs.bal"},
                {"functionInSameFile.json", "functionInSameFile.bal"},
                {"functionInSameFile2.json", "functionInSameFile2.bal"},
                {"functionInSameFile3.json", "functionInSameFile3.bal"},
                {"functionInSameFile4.json", "functionInSameFile4.bal"},
                {"functionInSameFileWithoutDocumentation.json", "functionInSameFileWithoutDocumentation.bal"},
                {"typeAttachedFunctions.json", "typeAttachedFunctions.bal"},
                {"functionInBuiltinPackage.json", "functionInBuiltinPackage.bal"},
                {"endpointActions.json", "endpointActions.bal"},
                {"signatureWithinResources.json", "signatureWithinResources.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinIfElse1.bal"},
                {"signatureWithinIfElse2.json", "signatureWithinIfElse2.bal"},
                {"signatureWithinIfElse3.json", "signatureWithinIfElse3.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinIfElse4.bal"},
                {"signatureWithinIfElse1.json", "signatureWithinWhile.bal"},
                {"signatureWithinForeach.json", "signatureWithinForeach.bal"},
                //TODO: Fix #24833
//                {"signatureWithinTransaction1.json", "signatureWithinTransaction1.bal"},
                {"signatureWithinObjectFunctions.json", "signatureWithinObjectFunctions.bal"},
                {"signatureWithinCheckPanic.json", "signatureWithinCheckPanic.bal"},
                {"signatureWithinAnnotation.json", "signatureWithinAnnotations.bal"},
                {"signatureSpreadOperator.json", "signatureSpreadOperator.bal"},
                {"signatureLangLib.json", "signatureLangLib.bal"}
        };
    }

    public abstract String getTestResourceDir();

    public Object[][] testSubset() {
        return new Object[0][];
    }

    public List<String> skipList() {
        return new ArrayList<>();
    }

    protected Object[][] getConfigsList() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            return Files.walk(this.testRoot.resolve(this.getTestResourceDir()).resolve(this.configDir))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".json")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName(), this.getTestResourceDir()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            return new Object[0][];
        }
    }

    private String getSignatureResponse(JsonObject config, Path sourcePath) throws IOException {
        JsonObject positionObj = config.get("position").getAsJsonObject();
        Position position = new Position();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        TestUtil.openDocument(this.serviceEndpoint, sourcePath);
        String resp = TestUtil.getSignatureHelpResponse(sourcePath.toString(), position, this.serviceEndpoint);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
        return resp;
    }
    
    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
