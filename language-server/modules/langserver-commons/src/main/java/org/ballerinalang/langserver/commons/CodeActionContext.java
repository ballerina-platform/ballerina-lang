/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents the Code action operation context.
 *
 * @since 2.0.0
 */
public interface CodeActionContext extends DocumentServiceContext {

    /**
     * Get the cursor position.
     *
     * @return {@link Position}
     */
    Position cursorPosition();

    /**
     * Get the diagnostics of the file.
     *
     * @return {@link List} of diagnostics
     */
    List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics(Path filePath);

    /**
     * Get the diagnostics at the cursor.
     *
     * @return {@link  List} of diagnostics at the cursor
     */
    List<Diagnostic> cursorDiagnostics();
}
