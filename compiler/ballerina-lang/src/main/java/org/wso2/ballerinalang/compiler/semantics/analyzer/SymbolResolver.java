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

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.IntersectableReferenceType;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.compiler.util.Constants.INFERRED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_ARRAY_INDICATOR;

/**
 * @since 0.94
 */
public class SymbolResolver extends BLangNodeVisitor {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 10; // -10 was added due to the JVM limitations
    private static final CompilerContext.Key<SymbolResolver> SYMBOL_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Names names;
    private BLangDiagnosticLog dlog;
    private Types types;

    private SymbolEnv env;
    private BType resultType;
    private DiagnosticCode diagCode;
    private SymbolEnter symbolEnter;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private Unifier unifier;

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
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.unifier = new Unifier();
    }

    public boolean checkForUniqueSymbol(Location pos, SymbolEnv env, BSymbol symbol) {
        //lookup symbol
        BSymbol foundSym = symTable.notFoundSymbol;
        int expSymTag = symbol.tag;
        if ((expSymTag & SymTag.IMPORT) == SymTag.IMPORT) {
            foundSym = lookupSymbolInPrefixSpace(env, symbol.name);
        } else if ((expSymTag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
            foundSym = lookupSymbolInAnnotationSpace(env, symbol.name);
        } else if ((expSymTag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
            foundSym = lookupSymbolInConstructorSpace(env, symbol.name);
        }  else if ((expSymTag & SymTag.MAIN) == SymTag.MAIN) {
            // Using this method for looking up in the main symbol space since record field symbols lookup have
            // different semantics depending on whether it's looking up a referenced symbol or looking up to see if
            // the symbol is unique within the scope.
            foundSym = lookupSymbolForDecl(env, symbol.name, SymTag.MAIN);
        }

        if (foundSym == symTable.notFoundSymbol && expSymTag == SymTag.FUNCTION) {
            int dotPosition = symbol.name.value.indexOf('.');
            if (dotPosition > 0 && dotPosition != symbol.name.value.length()) {
                String funcName = symbol.name.value.substring(dotPosition + 1);
                foundSym = lookupSymbolForDecl(env, names.fromString(funcName), SymTag.MAIN);
            }
        }

        //if symbol is not found then it is unique for the current scope
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }

        // if a symbol is found, then check whether it is unique
        if (!isDistinctSymbol(pos, symbol, foundSym)) {
            return false;
        }

        if (isRedeclaredSymbol(symbol, foundSym)) {

            Name name = symbol.name;
            if (Symbols.isRemote(symbol) && !Symbols.isRemote(foundSym)
                || !Symbols.isRemote(symbol) && Symbols.isRemote(foundSym)) {
                dlog.error(pos, DiagnosticErrorCode.UNSUPPORTED_REMOTE_METHOD_NAME_IN_SCOPE, name);
                return false;
            }
            if (symbol.kind != SymbolKind.CONSTANT) {
                dlog.error(pos, DiagnosticErrorCode.REDECLARED_SYMBOL, name);
            }
            return false;
        }

        if ((foundSym.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            // In order to remove duplicate errors.
            return false;
        }

        return true;
    }

    private boolean isRedeclaredSymbol(BSymbol symbol, BSymbol foundSym) {
        return hasSameOwner(symbol, foundSym) || isSymbolRedeclaredInTestPackage(symbol, foundSym) ||
                isRedeclaredTypeDefinitionSymbol(symbol, foundSym);
    }

    public boolean checkForUniqueSymbol(SymbolEnv env, BSymbol symbol) {
        BSymbol foundSym = lookupSymbolInMainSpace(env, symbol.name);
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }
        if (symbol.tag == SymTag.CONSTRUCTOR && foundSym.tag == SymTag.ERROR) {
            return false;
        }
        return !hasSameOwner(symbol, foundSym);
    }

    /**
     * This method will check whether the given symbol that is being defined is unique by only checking its current
     * environment scope.
     *
     * @param pos       symbol pos for diagnostic purpose.
     * @param env       symbol environment to lookup.
     * @param symbol    the symbol that is being defined.
     * @param expSymTag expected tag of the symbol for.
     * @return true if the symbol is unique, false otherwise.
     */
    public boolean checkForUniqueSymbolInCurrentScope(Location pos, SymbolEnv env, BSymbol symbol,
                                                      int expSymTag) {
        //lookup in current scope
        BSymbol foundSym = lookupSymbolInGivenScope(env, symbol.name, expSymTag);

        //if symbol is not found then it is unique for the current scope
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }

        //if a symbol is found, then check whether it is unique
        return isDistinctSymbol(pos, symbol, foundSym);
    }

    /**
     * This method will check whether the symbol being defined is unique comparing it with the found symbol
     * from the scope.
     *
     * @param pos      symbol pos for diagnostic purpose.
     * @param symbol   symbol that is being defined.
     * @param foundSym symbol that is found from the scope.
     * @return true if the symbol is unique, false otherwise.
     */
    private boolean isDistinctSymbol(Location pos, BSymbol symbol, BSymbol foundSym) {
        int symbolTag = symbol.tag;
        int foundSymTag = foundSym.tag;
        // It is allowed to have a error constructor symbol with the same name as a type def.
        if (symbolTag == SymTag.CONSTRUCTOR && foundSymTag == SymTag.ERROR) {
            return false;
        }

        if (isSymbolDefinedInRootPkgLvl(foundSym)) {
            dlog.error(pos, DiagnosticErrorCode.REDECLARED_BUILTIN_SYMBOL, symbol.name);
            return false;
        }

        return true;
    }

    /**
     * This method will check whether the symbol being defined is unique comparing it with the found symbol
     * from the scope.
     *
     * @param symbol   symbol that is being defined.
     * @param foundSym symbol that is found from the scope.
     * @return true if the symbol is unique, false otherwise.
     */
    private boolean isDistinctSymbol(BSymbol symbol, BSymbol foundSym) {
        // It is allowed to have a error constructor symbol with the same name as a type def.
        if (symbol.tag == SymTag.CONSTRUCTOR && foundSym.tag == SymTag.ERROR) {
            return false;
        }

        if (isSymbolDefinedInRootPkgLvl(foundSym)) {
            return false;
        }

        return !hasSameOwner(symbol, foundSym);
    }

    private boolean hasSameOwner(BSymbol symbol, BSymbol foundSym) {
        // check whether the given symbol owner is same as found symbol's owner
        if (foundSym.owner == symbol.owner) {
            return true;
        } else if (Symbols.isFlagOn(symbol.owner.flags, Flags.LAMBDA) &&
                ((foundSym.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE)) {
            // If the symbol being defined is inside a lambda and the existing symbol is defined inside a function, both
            // symbols are in the same block scope.
            return true;
        } else if (((symbol.owner.tag & SymTag.LET) == SymTag.LET) &&
                ((foundSym.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE)) {
            // If the symbol being defined is inside a let expression and the existing symbol is defined inside a
            // function both symbols are in the same scope.
            return  true;
        }
        return  false;
    }

    private boolean isRedeclaredTypeDefinitionSymbol(BSymbol symbol, BSymbol foundSym) {
        if (symbol.kind != SymbolKind.TYPE_DEF && foundSym.kind != SymbolKind.TYPE_DEF) {
            return false;
        }
        if (symbol.kind == SymbolKind.TYPE_DEF) {
            return hasSameOwner(symbol.type.tsymbol, foundSym);
        } else {
            return hasSameOwner(symbol, foundSym.type.tsymbol);
        }
    }

    private boolean isSymbolRedeclaredInTestPackage(BSymbol symbol, BSymbol foundSym) {
        if (Symbols.isFlagOn(symbol.owner.flags, Flags.TESTABLE) &&
                !Symbols.isFlagOn(foundSym.owner.flags, Flags.TESTABLE)) {
            return true;
        }
        return false;
    }

    private boolean isSymbolDefinedInRootPkgLvl(BSymbol foundSym) {
        int foundSymTag = foundSym.tag;
        return symTable.rootPkgSymbol.pkgID.equals(foundSym.pkgID) &&
                (foundSymTag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME;
    }

    /**
     * Lookup the symbol using given name in the given environment scope only.
     *
     * @param env       environment to lookup the symbol.
     * @param name      name of the symbol to lookup.
     * @param expSymTag expected tag of the symbol.
     * @return if a symbol is found return it.
     */
    public BSymbol lookupSymbolInGivenScope(SymbolEnv env, Name name, int expSymTag) {
        ScopeEntry entry = env.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if (symTable.rootPkgSymbol.pkgID.equals(entry.symbol.pkgID) &&
                    (entry.symbol.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME) {
                return entry.symbol;
            }
            if ((entry.symbol.tag & expSymTag) == expSymTag && !isFieldRefFromWithinARecord(entry.symbol, env)) {
                return entry.symbol;
            }
            entry = entry.next;
        }
        return symTable.notFoundSymbol;
    }

    public boolean checkForUniqueMemberSymbol(Location pos, SymbolEnv env, BSymbol symbol) {
        BSymbol foundSym = lookupMemberSymbol(pos, env.scope, env, symbol.name, symbol.tag);
        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.REDECLARED_SYMBOL, symbol.name);
            return false;
        }

        return true;
    }

    public BSymbol resolveBinaryOperator(OperatorKind opKind,
                                         BType lhsType,
                                         BType rhsType) {
        return resolveOperator(names.fromString(opKind.value()), Lists.of(lhsType, rhsType));
    }

    BSymbol createEqualityOperator(OperatorKind opKind, BType lhsType, BType rhsType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        BType retType = symTable.booleanType;
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        return new BOperatorSymbol(names.fromString(opKind.value()), null, opType, null, symTable.builtinPos, VIRTUAL);
    }

    public BSymbol resolveUnaryOperator(Location pos,
                                        OperatorKind opKind,
                                        BType type) {
        return resolveOperator(names.fromString(opKind.value()), Lists.of(type));
    }

    public BSymbol resolveOperator(Name name, List<BType> types) {
        ScopeEntry entry = symTable.rootScope.lookup(name);
        return resolveOperator(entry, types);
    }

    BSymbol createBinaryComparisonOperator(OperatorKind opKind, BType lhsType, BType rhsType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        BInvokableType opType = new BInvokableType(paramTypes, symTable.booleanType, null);
        return new BOperatorSymbol(names.fromString(opKind.value()), null, opType, null, symTable.builtinPos, VIRTUAL);
    }

    BSymbol createBinaryOperator(OperatorKind opKind, BType lhsType, BType rhsType, BType retType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        return new BOperatorSymbol(names.fromString(opKind.value()), null, opType, null, symTable.builtinPos, VIRTUAL);
    }

    public BSymbol resolvePkgSymbol(Location pos, SymbolEnv env, Name pkgAlias) {
        if (pkgAlias == Names.EMPTY) {
            // Return the current package symbol
            return env.enclPkg.symbol;
        }

        // Lookup for an imported package
        BSymbol pkgSymbol = lookupSymbolInPrefixSpace(env, pkgAlias);
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias.value);
        }

        return pkgSymbol;
    }

    public BSymbol resolvePrefixSymbol(SymbolEnv env, Name pkgAlias, Name compUnit) {
        if (pkgAlias == Names.EMPTY) {
            // Return the current package symbol
            return env.enclPkg.symbol;
        }

        // Lookup for an imported package
        ScopeEntry entry = env.scope.lookup(pkgAlias);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.XMLNS) == SymTag.XMLNS) {
                return entry.symbol;
            }

            if ((entry.symbol.tag & SymTag.IMPORT) == SymTag.IMPORT &&
                    ((BPackageSymbol) entry.symbol).compUnit.equals(compUnit)) {
                ((BPackageSymbol) entry.symbol).isUsed = true;
                return entry.symbol;
            }

            entry = entry.next;
        }

        if (env.enclEnv != null) {
            return resolvePrefixSymbol(env.enclEnv, pkgAlias, compUnit);
        }

        return symTable.notFoundSymbol;
    }

    public BSymbol resolveAnnotation(Location pos, SymbolEnv env, Name pkgAlias, Name annotationName) {
        return this.lookupAnnotationSpaceSymbolInPackage(pos, env, pkgAlias, annotationName);
    }

    public BSymbol resolveStructField(Location location, SymbolEnv env, Name fieldName,
                                      BTypeSymbol structSymbol) {
        return lookupMemberSymbol(location, structSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BSymbol resolveObjectField(Location location, SymbolEnv env, Name fieldName,
                                      BTypeSymbol objectSymbol) {
        return lookupMemberSymbol(location, objectSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BSymbol resolveObjectMethod(Location pos, SymbolEnv env, Name fieldName,
                                       BObjectTypeSymbol objectSymbol) {
        return lookupMemberSymbol(pos, objectSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BSymbol resolveInvocableObjectField(Location pos, SymbolEnv env, Name fieldName,
                                               BObjectTypeSymbol objectTypeSymbol) {
        return lookupMemberSymbol(pos, objectTypeSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BType resolveTypeNode(BLangType typeNode, SymbolEnv env) {
        return resolveTypeNode(typeNode, env, DiagnosticErrorCode.UNKNOWN_TYPE);
    }

    public BType resolveTypeNode(BLangType typeNode, SymbolEnv env, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        DiagnosticCode preDiagCode = this.diagCode;

        this.env = env;
        this.diagCode = diagCode;
        typeNode.accept(this);
        this.env = prevEnv;
        this.diagCode = preDiagCode;

        if (types.getReferredType(this.resultType) != symTable.noType) {
            // If the typeNode.nullable is true then convert the resultType to a union type
            // if it is not already a union type, JSON type, or any type
            BType referredType = types.getReferredType(this.resultType);
            if (typeNode.nullable && referredType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) referredType;
                unionType.add(symTable.nilType);
            } else if (typeNode.nullable && referredType.tag != TypeTags.JSON && referredType.tag != TypeTags.ANY) {
                this.resultType = BUnionType.create(null, this.resultType, symTable.nilType);
            }
        }

        validateDistinctType(typeNode, resultType);

        typeNode.setBType(resultType);
        return resultType;
    }

    private void validateDistinctType(BLangType typeNode, BType type) {
        if (typeNode.flagSet.contains(Flag.DISTINCT) && !isDistinctAllowedOnType(type)) {
            dlog.error(typeNode.pos, DiagnosticErrorCode.DISTINCT_TYPING_ONLY_SUPPORT_OBJECTS_AND_ERRORS);
        }
    }

    private boolean isDistinctAllowedOnType(BType type) {
        if (type.tag == TypeTags.TYPEREFDESC) {
            return isDistinctAllowedOnType(types.getReferredType(type));
        }
        if (type.tag == TypeTags.INTERSECTION) {
            for (BType constituentType : ((BIntersectionType) type).getConstituentTypes()) {
                if (!isDistinctAllowedOnType(constituentType)) {
                    return false;
                }
            }
            return true;
        }

        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (!isDistinctAllowedOnType(memberType)) {
                    return false;
                }
            }
            return true;

        }

        return type.tag == TypeTags.ERROR
                || type.tag == TypeTags.OBJECT
                || type.tag == TypeTags.NONE
                || type.tag == TypeTags.SEMANTIC_ERROR;
    }

    /**
     * Return the symbol associated with the given name in the current package. This method first searches the symbol in
     * the current scope and proceeds the enclosing scope, if it is not there in the current scope. This process
     * continues until the symbol is found or the root scope is reached. This method is mainly meant for checking
     * whether a given symbol is already defined in the scope hierarchy.
     *
     * @param env       current symbol environment
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    private BSymbol lookupSymbolForDecl(SymbolEnv env, Name name, int expSymTag) {
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
     * Return the symbol associated with the given name in the current package. This method first searches the symbol in
     * the current scope and proceeds the enclosing scope, if it is not there in the current scope. This process
     * continues until the symbol is found or the root scope is reached. This method is meant for looking up a symbol
     * when they are referenced. If looking up a symbol from within a record type definition, this method ignores record
     * fields. This is done so that default value expressions cannot refer to other record fields.
     *
     * @param env       current symbol environment
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    private BSymbol lookupSymbol(SymbolEnv env, Name name, int expSymTag) {
        ScopeEntry entry = env.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & expSymTag) == expSymTag && !isFieldRefFromWithinARecord(entry.symbol, env)) {
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
     * Checks whether the specified symbol is a symbol of a record field and whether that field is referred to from
     * within a record type definition (not necessarily the owner of the field).
     *
     * @param symbol symbol to be tested
     * @param env    the environment in which the symbol was found
     * @return returns `true` if the aboove described condition holds
     */
    private boolean isFieldRefFromWithinARecord(BSymbol symbol, SymbolEnv env) {
        return (symbol.owner.tag & SymTag.RECORD) == SymTag.RECORD &&
                env.enclType != null && env.enclType.getKind() == NodeKind.RECORD_TYPE;
    }

    public BSymbol lookupSymbolInMainSpace(SymbolEnv env, Name name) {
        return lookupSymbol(env, name, SymTag.MAIN);
    }

    public BSymbol lookupSymbolInAnnotationSpace(SymbolEnv env, Name name) {
        return lookupSymbol(env, name, SymTag.ANNOTATION);
    }

    public BSymbol lookupSymbolInPrefixSpace(SymbolEnv env, Name name) {
        return lookupSymbol(env, name, SymTag.IMPORT);
    }

    public BSymbol lookupSymbolInConstructorSpace(SymbolEnv env, Name name) {
        return lookupSymbol(env, name, SymTag.CONSTRUCTOR);
    }

    public BSymbol lookupLangLibMethod(BType type, Name name) {

        if (symTable.langAnnotationModuleSymbol == null) {
            return symTable.notFoundSymbol;
        }
        BSymbol bSymbol;
        switch (type.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                bSymbol = lookupMethodInModule(symTable.langArrayModuleSymbol, name, env);
                break;
            case TypeTags.DECIMAL:
                bSymbol = lookupMethodInModule(symTable.langDecimalModuleSymbol, name, env);
                break;
            case TypeTags.ERROR:
                bSymbol = lookupMethodInModule(symTable.langErrorModuleSymbol, name, env);
                break;
            case TypeTags.FLOAT:
                bSymbol = lookupMethodInModule(symTable.langFloatModuleSymbol, name, env);
                break;
            case TypeTags.FUTURE:
                bSymbol = lookupMethodInModule(symTable.langFutureModuleSymbol, name, env);
                break;
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.BYTE:
                bSymbol = lookupMethodInModule(symTable.langIntModuleSymbol, name, env);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                bSymbol = lookupMethodInModule(symTable.langMapModuleSymbol, name, env);
                break;
            case TypeTags.OBJECT:
                bSymbol = lookupMethodInModule(symTable.langObjectModuleSymbol, name, env);
                break;
            case TypeTags.STREAM:
                bSymbol = lookupMethodInModule(symTable.langStreamModuleSymbol, name, env);
                break;
            case TypeTags.TABLE:
                bSymbol = lookupMethodInModule(symTable.langTableModuleSymbol, name, env);
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                bSymbol = lookupMethodInModule(symTable.langStringModuleSymbol, name, env);
                break;
            case TypeTags.TYPEDESC:
                bSymbol = lookupMethodInModule(symTable.langTypedescModuleSymbol, name, env);
                break;
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_PI:
                bSymbol = lookupMethodInModule(symTable.langXmlModuleSymbol, name, env);
                break;
            case TypeTags.XML_TEXT:
                bSymbol = lookupMethodInModule(symTable.langXmlModuleSymbol, name, env);
                if (bSymbol == symTable.notFoundSymbol) {
                    bSymbol = lookupMethodInModule(symTable.langStringModuleSymbol, name, env);
                }
                break;
            case TypeTags.BOOLEAN:
                bSymbol = lookupMethodInModule(symTable.langBooleanModuleSymbol, name, env);
                break;
            case TypeTags.UNION:
                Iterator<BType> itr = ((BUnionType) type).getMemberTypes().iterator();

                if (!itr.hasNext()) {
                    throw new IllegalArgumentException(
                            format("Union type '%s' does not have member types", type.toString()));
                }

                BType member = types.getReferredType(itr.next());

                if (TypeTags.isIntegerTypeTag(member.tag) || member.tag == TypeTags.BYTE) {
                    member = symTable.intType;
                }

                if (TypeTags.isStringTypeTag(member.tag)) {
                    member = symTable.stringType;
                }

                if (types.isSubTypeOfBaseType(type, member.tag)) {
                    bSymbol = lookupLangLibMethod(member, name);
                } else {
                    bSymbol = symTable.notFoundSymbol;
                }
                break;
            case TypeTags.TYPEREFDESC:
                bSymbol = lookupLangLibMethod(types.getReferredType(type), name);
                break;
            case TypeTags.FINITE:
                if (types.isAssignable(type, symTable.intType)) {
                    return lookupLangLibMethod(symTable.intType, name);
                }

                if (types.isAssignable(type, symTable.stringType)) {
                    return lookupLangLibMethod(symTable.stringType, name);
                }

                if (types.isAssignable(type, symTable.decimalType)) {
                    return lookupLangLibMethod(symTable.decimalType, name);
                }

                if (types.isAssignable(type, symTable.floatType)) {
                    return lookupLangLibMethod(symTable.floatType, name);
                }

                if (types.isAssignable(type, symTable.booleanType)) {
                    return lookupLangLibMethod(symTable.booleanType, name);
                }

                bSymbol = symTable.notFoundSymbol;
                break;
            default:
                bSymbol = symTable.notFoundSymbol;
        }
        if (bSymbol == symTable.notFoundSymbol && type.tag != TypeTags.OBJECT) {
            bSymbol = lookupMethodInModule(symTable.langValueModuleSymbol, name, env);
        }

        if (bSymbol == symTable.notFoundSymbol) {
            bSymbol = lookupMethodInModule(symTable.langInternalModuleSymbol, name, env);
        }

        return bSymbol;
    }

    /**
     * Recursively analyse the symbol env to find the closure variable symbol that is being resolved.
     *
     * @param env       symbol env to analyse and find the closure variable.
     * @param name      name of the symbol to lookup
     * @param expSymTag symbol tag
     * @return closure symbol wrapper along with the resolved count
     */
    public BSymbol lookupClosureVarSymbol(SymbolEnv env, Name name, int expSymTag) {
        ScopeEntry entry = env.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if (symTable.rootPkgSymbol.pkgID.equals(entry.symbol.pkgID) &&
                    (entry.symbol.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME) {
                return entry.symbol;
            }
            if ((entry.symbol.tag & expSymTag) == expSymTag && !isFieldRefFromWithinARecord(entry.symbol, env)) {
                return entry.symbol;
            }
            entry = entry.next;
        }

        if (env.enclEnv == null || env.enclEnv.node == null) {
            return symTable.notFoundSymbol;
        }

        return lookupClosureVarSymbol(env.enclEnv, name, expSymTag);
    }

    public BSymbol lookupMainSpaceSymbolInPackage(Location pos,
                                                  SymbolEnv env,
                                                  Name pkgAlias,
                                                  Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInMainSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.lineRange().filePath()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.MAIN);
    }

    public BSymbol lookupPrefixSpaceSymbolInPackage(Location pos,
                                                    SymbolEnv env,
                                                    Name pkgAlias,
                                                    Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInPrefixSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.lineRange().filePath()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.IMPORT);
    }

    public BSymbol lookupAnnotationSpaceSymbolInPackage(Location pos,
                                                        SymbolEnv env,
                                                        Name pkgAlias,
                                                        Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInAnnotationSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.lineRange().filePath()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.ANNOTATION);
    }

    public BSymbol lookupConstructorSpaceSymbolInPackage(Location pos,
                                                         SymbolEnv env,
                                                         Name pkgAlias,
                                                         Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInConstructorSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.lineRange().filePath()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.CONSTRUCTOR);
    }

    public BSymbol lookupMethodInModule(BPackageSymbol moduleSymbol, Name name, SymbolEnv env) {
        // What we get here is T.Name, this should convert to
        ScopeEntry entry = moduleSymbol.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.FUNCTION) != SymTag.FUNCTION) {
                entry = entry.next;
                continue;
            }
            if (isMemberAccessAllowed(env, entry.symbol)) {
                return entry.symbol;
            }
            return symTable.notFoundSymbol;
        }
        return symTable.notFoundSymbol;
    }

    /**
     * Return the symbol with the given name.
     * This method only looks at the symbol defined in the given scope.
     *
     * @param pos       diagnostic position
     * @param scope     current scope
     * @param env       symbol environment
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    public BSymbol lookupMemberSymbol(Location pos,
                                      Scope scope,
                                      SymbolEnv env,
                                      Name name,
                                      int expSymTag) {
        ScopeEntry entry = scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & expSymTag) != expSymTag) {
                entry = entry.next;
                continue;
            }

            if (isMemberAccessAllowed(env, entry.symbol)) {
                return entry.symbol;
            } else {
                dlog.error(pos, DiagnosticErrorCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, entry.symbol.name);
                return symTable.notFoundSymbol;
            }
        }

        return symTable.notFoundSymbol;
    }

    /**
     * Resolve and return the namespaces visible to the given environment, as a map.
     *
     * @param env Environment to get the visible namespaces
     * @return Map of namespace symbols visible to the given environment
     */
    public Map<Name, BXMLNSSymbol> resolveAllNamespaces(SymbolEnv env) {
        Map<Name, BXMLNSSymbol> namespaces = new LinkedHashMap<Name, BXMLNSSymbol>();
        addNamespacesInScope(namespaces, env);
        return namespaces;
    }

    public void boostrapErrorType() {

        ScopeEntry entry = symTable.rootPkgSymbol.scope.lookup(Names.ERROR);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            symTable.errorType = (BErrorType) entry.symbol.type;
            symTable.detailType = (BMapType) symTable.errorType.detailType;
            return;
        }
        throw new IllegalStateException("built-in error not found ?");
    }

    public void defineOperators() {
        symTable.defineOperators();
    }

    public void bootstrapAnydataType() {
        ScopeEntry entry = symTable.langAnnotationModuleSymbol.scope.lookup(Names.ANYDATA);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            BUnionType type = (BUnionType) entry.symbol.type;
            symTable.anydataType = new BAnydataType(type);
            symTable.anydataOrReadonly = BUnionType.create(null, symTable.anydataType, symTable.readonlyType);
            entry.symbol.type = symTable.anydataType;
            entry.symbol.origin = BUILTIN;

            symTable.anydataType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.ANYDATA,
                    PackageID.ANNOTATIONS, symTable.anydataType, symTable.rootPkgSymbol, symTable.builtinPos, BUILTIN);
            return;
        }
        throw new IllegalStateException("built-in 'anydata' type not found");
    }

    public void bootstrapJsonType() {
        ScopeEntry entry = symTable.langAnnotationModuleSymbol.scope.lookup(Names.JSON);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            BUnionType type = (BUnionType) entry.symbol.type;
            symTable.jsonType = new BJSONType(type);
            symTable.jsonType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.JSON, PackageID.ANNOTATIONS,
                    symTable.jsonType, symTable.langAnnotationModuleSymbol, symTable.builtinPos, BUILTIN);
            entry.symbol.type = symTable.jsonType;
            entry.symbol.origin = BUILTIN;
            return;
        }
        throw new IllegalStateException("built-in 'json' type not found");
    }

    public void bootstrapCloneableType() {
        if (symTable.langValueModuleSymbol != null) {
            ScopeEntry entry = symTable.langValueModuleSymbol.scope.lookup(Names.CLONEABLE);
            while (entry != NOT_FOUND_ENTRY) {
                if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                    entry = entry.next;
                    continue;
                }
                symTable.cloneableType = (BUnionType) entry.symbol.type;
                symTable.cloneableType.tsymbol =
                        new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.CLONEABLE,
                                PackageID.VALUE, symTable.cloneableType, symTable.langValueModuleSymbol,
                                symTable.builtinPos, BUILTIN);
                symTable.detailType = new BMapType(TypeTags.MAP, symTable.cloneableType, null);
                symTable.errorType = new BErrorType(null, symTable.detailType);
                symTable.errorType.tsymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.ERROR,
                        symTable.rootPkgSymbol.pkgID, symTable.errorType, symTable.rootPkgSymbol, symTable.builtinPos
                        , BUILTIN);

                symTable.errorOrNilType = BUnionType.create(null, symTable.errorType, symTable.nilType);
                symTable.anyOrErrorType = BUnionType.create(null, symTable.anyType, symTable.errorType);

                symTable.mapAllType = new BMapType(TypeTags.MAP, symTable.anyOrErrorType, null);
                symTable.arrayAllType = new BArrayType(symTable.anyOrErrorType);
                symTable.typeDesc.constraint = symTable.anyOrErrorType;
                symTable.futureType.constraint = symTable.anyOrErrorType;

                symTable.pureType = BUnionType.create(null, symTable.anydataType, symTable.errorType);
                return;
            }
            throw new IllegalStateException("built-in 'lang.value:Cloneable' type not found");
        }

        ScopeEntry entry = symTable.rootPkgSymbol.scope.lookup(Names.CLONEABLE_INTERNAL);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            entry.symbol.type = symTable.cloneableType;
            break;
        }

    }

    public void bootstrapIntRangeType() {
        ScopeEntry entry = symTable.langInternalModuleSymbol.scope.lookup(Names.CREATE_INT_RANGE);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.INVOKABLE) != SymTag.INVOKABLE) {
                entry = entry.next;
                continue;
            }
            symTable.intRangeType = (BObjectType) types
                    .getReferredType(((BInvokableType) entry.symbol.type).retType);
            symTable.defineIntRangeOperations();
            return;
        }
        throw new IllegalStateException("built-in Integer Range type not found ?");
    }

    public void bootstrapIterableType() {

        ScopeEntry entry = symTable.langObjectModuleSymbol.scope.lookup(Names.OBJECT_ITERABLE);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            symTable.iterableType = (BObjectType) entry.symbol.type;
            return;
        }
        throw new IllegalStateException("built-in distinct Iterable type not found ?");
    }

    public void loadRawTemplateType() {
        ScopeEntry entry = symTable.langObjectModuleSymbol.scope.lookup(Names.RAW_TEMPLATE);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            symTable.rawTemplateType = (BObjectType) entry.symbol.type;
            return;
        }
        throw new IllegalStateException("'lang.object:RawTemplate' type not found");
    }

    // visit type nodes

    public void visit(BLangValueType valueTypeNode) {
        visitBuiltInTypeNode(valueTypeNode, valueTypeNode.typeKind, this.env);
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        visitBuiltInTypeNode(builtInRefType, builtInRefType.typeKind, this.env);
    }

    public void visit(BLangArrayType arrayTypeNode) {
        // The value of the dimensions field should always be >= 1
        // If sizes is null array is unsealed
        resultType = resolveTypeNode(arrayTypeNode.elemtype, env, diagCode);
        if (resultType == symTable.noType) {
            return;
        }
        boolean isError = false;
        for (int i = 0; i < arrayTypeNode.dimensions; i++) {
            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                                                                   env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                   arrayTypeNode.pos, SOURCE);
            BArrayType arrType;
            if (arrayTypeNode.sizes.length == 0) {
                arrType = new BArrayType(resultType, arrayTypeSymbol);
            } else {
                BLangExpression size = arrayTypeNode.sizes[i];
                if (size.getKind() == NodeKind.LITERAL || size.getKind() == NodeKind.NUMERIC_LITERAL) {
                    Integer sizeIndicator = (Integer) (((BLangLiteral) size).getValue());
                    BArrayState arrayState;
                    if (sizeIndicator == OPEN_ARRAY_INDICATOR) {
                        arrayState = BArrayState.OPEN;
                    } else if (sizeIndicator == INFERRED_ARRAY_INDICATOR) {
                        arrayState = BArrayState.INFERRED;
                    } else {
                        arrayState = BArrayState.CLOSED;
                    }
                    arrType =  new BArrayType(resultType, arrayTypeSymbol,  sizeIndicator, arrayState);
                } else {
                    if (size.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                        dlog.error(size.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.intType,
                                ((BLangTypedescExpr) size).getTypeNode());
                        isError = true;
                        continue;
                    }

                    BLangSimpleVarRef sizeReference = (BLangSimpleVarRef) size;
                    Name pkgAlias = names.fromIdNode(sizeReference.pkgAlias);
                    Name typeName = names.fromIdNode(sizeReference.variableName);

                    BSymbol sizeSymbol = lookupMainSpaceSymbolInPackage(size.pos, env, pkgAlias, typeName);
                    sizeReference.symbol = sizeSymbol;

                    if (symTable.notFoundSymbol == sizeSymbol) {
                        dlog.error(arrayTypeNode.pos, DiagnosticErrorCode.UNDEFINED_SYMBOL, size);
                        isError = true;
                        continue;
                    }

                    if (sizeSymbol.tag != SymTag.CONSTANT) {
                        dlog.error(size.pos, DiagnosticErrorCode.INVALID_ARRAY_SIZE_REFERENCE, sizeSymbol);
                        isError = true;
                        continue;
                    }

                    BConstantSymbol sizeConstSymbol = (BConstantSymbol) sizeSymbol;
                    BType lengthLiteralType = sizeConstSymbol.literalType;

                    if (lengthLiteralType.tag != TypeTags.INT) {
                        dlog.error(size.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.intType,
                                sizeConstSymbol.literalType);
                        isError = true;
                        continue;
                    }

                    int length;
                    long lengthCheck = Long.parseLong(sizeConstSymbol.type.toString());
                    if (lengthCheck > MAX_ARRAY_SIZE) {
                        length = 0;
                        dlog.error(size.pos,
                                DiagnosticErrorCode.ARRAY_LENGTH_GREATER_THAT_2147483637_NOT_YET_SUPPORTED);
                    } else {
                        length = (int) lengthCheck;
                    }
                    arrType = new BArrayType(resultType, arrayTypeSymbol, length, BArrayState.CLOSED);
                }
            }
            arrayTypeSymbol.type = arrType;
            resultType = arrayTypeSymbol.type;
            markParameterizedType(arrType, arrType.eType);
        }
        if (isError) {
            resultType = symTable.semanticError;
        }
    }

    public void visit(BLangUnionTypeNode unionTypeNode) {

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();

        for (BLangType langType : unionTypeNode.memberTypeNodes) {
            BType resolvedType = resolveTypeNode(langType, env);
            if (resolvedType == symTable.noType) {
                resultType = symTable.noType;
                return;
            }
            memberTypes.add(resolvedType);
        }

        BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                env.scope.owner, unionTypeNode.pos, SOURCE);

        BUnionType unionType = BUnionType.create(unionTypeSymbol, memberTypes);
        unionTypeSymbol.type = unionType;

        markParameterizedType(unionType, memberTypes);

        resultType = unionType;
    }

    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {

        resultType = computeIntersectionType(intersectionTypeNode);
    }

    public void visit(BLangObjectTypeNode objectTypeNode) {
        EnumSet<Flag> flags = EnumSet.copyOf(objectTypeNode.flagSet);
        if (objectTypeNode.isAnonymous) {
            flags.add(Flag.PUBLIC);
        }

        int typeFlags = 0;
        if (flags.contains(Flag.READONLY)) {
            typeFlags |= Flags.READONLY;
        }

        if (flags.contains(Flag.ISOLATED)) {
            typeFlags |= Flags.ISOLATED;
        }

        if (flags.contains(Flag.SERVICE)) {
            typeFlags |= Flags.SERVICE;
        }

        BTypeSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(flags), Names.EMPTY,
                env.enclPkg.symbol.pkgID, null, env.scope.owner, objectTypeNode.pos, SOURCE);

        BObjectType objectType = new BObjectType(objectSymbol, typeFlags);

        objectSymbol.type = objectType;
        objectTypeNode.symbol = objectSymbol;

        resultType = objectType;
    }

    public void visit(BLangRecordTypeNode recordTypeNode) {
        // If we cannot resolve a type of a type definition, we create a dummy symbol for it. If the type node is
        // a record, a symbol will be created for it when we define the dummy symbol (from here). When we define the
        // node later, this method will be called again. In such cases, we don't need to create a new symbol here.
        if (recordTypeNode.symbol == null) {
            EnumSet<Flag> flags = recordTypeNode.isAnonymous ? EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS)
                    : EnumSet.noneOf(Flag.class);
            BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                                                                        env.enclPkg.symbol.pkgID, null,
                                                                        env.scope.owner, recordTypeNode.pos,
                                                                        recordTypeNode.isAnonymous ? VIRTUAL : SOURCE);
            BRecordType recordType = new BRecordType(recordSymbol);
            recordSymbol.type = recordType;
            recordTypeNode.symbol = recordSymbol;

            if (env.node.getKind() != NodeKind.PACKAGE) {
                recordSymbol.name = names.fromString(
                        anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID));
                symbolEnter.defineSymbol(recordTypeNode.pos, recordTypeNode.symbol, env);
                symbolEnter.defineNode(recordTypeNode, env);
            }

            resultType = recordType;
        } else {
            resultType = recordTypeNode.symbol.type;
        }
    }

    public void visit(BLangStreamType streamTypeNode) {
        BType type = resolveTypeNode(streamTypeNode.type, env);
        BType constraintType = resolveTypeNode(streamTypeNode.constraint, env);
        BType error = streamTypeNode.error != null ? resolveTypeNode(streamTypeNode.error, env) : symTable.nilType;
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            resultType = symTable.noType;
            return;
        }

        BType streamType = new BStreamType(TypeTags.STREAM, constraintType, error, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        streamType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                                                      typeSymbol.originalName, typeSymbol.pkgID, streamType,
                                                      env.scope.owner, streamTypeNode.pos, SOURCE);

        markParameterizedType(streamType, constraintType);
        if (error != null) {
            markParameterizedType(streamType, error);
        }

        resultType = streamType;
    }

    public void visit(BLangTableTypeNode tableTypeNode) {
        BType type = resolveTypeNode(tableTypeNode.type, env);
        BType constraintType = resolveTypeNode(tableTypeNode.constraint, env);
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            resultType = symTable.noType;
            return;
        }

        BTableType tableType = new BTableType(TypeTags.TABLE, constraintType, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        tableType.tsymbol = Symbols.createTypeSymbol(SymTag.TYPE, Flags.asMask(EnumSet.noneOf(Flag.class)),
                                                     typeSymbol.name, typeSymbol.originalName, typeSymbol.pkgID,
                                                     tableType, env.scope.owner, tableTypeNode.pos, SOURCE);
        tableType.tsymbol.flags = typeSymbol.flags;
        tableType.constraintPos = tableTypeNode.constraint.pos;
        tableType.isTypeInlineDefined = tableTypeNode.isTypeInlineDefined;

        if (tableTypeNode.tableKeyTypeConstraint != null) {
            tableType.keyTypeConstraint = resolveTypeNode(tableTypeNode.tableKeyTypeConstraint.keyType, env);
            tableType.keyPos = tableTypeNode.tableKeyTypeConstraint.pos;
        } else if (tableTypeNode.tableKeySpecifier != null) {
            BLangTableKeySpecifier tableKeySpecifier = tableTypeNode.tableKeySpecifier;
            List<String> fieldNameList = new ArrayList<>();
            for (IdentifierNode identifier : tableKeySpecifier.fieldNameIdentifierList) {
                fieldNameList.add(((BLangIdentifier) identifier).value);
            }
            tableType.fieldNameList = fieldNameList;
            tableType.keyPos = tableKeySpecifier.pos;
        }

        if (types.getReferredType(constraintType).tag == TypeTags.MAP &&
                (tableType.fieldNameList != null || tableType.keyTypeConstraint != null) &&
                !tableType.tsymbol.owner.getFlags().contains(Flag.LANG_LIB)) {
            dlog.error(tableType.keyPos,
                    DiagnosticErrorCode.KEY_CONSTRAINT_NOT_SUPPORTED_FOR_TABLE_WITH_MAP_CONSTRAINT);
            resultType = symTable.semanticError;
            return;
        }

        markParameterizedType(tableType, constraintType);
        tableTypeNode.tableType = tableType;

        resultType = tableType;
    }

    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                                                                Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY,
                                                                env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                finiteTypeNode.pos, SOURCE);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        for (BLangExpression literal : finiteTypeNode.valueSpace) {
            ((BLangLiteral) literal).setBType(symTable.getTypeFromTag(((BLangLiteral) literal).getBType().tag));
            finiteType.addValue(literal);
        }
        finiteTypeSymbol.type = finiteType;

        resultType = finiteType;
    }

    public void visit(BLangTupleTypeNode tupleTypeNode) {
        List<BType> memberTypes = new ArrayList<>();
        for (BLangType memTypeNode : tupleTypeNode.memberTypeNodes) {
            BType type = resolveTypeNode(memTypeNode, env);

            // If at least one member is undefined, return noType as the type.
            if (type == symTable.noType) {
                resultType = symTable.noType;
                return;
            }

            memberTypes.add(type);
        }

        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                               Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                                                               env.scope.owner, tupleTypeNode.pos, SOURCE);

        BTupleType tupleType = new BTupleType(tupleTypeSymbol, memberTypes);
        tupleTypeSymbol.type = tupleType;

        if (tupleTypeNode.restParamType != null) {
            BType tupleRestType = resolveTypeNode(tupleTypeNode.restParamType, env);
            if (tupleRestType == symTable.noType) {
                resultType = symTable.noType;
                return;
            }
            tupleType.restType = tupleRestType;
            markParameterizedType(tupleType, tupleType.restType);
        }

        markParameterizedType(tupleType, memberTypes);

        resultType = tupleType;
    }

    public void visit(BLangErrorType errorTypeNode) {
        BType detailType = Optional.ofNullable(errorTypeNode.detailType)
                .map(bLangType -> resolveTypeNode(bLangType, env)).orElse(symTable.detailType);

        if (errorTypeNode.isAnonymous) {
            errorTypeNode.flagSet.add(Flag.PUBLIC);
            errorTypeNode.flagSet.add(Flag.ANONYMOUS);
        }

        // The builtin error type
        BErrorType bErrorType = symTable.errorType;

        boolean distinctErrorDef = errorTypeNode.flagSet.contains(Flag.DISTINCT);
        if (detailType == symTable.detailType && !distinctErrorDef &&
                !this.env.enclPkg.packageID.equals(PackageID.ANNOTATIONS)) {
            resultType = bErrorType;
            return;
        }

        // Define user define error type.
        BErrorTypeSymbol errorTypeSymbol = Symbols
                .createErrorSymbol(Flags.asMask(errorTypeNode.flagSet), Names.EMPTY, bErrorType.tsymbol.pkgID, null,
                        bErrorType.tsymbol.owner, errorTypeNode.pos, SOURCE);

        PackageID packageID = env.enclPkg.packageID;
        if (env.node.getKind() != NodeKind.PACKAGE) {
            errorTypeSymbol.name = names.fromString(
                    anonymousModelHelper.getNextAnonymousTypeKey(packageID));
            symbolEnter.defineSymbol(errorTypeNode.pos, errorTypeSymbol, env);
        }

        BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;

        markParameterizedType(errorType, detailType);

        errorType.typeIdSet = BTypeIdSet.emptySet();

        if (errorTypeNode.isAnonymous && errorTypeNode.flagSet.contains(Flag.DISTINCT)) {
            errorType.typeIdSet.add(
                    BTypeIdSet.from(packageID, anonymousModelHelper.getNextAnonymousTypeId(packageID), true));
        }

        resultType = errorType;
    }

    public void visit(BLangConstrainedType constrainedTypeNode) {
        BType type = resolveTypeNode(constrainedTypeNode.type, env);
        BType constraintType = resolveTypeNode(constrainedTypeNode.constraint, env);
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            resultType = symTable.noType;
            return;
        }

        BType constrainedType = null;
        if (type.tag == TypeTags.FUTURE) {
            constrainedType = new BFutureType(TypeTags.FUTURE, constraintType, null);
        } else if (type.tag == TypeTags.MAP) {
            constrainedType = new BMapType(TypeTags.MAP, constraintType, null);
        } else if (type.tag == TypeTags.TYPEDESC) {
            constrainedType = new BTypedescType(constraintType, null);
        } else if (type.tag == TypeTags.XML) {
            if (constraintType.tag == TypeTags.PARAMETERIZED_TYPE) {
                BType typedescType = ((BParameterizedType) constraintType).paramSymbol.type;
                BType typedescConstraint = ((BTypedescType) typedescType).constraint;
                validateXMLConstraintType(typedescConstraint, constrainedTypeNode.pos);
            } else {
                validateXMLConstraintType(constraintType, constrainedTypeNode.pos);
            }
            constrainedType = new BXMLType(constraintType, null);
        } else {
            return;
        }

        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                                                           typeSymbol.originalName, typeSymbol.pkgID, constrainedType,
                                                           typeSymbol.owner, constrainedTypeNode.pos, SOURCE);
        markParameterizedType(constrainedType, constraintType);
        resultType = constrainedType;
    }

    private void validateXMLConstraintType(BType type, Location pos) {
        BType constraintType = types.getReferredType(type);
        int constrainedTag = constraintType.tag;

        if (constrainedTag == TypeTags.UNION) {
            checkUnionTypeForXMLSubTypes((BUnionType) constraintType, pos);
            return;
        }

        if (!TypeTags.isXMLTypeTag(constrainedTag) && constrainedTag != TypeTags.NEVER) {
            dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_CONSTRAINT, symTable.xmlType, constraintType);
        }
    }

    private void checkUnionTypeForXMLSubTypes(BUnionType constraintUnionType, Location pos) {
        for (BType memberType : constraintUnionType.getMemberTypes()) {
            memberType = types.getReferredType(memberType);
            if (memberType.tag == TypeTags.UNION) {
                checkUnionTypeForXMLSubTypes((BUnionType) memberType, pos);
            }
            if (!TypeTags.isXMLTypeTag(memberType.tag)) {
                dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_CONSTRAINT, symTable.xmlType,
                           constraintUnionType);
            }
        }
    }

    public void visit(BLangUserDefinedType userDefinedTypeNode) {
        // 1) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        //    if not use the current package scope.
        // 2) lookup the typename in the package scope returned from step 1.
        // 3) If the symbol is not found, then lookup in the root scope. e.g. for types such as 'error'

        Name pkgAlias = names.fromIdNode(userDefinedTypeNode.pkgAlias);
        Name typeName = names.fromIdNode(userDefinedTypeNode.typeName);
        BSymbol symbol = symTable.notFoundSymbol;

        // 1) Resolve ANNOTATION type if and only current scope inside ANNOTATION definition.
        // Only valued types and ANNOTATION type allowed.
        if (env.scope.owner.tag == SymTag.ANNOTATION) {
            symbol = lookupAnnotationSpaceSymbolInPackage(userDefinedTypeNode.pos, env, pkgAlias, typeName);
        }

        // 2) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        if (symbol == symTable.notFoundSymbol) {
            BSymbol tempSymbol = lookupMainSpaceSymbolInPackage(userDefinedTypeNode.pos, env, pkgAlias, typeName);

            BSymbol refSymbol = tempSymbol.tag == SymTag.TYPE_DEF ? tempSymbol.type.tsymbol : tempSymbol;
            if ((refSymbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                symbol = tempSymbol;
            } else if (Symbols.isTagOn(refSymbol, SymTag.VARIABLE) && env.node.getKind() == NodeKind.FUNCTION) {
                BLangFunction func = (BLangFunction) env.node;
                boolean errored = false;

                if (func.returnTypeNode == null ||
                        (func.hasBody() && func.body.getKind() != NodeKind.EXTERN_FUNCTION_BODY)) {
                    dlog.error(userDefinedTypeNode.pos,
                               DiagnosticErrorCode.INVALID_NON_EXTERNAL_DEPENDENTLY_TYPED_FUNCTION);
                    errored = true;
                }

                if (refSymbol.type != null &&
                        types.getReferredType(refSymbol.type).tag != TypeTags.TYPEDESC) {
                    dlog.error(userDefinedTypeNode.pos, DiagnosticErrorCode.INVALID_PARAM_TYPE_FOR_RETURN_TYPE,
                            tempSymbol.type);
                    errored = true;
                }

                if (errored) {
                    this.resultType = symTable.semanticError;
                    return;
                }

                ParameterizedTypeInfo parameterizedTypeInfo =
                        getTypedescParamValueType(func.requiredParams, refSymbol);
                BType paramValType = parameterizedTypeInfo == null ? null : parameterizedTypeInfo.paramValueType;

                if (paramValType == symTable.semanticError) {
                    this.resultType = symTable.semanticError;
                    return;
                }

                if (paramValType != null) {
                    BTypeSymbol tSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PARAMETERIZED | tempSymbol.flags,
                                                          tempSymbol.name, tempSymbol.originalName, tempSymbol.pkgID,
                                                          null, func.symbol, tempSymbol.pos, VIRTUAL);
                    tSymbol.type = new BParameterizedType(paramValType, (BVarSymbol) tempSymbol,
                                                          tSymbol, tempSymbol.name, parameterizedTypeInfo.index);
                    tSymbol.type.flags |= Flags.PARAMETERIZED;

                    this.resultType = tSymbol.type;
                    userDefinedTypeNode.symbol = tSymbol;
                    return;
                }
            }
        }

        if (symbol == symTable.notFoundSymbol) {
            // 3) Lookup the root scope for types such as 'error'
            symbol = lookupMemberSymbol(userDefinedTypeNode.pos, symTable.rootScope, this.env, typeName,
                                        SymTag.VARIABLE_NAME);
        }

        if (this.env.logErrors && symbol == symTable.notFoundSymbol) {
            if (!missingNodesHelper.isMissingNode(pkgAlias) && !missingNodesHelper.isMissingNode(typeName) &&
                    !symbolEnter.isUnknownTypeRef(userDefinedTypeNode)) {
                dlog.error(userDefinedTypeNode.pos, diagCode, typeName);
            }
            resultType = symTable.semanticError;
            return;
        }

        userDefinedTypeNode.symbol = symbol;

        if (symbol.kind == SymbolKind.TYPE_DEF && !Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
            if (((BTypeDefinitionSymbol) symbol).referenceType == null) {
                BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_DEF, symbol.flags, symbol.name, symbol.pkgID,
                        symbol.type, symbol.owner, symbol.pos, symbol.origin);
                typeSymbol.markdownDocumentation = symbol.markdownDocumentation;
                BTypeReferenceType refType = new BTypeReferenceType(symbol.type, typeSymbol, symbol.type.flags);
                ((BTypeDefinitionSymbol) symbol).referenceType = refType;
                resultType = refType;
            } else {
                resultType = ((BTypeDefinitionSymbol) symbol).referenceType;
            }
        } else {
            resultType = symbol.type;
        }
    }

    private ParameterizedTypeInfo getTypedescParamValueType(List<BLangSimpleVariable> params, BSymbol varSym) {
        for (int i = 0; i < params.size(); i++) {
            BLangSimpleVariable param = params.get(i);

            if (param.name.value.equals(varSym.name.value)) {
                if (param.expr == null || param.expr.getKind() == NodeKind.INFER_TYPEDESC_EXPR) {
                    return new ParameterizedTypeInfo(
                            ((BTypedescType) types.getReferredType(varSym.type)).constraint, i);
                }

                NodeKind defaultValueExprKind = param.expr.getKind();

                if (defaultValueExprKind == NodeKind.TYPEDESC_EXPRESSION) {
                    return new ParameterizedTypeInfo(
                            resolveTypeNode(((BLangTypedescExpr) param.expr).typeNode, this.env), i);
                }

                if (defaultValueExprKind == NodeKind.SIMPLE_VARIABLE_REF) {
                    Name varName = names.fromIdNode(((BLangSimpleVarRef) param.expr).variableName);
                    BSymbol typeRefSym = lookupSymbolInMainSpace(this.env, varName);
                    if (typeRefSym != symTable.notFoundSymbol) {
                        return new ParameterizedTypeInfo(typeRefSym.type, i);
                    }
                    return new ParameterizedTypeInfo(symTable.semanticError);
                }

                dlog.error(param.pos, DiagnosticErrorCode.INVALID_TYPEDESC_PARAM);
                return new ParameterizedTypeInfo(symTable.semanticError);
            }
        }

        return null;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        List<BLangVariable> params = functionTypeNode.getParams();
        Location pos = functionTypeNode.pos;
        BLangType returnTypeNode = functionTypeNode.returnTypeNode;
        BType invokableType = createInvokableType(params, functionTypeNode.restParam, returnTypeNode,
                                                  Flags.asMask(functionTypeNode.flagSet), env, pos);
        resultType = validateInferTypedescParams(pos, params, returnTypeNode == null ?
                null : returnTypeNode.getBType()) ? invokableType : symTable.semanticError;
    }

    public BType createInvokableType(List<? extends BLangVariable> paramVars,
                                     BLangVariable restVariable,
                                     BLangType retTypeVar,
                                     long flags,
                                     SymbolEnv env,
                                     Location location) {
        List<BType> paramTypes = new ArrayList<>();
        List<BVarSymbol> params = new ArrayList<>();

        boolean foundDefaultableParam = false;
        List<String> paramNames = new ArrayList<>();
        if (Symbols.isFlagOn(flags, Flags.ANY_FUNCTION)) {
            BInvokableType bInvokableType = new BInvokableType(null, null, null, null);
            bInvokableType.flags = flags;
            BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE, flags,
                                                                             env.enclPkg.symbol.pkgID, bInvokableType,
                                                                             env.scope.owner, location, SOURCE);
            tsymbol.params = null;
            tsymbol.restParam = null;
            tsymbol.returnType = null;
            bInvokableType.tsymbol = tsymbol;
            return bInvokableType;
        }

        for (BLangVariable paramNode : paramVars) {
            BLangSimpleVariable param = (BLangSimpleVariable) paramNode;
            Name paramName = names.fromIdNode(param.name);
            Name paramOrigName = names.originalNameFromIdNode(param.name);
            if (paramName != Names.EMPTY) {
                if (paramNames.contains(paramName.value)) {
                    dlog.error(param.name.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, paramName.value);
                } else {
                    paramNames.add(paramName.value);
                }
            }
            BType type = resolveTypeNode(param.getTypeNode(), env);
            if (type == symTable.noType) {
                return symTable.noType;
            }
            paramNode.setBType(type);
            paramTypes.add(type);

            long paramFlags = Flags.asMask(paramNode.flagSet);
            BVarSymbol symbol = new BVarSymbol(paramFlags, paramName, paramOrigName, env.enclPkg.symbol.pkgID,
                                               type, env.scope.owner, param.pos, SOURCE);
            param.symbol = symbol;

            if (param.expr != null) {
                foundDefaultableParam = true;
                symbol.isDefaultable = true;
                symbol.flags |= Flags.OPTIONAL;
            } else if (foundDefaultableParam) {
                dlog.error(param.pos, DiagnosticErrorCode.REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM);
            }

            params.add(symbol);
        }

        BType retType = resolveTypeNode(retTypeVar, env);
        if (retType == symTable.noType) {
            return symTable.noType;
        }

        BVarSymbol restParam = null;
        BType restType = null;

        if (restVariable != null) {
            restType = resolveTypeNode(restVariable.typeNode, env);
            if (restType == symTable.noType) {
                return symTable.noType;
            }
            BLangIdentifier id = ((BLangSimpleVariable) restVariable).name;
            restVariable.setBType(restType);
            restParam = new BVarSymbol(Flags.asMask(restVariable.flagSet),
                                       names.fromIdNode(id), names.originalNameFromIdNode(id),
                                       env.enclPkg.symbol.pkgID, restType, env.scope.owner, restVariable.pos, SOURCE);
        }

        BInvokableType bInvokableType = new BInvokableType(paramTypes, restType, retType, null);
        bInvokableType.flags = flags;
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE, flags,
                                                                         env.enclPkg.symbol.pkgID, bInvokableType,
                                                                         env.scope.owner, location, SOURCE);

        tsymbol.params = params;
        tsymbol.restParam = restParam;
        tsymbol.returnType = retType;
        bInvokableType.tsymbol = tsymbol;

        List<BType> allConstituentTypes = new ArrayList<>(paramTypes);
        allConstituentTypes.add(restType);
        allConstituentTypes.add(retType);
        markParameterizedType(bInvokableType, allConstituentTypes);

        return bInvokableType;
    }
    /**
     * Lookup all the visible in-scope symbols for a given environment scope.
     *
     * @param env Symbol environment
     * @return all the visible symbols
     */
    public Map<Name, List<ScopeEntry>> getAllVisibleInScopeSymbols(SymbolEnv env) {
        Map<Name, List<ScopeEntry>> visibleEntries = new HashMap<>();
        env.scope.entries.forEach((key, value) -> {
            ArrayList<ScopeEntry> entryList = new ArrayList<>();
            entryList.add(value);
            visibleEntries.put(key, entryList);
        });
        if (env.enclEnv != null) {
            getAllVisibleInScopeSymbols(env.enclEnv).forEach((name, entryList) -> {
                if (!visibleEntries.containsKey(name)) {
                    visibleEntries.put(name, entryList);
                } else {
                    List<ScopeEntry> scopeEntries = visibleEntries.get(name);
                    entryList.forEach(scopeEntry -> {
                        if (!scopeEntries.contains(scopeEntry) && !isModuleLevelVar(scopeEntry.symbol)) {
                            scopeEntries.add(scopeEntry);
                        }
                    });
                }
            });
        }
        return visibleEntries;
    }

    public BSymbol getBinaryEqualityForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType,
                                                BLangBinaryExpr binaryExpr, SymbolEnv env) {
        boolean validEqualityIntersectionExists;
        switch (opKind) {
            case EQUAL:
            case NOT_EQUAL:
                validEqualityIntersectionExists = types.validEqualityIntersectionExists(lhsType, rhsType);
                break;
            case REF_EQUAL:
            case REF_NOT_EQUAL:
                validEqualityIntersectionExists =
                        types.getTypeIntersection(Types.IntersectionContext.compilerInternalIntersectionTestContext(),
                                lhsType, rhsType, env) != symTable.semanticError;
                break;
            default:
                return symTable.notFoundSymbol;
        }

        if (validEqualityIntersectionExists) {
            if ((!types.isValueType(lhsType) && !types.isValueType(rhsType)) ||
                    (types.isValueType(lhsType) && types.isValueType(rhsType))) {
                return createEqualityOperator(opKind, lhsType, rhsType);
            } else {
                types.setImplicitCastExpr(binaryExpr.rhsExpr, rhsType, symTable.anyType);
                types.setImplicitCastExpr(binaryExpr.lhsExpr, lhsType, symTable.anyType);

                switch (opKind) {
                    case REF_EQUAL:
                        // if one is a value type, consider === the same as ==
                        return createEqualityOperator(OperatorKind.EQUAL, symTable.anyType,
                                symTable.anyType);
                    case REF_NOT_EQUAL:
                        // if one is a value type, consider !== the same as !=
                        return createEqualityOperator(OperatorKind.NOT_EQUAL, symTable.anyType,
                                                      symTable.anyType);
                    default:
                        return createEqualityOperator(opKind, symTable.anyType, symTable.anyType);
                }
            }
        }
        return symTable.notFoundSymbol;
    }

    public BSymbol getBitwiseShiftOpsForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType) {
        boolean validIntTypesExists;
        switch (opKind) {
            case BITWISE_LEFT_SHIFT:
            case BITWISE_RIGHT_SHIFT:
            case BITWISE_UNSIGNED_RIGHT_SHIFT:
                validIntTypesExists = types.validIntegerTypeExists(lhsType) && types.validIntegerTypeExists(rhsType);
                break;
            default:
                return symTable.notFoundSymbol;
        }

        if (validIntTypesExists) {
            switch (opKind) {
                case BITWISE_LEFT_SHIFT:
                    return createBinaryOperator(opKind, lhsType, rhsType, symTable.intType);
                case BITWISE_RIGHT_SHIFT:
                case BITWISE_UNSIGNED_RIGHT_SHIFT:
                    switch (lhsType.tag) {
                        case TypeTags.UNSIGNED32_INT:
                        case TypeTags.UNSIGNED16_INT:
                        case TypeTags.UNSIGNED8_INT:
                        case TypeTags.BYTE:
                            return createBinaryOperator(opKind, lhsType, rhsType, lhsType);
                        case TypeTags.TYPEREFDESC:
                            return getBitwiseShiftOpsForTypeSets(opKind,
                                    types.getReferredType(lhsType), rhsType);
                        default:
                            return createBinaryOperator(opKind, lhsType, rhsType, symTable.intType);
                    }
            }
        }
        return symTable.notFoundSymbol;
    }

    public BSymbol getArithmeticOpsForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType) {
        boolean validNumericOrStringTypeExists;
        switch (opKind) {
            case ADD:
                validNumericOrStringTypeExists = (types.validNumericTypeExists(lhsType) &&
                        types.validNumericTypeExists(rhsType)) || (types.validStringOrXmlTypeExists(lhsType) &&
                        types.validStringOrXmlTypeExists(rhsType));
                break;
            case SUB:
            case DIV:
            case MUL:
            case MOD:
                validNumericOrStringTypeExists = types.validNumericTypeExists(lhsType) &&
                        types.validNumericTypeExists(rhsType);
                break;
            default:
                return symTable.notFoundSymbol;
        }

        if (validNumericOrStringTypeExists) {
            BType compatibleType1 = types.findCompatibleType(lhsType);
            BType compatibleType2 = types.findCompatibleType(rhsType);
            if (types.isBasicNumericType(compatibleType1) && compatibleType1 != compatibleType2) {
                return symTable.notFoundSymbol;
            }
            if (compatibleType1.tag < compatibleType2.tag) {
                return createBinaryOperator(opKind, lhsType, rhsType, compatibleType2);
            }
            return createBinaryOperator(opKind, lhsType, rhsType, compatibleType1);
        }
        return symTable.notFoundSymbol;
    }

    public BSymbol getBinaryBitwiseOpsForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType) {
        boolean validIntTypesExists;
        switch (opKind) {
            case BITWISE_AND:
            case BITWISE_OR:
            case BITWISE_XOR:
                validIntTypesExists = types.validIntegerTypeExists(lhsType) && types.validIntegerTypeExists(rhsType);
                break;
            default:
                return symTable.notFoundSymbol;
        }

        if (validIntTypesExists) {
            switch (opKind) {
                case BITWISE_AND:
                    switch (lhsType.tag) {
                        case TypeTags.UNSIGNED8_INT:
                        case TypeTags.BYTE:
                        case TypeTags.UNSIGNED16_INT:
                        case TypeTags.UNSIGNED32_INT:
                            return createBinaryOperator(opKind, lhsType, rhsType, lhsType);
                        case TypeTags.TYPEREFDESC:
                            return getBinaryBitwiseOpsForTypeSets(opKind,
                                    types.getReferredType(lhsType), rhsType);
                    }
                    switch (rhsType.tag) {
                        case TypeTags.UNSIGNED8_INT:
                        case TypeTags.BYTE:
                        case TypeTags.UNSIGNED16_INT:
                        case TypeTags.UNSIGNED32_INT:
                            return createBinaryOperator(opKind, lhsType, rhsType, rhsType);
                        case TypeTags.TYPEREFDESC:
                            return getBinaryBitwiseOpsForTypeSets(opKind, lhsType,
                                    types.getReferredType(rhsType));
                    }
                    return createBinaryOperator(opKind, lhsType, rhsType, symTable.intType);
                case BITWISE_OR:
                case BITWISE_XOR:
                    return createBinaryOperator(opKind, lhsType, rhsType, symTable.intType);
            }
        }
        return symTable.notFoundSymbol;
    }

    /**
     * Define binary comparison operator for valid ordered types.
     *
     * @param opKind Binary operator kind
     * @param lhsType Type of the left hand side value
     * @param rhsType Type of the right hand side value
     * @return <, <=, >, or >= symbol
     */
    public BSymbol getBinaryComparisonOpForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType) {
        boolean validOrderedTypesExist;
        switch (opKind) {
            case LESS_THAN:
            case LESS_EQUAL:
            case GREATER_THAN:
            case GREATER_EQUAL:
                validOrderedTypesExist = types.isOrderedType(lhsType, false) &&
                        types.isOrderedType(rhsType, false) && types.isSameOrderedType(lhsType, rhsType);
                break;
            default:
                return symTable.notFoundSymbol;
        }

        if (validOrderedTypesExist) {
            switch (opKind) {
                case LESS_THAN:
                    return createBinaryComparisonOperator(OperatorKind.LESS_THAN, lhsType, rhsType);
                case LESS_EQUAL:
                    return createBinaryComparisonOperator(OperatorKind.LESS_EQUAL, lhsType, rhsType);
                case GREATER_THAN:
                    return createBinaryComparisonOperator(OperatorKind.GREATER_THAN, lhsType, rhsType);
                default:
                    return createBinaryComparisonOperator(OperatorKind.GREATER_EQUAL, lhsType, rhsType);
            }
        }
        return symTable.notFoundSymbol;
    }

    public boolean isBinaryShiftOperator(OperatorKind binaryOpKind) {
        return binaryOpKind == OperatorKind.BITWISE_LEFT_SHIFT ||
                binaryOpKind == OperatorKind.BITWISE_RIGHT_SHIFT ||
                binaryOpKind == OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT;
    }

    public boolean isArithmeticOperator(OperatorKind binaryOpKind) {
        return binaryOpKind == OperatorKind.ADD || binaryOpKind == OperatorKind.SUB ||
                binaryOpKind == OperatorKind.DIV || binaryOpKind == OperatorKind.MUL ||
                binaryOpKind == OperatorKind.MOD;
    }

    public boolean isBinaryComparisonOperator(OperatorKind binaryOpKind) {
        return binaryOpKind == OperatorKind.LESS_THAN ||
                binaryOpKind == OperatorKind.LESS_EQUAL || binaryOpKind == OperatorKind.GREATER_THAN ||
                binaryOpKind == OperatorKind.GREATER_EQUAL;
    }

    public boolean markParameterizedType(BType type, BType constituentType) {
        if (Symbols.isFlagOn(constituentType.flags, Flags.PARAMETERIZED)) {
            type.tsymbol.flags |= Flags.PARAMETERIZED;
            type.flags |= Flags.PARAMETERIZED;
            return true;
        }
        return false;
    }

    public void markParameterizedType(BType enclosingType, Collection<BType> constituentTypes) {
        if (Symbols.isFlagOn(enclosingType.flags, Flags.PARAMETERIZED)) {
            return;
        }

        for (BType type : constituentTypes) {
            if (type == null) {
                // This is to avoid having to have this null check in each caller site, where some constituent types
                // are expected to be null at times. e.g., rest param
                continue;
            }
            if (markParameterizedType(enclosingType, type)) {
                break;
            }
        }
    }

    // private methods

    private BSymbol resolveOperator(ScopeEntry entry, List<BType> types) {
        BSymbol foundSymbol = symTable.notFoundSymbol;
        while (entry != NOT_FOUND_ENTRY) {
            BInvokableType opType = (BInvokableType) entry.symbol.type;
            if (types.size() == opType.paramTypes.size()) {
                boolean match = true;
                for (int i = 0; i < types.size(); i++) {
                    BType t = this.types.getReferredType(types.get(i));
                    if (t.tag != opType.paramTypes.get(i).tag) {
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

    private void visitBuiltInTypeNode(BLangType typeNode, TypeKind typeKind, SymbolEnv env) {
        Name typeName = names.fromTypeKind(typeKind);
        BSymbol typeSymbol = lookupMemberSymbol(typeNode.pos, symTable.rootScope,
                                                env, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            dlog.error(typeNode.pos, diagCode, typeName);
        }

        typeNode.setBType(typeSymbol.type);
        resultType = typeSymbol.type;
    }

    private void addNamespacesInScope(Map<Name, BXMLNSSymbol> namespaces, SymbolEnv env) {
        if (env == null) {
            return;
        }
        env.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol.kind == SymbolKind.XMLNS) {
                BXMLNSSymbol nsSymbol = (BXMLNSSymbol) scopeEntry.symbol;
                // Skip if the namespace is already added, by a child scope. That means it has been overridden.
                if (!namespaces.containsKey(name)) {
                    namespaces.put(name, nsSymbol);
                }
            }
        });
        addNamespacesInScope(namespaces, env.enclEnv);
    }

    private boolean isMemberAccessAllowed(SymbolEnv env, BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return true;
        }
        if (!Symbols.isPrivate(symbol)) {
            return env.enclPkg.symbol.pkgID == symbol.pkgID;
        }
        if (env.enclType != null) {
            return env.enclType.getBType().tsymbol == symbol.owner;
        }
        return isMemberAllowed(env, symbol);
    }

    private boolean isMemberAllowed(SymbolEnv env, BSymbol symbol) {
        return env != null && (env.enclInvokable != null
                && env.enclInvokable.symbol.receiverSymbol != null
                && env.enclInvokable.symbol.receiverSymbol.type.tsymbol == symbol.owner
                || isMemberAllowed(env.enclEnv, symbol));
    }

    private BType computeIntersectionType(BLangIntersectionTypeNode intersectionTypeNode) {
        List<BLangType> constituentTypeNodes = intersectionTypeNode.constituentTypeNodes;
        Map<BType, BLangType> typeBLangTypeMap = new HashMap<>();

        boolean validIntersection = true;
        boolean isErrorIntersection = false;
        boolean isAlreadyExistingType = false;

        BLangType bLangTypeOne = constituentTypeNodes.get(0);
        BType typeOne = resolveTypeNode(bLangTypeOne, env);
        if (typeOne == symTable.noType) {
            return symTable.noType;
        }
        typeOne = types.getReferredType(typeOne);

        typeBLangTypeMap.put(typeOne, bLangTypeOne);

        BLangType bLangTypeTwo = constituentTypeNodes.get(1);
        BType typeTwo = resolveTypeNode(bLangTypeTwo, env);
        if (typeTwo == symTable.noType) {
            return symTable.noType;
        }
        typeTwo = types.getReferredType(typeTwo);

        typeBLangTypeMap.put(typeTwo, bLangTypeTwo);

        boolean hasReadOnlyType = typeOne == symTable.readonlyType || typeTwo == symTable.readonlyType;

        if (typeOne.tag == TypeTags.ERROR || typeTwo.tag == TypeTags.ERROR) {
            isErrorIntersection = true;
        }

        if (!(hasReadOnlyType || isErrorIntersection)) {
            dlog.error(intersectionTypeNode.pos,
                    DiagnosticErrorCode.UNSUPPORTED_TYPE_INTERSECTION, intersectionTypeNode);
            return symTable.semanticError;
        }

        BType potentialIntersectionType = getPotentialIntersection(
                Types.IntersectionContext.from(dlog, bLangTypeOne.pos, bLangTypeTwo.pos),
                typeOne, typeTwo, this.env);
        if (typeOne == potentialIntersectionType || typeTwo == potentialIntersectionType) {
            isAlreadyExistingType = true;
        }

        LinkedHashSet<BType> constituentBTypes = new LinkedHashSet<>();
        constituentBTypes.add(typeOne);
        constituentBTypes.add(typeTwo);

        if (potentialIntersectionType == symTable.semanticError) {
            validIntersection = false;
        } else {
            for (int i = 2; i < constituentTypeNodes.size(); i++) {
                BLangType bLangType = constituentTypeNodes.get(i);
                BType type = resolveTypeNode(bLangType, env);
                if (type.tag == TypeTags.ERROR) {
                    isErrorIntersection = true;
                }
                typeBLangTypeMap.put(type, bLangType);

                if (!hasReadOnlyType) {
                    hasReadOnlyType = type == symTable.readonlyType;
                }

                if (type == symTable.noType) {
                    return symTable.noType;
                }

                BType tempIntersectionType = getPotentialIntersection(
                        Types.IntersectionContext.from(dlog, bLangTypeOne.pos, bLangTypeTwo.pos),
                        potentialIntersectionType, type, this.env);
                if (tempIntersectionType == symTable.semanticError) {
                    validIntersection = false;
                    break;
                }

                if (type == tempIntersectionType) {
                    potentialIntersectionType = type;
                    isAlreadyExistingType = true;
                } else if (potentialIntersectionType != tempIntersectionType) {
                    potentialIntersectionType = tempIntersectionType;
                    isAlreadyExistingType = false;
                }
                constituentBTypes.add(type);
            }
        }

        if (!validIntersection) {
            dlog.error(intersectionTypeNode.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, intersectionTypeNode);
            return symTable.semanticError;
        }

        if (isErrorIntersection && !isAlreadyExistingType) {
            BType detailType = ((BErrorType) potentialIntersectionType).detailType;

            boolean existingErrorDetailType = false;
            if (detailType.tsymbol != null) {
                BSymbol detailTypeSymbol = lookupSymbolInMainSpace(env, detailType.tsymbol.name);
                if (detailTypeSymbol != symTable.notFoundSymbol) {
                    existingErrorDetailType = true;
                }
            }

            return createIntersectionErrorType((BErrorType) potentialIntersectionType, intersectionTypeNode.pos,
                                          constituentBTypes, existingErrorDetailType, env);
        }

        if (types.isInherentlyImmutableType(potentialIntersectionType) ||
                (Symbols.isFlagOn(potentialIntersectionType.flags, Flags.READONLY) &&
                        // For objects, a new type has to be created.
                        !types.isSubTypeOfBaseType(potentialIntersectionType, TypeTags.OBJECT))) {
            return potentialIntersectionType;
        }

        if (!types.isSelectivelyImmutableType(potentialIntersectionType, false)) {
            if (types.isSelectivelyImmutableType(potentialIntersectionType)) {
                // This intersection would have been valid if not for `readonly object`s.
                dlog.error(intersectionTypeNode.pos, DiagnosticErrorCode.INVALID_READONLY_OBJECT_INTERSECTION_TYPE);
            } else {
                dlog.error(intersectionTypeNode.pos, DiagnosticErrorCode.INVALID_READONLY_INTERSECTION_TYPE,
                           potentialIntersectionType);
            }
            return symTable.semanticError;
        }

        BLangType typeNode = typeBLangTypeMap.get(potentialIntersectionType);
        Set<Flag> flagSet;
        if (typeNode == null) {
            flagSet = new HashSet<>();
        } else if (typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            flagSet = ((BLangObjectTypeNode) typeNode).flagSet;
        } else if (typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
            flagSet = ((BLangUserDefinedType) typeNode).flagSet;
        } else {
            flagSet = new HashSet<>();
        }
        return ImmutableTypeCloner.getImmutableIntersectionType(intersectionTypeNode.pos, types,
                                                                (SelectivelyImmutableReferenceType)
                                                                        potentialIntersectionType,
                                                                env, symTable, anonymousModelHelper, names, flagSet);
    }

    private BIntersectionType createIntersectionErrorType(BErrorType intersectionErrorType,
                                                          Location pos,
                                                          LinkedHashSet<BType> constituentBTypes,
                                                          boolean isAlreadyDefinedDetailType, SymbolEnv env) {

        BSymbol owner = intersectionErrorType.tsymbol.owner;
        PackageID pkgId = intersectionErrorType.tsymbol.pkgID;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(env.enclPkg.symbol);

        if (!isAlreadyDefinedDetailType && intersectionErrorType.detailType.tag == TypeTags.RECORD) {
            defineErrorDetailRecord((BRecordType) intersectionErrorType.detailType, pos, pkgEnv);
        }
        return createIntersectionErrorType(intersectionErrorType, constituentBTypes, pkgId, owner, pos);
    }

    private void defineErrorDetailRecord(BRecordType detailRecord, Location pos, SymbolEnv env) {
        BRecordTypeSymbol detailRecordSymbol = (BRecordTypeSymbol) detailRecord.tsymbol;

        for (BField field : detailRecord.fields.values()) {
            BVarSymbol fieldSymbol = field.symbol;
            detailRecordSymbol.scope.define(fieldSymbol.name, fieldSymbol);
        }

        BLangRecordTypeNode detailRecordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(new ArrayList<>(),
                                                                                             detailRecord, pos);
        TypeDefBuilderHelper.createInitFunctionForRecordType(detailRecordTypeNode, env, names, symTable);
        BLangTypeDefinition detailRecordTypeDefinition = TypeDefBuilderHelper.addTypeDefinition(detailRecord,
                                                                                                detailRecordSymbol,
                                                                                                detailRecordTypeNode,
                                                                                                env);
        detailRecordTypeDefinition.pos = pos;
    }

    private BIntersectionType createIntersectionErrorType(IntersectableReferenceType effectiveType,
                                                          LinkedHashSet<BType> constituentBTypes, PackageID pkgId,
                                                          BSymbol owner, Location pos) {

        BTypeSymbol intersectionTypeSymbol =
                Symbols.createTypeSymbol(SymTag.INTERSECTION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)), Names.EMPTY,
                        pkgId, null, owner, pos, VIRTUAL);

        BIntersectionType intersectionType =
                new BIntersectionType(intersectionTypeSymbol, constituentBTypes, effectiveType);
        intersectionTypeSymbol.type = intersectionType;
        return intersectionType;
    }

    private BType getPotentialIntersection(Types.IntersectionContext intersectionContext,
                                           BType lhsType, BType rhsType, SymbolEnv env) {
        if (lhsType == symTable.readonlyType) {
            return rhsType;
        }

        if (rhsType == symTable.readonlyType) {
            return lhsType;
        }

        return types.getTypeIntersection(intersectionContext, lhsType, rhsType, env);
    }

    boolean validateInferTypedescParams(Location pos, List<? extends BLangVariable> parameters, BType retType) {
        int inferTypedescParamCount = 0;
        BVarSymbol paramWithInferredTypedescDefault = null;
        Location inferDefaultLocation = null;

        for (BLangVariable parameter : parameters) {
            BType type = parameter.getBType();
            BLangExpression expr = parameter.expr;
            if (type != null && type.tag == TypeTags.TYPEDESC && expr != null &&
                    expr.getKind() == NodeKind.INFER_TYPEDESC_EXPR) {
                paramWithInferredTypedescDefault = parameter.symbol;
                inferDefaultLocation = expr.pos;
                inferTypedescParamCount++;
            }
        }

        if (inferTypedescParamCount > 1) {
            dlog.error(pos, DiagnosticErrorCode.MULTIPLE_INFER_TYPEDESC_PARAMS);
            return false;
        }

        if (paramWithInferredTypedescDefault == null) {
            return true;
        }

        if (retType == null) {
            dlog.error(inferDefaultLocation,
                       DiagnosticErrorCode.CANNOT_USE_INFERRED_TYPEDESC_DEFAULT_WITH_UNREFERENCED_PARAM);
            return false;
        }

        if (unifier.refersInferableParamName(paramWithInferredTypedescDefault.name.value, retType)) {
            return true;
        }

        dlog.error(inferDefaultLocation,
                   DiagnosticErrorCode.CANNOT_USE_INFERRED_TYPEDESC_DEFAULT_WITH_UNREFERENCED_PARAM);
        return false;
    }

    private boolean isModuleLevelVar(BSymbol symbol) {
        return symbol.getKind() == SymbolKind.VARIABLE && symbol.owner.getKind() == SymbolKind.PACKAGE;
    }

    public Set<BVarSymbol> getConfigVarSymbolsIncludingImportedModules(BPackageSymbol packageSymbol) {
        Set<BVarSymbol> configVars = new HashSet<>();
        populateConfigurableVars(packageSymbol, configVars);
        if (!packageSymbol.imports.isEmpty()) {
            for (BPackageSymbol importSymbol : packageSymbol.imports) {
                populateConfigurableVars(importSymbol, configVars);
            }
        }
        return configVars;
    }

    private void populateConfigurableVars(BPackageSymbol pkgSymbol, Set<BVarSymbol> configVars) {
        for (Scope.ScopeEntry entry : pkgSymbol.scope.entries.values()) {
            BSymbol symbol = entry.symbol.tag == SymTag.TYPE_DEF ? entry.symbol.type.tsymbol : entry.symbol;
            int symbolTag = symbol.tag == SymTag.TYPE_DEF ? symbol.type.tsymbol.tag : symbol.tag;
            if (symbol != null && symbolTag == SymTag.VARIABLE && Symbols.isFlagOn(symbol.flags, Flags.CONFIGURABLE)) {
                configVars.add((BVarSymbol) symbol);
            }
        }
    }

    private static class ParameterizedTypeInfo {
        BType paramValueType;
        int index = -1;

        private ParameterizedTypeInfo(BType paramValueType) {
            this.paramValueType = paramValueType;
        }

        private ParameterizedTypeInfo(BType paramValueType, int index) {
            this.paramValueType = paramValueType;
            this.index = index;
        }
    }
}
