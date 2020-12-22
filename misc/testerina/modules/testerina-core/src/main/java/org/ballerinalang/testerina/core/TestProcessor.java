/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.testsuite.Test;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.projects.testsuite.TesterinaRegistry;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responsible of processing Testerina annotations.
 * Lifetime of an instance of this class will end upon the completion of processing a ballerina package.
 */
public class TestProcessor {
    private static final String TEST_ANNOTATION_NAME = "Config";
    private static final String BEFORE_SUITE_ANNOTATION_NAME = "BeforeSuite";
    private static final String AFTER_SUITE_ANNOTATION_NAME = "AfterSuite";
    private static final String BEFORE_EACH_ANNOTATION_NAME = "BeforeEach";
    private static final String AFTER_EACH_ANNOTATION_NAME = "AfterEach";
    private static final String BEFORE_FUNCTION = "before";
    private static final String AFTER_FUNCTION = "after";
    private static final String DEPENDS_ON_FUNCTIONS = "dependsOn";
    private static final String GROUP_ANNOTATION_NAME = "groups";
    private static final String VALUE_SET_ANNOTATION_NAME = "dataProvider";
    private static final String TEST_ENABLE_ANNOTATION_NAME = "enable";
    private static final String AFTER_SUITE_ALWAYS_RUN_FIELD_NAME = "alwaysRun";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String BEFORE_GROUPS_ANNOTATION_NAME = "BeforeGroups";
    private static final String AFTER_GROUPS_ANNOTATION_NAME = "AfterGroups";
    private static final String TEST_PREFIX = "@test:";

    private TesterinaRegistry registry = TesterinaRegistry.getInstance();
    private boolean enabled = true;

    /**
     * this property is used as a work-around to initialize test suites only once for a package as Compiler
     * Annotation currently emits package import events too to the process method.
     */
    public void init() {
        if (TesterinaRegistry.getInstance().isTestSuitesCompiled()) {
            enabled = false;
        }
    }

    /**
     * Generate and return the testsuite for module tests.
     *
     * @param module  Module
     * @param project Project
     * @return Optional<TestSuite>
     */
    public Optional<TestSuite> testSuite(Module module, Project project) {
        if (project.kind() != ProjectKind.SINGLE_FILE_PROJECT
                && module.testDocumentIds().isEmpty()) {
            return Optional.empty();
        }
        // skip generation of the testsuite if --skip-tests option is set to true
        if (project.buildOptions().skipTests()) {
            return Optional.empty();
        }
        return Optional.of(generateTestSuite(module, project));
    }

    /**
     * Generate the testsuite for module using syntax and semantic APIs.
     *
     * @param module  Module
     * @param project Project
     */
    private TestSuite generateTestSuite(Module module, Project project) {
        TestSuite testSuite = new TestSuite(module.descriptor().name().toString(),
                module.descriptor().packageName().toString(),
                module.descriptor().org().value(), module.descriptor().version().toString());
        TesterinaRegistry.getInstance().getTestSuites().put(
                module.descriptor().name().toString(), testSuite);
        testSuite.setInitFunctionName(".<init>");
        testSuite.setStartFunctionName(".<start>");
        testSuite.setStopFunctionName(".<stop>");
        testSuite.setPackageName(module.descriptor().packageName().toString());
        testSuite.setSourceRootPath(project.sourceRoot().toString());
        //Get syntax tree for source files in the module
        Map<String, SyntaxTree> sourceSyntaxTreeMap = new HashMap<>();
        module.documentIds().forEach(documentId -> {
            Document document = module.document(documentId);
            sourceSyntaxTreeMap.put(document.name(), document.syntaxTree());
        });
        addUtilityFunctions(sourceSyntaxTreeMap, module, project, testSuite);
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
            addUtilityFunctions(testSyntaxTreeMap, module, project, testSuite);
        }
        // process annotations in test functions
        init();
        processAnnotations(module, testSyntaxTreeMap);
        return testSuite;
    }

    /**
     * Process the annotations for a given module.
     *
     * @param module        Module
     * @param syntaxTreeMap Map<String, SyntaxTree>
     */
    private void processAnnotations(Module module, Map<String, SyntaxTree> syntaxTreeMap) {
        if (!enabled) {
            return;
        }
        String packageName = module.descriptor().packageName().toString();
        TestSuite suite = registry.getTestSuites().get(module.descriptor().name().toString());
        // Check if the registry contains a test suite for the package
        boolean isTestable = false;
        if (suite == null) {
            //Set testable flag for single bal file execution
            if ((Names.DOT.getValue()).equals(packageName)) {
                isTestable = true;
            } else {
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
        List<FunctionSymbol> functionSymbolList = getFunctionSymbolList(syntaxTreeMap, module);
        for (FunctionSymbol functionSymbol : functionSymbolList) {
            String functionName = functionSymbol.name();
            List<AnnotationSymbol> annotations = functionSymbol.annotations();
            for (AnnotationSymbol annotationSymbol : annotations) {
                String annotationName = annotationSymbol.name();
                if (annotationName.contains(BEFORE_SUITE_ANNOTATION_NAME)) {
                    suite.addBeforeSuiteFunction(functionName);
                } else if (annotationName.contains(AFTER_SUITE_ANNOTATION_NAME)) {
                    suite.addAfterSuiteFunction(functionName,
                            isAlwaysRunAfterSuite(getAnnotationNode(annotationSymbol, syntaxTreeMap, functionName)));
                } else if (annotationName.contains(BEFORE_GROUPS_ANNOTATION_NAME)) {
                    processGroupsAnnotation(getAnnotationNode(annotationSymbol, syntaxTreeMap, functionName),
                            functionName, suite, true);
                } else if (annotationName.contains(AFTER_GROUPS_ANNOTATION_NAME)) {
                    processGroupsAnnotation(getAnnotationNode(annotationSymbol, syntaxTreeMap, functionName),
                            functionName, suite, false);
                } else if (annotationName.contains(BEFORE_EACH_ANNOTATION_NAME)) {
                    suite.addBeforeEachFunction(functionName);
                } else if (annotationName.contains(AFTER_EACH_ANNOTATION_NAME)) {
                    suite.addAfterEachFunction(functionName);
                } else if (annotationName.contains(TEST_ANNOTATION_NAME)) {
                    processTestAnnotation(getAnnotationNode(annotationSymbol, syntaxTreeMap, functionName),
                            functionName, suite);
                } else {
                    // disregard this annotation
                }
            }
        }
    }

    private AnnotationNode getAnnotationNode(AnnotationSymbol annotationSymbol, Map<String, SyntaxTree> syntaxTreeMap,
                                             String function) {
        for (Map.Entry<String, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            if (syntaxTreeEntry.getValue().containsModulePart()) {
                ModulePartNode modulePartNode = syntaxTreeMap.get(syntaxTreeEntry.getKey()).rootNode();
                for (Node node : modulePartNode.members()) {
                    if ((node.kind() == SyntaxKind.FUNCTION_DEFINITION) && node instanceof FunctionDefinitionNode) {
                        String functionName = ((FunctionDefinitionNode) node).functionName().text();
                        if (functionName.equals(function)) {
                            Optional<MetadataNode> optionalMetadataNode = ((FunctionDefinitionNode) node).metadata();
                            if (optionalMetadataNode.isEmpty()) {
                                continue;
                            } else {
                                NodeList<AnnotationNode> annotations = optionalMetadataNode.get().annotations();
                                for (AnnotationNode annotation : annotations) {
                                    if ((annotation.toString().trim()).contains(
                                            TEST_PREFIX + annotationSymbol.name())) {
                                        return annotation;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get function symbols list using syntax tree and semantic API.
     *
     * @param syntaxTreeMap Map<String, SyntaxTree>
     * @param module        Module
     * @return List<FunctionSymbol>
     */
    private List<FunctionSymbol> getFunctionSymbolList(Map<String, SyntaxTree> syntaxTreeMap, Module module) {
        List<FunctionSymbol> functionSymbolList = new ArrayList<>();
        List<String> functionNamesList = new ArrayList<>();
        for (Map.Entry<String, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            List<Symbol> symbols = module.getCompilation().getSemanticModel().visibleSymbols(
                    syntaxTreeEntry.getKey(),
                    LinePosition.from(syntaxTreeEntry.getValue().rootNode().location().lineRange().endLine().line(),
                            syntaxTreeEntry.getValue().rootNode().location().lineRange().endLine().offset()));
            for (Symbol symbol : symbols) {
                if (symbol.kind() == SymbolKind.FUNCTION && symbol instanceof FunctionSymbol &&
                        !functionNamesList.contains(symbol.name())) {
                    functionSymbolList.add((FunctionSymbol) symbol);
                    functionNamesList.add(symbol.name());
                }
            }
        }
        return functionSymbolList;
    }

    /**
     * Add utility functions for the test suite.
     *
     * @param syntaxTreeMap Map<String, SyntaxTree>
     * @param module        Module
     * @param project       Project
     * @param testSuite     TestSuite
     */
    private void addUtilityFunctions(Map<String, SyntaxTree> syntaxTreeMap, Module module,
                                     Project project, TestSuite testSuite) {
        List<FunctionSymbol> functionSymbolList = getFunctionSymbolList(syntaxTreeMap, module);
        for (FunctionSymbol functionSymbol : functionSymbolList) {
            String functionName = functionSymbol.name();
            Location pos = functionSymbol.location();
            List<Qualifier> qualifiers = functionSymbol.qualifiers();
            boolean isUtility = true;
            for (Qualifier qualifier : qualifiers) {
                if (Flag.RESOURCE.name().equals(qualifier.getValue()) ||
                        Flag.REMOTE.name().equals(qualifier.getValue())) {
                    isUtility = false;
                    break;
                }
            }
            if (pos != null && isUtility) {
                // Remove the duplicated annotations.
                String className = pos.lineRange().filePath().replace(".bal", "")
                        .replace("/", ".");
                String functionClassName = JarResolver.getQualifiedClassName(
                        module.descriptor().org().value(),
                        module.descriptor().name().toString(),
                        module.descriptor().version().toString(),
                        className);
                testSuite.addTestUtilityFunction(functionName, functionClassName);
            }
        }
    }

    /**
     * Check whether there is a common element in two Lists.
     *
     * @param inputGroups    String @{@link List} to match
     * @param functionGroups String @{@link List} to match against
     * @return true if a match is found
     */
    private boolean isGroupAvailable(List<String> inputGroups, List<String> functionGroups) {
        for (String group : inputGroups) {
            for (String funcGroup : functionGroups) {
                if (group.equals(funcGroup)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Format and get the string value of a value expression node.
     *
     * @param valueExpr Node
     * @return String
     */
    private String getStringValue(Node valueExpr) {
        return valueExpr.toString().replaceAll("\\\"", "").trim();
    }

    /**
     * Check whether alwaysRun property is set for after suite function.
     *
     * @param annotationNode AnnotationNode
     * @return AtomicBoolean
     */
    private AtomicBoolean isAlwaysRunAfterSuite(AnnotationNode annotationNode) {
        AtomicBoolean alwaysRun = new AtomicBoolean(false);
        if (annotationNode != null && !annotationNode.annotValue().isEmpty()) {
            Optional<MappingConstructorExpressionNode> mappingNodes = annotationNode.annotValue();
            if (!mappingNodes.isEmpty()) {
                mappingNodes.get().fields().forEach(mappingFieldNode -> {
                    if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                        SpecificFieldNode specificField = (SpecificFieldNode) mappingFieldNode;
                        if (AFTER_SUITE_ALWAYS_RUN_FIELD_NAME.equals(specificField.fieldName().toString().trim())) {
                            ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                            if (valueExpr != null) {
                                if (Boolean.TRUE.toString().equals(valueExpr.toString())) {
                                    alwaysRun.set(true);
                                }
                            }
                        }
                    }
                });
            }
        }
        return alwaysRun;
    }

    /**
     * Process group annotations from an AnnotationNode.
     *
     * @param annotationNode AnnotationNode
     * @param functionName   String
     * @param suite          TestSuite
     * @param isBeforeGroups boolean
     */
    private void processGroupsAnnotation(AnnotationNode annotationNode, String functionName, TestSuite suite,
                                         boolean isBeforeGroups) {
        if (annotationNode != null && !annotationNode.annotValue().isEmpty()) {
            Optional<MappingConstructorExpressionNode> mappingNodes = annotationNode.annotValue();
            if (!mappingNodes.isEmpty()) {
                for (MappingFieldNode mappingFieldNode : mappingNodes.get().fields()) {
                    if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                        SpecificFieldNode specificField = (SpecificFieldNode) mappingFieldNode;
                        if (VALUE_FIELD_NAME.equals(specificField.fieldName().toString().trim())) {
                            ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                            if (SyntaxKind.LIST_CONSTRUCTOR == valueExpr.kind() &&
                                    valueExpr instanceof ListConstructorExpressionNode) {
                                List<String> groupList = new ArrayList<>();
                                ((ListConstructorExpressionNode) valueExpr).expressions().forEach(
                                        expression -> groupList.add(getStringValue(expression)));
                                if (isBeforeGroups) {
                                    suite.addBeforeGroupsFunction(functionName, groupList);
                                } else {
                                    suite.addAfterGroupFunction(functionName, groupList);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Process the test annotations using an AnnotationNode.
     *
     * @param annotationNode AnnotationNode
     * @param functionName   String
     * @param suite          TestSuite
     */
    private void processTestAnnotation(AnnotationNode annotationNode, String functionName, TestSuite suite) {
        Test test = new Test();
        test.setTestName(functionName);
        AtomicBoolean shouldSkip = new AtomicBoolean();
        AtomicBoolean groupsFound = new AtomicBoolean();
        List<String> groups = registry.getGroups();
        boolean shouldIncludeGroups = registry.shouldIncludeGroups();
        if (annotationNode != null && !annotationNode.annotValue().isEmpty()) {
            Optional<MappingConstructorExpressionNode> mappingNodes = annotationNode.annotValue();
            if (!mappingNodes.isEmpty()) {
                for (MappingFieldNode mappingFieldNode : mappingNodes.get().fields()) {
                    if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                        SpecificFieldNode specificField = (SpecificFieldNode) mappingFieldNode;
                        String fieldName = specificField.fieldName().toString().trim();
                        ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                        if (valueExpr != null) {
                            if (TEST_ENABLE_ANNOTATION_NAME.equals(fieldName)) {
                                if (Boolean.FALSE.toString().equals(getStringValue(valueExpr))) {
                                    shouldSkip.set(true);
                                    continue;
                                }
                            }
                            if (GROUP_ANNOTATION_NAME.equals(fieldName)) {
                                if (SyntaxKind.LIST_CONSTRUCTOR == valueExpr.kind() &&
                                        valueExpr instanceof ListConstructorExpressionNode) {
                                    List<String> groupList = new ArrayList<>();
                                    ((ListConstructorExpressionNode) valueExpr).expressions().forEach(
                                            expression -> groupList.add(getStringValue(expression)));
                                    test.setGroups(groupList);
                                    suite.addTestToGroups(test);

                                    // Check whether user has provided a group list
                                    if (groups != null && !groups.isEmpty()) {
                                        boolean isGroupPresent = isGroupAvailable(groups, test.getGroups());
                                        if (shouldIncludeGroups) {
                                            // include only if the test belong to one of these groups
                                            if (!isGroupPresent) {
                                                // skip the test if this group is not defined in this test
                                                shouldSkip.set(true);
                                                continue;
                                            }
                                        } else {
                                            // exclude only if the test belong to one of these groups
                                            if (isGroupPresent) {
                                                // skip if this test belongs to one of the excluded groups
                                                shouldSkip.set(true);
                                                continue;
                                            }
                                        }
                                        groupsFound.set(true);
                                    }
                                }
                            }
                            if (VALUE_SET_ANNOTATION_NAME.equals(fieldName)) {
                                test.setDataProvider(getStringValue(valueExpr));
                            }
                            if (BEFORE_FUNCTION.equals(fieldName)) {
                                test.setBeforeTestFunction(getStringValue(valueExpr));
                            }
                            if (AFTER_FUNCTION.equals(fieldName)) {
                                test.setAfterTestFunction(getStringValue(valueExpr));
                            }
                            if (DEPENDS_ON_FUNCTIONS.equals(fieldName)) {
                                if (SyntaxKind.LIST_CONSTRUCTOR == valueExpr.kind() &&
                                        valueExpr instanceof ListConstructorExpressionNode) {
                                    List<String> dependsOnFunctions = new ArrayList<>();
                                    ((ListConstructorExpressionNode) valueExpr).expressions().forEach(
                                            expression -> dependsOnFunctions.add(getStringValue(expression)));
                                    for (String function : dependsOnFunctions) {
                                        test.addDependsOnTestFunction(function);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (groups != null && !groups.isEmpty() && !groupsFound.get() && shouldIncludeGroups) {
            // if the user has asked to run only a specific list of groups and this test doesn't have
            // that group, we should skip the test
            shouldSkip.set(true);
        }
        if (!shouldSkip.get()) {
            suite.addTests(test);
        }
    }

}
