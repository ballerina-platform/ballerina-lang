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

/**
 * Visit the functions with test annotations.
 *
 * @since 2201.3.0
 */
public class TestFunctionVisitor extends NodeVisitor {

    private static final List<String> TEST_STATIC_ANNOTATION_NAMES = List.of(
            "Config", "BeforeSuite", "AfterSuite", "BeforeGroups", "AfterGroups", "BeforeEach", "AfterEach");
    private static final String TEST_DYNAMIC_ANNOTATION_NAME = "Factory";
    private static final String TEST_MODULE_NAME = "test";

    private final List<FunctionDefinitionNode> testStaticFunctions;
    private final List<FunctionDefinitionNode> testDynamicFunctions;

    public TestFunctionVisitor() {
        this.testStaticFunctions = new ArrayList<>();
        this.testDynamicFunctions = new ArrayList<>();
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
        super.visit(modulePartNode);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.metadata().isPresent()) {
            MetadataNode metadataNode = functionDefinitionNode.metadata().get();
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
                        testStaticFunctions.add(functionDefinitionNode);
                    } else if (TEST_DYNAMIC_ANNOTATION_NAME.equals(identifier)) {
                        testDynamicFunctions.add(functionDefinitionNode);
                    }
                }
            }
        }
    }

    public List<FunctionDefinitionNode> getTestStaticFunctions() {
        return this.testStaticFunctions;
    }

    public List<FunctionDefinitionNode> getTestDynamicFunctions() {
        return this.testDynamicFunctions;
    }
}
