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
 * DTO for OtherTypeSymbol.
 * 
 * @since 0.983.0
 */
public final class OtherTypeSymbolDTO {
    
    private int id;
    
    private int packageId;
    
    private String name;
    
    private String fields;

    private CompletionItem completionItem;

    private OtherTypeSymbolDTO(int id, int packageId, String name, String fields, CompletionItem completionItem) {
        this.id = id;
        this.packageId = packageId;
        this.name = name;
        this.fields = fields;
        this.completionItem = completionItem;
    }

    public int getId() {
        return id;
    }

    public int getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }

    public String getFields() {
        return fields;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }

    /**
     * Builder for OtherTypeSymbolDTO.
     */
    public static class OtherTypeSymbolDTOBuilder {

        private int id = -1;

        private int packageId = -1;

        private String name = "";

        private String fields = "";

        private CompletionItem completionItem;

        public OtherTypeSymbolDTOBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public OtherTypeSymbolDTOBuilder setPackageId(int packageId) {
            this.packageId = packageId;
            return this;
        }

        public OtherTypeSymbolDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public OtherTypeSymbolDTOBuilder setFields(String fields) {
            this.fields = fields;
            return this;
        }

        public OtherTypeSymbolDTOBuilder setCompletionItem(CompletionItem completionItem) {
            this.completionItem = completionItem;
            return this;
        }
        
        public OtherTypeSymbolDTO build() {
            return new OtherTypeSymbolDTO(this.id, this.packageId, this.name, this.fields, this.completionItem);
        }
    }
}
