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
package io.samjs.plugins.badsad;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.CompilerPluginException;

import java.io.PrintStream;

/**
 * A {@code CompilerPlugin} that throws {@code CompilerPluginException} error when initializing.
 *
 * @since 2.0.0
 */
public class BadSadCompilerPlugin40 extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new BadSadCodeAnalyzer());
    }

    static class BadSadCodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addCompilationAnalysisTask(compilationAnalysisContext -> {
                PrintStream out = System.out;
                try {
                    String value = getStr(-10);
                    out.println(value);
                } catch (Exception e) {
                    throw new CompilerPluginException(e.getMessage(), e);
                }
            });
        }

        String getStr(int n) throws Exception {
            if (n < 0) {
                throw new Exception("The value cannot be less than zero");
            }
            return "Looks okay";
        }
    }
}
