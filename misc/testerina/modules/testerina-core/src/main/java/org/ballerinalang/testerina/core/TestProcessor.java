/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestSuite;

import java.nio.file.Path;
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
    private static final String ALWAYS_RUN_FIELD_NAME = "alwaysRun";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String BEFORE_GROUPS_ANNOTATION_NAME = "BeforeGroups";
    private static final String AFTER_GROUPS_ANNOTATION_NAME = "AfterGroups";
    private static final String TEST_PREFIX = "@test:";
    private static final String FILE_NAME_PERIOD_SEPARATOR = "$$$";

    private TesterinaRegistry registry = TesterinaRegistry.getInstance();

    private JarResolver jarResolver;

    public TestProcessor() {
    }

    public TestProcessor(JarResolver jarResolver) {
        this.jarResolver = jarResolver;
    }

    /**
     * Generate and return the testsuite for module tests.
     *
     * @param module  Module
     * @return Optional<TestSuite>
     */
    public Optional<TestSuite> testSuite(Module module) {
        if (module.project().kind() != ProjectKind.SINGLE_FILE_PROJECT
                && module.testDocumentIds().isEmpty()) {
            return Optional.empty();
        }
        // skip generation of the testsuite if --with-tests option is set to false
        if (module.project().buildOptions().skipTests()) {
            return Optional.empty();
        }
        return Optional.of(generateTestSuite(module, this.jarResolver));
    }

    /**
     * Get the syntax tree for tests.
     *
     * @param module Module
     * @return Map<Document, SyntaxTree>
     */
    private Map<Document, SyntaxTree> getTestSyntaxTreeMap(Module module) {
        Map<Document, SyntaxTree> syntaxTreeMap = new HashMap<>();
        if (isSingleFileProject(module.project())) {
            module.documentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document, document.syntaxTree());
            });
        } else {
            module.testDocumentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document, document.syntaxTree());
            });
        }
        return syntaxTreeMap;
    }

    /**
     * Generate the testsuite for module using syntax and semantic APIs.
     *
     * @param module Module
     * @return TestSuite
     */
    private TestSuite generateTestSuite(Module module, JarResolver jarResolver) {
        TestSuite testSuite = new TestSuite(module.descriptor().name().toString(),
                module.descriptor().packageName().toString(),
                module.descriptor().org().value(), module.descriptor().version().toString());
        TesterinaRegistry.getInstance().getTestSuites().put(
                module.descriptor().name().toString(), testSuite);
        testSuite.setPackageName(module.descriptor().packageName().toString());
        testSuite.setSourceRootPath(module.project().sourceRoot().toString());

        if (jarResolver != null) {
            List<Path> jarPaths = new ArrayList<>();
            for (JarLibrary jarLibrary : jarResolver.getJarFilePathsRequiredForTestExecution(module.moduleName())) {
                jarPaths.add(jarLibrary.path());
            }
            testSuite.addTestExecutionDependencies(jarPaths);
        }

        addUtilityFunctions(module, testSuite);
        processAnnotations(module, testSuite);
        testSuite.sort();
        return testSuite;
    }

    /**
     * Process the annotations for a given module.
     *
     * @param module        Module
     * @param suite        TestSuite
     */
    private void processAnnotations(Module module, TestSuite suite) {
        Map<Document, SyntaxTree> syntaxTreeMap = getTestSyntaxTreeMap(module);
        List<FunctionSymbol> functionSymbolList = getFunctionSymbolList(syntaxTreeMap, module);
        for (FunctionSymbol functionSymbol : functionSymbolList) {
            String functionName = functionSymbol.getName().get();
            List<AnnotationSymbol> annotations = functionSymbol.annotations();
            for (AnnotationSymbol annotationSymbol : annotations) {
                String annotationName = annotationSymbol.getName().get();
                if (annotationName.contains(BEFORE_SUITE_ANNOTATION_NAME)) {
                    suite.addBeforeSuiteFunction(functionName);
                } else if (annotationName.contains(AFTER_SUITE_ANNOTATION_NAME)) {
                    suite.addAfterSuiteFunction(functionName,
                            isAlwaysRunValue(getAnnotationNode(annotationSymbol, syntaxTreeMap, functionName)));
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

    /**
     * Returns the relevant AnnotationNode from Syntax Tree for a AnnotationSymbol.
     *
     * @param annotationSymbol AnnotationSymbol
     * @param syntaxTreeMap    Map<String, SyntaxTree>
     * @param function         String
     * @return AnnotationNode
     */
    private AnnotationNode getAnnotationNode(AnnotationSymbol annotationSymbol, Map<Document, SyntaxTree> syntaxTreeMap,
                                             String function) {
        for (Map.Entry<Document, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
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
                                            TEST_PREFIX + annotationSymbol.getName().get())) {
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
     * @param syntaxTreeMap Map<Document, SyntaxTree>
     * @param module        Module
     * @return List<FunctionSymbol>
     */
    private List<FunctionSymbol> getFunctionSymbolList(Map<Document, SyntaxTree> syntaxTreeMap, Module module) {
        List<FunctionSymbol> functionSymbolList = new ArrayList<>();
        List<String> functionNamesList = new ArrayList<>();
        for (Map.Entry<Document, SyntaxTree> syntaxTreeEntry : syntaxTreeMap.entrySet()) {
            // we cannot remove the module.getCompilation() here since the semantic model is accessed
            // after the code gen phase here. package.getCompilation() throws an IllegalStateException
            List<Symbol> symbols = module.getCompilation().getSemanticModel().visibleSymbols(
                    syntaxTreeEntry.getKey(),
                    LinePosition.from(syntaxTreeEntry.getValue().rootNode().location().lineRange().endLine().line(),
                                      syntaxTreeEntry.getValue().rootNode().location().lineRange().endLine().offset()));
            for (Symbol symbol : symbols) {
                if (symbol.kind() == SymbolKind.FUNCTION && symbol instanceof FunctionSymbol &&
                        !functionNamesList.contains(symbol.getName().get())) {
                    functionSymbolList.add((FunctionSymbol) symbol);
                    functionNamesList.add(symbol.getName().get());
                }
            }
        }
        return functionSymbolList;
    }

    /**
     * Checks whether given project is a single file project.
     *
     * @param project Project
     * @return boolean
     */
    private boolean isSingleFileProject(Project project) {
        boolean isSingleFileProject = false;
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            isSingleFileProject = true;
        }
        return isSingleFileProject;
    }

    /**
     * Add utility functions for the test suite.
     *
     * @param module        Module
     * @param testSuite     TestSuite
     */
    private void addUtilityFunctions(Module module, TestSuite testSuite) {
        Map<Document, SyntaxTree> syntaxTreeMap = new HashMap<>();
        module.documentIds().forEach(documentId -> {
            Document document = module.document(documentId);
            syntaxTreeMap.put(document, document.syntaxTree());
        });
        if (!isSingleFileProject(module.project())) {
            module.testDocumentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document, document.syntaxTree());
            });
        }
        List<FunctionSymbol> functionSymbolList = getFunctionSymbolList(syntaxTreeMap, module);
        for (FunctionSymbol functionSymbol : functionSymbolList) {
            String functionName = functionSymbol.getName().get();
            Location pos = functionSymbol.getLocation().get();
            List<Qualifier> qualifiers = functionSymbol.qualifiers();
            boolean isUtility = true;
            for (Qualifier qualifier : qualifiers) {
                if (Flag.RESOURCE.name().equals(qualifier.getValue()) ||
                        Flag.REMOTE.name().equals(qualifier.getValue())) {
                    isUtility = false;
                    break;
                }
            }
            if (isUtility) {
                // Remove the duplicated annotations.
                String className = pos.lineRange().filePath()
                        .replace(ProjectConstants.BLANG_SOURCE_EXT, "")
                        .replace(ProjectConstants.DOT, FILE_NAME_PERIOD_SEPARATOR)
                        .replace("/", ProjectConstants.DOT);
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
    private AtomicBoolean isAlwaysRunValue(AnnotationNode annotationNode) {
        AtomicBoolean alwaysRun = new AtomicBoolean(false);
        if (annotationNode != null && !annotationNode.annotValue().isEmpty()) {
            Optional<MappingConstructorExpressionNode> mappingNodes = annotationNode.annotValue();
            if (!mappingNodes.isEmpty()) {
                mappingNodes.get().fields().forEach(mappingFieldNode -> {
                    if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                        SpecificFieldNode specificField = (SpecificFieldNode) mappingFieldNode;
                        if (ALWAYS_RUN_FIELD_NAME.equals(specificField.fieldName().toString().trim())) {
                            ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                            if (valueExpr != null) {

                                if (SyntaxKind.BOOLEAN_LITERAL == valueExpr.kind()) {
                                    if (getStringValue(valueExpr).startsWith(Boolean.TRUE.toString())) {
                                        alwaysRun.set(true);
                                    }
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
                                    suite.addAfterGroupFunction(functionName, groupList,
                                            isAlwaysRunValue(annotationNode));
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
                                if (SyntaxKind.BOOLEAN_LITERAL == valueExpr.kind()) {
                                    if (getStringValue(valueExpr).startsWith(Boolean.FALSE.toString())) {
                                        shouldSkip.set(true);
                                        break;
                                    }
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
