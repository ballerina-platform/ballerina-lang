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
package io.ballerina.projects;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.Collection;

/**
 * A wrapper class for the syntax analysis task.
 *
 * @since 2.0.0
 */
class SyntaxNodeAnalysisTask {
    private final AnalysisTask<SyntaxNodeAnalysisContext> analysisTask;
    private final Collection<SyntaxKind> syntaxKinds;
    private final CompilerPluginInfo compilerPluginInfo;

    SyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                           Collection<SyntaxKind> syntaxKinds,
                           CompilerPluginInfo compilerPluginInfo) {
        this.analysisTask = analysisTask;
        this.syntaxKinds = syntaxKinds;
        this.compilerPluginInfo = compilerPluginInfo;
    }

    void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        try {
            analysisTask.perform(syntaxNodeAnalysisContext);
        } catch (Throwable e) {
            // Used Throwable here catch any sort of error produced by the third-party compiler plugin code
            String message;
            if (compilerPluginInfo.kind().equals(CompilerPluginKind.PACKAGE_PROVIDED)) {
                PackageProvidedCompilerPluginInfo pkgProvidedCompilerPluginInfo =
                        (PackageProvidedCompilerPluginInfo) compilerPluginInfo;
                PackageDescriptor pkgDesc = pkgProvidedCompilerPluginInfo.packageDesc();
                message = "The compiler extension in package '" +
                        pkgDesc.org() +
                        ":" + pkgDesc.name() +
                        ":" + pkgDesc.version() + "' failed to complete. ";
            } else {
                message = "The compiler extension '" + compilerPluginInfo.compilerPlugin().getClass().getName()
                        + "' failed to complete. ";
            }
            throw new ProjectException(message + e.getMessage(), e);
        }
    }

    Collection<SyntaxKind> syntaxKinds() {
        return syntaxKinds;
    }
}
