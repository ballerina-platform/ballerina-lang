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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * This class provides a context for the syntax node analysis task.
 *
 * @see CodeAnalysisContext
 * @since 2.0.0
 */
public interface SyntaxNodeAnalysisContext {

    /**
     * Returns the syntax node that matches with one of the specified {@code SyntaxKind}s.
     *
     * @return the syntax node on which the {@code AnalysisTask<SyntaxNodeAnalysisContext>} executed
     */
    Node node();

    /**
     * Returns the {@code ModuleId} of the {@code Module} that contains the syntax node.
     *
     * @return the {@code ModuleId} of the {@code Module} that contains the syntax node
     */
    ModuleId moduleId();

    /**
     * Returns the {@code DocumentId} of the {@code Document} that contains the syntax node.
     *
     * @return the {@code DocumentId} of the {@code Document} that contains the syntax node
     */
    DocumentId documentId();

    /**
     * Returns the {@code SyntaxTree} associated with the {@code Node}.
     *
     * @return the {@code SyntaxTree}
     */
    SyntaxTree syntaxTree();

    /**
     * Returns the {@code SemanticModel} of the module that contains the syntax node.
     *
     * @return the {@code SemanticModel} of the module that contains the syntax node.
     */
    SemanticModel semanticModel();

    /**
     * Returns the current {@code Package} instance on which the compilation is being performed.
     *
     * @return the current {@code Package} instance
     */
    Package currentPackage();

    /**
     * Returns the compilation instance that captures the state of the package compilation.
     *
     * @return the package compilation instance
     */
    PackageCompilation compilation();

    /**
     * Reports a diagnostic against the compilation.
     *
     * @param diagnostic the {@code Diagnostic} to be reported
     */
    void reportDiagnostic(Diagnostic diagnostic);
}
