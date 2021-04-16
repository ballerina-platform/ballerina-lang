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
package io.samjs.plugins.eventlogger;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

import java.io.PrintStream;

/**
 * A sample {@code CompilerPlugin} that logs events.
 *
 * @since 2.0.0
 */
public class EventLoggerPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new LogCodeAnalyzer());
    }

    /**
     * A sample {@code CodeAnalyzer} that logs events.
     *
     * @since 2.0.0
     */
    public static class LogCodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            PrintStream out = System.out;
            analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
                // 1)
                out.println("EventLoggerPlugin:LogCodeAnalyzer:CompilationAnalysisTask 1 called");

                // 2) Reporting diagnostics
            });

            analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
                // 1)
                out.println("EventLoggerPlugin:LogCodeAnalyzer:CompilationAnalysisTask 2 called");
            });
        }
    }
}
