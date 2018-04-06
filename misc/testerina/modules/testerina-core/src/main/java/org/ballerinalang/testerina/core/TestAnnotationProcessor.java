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
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Responsible of processing testerina annotations. This class is an implementation of the
 * {@link org.ballerinalang.compiler.plugins.CompilerPlugin}. Lifetime of an instance of this class will end upon the
 * completion of processing a ballerina package.
 */
@SupportedAnnotationPackages(
        value = "ballerina.test"
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
    private static final String PACKAGE = "packageName";
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
    private boolean packageInit;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        if (registry.getInstance().isTestSuitesCompiled()) {
            enabled = false;
        }
    }

    @Override
    public void process(PackageNode packageNode) {
        if (!enabled) {
            return;
        }
        if (!packageInit) {
            String packageName = ((BLangPackage) packageNode).packageID == null ? "." : ((BLangPackage) packageNode)
                    .packageID.getName().getValue();
            suite = registry.getTestSuites().computeIfAbsent(packageName, func -> new TestSuite(packageName));
            packageInit = true;
        }
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        if (!enabled) {
            return;
        }
        // annotation processor currently triggers this function for the functions of imported packages too. In order
        // to avoid processing those, we have to have below check.
        if (!suite.getSuiteName().equals(functionNode.getPosition().getSource().getPackageName())) {
            return;
        }
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
                vals[0] = ".";
                if (attachmentNode.getExpression() instanceof BLangRecordLiteral) {
                    List<BLangRecordLiteral.BLangRecordKeyValue> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getKeyValuePairs();
                    attributes.forEach(attributeNode -> {
                        String name = attributeNode.getKey().toString();
                        String value = attributeNode.getValue().toString();
                        if (PACKAGE.equals(name)) {
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
                    List<BLangRecordLiteral.BLangRecordKeyValue> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getKeyValuePairs();

                    attributes.forEach(attributeNode -> {
                        String name = attributeNode.getKey().toString();
                        // Check if enable property is present in the annotation
                        if (TEST_ENABLE_ANNOTATION_NAME.equals(name) && "false".equals(attributeNode.getValue()
                                .toString())) {
                            // If enable is false, disable the test, no further processing is needed
                            shouldSkip.set(true);
                            return;
                        }

                        // check if groups attribute is present in the annotation
                        if (GROUP_ANNOTATION_NAME.equals(name)) {
                            if (attributeNode.getValue() instanceof BLangArrayLiteral) {
                                BLangArrayLiteral values = (BLangArrayLiteral) attributeNode.getValue();
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
                            test.setDataProvider(attributeNode.getValue().toString());
                        }

                        if (BEFORE_FUNCTION.equals(name)) {
                            test.setBeforeTestFunction(attributeNode.getValue().toString());
                        }

                        if (AFTER_FUNCTION.equals(name)) {
                            test.setAfterTestFunction(attributeNode.getValue().toString());
                        }

                        if (DEPENDS_ON_FUNCTIONS.equals(name)) {
                            if (attributeNode.getValue() instanceof BLangArrayLiteral) {
                                BLangArrayLiteral values = (BLangArrayLiteral) attributeNode.getValue();
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
     * TODO this is a temporary solution, till we get a proper API from Ballerina Core.
     * This method will get executed at the completion of the processing of a ballerina package.
     *
     * @param programFile {@link ProgramFile} corresponds to the current ballerina package
     */
    public void packageProcessed(ProgramFile programFile) {
        if (!enabled) {
            return;
        }
        packageInit = false;
        // TODO the below line is required since this method is currently getting explicitly called from BTestRunner
        suite = TesterinaRegistry.getInstance().getTestSuites().get(programFile.getEntryPkgName());
        if (suite == null) {
            throw new BallerinaException("No test suite found for [package]: " + programFile.getEntryPkgName());
        }
        suite.setInitFunction(new TesterinaFunction(programFile, programFile.getEntryPackage().getInitFunctionInfo(),
                TesterinaFunction.Type.INIT));
        // add all functions of the package as utility functions
        Arrays.stream(programFile.getEntryPackage().getFunctionInfoEntries()).forEach(functionInfo -> {
            suite.addTestUtilityFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type.UTIL));
        });
        resolveFunctions(suite);
        int[] testExecutionOrder = checkCyclicDependencies(suite.getTests());
        List<Test> sortedTests = orderTests(suite.getTests(), testExecutionOrder);
        suite.setTests(sortedTests);
        suite.setProgramFile(programFile);
    }

    /**
     * Process a given {@link TestSuite} and inject the user defined mock functions.
     *
     * @param suite a @{@link TestSuite}
     */
    public static void injectMocks(TestSuite suite) {
        ProgramFile programFile = suite.getProgramFile();
        Map<String, TesterinaFunction> mockFunctions = suite.getMockFunctionsMap();
        mockFunctions.forEach((k, v) -> {
            String[] info = k.split(MOCK_ANNOTATION_DELIMITER);
            if (info.length != 2) {
                return;
            }

            for (PackageInfo packageInfo : programFile.getPackageInfoEntries()) {
                for (Instruction ins : packageInfo.getInstructions()) {
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
        });
    }

    /**
     * Process a given {@link TestSuite} and reset the mock functions with their original pointers.
     *
     * @param suite a @{@link TestSuite}
     */
    public static void resetMocks(TestSuite suite) {
        ProgramFile programFile = suite.getProgramFile();
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
        });
    }

    private static List<Test> orderTests(List<Test> tests, int[] testExecutionOrder) {
        List<Test> sortedTests = new ArrayList<>();
//        outStream.println("Test execution order: ");
        for (int idx : testExecutionOrder) {
            sortedTests.add(tests.get(idx));
//            outStream.println(sortedTests.get(sortedTests.size() - 1).getTestFunction().getName());
        }
//        outStream.println("**********************");
        return sortedTests;
    }

    /**
     * Resolve function names to {@link TesterinaFunction}s.
     *
     * @param suite {@link TestSuite} whose functions to be resolved.
     */
    private static void resolveFunctions(TestSuite suite) {
        List<TesterinaFunction> functions = suite.getTestUtilityFunctions();
        List<String> functionNames = functions.stream().map(testerinaFunction -> testerinaFunction.getName()).collect
                (Collectors.toList());
        for (Test test : suite.getTests()) {
            if (test.getTestName() != null && functionNames.contains(test.getTestName())) {
                test.setTestFunction(functions.stream().filter(e -> e.getName().equals(test
                        .getTestName())).findFirst().get());
            }

            if (test.getBeforeTestFunction() != null && functionNames.contains(test.getBeforeTestFunction())) {
                test.setBeforeTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                        .getBeforeTestFunction())).findFirst().get());
            }

            if (test.getAfterTestFunction() != null && functionNames.contains(test.getAfterTestFunction())) {
                test.setAfterTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                        .getAfterTestFunction())).findFirst().get());
            }

            if (test.getDataProvider() != null && functionNames.contains(test.getDataProvider())) {
                String dataProvider = test.getDataProvider();
                test.setDataProviderFunction(functions.stream().filter(e -> e.getName().equals(test.getDataProvider()
                )).findFirst().map(func -> {
                    // TODO these validations are not working properly with the latest refactoring
                    if (func.getbFunction().getRetParamTypes().length == 1) {
                        BType bType = func.getbFunction().getRetParamTypes()[0];
                        if (bType.getTag() == TypeTags.ARRAY_TAG) {
                            BArrayType bArrayType = (BArrayType) bType;
                            if (bArrayType.getElementType().getTag() != TypeTags.ARRAY_TAG) {
                                String message = String.format("Data provider function [%s] should return an array of" +
                                        " arrays.", dataProvider);
                                throw new BallerinaException(message);
                            }
                        } else {
                            String message = String.format("Data provider function [%s] should return an array of " +
                                    "arrays.", dataProvider);
                            throw new BallerinaException(message);
                        }
                    } else {
                        String message = String.format("Data provider function [%s] should have only one return type" +
                                ".", dataProvider);
                        throw new BallerinaException(message);
                    }
                    return func;
                }).get());

                if (test.getDataProviderFunction() == null) {
                    String message = String.format("Data provider function [%s] cannot be found.", dataProvider);
                    throw new BallerinaException(message);
                }
            }
            for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                if (!functions.stream().parallel().anyMatch(func -> func.getName().equals(dependsOnFn))) {
                    throw new BallerinaException("Cannot find the specified dependsOn function : " + dependsOnFn);
                }
                test.addDependsOnTestFunction(functions.stream().filter(e -> e.getName().equals(dependsOnFn))
                                                       .findFirst().get());
            }
        }

        // resolve mock functions
        suite.getMockFunctionNamesMap().forEach((id, functionName) -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addMockFunctionObj(id, function);
        });

        suite.getBeforeSuiteFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addBeforeSuiteFunctionObj(function);
        });

        suite.getAfterSuiteFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addAfterSuiteFunctionObj(function);
        });

        suite.getBeforeEachFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addBeforeEachFunctionObj(function);
        });

        suite.getAfterEachFunctionNames().forEach(functionName -> {
            TesterinaFunction function = suite.getTestUtilityFunctions().stream().filter(e -> e.getName().equals
                    (functionName)).findFirst().get();
            suite.addAfterEachFunctionObj(function);
        });

    }

    private static int[] checkCyclicDependencies(List<Test> tests) {
        int numberOfNodes = tests.size();
        int[] indegrees = new int[numberOfNodes];
        int[] sortedElts = new int[numberOfNodes];

        List<Integer> dependencyMatrix[] = new ArrayList[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            dependencyMatrix[i] = new ArrayList<>();
        }
        List<String> testNames = tests.stream().map(k -> k.getTestName()).collect(Collectors.toList());

        int i = 0;
        for (Test test : tests) {
            if (!test.getDependsOnTestFunctions().isEmpty()) {
                for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                    int idx = testNames.indexOf(dependsOnFn);
                    if (idx == -1) {
                        String message = String.format("Test [%s] depends on function [%s], but it couldn't be found" +
                                ".", test.getTestFunction().getName(), dependsOnFn);
                        throw new BallerinaException(message);
                    }
                    dependencyMatrix[i].add(idx);
                }
            }
            i++;
        }

        // fill in degrees
        for (int j = 0; j < numberOfNodes; j++) {
            List<Integer> dependencies = dependencyMatrix[j];
            for (int node : dependencies) {
                indegrees[node]++;
            }

        }

        // Create a queue and enqueue all vertices with indegree 0
        Queue<Integer> q = new LinkedList<Integer>();
        for (i = 0; i < numberOfNodes; i++) {
            if (indegrees[i] == 0) {
                q.add(i);
            }
        }

        // Initialize count of visited vertices
        int cnt = 0;

        // Create a vector to store result (A topological ordering of the vertices)
        Vector<Integer> topOrder = new Vector<Integer>();
        while (!q.isEmpty()) {
            // Extract front of queue (or perform dequeue) and add it to topological order
            int u = q.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes of dequeued node u and decrease their in-degree by 1
            for (int node : dependencyMatrix[u]) {
                // If in-degree becomes zero, add it to queue
                if (--indegrees[node] == 0) {
                    q.add(node);
                }
            }
            cnt++;
        }

        // Check if there was a cycle
        if (cnt != numberOfNodes) {
            String message = "Cyclic test dependency detected";
            throw new BallerinaException(message);
        }

        i = numberOfNodes - 1;
        for (int elt : topOrder) {
            sortedElts[i] = elt;
            i--;
        }

        return sortedElts;
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
}
