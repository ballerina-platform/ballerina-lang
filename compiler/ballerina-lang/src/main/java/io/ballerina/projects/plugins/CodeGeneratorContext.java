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

import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;

/**
 * Represent the context required to initialize a {@code CodeGenerator}.
 * <p>
 * This class can be used to add various generator tasks during the {@code CodeGenerator} initialization.
 *
 * @since 2.0.0
 */
public interface CodeGeneratorContext {

    /**
     * Add a source code generator task to be triggered once the compilation is completed.
     *
     * @param generatorTask the source code generator task to be executed
     */
    void addSourceGeneratorTask(GeneratorTask<SourceGeneratorContext> generatorTask);

    /**
     * Add a code analysis task to be triggered once the semantic analysis of a syntax node with
     * one of the specified {@code SyntaxKind}s.
     *
     * @param analysisTask the analysis task to be executed
     * @param syntaxKind   the {@code SyntaxKind} of the nodes on which the analysis task is triggered
     */
    void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask, SyntaxKind syntaxKind);

    /**
     * Add a code analysis task to be triggered once the semantic analysis of a syntax node with
     * one of the specified {@code SyntaxKind}s.
     *
     * @param analysisTask the analysis task to be executed
     * @param syntaxKinds  the collection of {@code SyntaxKind}s of nodes on which the analysis task is triggered
     */
    void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                   Collection<SyntaxKind> syntaxKinds);
}
