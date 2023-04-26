/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.eclipse.lsp4j.DidChangeNotebookDocumentParams;
import org.eclipse.lsp4j.DidCloseNotebookDocumentParams;
import org.eclipse.lsp4j.DidOpenNotebookDocumentParams;
import org.eclipse.lsp4j.DidSaveNotebookDocumentParams;
import org.eclipse.lsp4j.services.NotebookDocumentService;

/**
 * Notebook document service implementation for ballerina.
 *
 * @since 2201.4.0
 */
public class BallerinaNotebookDocumentService implements NotebookDocumentService {

    private final LanguageServerContext serverContext;
    private final LSClientLogger clientLogger;

    BallerinaNotebookDocumentService(LanguageServerContext serverContext) {
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(this.serverContext);
    }

    @Override
    public void didOpen(DidOpenNotebookDocumentParams params) {
        this.clientLogger.logMessage("Not supported yet");
    }

    @Override
    public void didChange(DidChangeNotebookDocumentParams params) {
        this.clientLogger.logMessage("Not supported yet");
    }

    @Override
    public void didSave(DidSaveNotebookDocumentParams params) {
        this.clientLogger.logMessage("Not supported yet");
    }

    @Override
    public void didClose(DidCloseNotebookDocumentParams params) {
        this.clientLogger.logMessage("Not supported yet");
    }
}
