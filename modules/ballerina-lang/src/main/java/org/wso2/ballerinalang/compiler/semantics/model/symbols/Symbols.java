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

import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

/**
 * @since 0.94
 */
public class Symbols {

    public static BTypeSymbol createStructSymbol(int flags, Name name, BType type, BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.STRUCT, flags, name, type, owner);
        typeSymbol.kind = SymbolKind.STRUCT;
        return typeSymbol;
    }
    
    public static BTypeSymbol createWorkerSymbol(int flags, Name name, BType type, BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.WORKER, flags, name, type, owner);
        typeSymbol.kind = SymbolKind.WORKER;
        return typeSymbol;
    }

    public static BTypeSymbol createConnectorSymbol(int flags, Name name, BType type, BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.CONNECTOR, flags, name, type, owner);
        typeSymbol.kind = SymbolKind.CONNECTOR;
        return typeSymbol;
    }

    public static BTypeSymbol createServiceSymbol(int flags, Name name, BType type, BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.SERVICE, flags, name, type, owner);
        typeSymbol.kind = SymbolKind.SERVICE;
        return typeSymbol;
    }

    public static BInvokableSymbol createFunctionSymbol(int flags, Name name, BType type, BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.FUNCTION, flags, name, type, owner);
        symbol.kind = SymbolKind.FUNCTION;
        return symbol;
    }

    public static BInvokableSymbol createActionSymbol(int flags, Name name, BType type, BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.ACTION, flags, name, type, owner);
        symbol.kind = SymbolKind.ACTION;
        return symbol;
    }

    public static BInvokableSymbol createResourceSymbol(int flags, Name name, BType type, BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.RESOURCE, flags, name, type, owner);
        symbol.kind = SymbolKind.RESOURCE;
        return symbol;
    }

    public static BTypeSymbol createTypeSymbol(int symTag,
                                               int flags,
                                               Name name,
                                               BType type,
                                               BSymbol owner) {
        return new BTypeSymbol(symTag, flags, name, type, owner);
    }

    public static BInvokableSymbol createInvokableSymbol(int kind,
                                                         int flags,
                                                         Name name,
                                                         BType type,
                                                         BSymbol owner) {
        return new BInvokableSymbol(kind, flags, name, type, owner);
    }

    public static boolean isNative(BSymbol sym) {
        return (sym.flags & Flags.NATIVE) == Flags.NATIVE;
    }

    public static boolean isPublic(BSymbol sym) {
        return (sym.flags & Flags.PUBLIC) == Flags.PUBLIC;
    }
}
