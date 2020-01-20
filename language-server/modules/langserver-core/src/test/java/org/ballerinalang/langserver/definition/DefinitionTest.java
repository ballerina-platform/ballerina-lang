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
package org.ballerinalang.langserver.definition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
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
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test goto definition language server feature.
 */
public class DefinitionTest {
    private Path configRoot;
    private Path sourceRoot;
    private Path projectPath = FileUtils.RES_DIR.resolve("referencesProject");
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(DefinitionTest.class);

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("definition").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("definition").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test goto definitions", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, sourceRoot);
    }

    @Test(description = "Test Go to definition between two files in same module", enabled = false)
    public void testDifferentFiles() throws IOException {
        log.info("Test textDocument/definition for Two Files in same module");
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multifile")
                .resolve("defMultiFile1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    @Test(description = "Test Go to definition between two modules", enabled = false)
    public void testDifferentModule() throws IOException {
        log.info("Test textDocument/definition for two modules");
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multipkg")
                .resolve("defMultiPkg1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    private void compareResults(Path sourcePath, Position position, JsonObject configObject, Path root)
            throws IOException {
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getDefinitionResponse(sourcePath.toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = parser.parse(actualStr).getAsJsonObject().getAsJsonObject("result").getAsJsonArray("left");
        this.alterExpectedUri(expected, root);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] testDataProvider() throws IOException {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                // Note: Variable Reference Expressions will be addressed in almost all the test cases
                // Covers the Service Definition Test cases
                {"defService1.json", "service"},
                {"defService2.json", "service"},
                {"defService3.json", "service"},
                {"defService4.json", "service"},
                {"defService5.json", "service"},
                {"defService7.json", "service"},
                // Covers the Function Definition Test cases
                {"defFunction1.json", "function"},
                {"defFunction3.json", "function"},
                {"defFunction4.json", "function"},
                {"defFunction5.json", "function"},
                {"defFunction6.json", "function"},
                {"defFunction7.json", "function"},
                {"defFunction8.json", "function"},
                {"defFunction9.json", "function"},
                {"defFunction10.json", "function"},
                {"defFunction11.json", "function"},
                {"defFunction12.json", "function"},
                // Following tests covers the type descriptor and Module Type Definitions
                // Covers Simple Type Descriptor
                {"defTypeDesc1.json", "typedesc"},
                {"defTypeDesc2.json", "typedesc"},
                // Covers List Type Descriptor
                {"defTypeDesc3.json", "typedesc"},
                {"defTypeDesc4.json", "typedesc"},
                {"defTypeDesc5.json", "typedesc"},
                {"defTypeDesc6.json", "typedesc"},
                {"defTypeDesc7.json", "typedesc"},
                {"defTypeDesc8.json", "typedesc"},
                // Covers Map Type Descriptor
                {"defTypeDesc9.json", "typedesc"},
                {"defTypeDesc10.json", "typedesc"},
                {"defTypeDesc11.json", "typedesc"},
                {"defTypeDesc12.json", "typedesc"},
                {"defTypeDesc13.json", "typedesc"},
                {"defTypeDesc14.json", "typedesc"},
                {"defTypeDesc15.json", "typedesc"},
                {"defTypeDesc16.json", "typedesc"},
                // Covers Function Type Descriptor
                {"defTypeDesc17.json", "typedesc"},
                {"defTypeDesc18.json", "typedesc"},
                {"defTypeDesc19.json", "typedesc"},
                {"defTypeDesc20.json", "typedesc"},
                {"defTypeDesc21.json", "typedesc"},
                // Covers Object Type Descriptor
                {"defTypeDesc22.json", "typedesc"},
                {"defTypeDesc23.json", "typedesc"},
                {"defTypeDesc24.json", "typedesc"},
                {"defTypeDesc25.json", "typedesc"},
//                {"defTypeDesc26.json", "typedesc"},
//                {"defTypeDesc27.json", "typedesc"},
//                {"defTypeDesc28.json", "typedesc"},
//                {"defTypeDesc29.json", "typedesc"},
//                {"defTypeDesc30.json", "typedesc"},
//                {"defTypeDesc31.json", "typedesc"},
//                {"defTypeDesc32.json", "typedesc"},
//                {"defTypeDesc33.json", "typedesc"},
//                {"defTypeDesc34.json", "typedesc"},
//                {"defTypeDesc35.json", "typedesc"},
//                {"defTypeDesc36.json", "typedesc"},
//                {"defTypeDesc37.json", "typedesc"},
                // Covers Future type Descriptor
//                {"defTypeDesc38.json", "typedesc"},
//                {"defTypeDesc39.json", "typedesc"},
//                {"defTypeDesc40.json", "typedesc"},
                // Covers TypeDesc Type Descriptor
//                {"defTypeDesc41.json", "typedesc"},
//                {"defTypeDesc42.json", "typedesc"},
//                {"defTypeDesc43.json", "typedesc"},
                // Covers Handle Type Descriptor
//                {"defTypeDesc44.json", "typedesc"},
                // Covers Union Type Descriptor
//                {"defTypeDesc45.json", "typedesc"},
//                {"defTypeDesc46.json", "typedesc"},
//                {"defTypeDesc47.json", "typedesc"},
                // Optional Type Descriptor
//                {"defTypeDesc48.json", "typedesc"},
//                {"defTypeDesc49.json", "typedesc"},
//                {"defTypeDesc50.json", "typedesc"},
                // Error Type Descriptor
//                {"defTypeDesc51.json", "typedesc"},
//                {"defTypeDesc52.json", "typedesc"},
                // Covers the Module Level variable declarations
                {"defModuleVar1.json", "modulevardecl"},
                {"defModuleVar2.json", "modulevardecl"},
                {"defModuleVar3.json", "modulevardecl"},
                {"defModuleVar4.json", "modulevardecl"},
                // Covers the Module Level variable declarations
                {"defModuleConst1.json", "moduleconst"},
                {"defModuleConst2.json", "moduleconst"},
                // Covers the List Constructor Expression
                {"defListConstructorExpr1.json", "expression"},
                // Covers the Mapping Constructor Expression
                {"defMappingConstructorExpr1.json", "expression"},
                {"defMappingConstructorExpr2.json", "expression"},
                {"defServiceConstructorExpr1.json", "expression"},
                // Covers the String Template Expression
                {"defStringTemplateExpr1.json", "expression"},
                // Covers the New Expression
                {"defNewExpr1.json", "expression"},
                {"defNewExpr2.json", "expression"},
                {"defNewExpr3.json", "expression"},
                // Covers Function Call Expression
                {"defFunctionCallExpr1.json", "expression"},
                {"defFunctionCallExpr2.json", "expression"},
                {"defFunctionCallExpr3.json", "expression"},
                {"defFunctionCallExpr4.json", "expression"},
                {"defFunctionCallExpr5.json", "expression"},
                {"defFunctionCallExpr6.json", "expression"},
                // Covers Method Call Expression
                {"defMethodCallExpr1.json", "expression"},
                {"defMethodCallExpr2.json", "expression"},
                {"defMethodCallExpr3.json", "expression"},
                // Covers Anon Function Expression
                {"defAnonFunctionExpr1.json", "expression"},
                {"defAnonFunctionExpr2.json", "expression"},
                {"defAnonFunctionExpr3.json", "expression"},
                {"defAnonFunctionExpr4.json", "expression"},
                {"defAnonFunctionExpr5.json", "expression"},
                {"defAnonFunctionExpr6.json", "expression"},
                {"defAnonFunctionExpr7.json", "expression"},
                // Covers Arrow Function Expression
                {"defArrowFunctionExpr1.json", "expression"},
                // Covers Type Cast Expression
                {"defTypeCastExpr1.json", "expression"},
                {"defTypeCastExpr2.json", "expression"},
                // Covers Typeof Expression
                {"defTypeOfExpr1.json", "expression"},
                // Covers Unary Expression
                {"defUnaryExpr1.json", "expression"},
                // Covers Multiplicative Expression
                {"defMultiplicativeExpr1.json", "expression"},
                {"defMultiplicativeExpr2.json", "expression"},
                // Covers Additive Expression
                {"defAdditiveExpr1.json", "expression"},
                {"defAdditiveExpr2.json", "expression"},
                // Covers Shift Expression
                {"defShiftExpr1.json", "expression"},
                {"defShiftExpr2.json", "expression"},
                // Covers Range Expression
                {"defRangeExpr1.json", "expression"},
                {"defRangeExpr2.json", "expression"},
                // Covers Numeric Expression
                {"defNumericExpr1.json", "expression"},
                {"defNumericExpr2.json", "expression"},
                // Covers Equality Expression
                {"defEqualityExpr1.json", "expression"},
                {"defEqualityExpr2.json", "expression"},
                // Covers Logical Expression
                {"defLogicalExpr2.json", "expression"},
                {"defLogicalExpr1.json", "expression"},
                // Covers Conditional Expression
                {"defConditionalExpr1.json", "expression"},
                {"defConditionalExpr2.json", "expression"},
                {"defConditionalExpr3.json", "expression"},
                {"defConditionalExpr4.json", "expression"},
                {"defConditionalExpr5.json", "expression"},
                // Covers Check Expression
                {"defCheckExpr1.json", "expression"},
                // Covers Check Panic Expression
                {"defCheckPanicExpr1.json", "expression"},
                // Covers Trap Expression
                {"defTrapExpr1.json", "expression"},
                // Covers Error Constructor Expression
                {"defErrorConstructorExpr1.json", "expression"},
                {"defErrorConstructorExpr2.json", "expression"},
                {"defErrorConstructorExpr3.json", "expression"},
                {"defErrorConstructorExpr4.json", "expression"},
                {"defErrorConstructorExpr5.json", "expression"},
                {"defErrorConstructorExpr6.json", "expression"},
                {"defErrorConstructorExpr7.json", "expression"},
                // Covers the Start Action
                {"defStartAction1.json", "action"},
                {"defStartAction2.json", "action"}, // Remote method call action is also similar
                // Covers the wait action
                {"defWaitAction1.json", "action"},
                {"defWaitAction2.json", "action"},
                {"defWaitAction3.json", "action"},
                // Covers Worker Send Action
                {"defSyncSendAction1.json", "action"},
                {"defSyncSendAction2.json", "action"},
                {"defAsyncSendAction1.json", "action"},
                {"defAsyncSendAction2.json", "action"},
                // Covers Worker Receive Action
                {"defReceiveAction1.json", "action"},
                // Covers Flush Action
                {"defFlushAction1.json", "action"},
                // Covers Remote Action
                {"defRemoteAction1.json", "action"},
                {"defRemoteAction2.json", "action"},
                // Covers Variable Definition Statement with list Binding pattern
                {"defVarDefStmt1.json", "vardefstatement"},
                {"defVarDefStmt2.json", "vardefstatement"},
                {"defVarDefStmt3.json", "vardefstatement"},
                {"defVarDefStmt4.json", "vardefstatement"},
                // Covers Variable Definition Statement with map Binding pattern
                {"defVarDefStmt5.json", "vardefstatement"},
                {"defVarDefStmt6.json", "vardefstatement"},
                {"defVarDefStmt7.json", "vardefstatement"},
                {"defVarDefStmt8.json", "vardefstatement"},
                // Covers Variable Definition Statement with Error Binding pattern
                {"defVarDefStmt12.json", "vardefstatement"},
                {"defVarDefStmt13.json", "vardefstatement"},
                {"defVarDefStmt14.json", "vardefstatement"},
                {"defVarDefStmt15.json", "vardefstatement"},
                {"defVarDefStmt16.json", "vardefstatement"},
                {"defVarDefStmt17.json", "vardefstatement"},
                // TODO: Enable after compiler fix
//                {"defVarDefStmt18.json", "vardefstatement"},
//                {"defVarDefStmt19.json", "vardefstatement"},
//                {"defVarDefStmt20.json", "vardefstatement"},
//                {"defVarDefStmt21.json", "vardefstatement"},
//                {"defVarDefStmt22.json", "vardefstatement"},
//                {"defVarDefStmt23.json", "vardefstatement"},
//                {"defVarDefStmt24.json", "vardefstatement"},
//                {"defVarDefStmt25.json", "vardefstatement"},
                // Covers Variable Definition Statement with final and var
                {"defVarDefStmt9.json", "vardefstatement"},
                {"defVarDefStmt10.json", "vardefstatement"},
                {"defVarDefStmt11.json", "vardefstatement"},
                // Covers Assignment Statement
                {"defAssignment1.json", "assignment"},
                {"defAssignment2.json", "assignment"},
                {"defAssignment3.json", "assignment"},
                {"defAssignment4.json", "assignment"},
                {"defAssignment5.json", "assignment"},
                {"defAssignment6.json", "assignment"},
                {"defAssignment7.json", "assignment"},
                {"defAssignment8.json", "assignment"},
                {"defAssignment9.json", "assignment"},
                // Covers the destructuring assignment with the binding patterns
                {"defAssignment10.json", "assignment"},
                {"defAssignment11.json", "assignment"},
                {"defAssignment12.json", "assignment"},
                {"defAssignment13.json", "assignment"},
                {"defAssignment14.json", "assignment"},
                {"defAssignment15.json", "assignment"},
                {"defAssignment16.json", "assignment"},
                {"defAssignment17.json", "assignment"},
                {"defAssignment18.json", "assignment"},
                {"defAssignment19.json", "assignment"},
                {"defAssignment20.json", "assignment"},
                {"defAssignment21.json", "assignment"},
                {"defAssignment22.json", "assignment"},
                {"defAssignment23.json", "assignment"},
                // Action Statement is covered in the Action section
                // Covers the Call Statement
                {"defCallStmt1.json", "callstatement"},
                {"defCallStmt2.json", "callstatement"},
                {"defCallStmt3.json", "callstatement"},
                {"defCallStmt4.json", "callstatement"},
                {"defCallStmt5.json", "callstatement"},
                {"defCallStmt6.json", "callstatement"},
                {"defCallStmt7.json", "callstatement"},
                // Covers the Conditional Statement
                {"defConditionalStmt1.json", "conditional"},
                {"defConditionalStmt2.json", "conditional"},
                {"defConditionalStmt3.json", "conditional"},
                {"defConditionalStmt4.json", "conditional"},
                {"defConditionalStmt5.json", "conditional"},
                // Covers the Match statement - variable name Binding pattern
                {"defMatchStmt1.json", "matchstmt"},
                // Covers the Match statement - List Binding pattern
                {"defMatchStmt2.json", "matchstmt"},
                {"defMatchStmt3.json", "matchstmt"},
                {"defMatchStmt4.json", "matchstmt"},
                // Covers the Match statement - Mapping Binding pattern
                {"defMatchStmt5.json", "matchstmt"},
                {"defMatchStmt6.json", "matchstmt"},
                {"defMatchStmt7.json", "matchstmt"},
                // Covers the Match statement - Constant pattern
                {"defMatchStmt8.json", "matchstmt"},
                {"defMatchStmt9.json", "matchstmt"},
                // Covers the Match Statement - List Pattern
                {"defMatchStmt10.json", "matchstmt"},
                // Covers the Match Statement - Mapping Pattern
                {"defMatchStmt11.json", "matchstmt"},
                // Covers the Match Statement Error patterns
                // Enable following after compiler fix
//                {"defMatchStmt13.json", "matchstmt"},
//                {"defMatchStmt14.json", "matchstmt"},
//                {"defMatchStmt15.json", "matchstmt"},
//                {"defMatchStmt16.json", "matchstmt"},
//                {"defMatchStmt17.json", "matchstmt"},
//                {"defMatchStmt18.json", "matchstmt"},
//                {"defMatchStmt19.json", "matchstmt"},
//                {"defMatchStmt20.json", "matchstmt"},
                //Covers Match statement expression
                {"defMatchStmt12.json", "matchstmt"},
                // Covers Foreach Statement
                {"defForeach1.json", "foreach"},
                {"defForeach2.json", "foreach"},
                {"defForeach3.json", "foreach"},
                {"defForeach4.json", "foreach"},
                {"defForeach5.json", "foreach"},
                {"defForeach6.json", "foreach"},
                {"defForeach7.json", "foreach"},
                {"defForeach8.json", "foreach"},
                {"defForeach9.json", "foreach"},
                {"defForeach10.json", "foreach"},
                {"defForeach11.json", "foreach"},
                // Covers While Statement
                {"defWhileStmt1.json", "while"},
                {"defWhileStmt2.json", "while"},
                // Covers Panic Statement
                {"defPanicStmt1.json", "panic"},
                // Covers Return Statement
                {"defReturnStmt1.json", "return"},
        };
    }
    
    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private void alterExpectedUri(JsonArray expected, Path root) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");
            Path expectedPath = Paths.get(root.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getCanonicalPath());
        }
    }

    private void alterActualUri(JsonArray actual) throws IOException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String uri = item.get("uri").toString().replace("\"", "");
            String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }
}
