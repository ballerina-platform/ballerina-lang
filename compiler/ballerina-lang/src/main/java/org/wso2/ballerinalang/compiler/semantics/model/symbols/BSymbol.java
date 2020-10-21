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
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BSymbol implements Symbol {

    public int tag;
    public int flags;
    public Name name;
    public PackageID pkgID;
    public SymbolKind kind;
    public BType type;
    public BSymbol owner;
    public boolean tainted;
    public boolean closure;
    public MarkdownDocAttachment markdownDocumentation;
    public DiagnosticPos pos;
    public SymbolOrigin origin;

    /**
     * If a symbol has child symbols, then the scope will not be null.
     */
    public Scope scope;

    public BSymbol(int tag, int flags, Name name, PackageID pkgID, BType type, BSymbol owner, DiagnosticPos pos,
                   SymbolOrigin origin) {
        this.tag = tag;
        this.flags = flags;
        this.name = name;
        this.pkgID = pkgID;
        this.type = type;
        this.owner = owner;
        this.pos = pos;
        this.origin = origin;
    }

    public MarkdownDocAttachment getMarkdownDocAttachment() {
        return markdownDocumentation;
    }

    @Override
    public Name getName() {
        return name;
    }

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
        return Flags.unMask(flags);
    }

    @Override
    public BSymbol getEnclosingSymbol() {
        return owner;
    }

    @Override
    public List<BSymbol> getEnclosedSymbols() {
        return new ArrayList<>(0);
    }

    @Override
    public DiagnosticPosition getPosition() {
        return this.pos;
    }

    @Override
    public SymbolOrigin getOrigin() {
        return this.origin;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
