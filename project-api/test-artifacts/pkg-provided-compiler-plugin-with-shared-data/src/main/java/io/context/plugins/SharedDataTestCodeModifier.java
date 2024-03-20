/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.context.plugins;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;
import io.ballerina.projects.plugins.ModifierTask;
import io.ballerina.projects.plugins.SourceModifierContext;

import java.io.PrintStream;
import java.util.Map;

/**
 * A class representing a code modifier that uses shared data within the plugin.
 *
 * @since 2201.8.7
 */
public class SharedDataTestCodeModifier extends CodeModifier {
    private static final PrintStream OUT = System.out;
    private final Map<String, Object> userData;

    /**
     * Constructor which accepts user data map.
     *
     * @param userData map of user data
     */
    public SharedDataTestCodeModifier(Map<String, Object> userData) {
        this.userData = userData;
    }

    @Override
    public void init(CodeModifierContext modifierContext) {
        modifierContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
            // Report a test diagnostic
            Util.reportDiagnostic(syntaxNodeAnalysisContext, CompilationDiagnostic.DIAG_1, new NullLocation());
        }, SyntaxKind.FUNCTION_DEFINITION);
        modifierContext.addSourceModifierTask(new ModifyTask());
        modifierContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
            // Report a test diagnostic
            Util.reportDiagnostic(syntaxNodeAnalysisContext, CompilationDiagnostic.DIAG_2, new NullLocation());
            this.userData.put("isCompleted", true);
        }, SyntaxKind.FUNCTION_DEFINITION);
    }

    private static class ModifyTask implements ModifierTask<SourceModifierContext> {
        @Override
        public void modify(SourceModifierContext modifierContext) {
            if (!(modifierContext.currentPackage().project().kind() == ProjectKind.SINGLE_FILE_PROJECT)) {
                return;
            }
            for (ModuleId moduleId : modifierContext.currentPackage().moduleIds()) {
                Module module = modifierContext.currentPackage().module(moduleId);
                OUT.println("module " + module.moduleId().toString());
            }
        }
    }
}

