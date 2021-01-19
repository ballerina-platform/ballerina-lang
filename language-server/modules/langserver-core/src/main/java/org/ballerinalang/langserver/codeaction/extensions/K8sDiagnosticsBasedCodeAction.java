/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.extensions;

import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.List;

/**
 * Represents SPI extension for Kubernetes.toml diagnostics based code actions.
 *
 * @since 2.0.0
 */
public interface K8sDiagnosticsBasedCodeAction {

    /**
     * Checks if the provider matches the diagnostic in the users code.
     *
     * @return boolean with the validity of the provider
     */
    boolean validate(Diagnostic diagnostic, CodeActionContext ctx);

    /**
     * Generates the code action according to the diagnostic.
     *
     * @return list of code actions to resolve the diagnostic.
     */
    List<CodeAction> handle(Diagnostic diagnostic, CodeActionContext ctx);
}
