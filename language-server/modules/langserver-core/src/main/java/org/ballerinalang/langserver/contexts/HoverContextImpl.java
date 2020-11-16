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

import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

import javax.annotation.Nonnull;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class HoverContextImpl extends AbstractLSContext implements HoverContext {

    private Token tokenAtCursor;

    HoverContextImpl(LSOperation operation,
                     String fileUri,
                     WorkspaceManager wsManager) {
        super(operation, fileUri, wsManager);
    }

    @Override
    public void setTokenAtCursor(@Nonnull Token token) {
        this.tokenAtCursor = token;
    }

    @Override
    public Token getTokenAtCursor() {
        if (this.tokenAtCursor == null) {
            throw new RuntimeException("Token has to be set before accessing");
        }

        return this.tokenAtCursor;
    }

    /**
     * Represents Language server signature help context Builder.
     *
     * @since 2.0.0
     */
    protected static class HoverContextBuilder extends AbstractContextBuilder<HoverContextBuilder> {

        public HoverContextBuilder() {
            super(LSContextOperation.TXT_HOVER);
        }

        public HoverContext build() {
            return new HoverContextImpl(this.operation, this.fileUri, this.wsManager);
        }

        @Override
        public HoverContextBuilder self() {
            return this;
        }
    }
}
