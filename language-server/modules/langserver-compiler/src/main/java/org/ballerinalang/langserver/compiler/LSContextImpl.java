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
package org.ballerinalang.langserver.compiler;

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class LSContextImpl extends LSContext {
    protected LSContextImpl(LSOperation operation) {
        super(operation);
    }

    private final Map<Key<?>, Object> props = new HashMap<>();

    @Override
    public <V> void put(Key<V> key, V value) {
        props.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(Key<V> key) {
        return (V) this.props.get(key);
    }

    /**
     * Represents Language server context Builder.
     * @param <T> Context Type
     */
    protected abstract static class ContextBuilder<T extends ContextBuilder<T>> {
        protected LSContext lsContext;

        /**
         * Context Builder constructor.
         * @param lsOperation LS Operation for the particular invocation
         */
        public ContextBuilder(LSOperation lsOperation) {
            this.lsContext = new LSContextImpl(lsOperation);
        }

        public T withCommonParams(TextDocumentPositionParams positionParams, String fileUri,
                                  WorkspaceDocumentManager documentManager) {
            this.lsContext.put(DocumentServiceKeys.POSITION_KEY, positionParams);
            this.lsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            this.lsContext.put(DocumentServiceKeys.DOC_MANAGER_KEY, documentManager);
            return self();
        }

        public abstract LSContext build();

        protected abstract T self();
    }
}
