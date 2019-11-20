/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Ballerina Syntax Highlighter extension for Language Server.
 *
 * @since 1.0.2
 */
public class BallerinaSyntaxHighlightServiceImpl implements BallerinaSyntaxHighlightService {

    private final List<SemanticHighlightProvider.HighlightInfo> list;
    private final LSGlobalContext lsGlobalContext;

    public BallerinaSyntaxHighlightServiceImpl(LSGlobalContext lsGlobalContext) {
        this.list = lsGlobalContext.get(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY);
        this.lsGlobalContext = lsGlobalContext;
    }

    public CompletableFuture<BallerinaHighlightingResponse> list(BallerinaHighlightingRequest request) {

        request.getParams();
        BallerinaHighlightingResponse reply = new BallerinaHighlightingResponse();
        return CompletableFuture.supplyAsync(() -> reply);
    }

}
