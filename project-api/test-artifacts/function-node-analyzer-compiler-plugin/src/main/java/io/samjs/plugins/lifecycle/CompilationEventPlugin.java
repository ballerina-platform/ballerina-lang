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
package io.samjs.plugins.lifecycle;


import io.ballerina.projects.plugins.CompilerLifecycleContext;
import io.ballerina.projects.plugins.CompilerLifecycleListener;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import io.samjs.jarlibrary.diagnosticutils.DiagnosticUtils;

/**
 * A simple {@code CompilerPlugin}.
 *
 * @since 2.0.0
 */
public class CompilationEventPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCompilerLifecycleListener(new CompilationListener());
    }

    private static class CompilationListener extends CompilerLifecycleListener {
        @Override
        public void init(CompilerLifecycleContext lifecycleContext) {
            lifecycleContext.addCodeGenerationCompletedTask(compilerLifecycleEventContext ->
                compilerLifecycleEventContext.reportDiagnostic(DiagnosticUtils.createDiagnostic(
                        "CODE_GEN_COMPLETED_TEST_EVENT",
                        "End of codegen", new NullLocation(),
                        DiagnosticSeverity.WARNING)));
        }
    }

    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
