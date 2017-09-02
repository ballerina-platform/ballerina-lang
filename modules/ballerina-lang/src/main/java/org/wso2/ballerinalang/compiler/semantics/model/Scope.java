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
package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BLangSymbol;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.HashMap;

/**
 * @since 0.94
 */
public class Scope implements org.ballerinalang.model.Scope {

    private static final int DEFAULT_SIZE = 10;

    public static final ScopeEntry NOT_FOUND_ENTRY = new ScopeEntry(null, null);

    public BLangSymbol owner;

    public HashMap<Name, ScopeEntry> entries;

    public Scope(BLangSymbol owner) {
        this.owner = owner;
        this.entries = new HashMap<>(10);
    }

    public void define(Name name, BLangSymbol symbol) {
        ScopeEntry current = entries.get(name);
        if (current == null) {
            current = NOT_FOUND_ENTRY;
        }

        ScopeEntry newEntry = new ScopeEntry(symbol, current);
        entries.put(name, newEntry);
    }

    public ScopeEntry lookup(Name name) {
        ScopeEntry entry = entries.get(name);
        if (entry == null) {
            return NOT_FOUND_ENTRY;
        }

        return entry;
    }

    /**
     * @since 0.94
     */
    public static class ScopeEntry implements org.ballerinalang.model.ScopeEntry {

        public BLangSymbol symbol;
        public ScopeEntry next;

        public ScopeEntry(BLangSymbol symbol, ScopeEntry next) {
            this.symbol = symbol;
            this.next = next;
        }
    }
}
