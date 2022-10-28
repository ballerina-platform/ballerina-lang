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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.IDLClientGeneratorResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.test.TestUtils;
import io.ballerina.projects.util.ProjectUtils;
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

    private static final String UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR =
            "exposing a construct from a module generated for a client declaration is not yet supported";
    private static final String NO_CLIENT_OBJECT_NAMED_CLIENT_IN_GENERATED_MODULE_ERROR =
            "a module generated for a client declaration must have an object type or class named 'client'";
    private static final String MUTABLE_STATE_IN_GENERATED_MODULE_ERROR =
            "a module generated for a client declaration cannot have mutable state";
    private static final String INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR =
            "invalid usage of the 'client' keyword as an unquoted identifier in a qualified identifier: " +
                    "allowed only with client declarations";

    private CompileResult result;

    @BeforeSuite
    public void setup() {
        Project project = loadPackage("simpleclienttest");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        DiagnosticResult diagnosticResult = idlClientGeneratorResult.reportedDiagnostics();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 0,
                TestUtils.getDiagnosticsAsString(diagnosticResult));

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnostics().size(), 0,
                TestUtils.getDiagnosticsAsString(compilation.diagnosticResult()));

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        result = new CompileResult(project.currentPackage(), jBallerinaBackend);
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
            "testClientDeclScoping1",
            "testClientDeclModuleWithClientObjectType"
        };
    }

    @Test
    public void testClientDeclUndefinedSymbolsAndNoGeneratedModulesNegative() {
        Project project = loadPackage("simpleclientnegativetest");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();

        // Expected to get 2 'no matching plugin found' errors
        Assert.assertEquals(idlClientGeneratorResult.reportedDiagnostics().diagnostics().size(), 2,
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        Assert.assertEquals(TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()),
                "ERROR [main.bal:(40:1,40:82)] no matching plugin found for client declaration\n" +
                        "ERROR [main.bal:(43:5,43:87)] no matching plugin found for client declaration\n");
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, "no matching plugin found for client declaration", 40, 1);
        validateError(diagnostics, index++, "no matching plugin found for client declaration", 43, 5);
        validateError(diagnostics, index++, "unknown type 'Config'", 19, 1);
        validateError(diagnostics, index++, "unknown type 'Client'", 20, 1);
        validateError(diagnostics, index++, "unknown type 'ClientConfig'", 23, 5);
        validateError(diagnostics, index++, "unknown type 'Client'", 24, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguratin'", 29, 5);
        validateError(diagnostics, index++, "unknown type 'clients'", 30, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguration'", 36, 5);
        validateError(diagnostics, index++, "unknown type 'Config'", 37, 5);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testUnusedClientDeclPrefixNegative() {
        Project project = loadPackage("simpleclientnegativetesttwo");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, "unused client declaration prefix 'foo'", 17, 69);
        validateError(diagnostics, index++, "unused client declaration prefix 'bar'", 20, 74);
        validateError(diagnostics, index++, "unused client declaration prefix 'baz'", 23, 78);
        validateError(diagnostics, index++, "unused client declaration prefix 'p2'", 30, 70);
        validateError(diagnostics, index++, "unused client declaration prefix 'p1'", 38, 74);
        validateError(diagnostics, index++, "unused client declaration prefix 'p3'", 40, 74);
        validateError(diagnostics, index++, "unused client declaration prefix 'p4'", 43, 70);
        validateError(diagnostics, index++, "unused client declaration prefix 'p5'", 45, 70);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testExposingConstructFromGeneratedModuleNegative() {
        Project project = loadPackage("simpleclientnegativetestthree");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 20, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 23, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 37, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 45, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 51, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 54, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 57, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 63, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 72, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 78, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 81, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 84, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 87, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 90, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 93, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 96, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 99, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 102, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 105, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 108, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 114, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 123, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 127, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 138, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 141, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 143, 1);
        validateError(diagnostics, index++, UNSUPPORTED_EXPOSURE_OF_PUBLIC_CONSTRUCT_ERROR, 148, 1);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testInvalidGeneratedModuleNegative() {
        Project project = loadPackage("simpleclientnegativetestfour");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, NO_CLIENT_OBJECT_NAMED_CLIENT_IN_GENERATED_MODULE_ERROR, 1, 1);
        validateError(diagnostics, index++, NO_CLIENT_OBJECT_NAMED_CLIENT_IN_GENERATED_MODULE_ERROR, 1, 1);
        validateError(diagnostics, index++, NO_CLIENT_OBJECT_NAMED_CLIENT_IN_GENERATED_MODULE_ERROR, 1, 1);
        validateError(diagnostics, index++, MUTABLE_STATE_IN_GENERATED_MODULE_ERROR, 9, 1);
        validateError(diagnostics, index++, MUTABLE_STATE_IN_GENERATED_MODULE_ERROR, 11, 1);
        validateError(diagnostics, index++, MUTABLE_STATE_IN_GENERATED_MODULE_ERROR, 13, 1);
        validateError(diagnostics, index++, MUTABLE_STATE_IN_GENERATED_MODULE_ERROR, 15, 1);
        validateError(diagnostics, index++, MUTABLE_STATE_IN_GENERATED_MODULE_ERROR, 19, 1);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testInvalidImportOfGeneratedModuleNegative() {
        Project project = loadPackage("simpleclientnegativetestfive");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                          TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        validateError(diagnostics, index++, "a module generated for a client declaration cannot be imported", 17, 1);
        validateError(diagnostics, index++, "a module generated for a client declaration cannot be imported", 18, 1);
        validateError(diagnostics, index++, "undefined module 'client1'", 24, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguration'", 24, 5);
        validateError(diagnostics, index++, "undefined module 'bar'", 25, 5);
        validateError(diagnostics, index++, "unknown type 'ClientConfiguration'", 25, 5);
        Assert.assertEquals(diagnostics.length, index);
    }

    @Test
    public void testInvalidUsageOfUnquotedClientKeywordNegative() {
        Project project = loadPackage("simpleclientnegativetestsix");
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                          TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        PackageCompilation compilation = project.currentPackage().getCompilation();

        Diagnostic[] diagnostics = compilation.diagnosticResult().diagnostics().toArray(new Diagnostic[0]);
        int index = 0;
        // main.bal
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 21, 1);
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 25, 5);
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 26, 5);
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 26, 27);
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 31, 9);
        // oth.bal
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 20, 5);
        validateError(diagnostics, index++, INVALID_USAGE_OF_UNQUOTED_CLIENT_KEYWORD_ERROR, 21, 9);
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

        stmt = ((BLangBlockFunctionBody) ((BLangFunction) functionNode).body).stmts.get(2);
        attachments = ((BClientDeclarationSymbol) ((BLangClientDeclarationStatement) stmt).clientDeclaration.symbol)
                .getAnnotations();
        Assert.assertEquals(attachments.size(), 0);
    }

    private Project loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildOptions buildOptions = BuildOptions.builder().targetDir(ProjectUtils.getTemporaryTargetPath()).build();
        return TestUtils.loadBuildProject(projectDirPath, buildOptions);
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
