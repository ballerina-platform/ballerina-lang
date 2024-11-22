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

package org.ballerinalang.testerina.compiler;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visit the functions with test annotations.
 *
 * @since 2201.3.0
 */
public class TestFunctionVisitor extends NodeVisitor {

    static final List<String> TEST_STATIC_ANNOTATION_NAMES = List.of(
            "Config", "BeforeSuite", "AfterSuite", "BeforeGroups", "AfterGroups", "BeforeEach", "AfterEach");
    static final String CONFIG_ANNOTATION = "Config";
    private static final String TEST_DYNAMIC_ANNOTATION_NAME = "Factory";
    private static final String TEST_MODULE_NAME = "test";

    private final List<FunctionDefinitionNode> testSetUpTearDownFunctions;
    private final List<FunctionDefinitionNode> testDynamicFunctions;
    private final List<FunctionDefinitionNode> testFunctions;
    private final List<FunctionDefinitionNode> normalFunctions;

    public TestFunctionVisitor() {
        this.testSetUpTearDownFunctions = new ArrayList<>();
        this.testDynamicFunctions = new ArrayList<>();
        this.testFunctions = new ArrayList<>();
        this.normalFunctions = new ArrayList<>();
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
        super.visit(modulePartNode);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.parent().kind() == SyntaxKind.CLASS_DEFINITION) {
            return;
        }
        Optional<MetadataNode> metadataNodeOptional = functionDefinitionNode.metadata();
        if (metadataNodeOptional.isPresent()) {
            MetadataNode metadataNode = metadataNodeOptional.get();
            for (AnnotationNode annotation : metadataNode.annotations()) {
                if (annotation.annotReference().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    continue;
                }
                QualifiedNameReferenceNode qualifiedNameReferenceNode =
                        (QualifiedNameReferenceNode) annotation.annotReference();
                String modulePrefix = qualifiedNameReferenceNode.modulePrefix().text();
                String identifier = qualifiedNameReferenceNode.identifier().text();
                if (TEST_MODULE_NAME.equals(modulePrefix)) {
                    if (TEST_STATIC_ANNOTATION_NAMES.contains(identifier)) {
                        if (CONFIG_ANNOTATION.equals(identifier)) {
                            testFunctions.add(functionDefinitionNode);
                        } else {
                            testSetUpTearDownFunctions.add(functionDefinitionNode);
                        }
                    } else if (TEST_DYNAMIC_ANNOTATION_NAME.equals(identifier)) {
                        testDynamicFunctions.add(functionDefinitionNode);
                    }
                }
            }
        } else {
            normalFunctions.add(functionDefinitionNode);
        }
    }

    public List<FunctionDefinitionNode> getTestStaticFunctions() {
        List<FunctionDefinitionNode> testStaticFunctions = new ArrayList<>(testSetUpTearDownFunctions);
        testStaticFunctions.addAll(testFunctions);
        return testStaticFunctions;
    }

    public List<FunctionDefinitionNode> getNormalFunctions() {
        return normalFunctions;
    }

    public List<FunctionDefinitionNode> getTestDynamicFunctions() {
        return this.testDynamicFunctions;
    }
}
