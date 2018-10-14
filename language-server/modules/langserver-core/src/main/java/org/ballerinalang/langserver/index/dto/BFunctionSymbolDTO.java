/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.index.dto;

import org.eclipse.lsp4j.CompletionItem;

/**
 * DTO for BInvokableSymbol.
 * 
 * @since 0.983.0
 */
public final class BFunctionSymbolDTO {

    private int id;

    private int packageId;

    private int objectId;

    private boolean isPrivate;

    private boolean isAttached;

    private String name;

    private CompletionItem completionItem;

    public BFunctionSymbolDTO(int id, int packageId, int objectId, boolean isPrivate, boolean isAttached,
                              String name, CompletionItem completionItem) {
        this.id = id;
        this.packageId = packageId;
        this.objectId = objectId;
        this.isPrivate = isPrivate;
        this.isAttached = isAttached;
        this.name = name;
        this.completionItem = completionItem;
    }

    public int getId() {
        return id;
    }

    public int getPackageId() {
        return packageId;
    }

    public int getObjectId() {
        return objectId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public String getName() {
        return name;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }

    /**
     * Builder for BFunctionSymbolDTO.
     */
    public static class BFunctionDTOBuilder {

        private int id = -1;

        private int packageId = -1;

        private int objectId = -1;

        private boolean isPrivate;

        private boolean isAttached;

        private String name = "";

        private CompletionItem completionItem;

        public BFunctionDTOBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public BFunctionDTOBuilder setPackageId(int packageId) {
            this.packageId = packageId;
            return this;
        }

        public BFunctionDTOBuilder setObjectId(int objectId) {
            this.objectId = objectId;
            return this;
        }

        public BFunctionDTOBuilder setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
            return this;
        }

        public BFunctionDTOBuilder setAttached(boolean attached) {
            isAttached = attached;
            return this;
        }

        public BFunctionDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public BFunctionDTOBuilder setCompletionItem(CompletionItem completionItem) {
            this.completionItem = completionItem;
            return this;
        }
        
        public BFunctionSymbolDTO build() {
            return new BFunctionSymbolDTO(this.id, this.packageId, this.objectId, this.isPrivate,
                    this.isAttached, this.name, this.completionItem);
        }
    }
}
