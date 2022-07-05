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
package org.ballerinalang.langserver.completions;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.CompletionExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.contexts.BallerinaCompletionContextImpl;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Collections;
import java.util.List;

/**
 * Completion extension implementation for ballerina.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class BallerinaCompletionExtension implements CompletionExtension {
    @Override
    public boolean validate(CompletionParams inputParams) {
        // Here we check the .bal extension
        return inputParams.getTextDocument().getUri().endsWith(".bal");
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams,
                                        CompletionContext context,
                                        LanguageServerContext serverContext)
            throws Throwable {
        BallerinaCompletionContext bcContext = new BallerinaCompletionContextImpl(context, serverContext, inputParams);
        return CompletionUtil.getCompletionItems(bcContext);
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams,
                                        CompletionContext context,
                                        LanguageServerContext serverContext,
                                        CancelChecker cancelChecker)
            throws Throwable {
        BallerinaCompletionContext bcContext =
                new BallerinaCompletionContextImpl(context, serverContext, inputParams, cancelChecker);
        return CompletionUtil.getCompletionItems(bcContext);
    }

    @Override
    public List<String> handledCustomURISchemes(CompletionParams inputParams,
                                                CompletionContext context,
                                                LanguageServerContext serverContext) {
        return Collections.singletonList(CommonUtil.URI_SCHEME_EXPR);
    }
}
