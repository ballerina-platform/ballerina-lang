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

package io.ballerina.projects.test.plugins;

import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.test.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.ballerinalang.model.tree.ClientDeclarationNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClientDeclarationSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Compiler test cases for IDL clients.
 *
 * @since 2201.3.0
 */
public class IDLClientCompilerTests {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/idl_client_compiler_tests").toAbsolutePath();
    private static final String CARRIAGE_RETURN_CHAR = "\\r";
    private static final String EMPTY_STRING = "";

    private CompileResult result;

    @BeforeSuite
    public void setup() {
        Package currentPackage = loadPackage("simpleclienttest");
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnostics().size(), 0);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        result = new CompileResult(currentPackage, jBallerinaBackend);
        try {
            BRunUtil.runInit(result);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error while invoking init method");
        }
    }

    @Test(dataProvider = "testFuncNames")
    public void testModuleClientDecl(String testFuncName) {
        BRunUtil.invoke(result, testFuncName);
    }

    @DataProvider
    public Object[] testFuncNames() {
        return new Object[] {
            "testModuleClientDecl",
            "testClientDeclStmt",
            "testClientDeclScoping1"
        };
    }

    @Test
    public void testModuleClientDeclUndefinedSymbolsInGeneratedModuleNegative() {
        Package currentPackage = loadPackage("simpleclientnegativetest");
        PackageCompilation compilation = currentPackage.getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, "unknown type 'Config'", 19, 1);
        validateError(diagnostics, index++, "unknown type 'Client'", 20, 1);
        validateError(diagnostics, index++, "unknown type 'ClientConfig'", 23, 5);
        validateError(diagnostics, index++, "unknown type 'Client'", 24, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguratin'", 29, 5);
        validateError(diagnostics, index++, "unknown type 'clients'", 30, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguration'", 36, 5);
        validateError(diagnostics, index++, "unknown type 'Config'", 37, 5);
        validateError(diagnostics, index++, "no module generated for the client declaration", 40, 1);
        validateError(diagnostics, index++, "no module generated for the client declaration", 43, 5);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testAnnotationOnClientDeclaration() {
        List<? extends ClientDeclarationNode> clientDeclarations = result.getAST().getClientDeclarations();

        ClientDeclarationNode clientDeclarationNode = clientDeclarations.stream()
                .filter(cl -> "foo".equals(cl.getPrefix().getValue())).findFirst().get();
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BClientDeclarationSymbol) ((BLangClientDeclaration) clientDeclarationNode).symbol).getAnnotations();
        Assert.assertEquals(attachments.size(), 0);

        clientDeclarationNode = clientDeclarations.stream()
                .filter(cl -> "bar".equals(cl.getPrefix().getValue())).findFirst().get();
        attachments =
                ((BClientDeclarationSymbol) ((BLangClientDeclaration) clientDeclarationNode).symbol).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), 123L);
    }

    @Test
    public void testAnnotationOnClientDeclarationStmt() {
        FunctionNode functionNode = result.getAST().getFunctions().stream()
                .filter(fn -> "testClientDeclAnnotSymbols".equals(((BLangFunction) fn).name.value)).findFirst().get();

        BLangStatement stmt = ((BLangBlockFunctionBody) ((BLangFunction) functionNode).body).stmts.get(0);
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BClientDeclarationSymbol) ((BLangClientDeclarationStatement) stmt).clientDeclaration.symbol)
                        .getAnnotations();
        Assert.assertEquals(attachments.size(), 2);
        assertAttachmentSymbol(attachments.get(0), 12L);
        assertAttachmentSymbol(attachments.get(1), 13L);

        stmt = ((BLangBlockFunctionBody) ((BLangFunction) functionNode).body).stmts.get(1);
        attachments = ((BClientDeclarationSymbol) ((BLangClientDeclarationStatement) stmt).clientDeclaration.symbol)
                .getAnnotations();
        Assert.assertEquals(attachments.size(), 0);
    }

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        return buildProject.currentPackage();
    }

    private static void validateError(Diagnostic[] diagnostics, int errorIndex, String expectedErrMsg,
                                      int expectedErrLine, int expectedErrCol) {
        Diagnostic diag = diagnostics[errorIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                            expectedErrMsg.replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "incorrect error message:");
        Assert.assertEquals(diag.location().lineRange().startLine().line() + 1, expectedErrLine,
                            "incorrect line number:");
        Assert.assertEquals(diag.location().lineRange().startLine().offset() + 1, expectedErrCol,
                            "incorrect column position:");
    }

    private void assertAttachmentSymbol(AnnotationAttachmentSymbol attachmentSymbol, Object value) {
        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachmentSymbol;
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "ClientAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());

        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;

        Map<String, BLangConstantValue> mapConst = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(mapConst.size(), 1);
        Assert.assertEquals(mapConst.get("i").value, value);
    }
}
