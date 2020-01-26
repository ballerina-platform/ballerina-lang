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
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.List;
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
    private TestSuite suite;
    private boolean enabled = true;
    /**
     * this property is used as a work-around to initialize test suites only once for a package as Compiler
     * Annotation currently emits package import events too to the process method.
     */

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        if (registry.getInstance().isTestSuitesCompiled()) {
            enabled = false;
        }
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        if (!enabled) {
            return;
        }
        String packageName = getPackageName((BLangPackage) ((BLangFunction) functionNode).parent);
        suite = registry.getTestSuites().get(packageName);
        // Check if the registry contains a test suite for the package
        if (suite == null) {
            // Add a test suite to the registry if it does not contain one pertaining to the package name
            registry.getTestSuites().computeIfAbsent(packageName, func -> new TestSuite(packageName));
            // Get the test suite related to the package from registry
            suite = registry.getTestSuites().get(packageName);
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
                // If package property not present the package is .
                // TODO: when default values are supported in annotation struct we can remove this
                vals[0] = packageName;
                if (attachmentNode.getExpression() instanceof BLangRecordLiteral) {
                    List<RecordLiteralNode.RecordField> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getFields();
                    attributes.forEach(field -> {
                        String name;
                        BLangExpression valueExpr;

                        if (field.getKind() == NodeKind.RECORD_LITERAL_KEY_VALUE) {
                            BLangRecordLiteral.BLangRecordKeyValue attributeNode =
                                    (BLangRecordLiteral.BLangRecordKeyValue) field;
                            name = attributeNode.getKey().toString();
                            valueExpr = attributeNode.getValue();
                        } else {
                            BLangSimpleVarRef varRef = (BLangSimpleVarRef) field;
                            name = varRef.variableName.value;
                            valueExpr = varRef;
                        }

                        String value = valueExpr.toString();

                        if (MODULE.equals(name)) {
                            vals[0] = value;
                        } else if (FUNCTION.equals(name)) {
                            vals[1] = value;
                        }
                    });
                    suite.addMockFunction(vals[0] + MOCK_ANNOTATION_DELIMITER + vals[1], functionName);
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

                        if (field.getKind() == NodeKind.RECORD_LITERAL_KEY_VALUE) {
                            BLangRecordLiteral.BLangRecordKeyValue attributeNode =
                                    (BLangRecordLiteral.BLangRecordKeyValue) field;
                            name = attributeNode.getKey().toString();
                            valueExpr = attributeNode.getValue();
                        } else {
                            BLangSimpleVarRef varRef = (BLangSimpleVarRef) field;
                            name = varRef.variableName.value;
                            valueExpr = varRef;
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
     * Process a given {@link TestSuite} and inject the user defined mock functions.
     *
     * @param suite a @{@link TestSuite}
     */
    public static void injectMocks(TestSuite suite) {
        /*ProgramFile programFile = suite.getProgramFile();
        Map<String, TesterinaFunction> mockFunctions = suite.getMockFunctionsMap();
        mockFunctions.forEach((k, v) -> {
            String[] info = k.split(MOCK_ANNOTATION_DELIMITER);
            if (info.length != 2) {
                return;
            }

            for (PackageInfo packageInfo : programFile.getPackageInfoEntries()) {
                int limit = getTestInstructionsPosition(packageInfo);
                Instruction[] instructions = packageInfo.getInstructions();
                for (int i = 0; i < limit; i++) {
                    Instruction ins = instructions[i];
                    if (ins instanceof Instruction.InstructionCALL) {
                        // replace the function pointer of the instruction with the mock function pointer
                        Instruction.InstructionCALL call = (Instruction.InstructionCALL) ins;
                        if (call.functionInfo.getPkgPath().equals(info[0]) && call.functionInfo.getName().equals
                                (info[1])) {
                            suite.addMockedRealFunction(k, call.functionInfo);
                            call.functionInfo = v.getbFunction();
                        }
                    }
                }
            }
        });*/
    }

    /**
     * Process a given {@link TestSuite} and reset the mock functions with their original pointers.
     *
     * @param suite a @{@link TestSuite}
     */
    public static void resetMocks(TestSuite suite) {
        /*ProgramFile programFile = suite.getProgramFile();
        Map<String, TesterinaFunction> mockFunctions = suite.getMockFunctionsMap();
        Map<String, FunctionInfo> mockedRealFunctionsMap = suite.getMockedRealFunctionsMap();

        mockFunctions.forEach((k, v) -> {
            String[] info = k.split(MOCK_ANNOTATION_DELIMITER);
            if (info.length != 2) {
                return;
            }

            for (PackageInfo packageInfo : programFile.getPackageInfoEntries()) {
                for (Instruction ins : packageInfo.getInstructions()) {
                    if (ins instanceof Instruction.InstructionCALL) {
                        Instruction.InstructionCALL call = (Instruction.InstructionCALL) ins;
                        if (call.functionInfo.getPkgPath().equals(info[0]) && call.functionInfo.getName().equals
                                (info[1])) {
                            call.functionInfo = mockedRealFunctionsMap.get(k);
                        }
                    }
                }
            }
        });*/
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
