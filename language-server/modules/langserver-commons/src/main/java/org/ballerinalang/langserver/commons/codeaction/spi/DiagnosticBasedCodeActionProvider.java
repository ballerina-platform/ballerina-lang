/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.commons.codeaction.spi;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;

import java.util.List;

/**
 * Represents the interface for the Ballerina Diagnostic Based Code Action Provider.
 *
 * @since 2201.2.0
 */
public interface DiagnosticBasedCodeActionProvider extends LSCodeActionProvider {

    /**
     * Returns the list of code actions based on diagnostics.
     *
     * @param diagnostic      diagnostic to evaluate
     * @param positionDetails {@link DiagBasedPositionDetails}
     * @param context         code action context
     * @return list of Code Actions
     */
    List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                    CodeActionContext context);

    /**
     * Checks whether the syntax is valid.
     *
     * @param diagnostic      diagnostic to evaluate
     * @param positionDetails {@link DiagBasedPositionDetails}
     * @param context         code action context
     * @return True if syntactically correct, false otherwise.
     */
    boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, CodeActionContext context);
}
