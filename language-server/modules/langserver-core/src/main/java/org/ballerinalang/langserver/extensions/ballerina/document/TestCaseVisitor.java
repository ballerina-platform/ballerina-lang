/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;

import java.nio.file.Path;

/**
 * Visitor class for testcases.
 */
public class TestCaseVisitor extends NodeVisitor {

    private final JsonArray execPositions;
    private final Path filePath;

    TestCaseVisitor(JsonArray execPositions, Path filePath) {
        this.execPositions = execPositions;
        this.filePath = filePath;
    }

    public void visitTestCases(Node node) {
        visitSyntaxNode(node);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.metadata().isPresent()) {
            functionDefinitionNode.metadata().get().annotations().stream()
                    .filter(annotationNode -> annotationNode.annotReference().toString().trim()
                            .equals(ExecutorPositionsUtil.TEST_CONFIG))
                    .toList()
                    .forEach(annotationNode -> {
                        JsonObject testCase = new JsonObject();
                        testCase.addProperty(ExecutorPositionsUtil.KIND, ExecutorPositionsUtil.TEST);
                        testCase.addProperty(ExecutorPositionsUtil.NAME, functionDefinitionNode.functionName().text());
                        testCase.add(ExecutorPositionsUtil.RANGE,
                                ExecutorPositionsUtil.GSON.toJsonTree(functionDefinitionNode.location().lineRange()));
                        testCase.addProperty(ExecutorPositionsUtil.FILE_PATH, this.filePath.toString());
                        this.execPositions.add(testCase);
                    });
        }
    }
}
