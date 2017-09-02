/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangSymbol implements Symbol {

    public BType type;
    public BLangSymbol owner;

    @Override
    public SymbolKind getKind() {
        return SymbolKind.OTHER;
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public Set<Flag> getFlags() {
        return null;
    }

    @Override
    public BLangSymbol getEnclosingSymbol() {
        return owner;
    }

    @Override
    public List<BLangSymbol> getEnclosedSymbols() {
        return new ArrayList<>(0);
    }
}
