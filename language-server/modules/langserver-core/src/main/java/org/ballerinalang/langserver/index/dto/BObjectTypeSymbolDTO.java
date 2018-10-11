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

import org.ballerinalang.langserver.index.ObjectType;
import org.eclipse.lsp4j.CompletionItem;

/**
 * DTO for BObjectTypeSymbol.
 * 
 * @since 0.983.0
 */
public final class BObjectTypeSymbolDTO {

    private int id;

    private int packageId;

    private int actionHolderId;

    private boolean isPrivate;

    private String name;

    private String fields;

    private ObjectType type;

    private CompletionItem completionItem;

    private BObjectTypeSymbolDTO(int id, int packageId, int actionHolderId, String name, String fields, ObjectType type,
                                boolean isPrivate, CompletionItem completionItem) {
        this.id = id;
        this.packageId = packageId;
        this.actionHolderId = actionHolderId;
        this.name = name;
        this.fields = fields;
        this.type = type;
        this.isPrivate = isPrivate;
        this.completionItem = completionItem;
    }

    public int getId() {
        return id;
    }

    public int getPackageId() {
        return packageId;
    }

    public int getActionHolderId() {
        return actionHolderId;
    }

    public String getName() {
        return name;
    }

    public String getFields() {
        return fields;
    }

    public ObjectType getType() {
        return type;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }

    /**
     * Builder for BObjectTypeSymbolDTO.
     */
    public static class BObjectTypeSymbolDTOBuilder {

        private int id = -1;

        private int packageId = -1;

        private int actionHolderId = -1;

        private boolean isPrivate;

        private String name = "";

        private String fields = "";

        private ObjectType type;

        private CompletionItem completionItem;

        public BObjectTypeSymbolDTOBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setPackageId(int packageId) {
            this.packageId = packageId;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setActionHolderId(int actionHolderId) {
            this.actionHolderId = actionHolderId;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setFields(String fields) {
            this.fields = fields;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setType(ObjectType type) {
            this.type = type;
            return this;
        }

        public BObjectTypeSymbolDTOBuilder setCompletionItem(CompletionItem completionItem) {
            this.completionItem = completionItem;
            return this;
        }
        
        public BObjectTypeSymbolDTO build() {
            return new BObjectTypeSymbolDTO(this.id, this.packageId, this.actionHolderId, this.name, this.fields,
                    this.type, this.isPrivate, this.completionItem);
        }
    }
}
