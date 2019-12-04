/*

 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.List;

/**
 * Represents the interface for the Ballerina Code Action Router.
 *
 * @since 1.0.4
 */
public interface CodeActionRouter {

    /**
     * @param nodeType    code action node type
     * @param context     ls context
     * @param diagnostics list of diagnostics
     * @return list of code actions
     */
    List<CodeAction> getBallerinaCodeActions(CodeActionNodeType nodeType, LSContext context,
                                             List<Diagnostic> diagnostics);
}
