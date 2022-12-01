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
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class ReferencesContextImpl extends PositionedOperationContextImpl implements ReferencesContext {

    ReferencesContextImpl(LSOperation operation,
                          String fileUri,
                          WorkspaceManager wsManager,
                          Position cursorPos,
                          LanguageServerContext serverContext,
                          CancelChecker cancelChecker) {
        super(operation, fileUri, cursorPos, wsManager, serverContext, cancelChecker);
    }
    
    /**
     * Represents Language server references context Builder.
     *
     * @since 2.0.0
     */
    protected static class ReferencesContextBuilder extends PositionedOperationContextBuilder<ReferencesContext> {

        public ReferencesContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_REFERENCES, serverContext);
        }
        
        public ReferencesContext build() {
            return new ReferencesContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.position,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public ReferencesContextBuilder self() {
            return this;
        }
    }
}
