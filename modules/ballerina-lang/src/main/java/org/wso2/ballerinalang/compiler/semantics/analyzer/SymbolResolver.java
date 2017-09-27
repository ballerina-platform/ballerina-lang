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

import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * @since 0.94
 */
public class SymbolResolver extends BLangNodeVisitor {
    private static final CompilerContext.Key<SymbolResolver> SYMBOL_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Names names;
    private DiagnosticLog dlog;

    private SymbolEnv env;
    private BType resultType;
    private DiagnosticCode diagCode;

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
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public boolean checkForUniqueSymbol(DiagnosticPos pos, SymbolEnv env, BSymbol symbol) {
        BSymbol foundSym = lookupSymbol(env, symbol.name, symbol.tag);
        if (foundSym != symTable.notFoundSymbol && foundSym.owner == symbol.owner) {
            dlog.error(pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
            return false;
        }

        return true;
    }

    public BSymbol resolveExplicitCastOperator(BType sourceType,
                                               BType targetType) {
        ScopeEntry entry = symTable.rootScope.lookup(Names.CAST_OP);
        List<BType> types = Lists.of(sourceType, targetType);

        return resolveOperator(entry, types);
    }

    public BSymbol resolveBinaryOperator(OperatorKind opKind,
                                         BType lhsType,
                                         BType rhsType) {
        ScopeEntry entry = symTable.rootScope.lookup(names.fromString(opKind.value()));
        List<BType> types = Lists.of(lhsType, rhsType);
        return resolveOperator(entry, types);
    }

    public BSymbol resolveUnaryOperator(DiagnosticPos pos,
                                        OperatorKind opKind,
                                        BType type) {
        ScopeEntry entry = symTable.rootScope.lookup(names.fromString(opKind.value()));
        List<BType> types = new ArrayList<>(2);
        types.add(type);

        BSymbol symbol = resolveOperator(entry, types);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNARY_OP_INCOMPATIBLE_TYPES, opKind, type);
        }
        return symbol;
    }

    public BSymbol resolvePkgSymbol(DiagnosticPos pos, SymbolEnv env, Name pkgAlias) {

        if (pkgAlias == Names.EMPTY) {
            // Return the current package symbol
            return env.enclPkg.symbol;
        }

        // Lookup for an imported package
        BSymbol pkgSymbol = lookupSymbol(env, pkgAlias, SymTag.PACKAGE);
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_PACKAGE, pkgAlias.value);
        }

        return pkgSymbol;
    }

    public BSymbol resolveInvokable(DiagnosticPos pos,
                                    DiagnosticCode code,
                                    SymbolEnv env,
                                    Name pkgAlias,
                                    Name invokableName) {
        BSymbol pkgSymbol = resolvePkgSymbol(pos, env, pkgAlias);
        if (pkgSymbol == symTable.notFoundSymbol) {
            return pkgSymbol;
        }

        BSymbol symbol = lookupMemberSymbol(pkgSymbol.scope, invokableName, SymTag.INVOKABLE);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(pos, code, invokableName);
        }
        return symbol;
    }

    public BSymbol resolveStructField(DiagnosticPos pos, Name fieldName, BTypeSymbol structSymbol) {
        BSymbol symbol = lookupMemberSymbol(structSymbol.scope, fieldName, SymTag.VARIABLE);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_STRUCT_FIELD, fieldName, structSymbol);
        }

        return symbol;
    }

    public BType resolveTypeNode(BLangType typeNode, SymbolEnv env) {
        return resolveTypeNode(typeNode, env, DiagnosticCode.UNKNOWN_TYPE);
    }

    public BType resolveTypeNode(BLangType typeNode, SymbolEnv env, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        DiagnosticCode preDiagCode = this.diagCode;

        this.env = env;
        this.diagCode = diagCode;
        typeNode.accept(this);
        this.env = prevEnv;
        this.diagCode = preDiagCode;

        typeNode.type = resultType;
        return resultType;
    }

    /**
     * Return the symbol associated with the given name, starting
     * This method first searches the symbol in the current scope
     * and proceeds the enclosing scope, if it is not there in the
     * current scope. This process continues until the symbol is
     * found or the root scope is reached.
     *
     * @param env       current symbol environment
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    public BSymbol lookupSymbol(SymbolEnv env, Name name, int expSymTag) {
        ScopeEntry entry = env.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & expSymTag) == expSymTag) {
                return entry.symbol;
            }
            entry = entry.next;
        }

        if (env.enclEnv != null) {
            return lookupSymbol(env.enclEnv, name, expSymTag);
        }

        return symTable.notFoundSymbol;
    }


    /**
     * Return the symbol with the given name.
     * This method only looks at the symbol defined in the given scope.
     *
     * @param scope     current scope
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    public BSymbol lookupMemberSymbol(Scope scope, Name name, int expSymTag) {
        ScopeEntry entry = scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & expSymTag) == expSymTag) {
                return entry.symbol;
            }
            entry = entry.next;
        }

        return symTable.notFoundSymbol;
    }

    // visit type nodes

    public void visit(BLangValueType valueTypeNode) {
        visitBuiltInTypeNode(valueTypeNode, valueTypeNode.typeKind);
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        visitBuiltInTypeNode(builtInRefType, builtInRefType.typeKind);
    }

    public void visit(BLangArrayType arrayTypeNode) {
        // The value of the dimensions field should always be >= 1
        resultType = resolveTypeNode(arrayTypeNode.elemtype, env, diagCode);
        for (int i = 0; i < arrayTypeNode.dimensions; i++) {
            resultType = new BArrayType(resultType);
        }
    }

    public void visit(BLangConstrainedType constrainedTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedTypeNode) {
        // 1) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        //    if not use the current package scope.
        // 2) lookup the typename in the package scope returned from step 1.
        // 3) If the symbol is not found, then lookup in the root scope. e.g. for types such as 'error'

        BSymbol pkgSymbol = resolvePkgSymbol(userDefinedTypeNode.pos, this.env,
                names.fromIdNode(userDefinedTypeNode.pkgAlias));
        if (pkgSymbol == symTable.notFoundSymbol) {
            resultType = symTable.errType;
            return;
        }

        Name typeName = names.fromIdNode(userDefinedTypeNode.typeName);
        BSymbol symbol = symTable.notFoundSymbol;

        // 2) Resolve ANNOTATION type if and only current scope inside ANNOTATION definition.
        // Only valued types and ANNOTATION type allowed.
        if (env.scope.owner.tag == SymTag.ANNOTATION) {
            symbol = lookupMemberSymbol(pkgSymbol.scope, typeName, SymTag.ANNOTATION);
        }

        // 3) Lookup the current package scope.
        if (symbol == symTable.notFoundSymbol) {
            symbol = lookupMemberSymbol(pkgSymbol.scope, typeName, SymTag.TYPE);
        }

        if (symbol == symTable.notFoundSymbol) {
            // 4) Lookup the root scope for types such as 'error'
            symbol = lookupMemberSymbol(symTable.rootScope, typeName, SymTag.TYPE);
        }

        if (symbol == symTable.notFoundSymbol) {
            dlog.error(userDefinedTypeNode.pos, diagCode, typeName);
            resultType = symTable.errType;
            return;
        }
        resultType = symbol.type;
    }


    // private methods

    private BSymbol resolveOperator(ScopeEntry entry, List<BType> types) {
        BSymbol foundSymbol = symTable.notFoundSymbol;
        while (entry != NOT_FOUND_ENTRY) {
            BInvokableType opType = (BInvokableType) entry.symbol.type;
            if (types.size() == opType.paramTypes.size()) {
                boolean match = true;
                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).tag != opType.paramTypes.get(i).tag) {
                        match = false;
                    }
                }

                if (match) {
                    foundSymbol = entry.symbol;
                    break;
                }
            }

            entry = entry.next;
        }

        return foundSymbol;
    }

    private void visitBuiltInTypeNode(BLangType typeNode, TypeKind typeKind) {
        Name typeName = names.fromTypeKind(typeKind);
        BSymbol typeSymbol = lookupMemberSymbol(symTable.rootScope, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            dlog.error(typeNode.pos, diagCode, typeName);
        }

        resultType = typeNode.type = typeSymbol.type;
    }
}
