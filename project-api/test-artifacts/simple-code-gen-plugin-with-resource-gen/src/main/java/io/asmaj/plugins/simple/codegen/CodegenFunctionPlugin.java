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
package io.asmaj.plugins.simple.codegen;

import io.ballerina.projects.plugins.CodeGenerator;
import io.ballerina.projects.plugins.CodeGeneratorContext;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.nio.charset.Charset;

/**
 * A sample {@code CompilerPlugin} that generates a source file and a resource file.
 *
 * @since 2.0.0
 */
public class CodegenFunctionPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeGenerator(new DummySourceGenerator());
        pluginContext.addCodeGenerator(new OpenApiSpecGenerator());
    }

    /**
     * A sample {@code CodeGenerator} that creates a resource file.
     *
     * @since 2.0.0
     */
    public static class OpenApiSpecGenerator extends CodeGenerator {
        @Override
        public void init(CodeGeneratorContext generatorContext) {
            generatorContext.addSourceGeneratorTask(sourceGeneratorContext -> {
                sourceGeneratorContext.addResourceFile("".getBytes(Charset.defaultCharset()), "openapi-spec.yaml");
            });
        }
    }

    /**
     * A sample {@code CodeGenerator} that creates a source file with a function.
     *
     * @since 2.0.0
     */
    public static class DummySourceGenerator extends CodeGenerator {
        @Override
        public void init(CodeGeneratorContext generatorContext) {
            String content = "function dummyUtil() {}";
            generatorContext.addSourceGeneratorTask(sourceGeneratorContext -> {
                TextDocument textDocument = TextDocuments.from(content);
                sourceGeneratorContext.addSourceFile(textDocument, "dummyfunc");
            });
        }
    }

}
