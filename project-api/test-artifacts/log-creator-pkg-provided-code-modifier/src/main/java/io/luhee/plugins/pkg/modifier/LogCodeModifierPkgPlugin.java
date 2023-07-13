/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.luhee.plugins.pkg.modifier;


import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;


/***
 * A package provided code modifier which adds a log statement to the beginning of the file.
 */
public class LogCodeModifierPkgPlugin extends CompilerPlugin {

    static String filePath = "./src/test/resources/compiler_plugin_tests/" +
            "log_creator_combined_plugin/compiler-plugin.txt";

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeModifier(new LogCodeModifier());
    }



    public static class LogCodeModifier extends CodeModifier {
        @Override
        public void init(CodeModifierContext modifierContext) {
            modifierContext.addSourceModifierTask(sourceGeneratorContext -> {
                appendToOutputFile(filePath, "source-modifier");
            });

            modifierContext.addSyntaxNodeAnalysisTask(new LogSyntaxNodeAnalysis(), SyntaxKind.FUNCTION_DEFINITION);
        }

    }

    public static class LogSyntaxNodeAnalysis implements AnalysisTask<SyntaxNodeAnalysisContext> {

        @Override
        public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
            appendToOutputFile(filePath, "syntax-node-analysis-modifier");
        }
    }

    private static void appendToOutputFile(String filePath, String content) {
        File outputFile = new File(filePath);
        try (FileOutputStream fileStream = new FileOutputStream(outputFile, true);
             Writer writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)) {
            writer.write("pkg-provided-" + content + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
