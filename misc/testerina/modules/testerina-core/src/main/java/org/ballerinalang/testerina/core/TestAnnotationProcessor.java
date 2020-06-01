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

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.test.runtime.entity.Test;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Responsible of processing testerina annotations. This class is an implementation of the
 * {@link org.ballerinalang.compiler.plugins.CompilerPlugin}. Lifetime of an instance of this class will end upon the
 * completion of processing a ballerina package.
 */
@SupportedAnnotationPackages(
        value = "ballerina/test"
)
public class TestAnnotationProcessor extends AbstractCompilerPlugin {
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
    private static final String MOCK_ANNOTATION_DELIMITER = "#";

    private TesterinaRegistry registry = TesterinaRegistry.getInstance();
    private boolean enabled = true;
    private CompilerContext compilerContext;
    private DiagnosticLog diagnosticLog;
    private Types typeChecker;
    private SymbolResolver symbolResolver;
    private BLangPackage parent;
    private PackageCache packageCache;
    private Map<BPackageSymbol, SymbolEnv> packageEnvironmentMap;

    /**
     * this property is used as a work-around to initialize test suites only once for a package as Compiler
     * Annotation currently emits package import events too to the process method.
     */

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
        this.typeChecker = Types.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);
        this.packageEnvironmentMap = SymbolTable.getInstance(compilerContext).pkgEnvMap;
        this.packageCache = PackageCache.getInstance(compilerContext);

        if (TesterinaRegistry.getInstance().isTestSuitesCompiled()) {
            enabled = false;
        }
    }

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.compilerContext = context;
    }
    
    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        if (!enabled) {
            return;
        }
        parent = (BLangPackage) ((BLangFunction) functionNode).parent;
        String packageName = getPackageName(parent);
        TestSuite suite = registry.getTestSuites().get(packageName);
        // Check if the registry contains a test suite for the package
        if (suite == null) {
            // Add a test suite to the registry if it does not contain one pertaining to the package name
            suite = registry.getTestSuites().computeIfAbsent(packageName, func ->
                    new TestSuite(parent.packageID.name.value, packageName, parent.packageID.orgName.value,
                                  parent.packageID.version.value));
        }
        // Remove the duplicated annotations.
        annotations = annotations.stream().distinct().collect(Collectors.toList());
        // traverse through the annotations of this function
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationName = attachmentNode.getAnnotationName().getValue();
            String functionName = functionNode.getName().getValue();

            if (BEFORE_SUITE_ANNOTATION_NAME.equals(annotationName)) {
                suite.addBeforeSuiteFunction(functionName);
            } else if (AFTER_SUITE_ANNOTATION_NAME.equals(annotationName)) {
                suite.addAfterSuiteFunction(functionName);
            } else if (BEFORE_EACH_ANNOTATION_NAME.equals(annotationName)) {
                suite.addBeforeEachFunction(functionName);
            } else if (AFTER_EACH_ANNOTATION_NAME.equals(annotationName)) {
                suite.addAfterEachFunction(functionName);
            } else if (MOCK_ANNOTATION_NAME.equals(annotationName)) {
                String[] vals = new String[2];
                // TODO: when default values are supported in annotation struct we can remove this
                vals[0] = packageName;
                if (attachmentNode.getExpression() instanceof BLangRecordLiteral) {
                    List<RecordLiteralNode.RecordField> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getFields();
                    attributes.forEach(field -> {
                        String name;
                        BLangExpression valueExpr;

                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField attributeNode =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            name = attributeNode.getKey().toString();
                            valueExpr = attributeNode.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            name = varNameField.variableName.value;
                            valueExpr = varNameField;
                        }

                        String value = valueExpr.toString();

                        if (MODULE.equals(name)) {
                            value = formatPackageName(value); // Formats the single module to fully qualified name
                            vals[0] = value;
                        } else if (FUNCTION.equals(name)) {
                            vals[1] = value;
                        }
                    });
                    
                    // Check if Function in annotation is empty
                    if (vals[1].isEmpty()) {
                        diagnosticLog.logDiagnostic(Diagnostic.Kind.ERROR, attachmentNode.getPosition(),
                                "function name cannot be empty");
                        break;
                    }

                    // Find functionToMock in the packageID
                    PackageID functionToMockID = getPackageID(vals[0]);
                    if (functionToMockID == null) {
                        diagnosticLog.logDiagnostic(Diagnostic.Kind.ERROR, attachmentNode.getPosition(),
                                "could not find module specified ");
                    }

                    BType functionToMockType = getFunctionType(packageEnvironmentMap, functionToMockID, vals[1]);
                    BType mockFunctionType = getFunctionType(packageEnvironmentMap, parent.packageID,
                            ((BLangFunction) functionNode).name.toString());

                    if (functionToMockType != null && mockFunctionType != null) {
                        if (!typeChecker.isAssignable(mockFunctionType, functionToMockType)) {
                            diagnosticLog.logDiagnostic(Diagnostic.Kind.ERROR, ((BLangFunction) functionNode).pos,
                                    "incompatible types: expected " + functionToMockType.toString()
                                            + " but found " + mockFunctionType.toString());
                        }
                    } else {
                        diagnosticLog.logDiagnostic(Diagnostic.Kind.ERROR, attachmentNode.getPosition(),
                                "could not find functions in module");
                    }

                    //Creating a bLangTestablePackage to add a mock function
                    BLangTestablePackage bLangTestablePackage =
                            (BLangTestablePackage) ((BLangFunction) functionNode).parent;
                    bLangTestablePackage.addMockFunction(functionToMockID + MOCK_ANNOTATION_DELIMITER + vals[1],
                                                         functionName);
                }
            } else if (TEST_ANNOTATION_NAME.equals(annotationName)) {
                Test test = new Test();
                test.setTestName(functionName);
                AtomicBoolean shouldSkip = new AtomicBoolean();
                AtomicBoolean groupsFound = new AtomicBoolean();
                List<String> groups = registry.getGroups();
                boolean shouldIncludeGroups = registry.shouldIncludeGroups();

                if (attachmentNode.getExpression() instanceof BLangRecordLiteral) {
                    List<RecordLiteralNode.RecordField> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getFields();

                    attributes.forEach(field -> {
                        String name;
                        BLangExpression valueExpr;

                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField attributeNode =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            name = attributeNode.getKey().toString();
                            valueExpr = attributeNode.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            name = varNameField.variableName.value;
                            valueExpr = varNameField;
                        }

                        // Check if enable property is present in the annotation
                        if (TEST_ENABLE_ANNOTATION_NAME.equals(name) && "false".equals(valueExpr
                                .toString())) {
                            // If enable is false, disable the test, no further processing is needed
                            shouldSkip.set(true);
                            return;
                        }

                        // check if groups attribute is present in the annotation
                        if (GROUP_ANNOTATION_NAME.equals(name)) {
                            if (valueExpr instanceof BLangListConstructorExpr) {
                                BLangListConstructorExpr values = (BLangListConstructorExpr) valueExpr;
                                test.setGroups(values.exprs.stream().map(node -> node.toString())
                                                           .collect(Collectors.toList()));
                                // Check whether user has provided a group list
                                if (groups != null && !groups.isEmpty()) {
                                    boolean isGroupPresent = isGroupAvailable(groups, test.getGroups());
                                    if (shouldIncludeGroups) {
                                        // include only if the test belong to one of these groups
                                        if (!isGroupPresent) {
                                            // skip the test if this group is not defined in this test
                                            shouldSkip.set(true);
                                            return;
                                        }
                                    } else {
                                        // exclude only if the test belong to one of these groups
                                        if (isGroupPresent) {
                                            // skip if this test belongs to one of the excluded groups
                                            shouldSkip.set(true);
                                            return;
                                        }
                                    }
                                    groupsFound.set(true);
                                }
                            }
                        }
                        if (VALUE_SET_ANNOTATION_NAME.equals(name)) {
                            test.setDataProvider(valueExpr.toString());
                        }

                        if (BEFORE_FUNCTION.equals(name)) {
                            test.setBeforeTestFunction(valueExpr.toString());
                        }

                        if (AFTER_FUNCTION.equals(name)) {
                            test.setAfterTestFunction(valueExpr.toString());
                        }

                        if (DEPENDS_ON_FUNCTIONS.equals(name)) {
                            if (valueExpr instanceof BLangListConstructorExpr) {
                                BLangListConstructorExpr values = (BLangListConstructorExpr) valueExpr;
                                values.exprs.stream().map(node -> node.toString()).forEach
                                        (test::addDependsOnTestFunction);
                            }
                        }
                    });
                }
                if (groups != null && !groups.isEmpty() && !groupsFound.get() && shouldIncludeGroups) {
                    // if the user has asked to run only a specific list of groups and this test doesn't have
                    // that group, we should skip the test
                    shouldSkip.set(true);
                }
                if (!shouldSkip.get()) {
                    suite.addTests(test);
                }
            } else {
                // disregard this annotation
            }
        }

    }

    /**
     * Get the function type by iterating through the packageEnvironmentMap.
     * @param pkgEnvMap map of BPackageSymbol and its respective SymbolEnv
     * @param packageID Fully qualified package ID of the respective function
     * @param functionName Name of the function
     * @return Function type if found, null if not found
     */
    private BType getFunctionType(Map<BPackageSymbol, SymbolEnv> pkgEnvMap, PackageID packageID, String functionName) {
        // Symbol resolver, Pass the acquired package Symbol from package cache
        for (Map.Entry<BPackageSymbol, SymbolEnv> entry : pkgEnvMap.entrySet()) {
            // Multiple packages may be present with same name, so all entries must be checked
            if (entry.getKey().pkgID.equals(packageID)) {
                BSymbol symbol = symbolResolver.lookupSymbolInMainSpace(entry.getValue(), new Name(functionName));
                if (!symbol.getType().toString().equals("other")) {
                    return symbol.getType();
                }
            }
        }
        return null;
    }

    /**
     * Returns a PackageID for the passed moduleName.
     * @param moduleName Module name passed via function annotation
     * @return Module packageID
     */
    private PackageID getPackageID(String moduleName) {
        if (packageCache.getSymbol(moduleName) != null) {
            return packageCache.getSymbol(moduleName).pkgID;
        } else {
            return null;
        }
    }

    /**
     * Formats the package name obtained from the mock annotation.
     * Checks for empty, '.', or single module names and replaces them.
     * Ballerina modules and fully qualified packages are simply returned
     * @param value package name
     * @return formatted package name
     */
    private String formatPackageName(String value) {
        // If empty or '.' then return the current package ID
        if (value.isEmpty() || value.equals(Names.DOT.value)) {
            value = parent.packageID.toString();

        // If value does NOT contain 'ballerina/' then it could be fully qualified
        } else if (!value.contains(Names.ORG_NAME_SEPARATOR.value) && !value.contains(Names.VERSION_SEPARATOR.value)) {
            value = new PackageID(parent.packageID.orgName, new Name(value),
                                  parent.packageID.version).toString();
        }
        return value;
    }

    /**
     * Check whether there is a common element in two Lists.
     *
     * @param inputGroups    String @{@link List} to match
     * @param functionGroups String @{@link List} to match agains
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

    private String getPackageName(PackageNode packageNode) {
        BLangPackage bLangPackage = ((BLangPackage) packageNode);
        return bLangPackage.packageID.toString();
    }

    /*private static int getTestInstructionsPosition(PackageInfo packageInfo) {
        FunctionInfo testInitFunctionInfo = packageInfo.getTestInitFunctionInfo();
        if (testInitFunctionInfo != null) {
            return testInitFunctionInfo.getDefaultWorkerInfo().getCodeAttributeInfo().getCodeAddrs();
        }
        return packageInfo.getInstructions().length;
    }*/
}
