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
 */
public class OtherTypeSymbolDTO {
    
    private int packageId;
    
    private String name;
    
    private String fields;

    private CompletionItem completionItem;

    public OtherTypeSymbolDTO(int packageId, String name, String fields, CompletionItem completionItem) {
        this.packageId = packageId;
        this.name = name;
        this.fields = fields;
        this.completionItem = completionItem;
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
}
