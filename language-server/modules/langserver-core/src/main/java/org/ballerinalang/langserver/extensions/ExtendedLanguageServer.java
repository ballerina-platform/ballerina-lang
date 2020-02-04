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
package org.ballerinalang.langserver.extensions;

import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaDocumentService;
import org.ballerinalang.langserver.extensions.ballerina.example.BallerinaExampleService;
import org.ballerinalang.langserver.extensions.ballerina.fragment.BallerinaFragmentService;
import org.ballerinalang.langserver.extensions.ballerina.project.BallerinaProjectService;
import org.ballerinalang.langserver.extensions.ballerina.symbol.BallerinaSymbolService;
import org.ballerinalang.langserver.extensions.ballerina.traces.BallerinaTraceService;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Extended Language Server interface which includes ballerina document services.
 *
 * @since 0.981.2
 */
public interface ExtendedLanguageServer extends LanguageServer {
    @JsonDelegate
    BallerinaDocumentService getBallerinaDocumentService();
    @JsonDelegate
    BallerinaExampleService getBallerinaExampleService();
    @JsonDelegate
    BallerinaProjectService getBallerinaProjectService();
    @JsonDelegate
    BallerinaTraceService getBallerinaTraceService();
    @JsonDelegate
    BallerinaSymbolService getBallerinaSymbolService();
    @JsonDelegate
    BallerinaFragmentService getBallerinaFragmentService();
}
