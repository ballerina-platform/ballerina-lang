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

import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.samjs.jarlibrary.diagnosticutils.DiagnosticUtils;

import java.io.PrintStream;

/**
 * A {@code CompilerPlugin} that throws no class def error when loading the class.
 *
 * @since 2.0.0
 */
public class BadSadCompilerPlugin6 extends CompilerPlugin {

    static {
        // The DiagnosticUtils class will not be available at the runtime
        Diagnostic diagnostic = DiagnosticUtils.createDiagnostic("", "",
                null, DiagnosticSeverity.ERROR);
        PrintStream out = System.out;
        out.println(diagnostic);
    }

    @Override
    public void init(CompilerPluginContext pluginContext) {
    }
}
