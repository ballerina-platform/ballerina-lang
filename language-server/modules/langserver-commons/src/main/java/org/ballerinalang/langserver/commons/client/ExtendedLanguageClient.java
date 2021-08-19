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

package org.ballerinalang.langserver.commons.client;

import org.ballerinalang.langserver.commons.trace.TraceRecord;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Extended Language Client interface.
 */
public interface ExtendedLanguageClient extends LanguageClient {
    @JsonNotification("window/traceLogs")
    void traceLogs(TraceRecord rawTrace);

    @JsonNotification("window/showTextDocument")
    void showTextDocument(Location location);
}
