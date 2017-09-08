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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymbolTags;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * @since 0.94
 */
public class SymbolResolver extends BLangNodeVisitor {
    private static final CompilerContext.Key<SymbolResolver> SYMBOL_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Names names;

    private SymbolEnv env;
    private BType result;
    private String errMsgKey;

    public static SymbolResolver getInstance(CompilerContext context) {
        SymbolResolver symbolResolver = context.get(SYMBOL_RESOLVER_KEY);
        if (symbolResolver == null) {
            symbolResolver = new SymbolResolver(context);
        }

        return symbolResolver;
    }

    public SymbolResolver(CompilerContext context) {
        context.put(SYMBOL_RESOLVER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
    }


    // visit type nodes

    public void visit(BLangValueType valueTypeNode) {
        visitBuiltInTypeNode(names.fromTypeKind(valueTypeNode.typeKind));
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        visitBuiltInTypeNode(names.fromTypeKind(builtInRefType.typeKind));
    }

    public void visit(BLangArrayType arrayTypeNode) {
        // The value of the dimensions field should always be >= 1
        result = resolveTypeNode(arrayTypeNode.elemtype, env, errMsgKey);
        for (int i = 0; i < arrayTypeNode.dimensions; i++) {
            result = new BArrayType(result);
        }
    }

    public void visit(BLangConstrainedType constrainedTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedTypeNode) {
        throw new AssertionError();
    }


    boolean checkForUniqueSymbol(BSymbol symbol, Scope scope) {
        if (lookupSymbol(scope, symbol.name, symbol.tag) != null) {
            // TODO out.println("duplicate symbol detected: " + symbol.name);
            return false;
        }

        return true;
    }


    BType resolveTypeNode(BLangType typeNode, SymbolEnv env) {
        return resolveTypeNode(typeNode, env, "unknown.type");
    }

    BType resolveTypeNode(BLangType typeNode, SymbolEnv env, String errMsgKey) {
        SymbolEnv prevEnv = this.env;
        String preErrMsgKey = this.errMsgKey;

        this.env = env;
        this.errMsgKey = errMsgKey;
        typeNode.accept(this);
        this.env = prevEnv;
        this.errMsgKey = preErrMsgKey;

        return result;
    }

    BSymbol lookupSymbol(Scope scope, Name name, int expSymbolTag) {
        ScopeEntry entry = scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if (entry.symbol.tag == expSymbolTag) {
                return entry.symbol;
            }
        }

        // TODO return error symbol with error type.
        return null;
    }

    private void visitBuiltInTypeNode(Name name) {
        BSymbol typeSymbol = lookupSymbol(symTable.rootScope,
                name, SymbolTags.TYPE);
        result = typeSymbol.type;
    }
}
