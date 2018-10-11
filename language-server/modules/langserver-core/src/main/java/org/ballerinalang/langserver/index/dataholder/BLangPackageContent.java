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
package org.ballerinalang.langserver.index.dataholder;

import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the blang package content.
 * 
 * @since 0.983.0
 */
public final class BLangPackageContent {
    
    private BPackageSymbolDTO packageSymbolDTO;
    
    private List<BRecordTypeSymbol> recordTypeSymbols = new ArrayList<>();

    private List<BTypeSymbol> otherTypeSymbols = new ArrayList<>();

    private List<BObjectTypeSymbol> objectTypeSymbols = new ArrayList<>();

    private List<BInvokableSymbol> bInvokableSymbols = new ArrayList<>();


    private BLangPackageContent(BPackageSymbolDTO packageSymbolDTO, List<BRecordTypeSymbol> recordTypeSymbols,
                               List<BTypeSymbol> otherTypeSymbols, List<BObjectTypeSymbol> objectTypeSymbols,
                               List<BInvokableSymbol> bInvokableSymbols) {
        this.packageSymbolDTO = packageSymbolDTO;
        this.recordTypeSymbols = recordTypeSymbols;
        this.otherTypeSymbols = otherTypeSymbols;
        this.objectTypeSymbols = objectTypeSymbols;
        this.bInvokableSymbols = bInvokableSymbols;
    }

    public BPackageSymbolDTO getPackageSymbolDTO() {
        return packageSymbolDTO;
    }

    public List<BRecordTypeSymbol> getRecordTypeSymbols() {
        return recordTypeSymbols;
    }

    public List<BTypeSymbol> getOtherTypeSymbols() {
        return otherTypeSymbols;
    }

    public List<BObjectTypeSymbol> getObjectTypeSymbols() {
        return objectTypeSymbols;
    }

    public List<BInvokableSymbol> getbInvokableSymbols() {
        return bInvokableSymbols;
    }

    /**
     * Builder for BLangPackageContent.
     */
    public static class BLangPackageContentBuilder {

        private BPackageSymbolDTO packageSymbolDTO;

        private List<BRecordTypeSymbol> recordTypeSymbols = new ArrayList<>();

        private List<BTypeSymbol> otherTypeSymbols = new ArrayList<>();

        private List<BObjectTypeSymbol> objectTypeSymbols = new ArrayList<>();

        private List<BInvokableSymbol> bInvokableSymbols = new ArrayList<>();

        public BLangPackageContentBuilder setPackageSymbolDTO(BPackageSymbolDTO packageSymbolDTO) {
            this.packageSymbolDTO = packageSymbolDTO;
            return this;
        }

        public BLangPackageContentBuilder setRecordTypeSymbols(List<BRecordTypeSymbol> recordTypeSymbols) {
            this.recordTypeSymbols = recordTypeSymbols;
            return this;
        }

        public BLangPackageContentBuilder setOtherTypeSymbols(List<BTypeSymbol> otherTypeSymbols) {
            this.otherTypeSymbols = otherTypeSymbols;
            return this;
        }

        public BLangPackageContentBuilder setObjectTypeSymbols(List<BObjectTypeSymbol> objectTypeSymbols) {
            this.objectTypeSymbols = objectTypeSymbols;
            return this;
        }

        public BLangPackageContentBuilder setbInvokableSymbols(List<BInvokableSymbol> bInvokableSymbols) {
            this.bInvokableSymbols = bInvokableSymbols;
            return this;
        }
        
        public BLangPackageContent build() {
            return new BLangPackageContent(this.packageSymbolDTO, this.recordTypeSymbols, this.otherTypeSymbols,
                    this.objectTypeSymbols, this.bInvokableSymbols);
        }
    }
}
