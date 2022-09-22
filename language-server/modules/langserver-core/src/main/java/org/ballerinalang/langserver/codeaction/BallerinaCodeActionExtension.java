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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionExtension;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;

import java.util.Collections;
import java.util.List;

/**
 * Code action extension implementation for ballerina.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class BallerinaCodeActionExtension implements CodeActionExtension {
    @Override
    public boolean validate(CodeActionParams inputParams) {
        // Here we check the .bal extension
        return inputParams.getTextDocument().getUri().endsWith(".bal");
    }

    @Override
    public List<? extends CodeAction> execute(CodeActionParams inputParams,
                                              CodeActionContext context,
                                              LanguageServerContext serverContext) {
        return CodeActionRouter.getAvailableCodeActions(context);
    }

    @Override
    public CodeAction resolve(ResolvableCodeAction codeAction,
                              CodeActionResolveContext resolveContext) {
        return CodeActionRouter.resolveCodeAction(codeAction, resolveContext);
    }

    @Override
    public List<String> handledCustomURISchemes(CodeActionParams inputParams,
                                                CodeActionContext context,
                                                LanguageServerContext serverContext) {
        return Collections.singletonList(CommonUtil.URI_SCHEME_EXPR);
    }
}
