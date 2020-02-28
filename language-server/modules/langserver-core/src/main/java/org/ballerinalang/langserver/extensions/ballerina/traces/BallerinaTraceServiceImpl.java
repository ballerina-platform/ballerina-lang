/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.ballerina.traces;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Ballerina Traces extension for Language Server.
 */
public class BallerinaTraceServiceImpl implements BallerinaTraceService {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaTraceServiceImpl.class);

    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;

    public BallerinaTraceServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public void pushLogToClient(TraceRecord traceRecord) {
        ExtendedLanguageClient client = this.ballerinaLanguageServer.getClient();
        if (client == null) {
            return;
        }
        client.traceLogs(traceRecord);
    }
}
