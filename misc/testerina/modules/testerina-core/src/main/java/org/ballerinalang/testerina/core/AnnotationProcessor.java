/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.core;

import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaAnnotation;
import org.ballerinalang.testerina.core.entity.TesterinaContext;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Process annotations added to a test function.
 *
 * @since 0.963.0
 */
public class AnnotationProcessor {

    private static final String TEST_ANNOTATION_NAME = "config";
    private static final String BEFORE_SUITE_ANNOTATION_NAME = "beforeSuite";
    private static final String AFTER_SUITE_ANNOTATION_NAME = "afterSuite";
    private static final String BEFORE_FUNCTION = "before";
    private static final String AFTER_FUNCTION = "after";
    private static final String DEPENDS_ON_FUNCTIONS = "dependsOn";
    private static final String BEFORE_EACH_ANNOTATION_NAME = "beforeEach";
    private static final String AFTER_EACH_ANNOTATION_NAME = "afterEach";
    private static final String MOCK_ANNOTATION = "mock";
    private static final String PACKAGE = "packageName";
    private static final String FUNCTION = "functionName";
    private static final String GROUP_ANNOTATION_NAME = "groups";
    private static final String VALUE_SET_ANNOTATION_NAME = "valueSets";
    private static final String TEST_DISABLE_ANNOTATION_NAME = "disabled";
    private static final String DEFAULT_TEST_GROUP_NAME = "default";
    private static PrintStream outStream = System.out;

    public static void processAnnotations(TesterinaContext ctxt, ProgramFile programFile, PackageInfo packageInfo,
                                          TestSuite suite,
                                          List<String> groups, boolean excludeGroups) {

        FunctionInfo[] functionInfos = packageInfo.getFunctionInfoEntries();

        for (FunctionInfo functionInfo : functionInfos) {

            AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) functionInfo.getAttributeInfo
                    (AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

            process(ctxt, functionInfo, attributeInfo.getAttachmentInfoEntries(), programFile, suite, groups,
                    excludeGroups);

        }

        int[] sortedElts = checkCyclicDependencies(suite.getTests());
        resolveFunctions(suite);
//        injectMocks(suite.getMockFunctionsMap(), programFile);
        List<Test> sortedTests = orderTests(suite.getTests(), sortedElts);
        suite.setTests(sortedTests);
        suite.addProgramFile(programFile);
    }

    public static void injectMocks(Map<String, TesterinaFunction> mockFunctions, ProgramFile programFile) {
        mockFunctions.forEach((k, v) -> {
            String[] info = k.split("#");
            if (info.length != 2) {
                return;
            }

            for (PackageInfo packageInfo: programFile.getPackageInfoEntries()) {
                for (Instruction ins : packageInfo.getInstructions()) {
                    if (ins instanceof Instruction.InstructionCALL) {
                        Instruction.InstructionCALL call = (Instruction.InstructionCALL) ins;
                        if (call.functionInfo.getName().equals(info[1])) {
                            call.functionInfo = v.getbFunction();
                        }
                    }
                }
            }
        });
    }

    private static List<Test> orderTests(List<Test> tests, int[] sortedElts) {
        List<Test> sortedTests = new ArrayList<>();
        outStream.println("Test execution order: ");
        for (int idx : sortedElts) {
            sortedTests.add(tests.get(idx));
            outStream.println(sortedTests.get(sortedTests.size() - 1).getTestFunction().getName());
        }
        outStream.println("**********************");
        return sortedTests;
    }

    private static void resolveFunctions(TestSuite suite) {
        List<TesterinaFunction> functions = suite.getTestUtilityFunctions();
        List<String> functionNames = functions.stream().map(testerinaFunction -> testerinaFunction.getName()).collect
                (Collectors.toList());
        for (Test test : suite.getTests()) {
            if (test.getBeforeTestFunction() != null && functionNames.contains(test.getBeforeTestFunction())) {
                //TODO handle missing func case
                test.setBeforeTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                        .getBeforeTestFunction())).findFirst().get());
            }

            if (test.getAfterTestFunction() != null && functionNames.contains(test.getAfterTestFunction())) {
                //TODO handle missing func case
                test.setAfterTestFunctionObj(functions.stream().filter(e -> e.getName().equals(test
                        .getAfterTestFunction())).findFirst().get());
            }
            for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                //TODO handle missing func case
                test.addDependsOnTestFunction(suite.getTests().stream().filter(e -> e.getTestFunction().getName()
                        .equals(dependsOnFn)).findFirst().get().getTestFunction());
            }
        }

    }

    private static int[] checkCyclicDependencies(List<Test> tests) {
        int numberOfNodes = tests.size();
        int[] indegrees = new int[numberOfNodes];
        int[] sortedElts = new int[numberOfNodes];

        List<Integer> dependencyMatrix[] = new ArrayList[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            dependencyMatrix[i] = new ArrayList<>();
        }
//        for (int i=0; i<tests.size();i++) {
//            for (int j = 0; j < tests.size(); j++) {
//                dependencyMatrix[i][j] = 0;
//            }
//        }
        List<String> testNames = tests.stream().map(k -> k.getTestFunction().getName()).collect(Collectors.toList());

        int i = 0;
        for (Test test : tests) {
            if (!test.getDependsOnTestFunctions().isEmpty()) {
                for (String dependsOnFn : test.getDependsOnTestFunctions()) {
                    int idx = testNames.indexOf(dependsOnFn);
                    if (idx == -1) {
                        throw new BallerinaException(String.format("Test [%s] depends on function [%s], but it " +
                                "couldn't be found.", test.getTestFunction().getName(), dependsOnFn));
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

        // Create a vector to store result (A topological
        // ordering of the vertices)
        Vector<Integer> topOrder = new Vector<Integer>();
        while (!q.isEmpty()) {
            // Extract front of queue (or perform dequeue)
            // and add it to topological order
            int u = q.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes
            // of dequeued node u and decrease their in-degree
            // by 1
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
            throw new BallerinaException("Cyclic test dependency detected");
        }

        i = numberOfNodes - 1;
        for (int elt : topOrder) {
            sortedElts[i] = elt;
            i--;
        }

        return sortedElts;

    }

//    public static boolean dfs(int[][] adjacencyMatrix, int source, int[] sortedElts) {
//        int numberOfNodes = adjacencyMatrix[source].length;
//        AnnotationProcessor.adjacencyMatrix = adjacencyMatrix;
//
////        adjacencyMatrix = new int[number_of_nodes + 1][number_of_nodes + 1];
////        for (int sourcevertex = 1; sourcevertex <= number_of_nodes; sourcevertex++)
////        {
////            for (int destinationvertex = 1; destinationvertex <= number_of_nodes; destinationvertex++)
////            {
////                adjacencyMatrix[sourcevertex][destinationvertex] =
////                        adjacency_matrix[sourcevertex][destinationvertex];
////            }
////        }
//
//        int visited[] = new int[numberOfNodes];
//        int element;
//        int destination;
//        visited[source] = 1;
//        stack.push(source);
//        boolean eltSet = false;
//
//        while (!stack.isEmpty()) {
//            element = stack.peek();
//            destination = element;
//            while (destination < numberOfNodes) {
//                if (AnnotationProcessor.adjacencyMatrix[element][destination] == 1 && visited[destination] == 1) {
//                    if (stack.contains(destination)) {
//                        return true;
//                    }
//                }
//
//                if (AnnotationProcessor.adjacencyMatrix[element][destination] == 1 && visited[destination] == 0) {
//                    stack.push(destination);
//                    visited[destination] = 1;
//                    AnnotationProcessor.adjacencyMatrix[element][destination] = 0;
//                    element = destination;
//                    sortedElts[source] = element;
//                    eltSet = true;
//                    destination = 0;
//                    continue;
//                }
//                destination++;
//            }
//            stack.pop();
//        }
//
//        if (!eltSet) {
//            sortedElts[source] = source;
//        }
//
//        return false;
//    }

    /**
     * Takes @{@link FunctionInfo} as a input and process annotations attached to the function.
     * Processor only returns the tFunctions that should only be executed.
     * All the test function that are excluded due to disabling or group filtering will not be returned.
     *
     * @param programFile  Ballerina program file
     * @param functionInfo ballerina FunctionInfo object
     * @return @{@link TesterinaAnnotation} object containing annotation information
     */
    private static void process(TesterinaContext ctxt, FunctionInfo functionInfo, AnnAttachmentInfo[] annotations,
                                ProgramFile
            programFile,
                                TestSuite suite, List<String> groups, boolean excludeGroups) {
        boolean functionAdded = false, functionSkipped = false;
        for (AnnAttachmentInfo attachmentInfo : annotations) {
            if (attachmentInfo.getName().equals(BEFORE_SUITE_ANNOTATION_NAME)) {
                suite.addBeforeSuiteFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type
                        .BEFORE_TEST));
                functionAdded = true;
            } else if (attachmentInfo.getName().equals(AFTER_SUITE_ANNOTATION_NAME)) {
                suite.addAfterSuiteFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type
                        .AFTER_TEST));
                functionAdded = true;
            } else if (attachmentInfo.getName().equals(BEFORE_EACH_ANNOTATION_NAME)) {
                suite.addBeforeEachFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type
                        .BEFORE_TEST));
                functionAdded = true;
            } else if (attachmentInfo.getName().equals(AFTER_EACH_ANNOTATION_NAME)) {
                suite.addAfterEachFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type
                        .AFTER_TEST));
                functionAdded = true;
            } else if (attachmentInfo.getName().equals(MOCK_ANNOTATION)) {
                String pkg = ".", functionName = "";
                if (attachmentInfo.getAttributeValue(PACKAGE) != null) {
                    pkg = attachmentInfo.getAttributeValue(PACKAGE).getStringValue();
                }
                if (attachmentInfo.getAttributeValue(FUNCTION) != null) {
                    functionName = attachmentInfo.getAttributeValue(FUNCTION).getStringValue();
                }
                ctxt.addMockFunction(pkg + "#" + functionName, new TesterinaFunction(programFile, functionInfo,
                        TesterinaFunction.Type.MOCK));
                functionAdded = true;
            } else {
                if (attachmentInfo.getName().equals(TEST_ANNOTATION_NAME)) {
                    Test test = new Test();
                    TesterinaFunction tFunction = new TesterinaFunction(programFile, functionInfo, TesterinaFunction
                            .Type.TEST);
                    test.setTestFunction(tFunction);

                    // Check if disabled property is present in the annotation
                    if (attachmentInfo.getAttributeValue(TEST_DISABLE_ANNOTATION_NAME) != null && attachmentInfo
                            .getAttributeValue(TEST_DISABLE_ANNOTATION_NAME).getBooleanValue()) {
                        // If disable property is present disable the test, no further processing is needed
                        functionSkipped = true;
                        break;
                    }
                    // Check whether user has provided a group list
//                    if (groups != null) {
//                        // check if groups attribute is present in the annotation
//                        if (attachmentInfo.getAttributeValue(GROUP_ANNOTATION_NAME) != null) {
//                            // Check whether function is included in group filter
//                            // against the user provided flag to include or exclude groups
//                            if (isGroupAvailable(groups, Arrays.stream(attachmentInfo.getAttributeValue
//                                    (GROUP_ANNOTATION_NAME).getAttributeValueArray()).map
//                                    (AnnAttributeValue::getStringValue).collect(Collectors.toList())) ==
//                                    excludeGroups) {
//                                functionSkipped = true;
//                                break;
//                            }
//                            // If groups are not present this belongs to default group
//                            // check whether user provided groups has default group
//                        } else if (isGroupAvailable(groups, Arrays.asList(DEFAULT_TEST_GROUP_NAME)) ==
// excludeGroups) {
//                            functionSkipped = true;
//                            break;
//                        }
//                    }
                    // Check the availability of value sets
                    if (attachmentInfo.getAttributeValue(VALUE_SET_ANNOTATION_NAME) != null) {
                        // extracts the value sets
                        tFunction.setValueSet(Arrays.stream(attachmentInfo.getAttributeValue
                                (VALUE_SET_ANNOTATION_NAME).getAttributeValueArray()).map(f -> f.getStringValue()
                                .split(",")).collect(Collectors.toList()));
                    }

                    if (attachmentInfo.getAttributeValue(BEFORE_FUNCTION) != null) {
                        test.setBeforeTestFunction(attachmentInfo.getAttributeValue(BEFORE_FUNCTION).getStringValue());
                    }

                    if (attachmentInfo.getAttributeValue(AFTER_FUNCTION) != null) {
                        test.setAfterTestFunction(attachmentInfo.getAttributeValue(AFTER_FUNCTION).getStringValue());
                    }

                    if (attachmentInfo.getAttributeValue(DEPENDS_ON_FUNCTIONS) != null) {
                        AnnAttributeValue[] dependsOnFns = attachmentInfo.getAttributeValue(DEPENDS_ON_FUNCTIONS)
                                .getAttributeValueArray();
                        for (AnnAttributeValue value : dependsOnFns) {
                            test.addDependsOnTestFunction(value.getStringValue());
                        }

                    }

                    suite.addTests(test);
                    functionAdded = true;
                }
            }

        }
        if (!functionAdded && !functionSkipped) {
            suite.addTestUtilityFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type.TEST));
        }
    }

//    /**
//     * Check whether there is a common element in two Lists.
//     *
//     * @param inputGroups    String @{@link List} to match
//     * @param functionGroups String @{@link List} to match agains
//     * @return true if a match is found
//     */
//    private static boolean isGroupAvailable(List<String> inputGroups, List<String> functionGroups) {
//        for (String group : inputGroups) {
//            for (String funcGroup : functionGroups) {
//                if (group.equals(funcGroup)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}
