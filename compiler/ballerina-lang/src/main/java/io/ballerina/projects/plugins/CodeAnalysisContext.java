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
package io.ballerina.projects.plugins;

import io.ballerina.toml.syntax.tree.SyntaxKind;

/**
 * Represent the context required to initialize a {@code CodeAnalyzer}.
 * <p>
 * This class can be used to add various analysis tasks during the {@code CodeAnalyzer} initialization.
 *
 * @since 2.0.0
 */
public class CodeAnalysisContext {

    /**
     * Add a code analysis task to be triggered once the compilation is completed.
     *
     * @param analysisTask the analysis task to be executed
     */
    public void addCompilationAnalysisTask(AnalysisTask<CompilationAnalysisContext> analysisTask) {
        throw new UnsupportedOperationException();

    }

    /**
     * Add a code analysis task to be triggered once the semantic analysis of a syntax node with
     * one of the specified {@code SyntaxKind}s.
     *
     * @param analysisTask the analysis task to be executed
     * @param syntaxKinds  the list of {@code SyntaxKind}s of syntax nodes on which the analysis task is triggered
     */
    public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                          SyntaxKind... syntaxKinds) {
        throw new UnsupportedOperationException();
    }
}
