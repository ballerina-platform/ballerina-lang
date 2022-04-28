/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;

/**
 * The context for the source code modifier task.
 *
 * @since 2201.0.3
 */
public interface SourceModifierContext {

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
     * Modify source code of a given document {@code Document} identified by the {@code DocumentId}.
     *
     * @param textDocument modified {@code TextDocument} that contains the source code
     * @param documentId   indicates the document which should be modified
     */
    void modifySourceFile(TextDocument textDocument, DocumentId documentId);

    /**
     * Modify source code of a given test document {@code Document} identified by the {@code DocumentId}.
     *
     * @param textDocument modified {@code TextDocument} that contains the source code
     * @param documentId   indicates the test document which should be modified
     */
    void modifyTestSourceFile(TextDocument textDocument, DocumentId documentId);

    /**
     * Reports a diagnostic against the compilation.
     *
     * @param diagnostic the {@code Diagnostic} to be reported
     */
    void reportDiagnostic(Diagnostic diagnostic);
}
