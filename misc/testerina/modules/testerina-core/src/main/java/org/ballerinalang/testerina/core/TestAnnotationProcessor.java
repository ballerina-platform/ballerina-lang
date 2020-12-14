/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.projects.Document;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.testsuite.Test;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.projects.testsuite.TesterinaRegistry;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Responsible of processing testerina annotations. This class is an implementation of the
 * {@link org.ballerinalang.compiler.plugins.CompilerPlugin}. Lifetime of an instance of this class will end upon the
 * completion of processing a ballerina package.
 */
public class TestAnnotationProcessor {
    private static final String TEST_ANNOTATION_NAME = "Config";
    private static final String BEFORE_SUITE_ANNOTATION_NAME = "BeforeSuite";
    private static final String AFTER_SUITE_ANNOTATION_NAME = "AfterSuite";
    private static final String BEFORE_EACH_ANNOTATION_NAME = "BeforeEach";
    private static final String AFTER_EACH_ANNOTATION_NAME = "AfterEach";
    private static final String MOCK_ANNOTATION_NAME = "Mock";
    private static final String BEFORE_FUNCTION = "before";
    private static final String AFTER_FUNCTION = "after";
    private static final String DEPENDS_ON_FUNCTIONS = "dependsOn";
    private static final String MODULE = "moduleName";
    private static final String FUNCTION = "functionName";
    private static final String GROUP_ANNOTATION_NAME = "groups";
    private static final String VALUE_SET_ANNOTATION_NAME = "dataProvider";
    private static final String TEST_ENABLE_ANNOTATION_NAME = "enable";
    private static final String AFTER_SUITE_ALWAYS_RUN_FIELD_NAME = "alwaysRun";
    private static final String MOCK_ANNOTATION_DELIMITER = "#";
    private static final String MOCK_FN_DELIMITER = "~";
    private static final String BEFORE_GROUPS_ANNOTATION_NAME = "BeforeGroups";
    private static final String AFTER_GROUPS_ANNOTATION_NAME = "AfterGroups";

    private TesterinaRegistry registry = TesterinaRegistry.getInstance();
    private boolean enabled = true;
    private DiagnosticLog diagnosticLog;
    private Types typeChecker;
    private SymbolResolver symbolResolver;

    private PackageCache packageCache;
    private Map<BPackageSymbol, SymbolEnv> packageEnvironmentMap;

    /**
     * this property is used as a work-around to initialize test suites only once for a package as Compiler
     * Annotation currently emits package import events too to the process method.
     */

    public void init(CompilerContext compilerContext) {
        this.diagnosticLog = BLangDiagnosticLog.getInstance(compilerContext);
        this.packageEnvironmentMap = SymbolTable.getInstance(compilerContext).pkgEnvMap;
        this.packageCache = PackageCache.getInstance(compilerContext);
        this.typeChecker = Types.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);

        if (TesterinaRegistry.getInstance().isTestSuitesCompiled()) {
            enabled = false;
        }
    }

    /**
     * Generate and return the testsuite for module tests.
     *
     * @param module module
     * @return test suite
     */
    public Optional<TestSuite> testSuite(Module module, Project project) {
        if (project.kind() != ProjectKind.SINGLE_FILE_PROJECT
                && !isTestable(module)) {
            return Optional.empty();
        }
        // skip generation of the testsuite if --skip-tests option is set to true
        if (project.buildOptions().skipTests()) {
            return Optional.empty();
        }

        return Optional.of(generateTestSuite(module, project));
    }

    private boolean isTestable(Module module){
        //module.moduleContext().bLangPackage().hasTestablePackage()
        return true;
    }

    private TestSuite generateTestSuite(Module module, Project project) {
        TestSuite testSuite = new TestSuite(module.descriptor().name().toString(),
                module.descriptor().packageName().toString(),
                module.descriptor().org().value(), module.descriptor().version().toString());
        TesterinaRegistry.getInstance().getTestSuites().put(
                module.descriptor().name().toString(), testSuite);

        testSuite.setInitFunctionName(".<init>");
        testSuite.setStartFunctionName(".<start>");
        testSuite.setStopFunctionName(".<stop>");
//        testSuite.setInitFunctionName(module.moduleContext().bLangPackage().initFunction.name.value);
//        testSuite.setStartFunctionName(module.moduleContext().bLangPackage().startFunction.name.value);
//        testSuite.setStopFunctionName(module.moduleContext().bLangPackage().stopFunction.name.value);
        testSuite.setPackageName(module.descriptor().packageName().toString());
        testSuite.setSourceRootPath(project.sourceRoot().toString());
        //Get syntax tree for source files in the module
        Map<String, SyntaxTree> sourceSyntaxTreeMap = new HashMap<>();
        module.documentIds().forEach(documentId -> {
            Document document = module.document(documentId);
            sourceSyntaxTreeMap.put(document.name(), document.syntaxTree());
        });
        addUtilityFunctions(sourceSyntaxTreeMap, project, testSuite);
        Map<String, SyntaxTree> testSyntaxTreeMap;
        //set test init functions
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            testSuite.setTestInitFunctionName(".<init>");
            testSuite.setTestStartFunctionName(".<start>");
            testSuite.setTestStopFunctionName(".<stop>");
            testSyntaxTreeMap = sourceSyntaxTreeMap;
        } else {
            //Get syntax tree for test files in the module
            testSyntaxTreeMap = new HashMap<>();
            module.testDocumentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                testSyntaxTreeMap.put(document.name(), document.syntaxTree());
            });
            testSuite.setTestInitFunctionName(".<testinit>");
            testSuite.setTestStartFunctionName(".<teststart>");
            testSuite.setTestStopFunctionName(".<teststop>");
        }
        addUtilityFunctions(testSyntaxTreeMap, project, testSuite);
        // process annotations in test functions
        TestAnnotationProcessor testAnnotationProcessor = new TestAnnotationProcessor();
        CompilerContext compilerContext = project.projectEnvironmentContext().getService(CompilerContext.class);
        testAnnotationProcessor.init(compilerContext);
        completeTestSuite(module, testSyntaxTreeMap);
        return testSuite;
    }

    private void completeTestSuite(Module module, Map<String, SyntaxTree> syntaxTreeMap) {
        if (!enabled) {
            return;
        }
        String packageName = module.descriptor().packageName().toString();
        TestSuite suite = registry.getTestSuites().get(module.descriptor().name().toString());
        BallerinaSemanticModel semanticModel = (BallerinaSemanticModel) module.getCompilation().getSemanticModel();
        // Check if the registry contains a test suite for the package
        boolean isTestable = false;
        if(suite == null){
            //Set testable flag for single bal file execution
            if ((Names.DOT.getValue()).equals(packageName)) {
                isTestable =true;
            }else{
                isTestable = !module.testDocumentIds().isEmpty();
            }
            // Skip adding test suite if no tests are available in the tests path
            if (isTestable) {
                // Add a test suite to the registry if it does not contain one pertaining to the package name
                suite = registry.getTestSuites().computeIfAbsent(packageName, func ->
                        new TestSuite(module.descriptor().name().toString(),
                        module.descriptor().packageName().toString(),
                        module.descriptor().org().value(), module.descriptor().version().toString()));
            } else {
                return;
            }
        }
        for (Map.Entry<String, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            if (syntaxTreeEntry.getValue().containsModulePart()) {
                ModulePartNode modulePartNode = syntaxTreeMap.get(syntaxTreeEntry.getKey()).rootNode();
                for (Node node : modulePartNode.members()) {
                    if ((node.kind() == SyntaxKind.FUNCTION_DEFINITION) && node instanceof FunctionDefinitionNode) {
                        //Add test utility functions
                        String functionName = ((FunctionDefinitionNode) node).functionName().text();
                        Optional<MetadataNode> optionalMetadataNode = ((FunctionDefinitionNode)node).metadata();
                        if (optionalMetadataNode.isEmpty()) {
                            continue;
                        }else{
                            NodeList<AnnotationNode> annotations = optionalMetadataNode.get().annotations();
                            for (AnnotationNode annotationNode : annotations) {
                                LinePosition linePosition = LinePosition.from(
                                        annotationNode.location().lineRange().startLine().line(),
                                        annotationNode.location().lineRange().startLine().offset());
                                Optional<Symbol> optionalSymbol = semanticModel.symbol(syntaxTreeEntry.getKey(), linePosition);
                                if(optionalSymbol.isEmpty()){
                                    continue;
                                } else{
                                    Symbol symbol = optionalSymbol.get();
                                    if(SymbolKind.ANNOTATION == symbol.kind()){
                                        //
                                    }
                                }
                                String annotationName = annotationNode.toString().trim();
                                if (BEFORE_SUITE_ANNOTATION_NAME.equals(annotationName)) {
                                    suite.addBeforeSuiteFunction(functionName);
                                } else if (AFTER_SUITE_ANNOTATION_NAME.equals(annotationName)) {
                                    AtomicBoolean alwaysRun = new AtomicBoolean(false);
//                                    if (attachmentNode.getExpression().getKind() == NodeKind.RECORD_LITERAL_EXPR) {
//                                        List<RecordLiteralNode.RecordField> attributes = ((BLangRecordLiteral) attachmentNode
//                                                .getExpression()).getFields();
//                                        attributes.forEach(field -> {
//                                            BLangRecordLiteral.BLangRecordKeyValueField attributeNode =
//                                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
//                                            String name = attributeNode.getKey().toString();
//                                            BLangExpression valueExpr = attributeNode.getValue();
//                                            if (AFTER_SUITE_ALWAYS_RUN_FIELD_NAME.equals(name) &&
//                                                    Boolean.TRUE.toString().equals(valueExpr.toString())) {
//                                                alwaysRun.set(true);
//                                            }
//                                        });
//                                    }
                                    suite.addAfterSuiteFunction(functionName, alwaysRun);
                                }

                            }
                        }
                    }
                }
            }
        }



    }

    private void addUtilityFunctions(Map<String, SyntaxTree> syntaxTreeMap, Project project, TestSuite testSuite) {
        for (Map.Entry<String, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            if (syntaxTreeEntry.getValue().containsModulePart()) {
                ModulePartNode modulePartNode = syntaxTreeMap.get(syntaxTreeEntry.getKey()).rootNode();
                for (Node node : modulePartNode.members()) {
                    if ((node.kind() == SyntaxKind.FUNCTION_DEFINITION) && node instanceof FunctionDefinitionNode) {
                        //Add test utility functions
                        String functionName = ((FunctionDefinitionNode) node).functionName().text();
                        Location pos = node.location();
                        NodeList<Token> qualifiers = ((FunctionDefinitionNode) node).qualifierList();
                        boolean isUtility = true;
                        for (Token qualifier : qualifiers) {
                            if (Flag.RESOURCE.name().equals(qualifier.text()) ||
                                    Flag.REMOTE.name().equals(qualifier.text())) {
                                isUtility = false;
                                break;
                            }
                        }
                        if (pos != null && isUtility) {
                            // Remove the duplicated annotations.
                            String className = pos.lineRange().filePath().replace(".bal", "")
                                    .replace("/", ".");
                            String functionClassName = JarResolver.getQualifiedClassName(
                                    project.currentPackage().packageOrg().value(),
                                    project.currentPackage().packageName().value(),
                                    project.currentPackage().packageVersion().value().toString(),
                                    className);
                            testSuite.addTestUtilityFunction(functionName, functionClassName);
                        }
                    }
                }
            }
        }
    }

    private NodeList<AnnotationNode> getAnnotations(Optional<MetadataNode> metadataNode) {
        return metadataNode.map(MetadataNode::annotations).orElse(null);
    }
}
