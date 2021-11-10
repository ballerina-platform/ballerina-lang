/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.samjs.plugins.init.codegen;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeGenerator;
import io.ballerina.projects.plugins.CodeGeneratorContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code CodeGenerator} implementation that produces a source file for each function definition.
 *
 * @since 2.0.0
 */
public class InitFunctionCodeGenerator extends CodeGenerator {

    @Override
    public void init(CodeGeneratorContext generatorContext) {
        FunctionInfoHolder holder = new FunctionInfoHolder();
        generatorContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
            FunctionDefinitionNode funcNode = (FunctionDefinitionNode) syntaxNodeAnalysisContext.node();
            String funcName = funcNode.functionName().text();
            holder.functionInfoList.add(new FunctionInfo(funcName));
        }, SyntaxKind.FUNCTION_DEFINITION);

        generatorContext.addSourceGeneratorTask(sourceGeneratorContext -> {
            for (FunctionInfo functionInfo : holder.functionInfoList) {
                TextDocument textDocument = TextDocuments.from("function generated_" +
                        functionInfo.functionName() + "(){}");
                sourceGeneratorContext.addSourceFile(textDocument, "function");
            }

            TextDocument textDocument = TextDocuments.from("import package_comp_plugin_1 as _;");
            sourceGeneratorContext.addSourceFile(textDocument, "function");
        });
    }

    private static class FunctionInfoHolder {
        List<FunctionInfo> functionInfoList = new ArrayList<>();
    }

    private static class FunctionInfo {
        private final String funcName;

        public FunctionInfo(String funcName) {
            this.funcName = funcName;
        }

        String functionName() {
            return funcName;
        }
    }
}
