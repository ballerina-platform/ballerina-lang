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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
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
import org.wso2.ballerinalang.compiler.util.FunctionalConstructorBuilder;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_SEALED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.UNSEALED_ARRAY_INDICATOR;

/**
 * @since 0.94
 */
public class SymbolResolver extends BLangNodeVisitor {
    private static final CompilerContext.Key<SymbolResolver> SYMBOL_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Names names;
    private BLangDiagnosticLogHelper dlog;
    private Types types;

    private SymbolEnv env;
    private BType resultType;
    private DiagnosticCode diagCode;
    private SymbolEnter symbolEnter;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private ResolvedTypeBuilder typeBuilder;

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
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.types = Types.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.typeBuilder = new ResolvedTypeBuilder();
    }

    public boolean checkForUniqueSymbol(DiagnosticPos pos, SymbolEnv env, BSymbol symbol) {
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

        //if symbol is not found then it is unique for the current scope
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }

        if (isRedeclaredSymbol(symbol, foundSym)) {
            dlog.error(pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
            return false;
        }

        if ((foundSym.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            // In order to remove duplicate errors.
            return false;
        }

        // if a symbol is found, then check whether it is unique
        return isDistinctSymbol(pos, symbol, foundSym);
    }

    private boolean isRedeclaredSymbol(BSymbol symbol, BSymbol foundSym) {
        return hasSameOwner(symbol, foundSym) || isSymbolRedeclaredInTestPackage(symbol, foundSym);
    }

    public boolean checkForUniqueSymbol(SymbolEnv env, BSymbol symbol) {
        BSymbol foundSym = lookupSymbolInMainSpace(env, symbol.name);
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }
        return isDistinctSymbol(symbol, foundSym);
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
    public boolean checkForUniqueSymbolInCurrentScope(DiagnosticPos pos, SymbolEnv env, BSymbol symbol,
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
    private boolean isDistinctSymbol(DiagnosticPos pos, BSymbol symbol, BSymbol foundSym) {
        // It is allowed to have a error constructor symbol with the same name as a type def.
        if (symbol.tag == SymTag.CONSTRUCTOR && foundSym.tag == SymTag.ERROR) {
            return false;
        }

        if (isSymbolDefinedInRootPkgLvl(foundSym)) {
            dlog.error(pos, DiagnosticCode.REDECLARED_BUILTIN_SYMBOL, symbol.name);
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

    private boolean isSymbolRedeclaredInTestPackage(BSymbol symbol, BSymbol foundSym) {
        if (Symbols.isFlagOn(symbol.owner.flags, Flags.TESTABLE) &&
                !Symbols.isFlagOn(foundSym.owner.flags, Flags.TESTABLE)) {
            return true;
        }
        return false;
    }

    private boolean isSymbolDefinedInRootPkgLvl(BSymbol foundSym) {
        return symTable.rootPkgSymbol.pkgID.equals(foundSym.pkgID) &&
                (foundSym.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME;
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

    public boolean checkForUniqueMemberSymbol(DiagnosticPos pos, SymbolEnv env, BSymbol symbol) {
        BSymbol foundSym = lookupMemberSymbol(pos, env.scope, env, symbol.name, symbol.tag);
        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
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
        return new BOperatorSymbol(names.fromString(opKind.value()), null, opType, null, symTable.builtinPos);
    }

    public BSymbol resolveUnaryOperator(DiagnosticPos pos,
                                        OperatorKind opKind,
                                        BType type) {
        return resolveOperator(names.fromString(opKind.value()), Lists.of(type));
    }

    public BSymbol resolveOperator(Name name, List<BType> types) {
        ScopeEntry entry = symTable.rootScope.lookup(name);
        return resolveOperator(entry, types);
    }

    public BSymbol resolvePkgSymbol(DiagnosticPos pos, SymbolEnv env, Name pkgAlias) {
        if (pkgAlias == Names.EMPTY) {
            // Return the current package symbol
            return env.enclPkg.symbol;
        }

        // Lookup for an imported package
        BSymbol pkgSymbol = lookupSymbolInPrefixSpace(env, pkgAlias);
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
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

            if ((entry.symbol.tag & SymTag.IMPORT) == SymTag.IMPORT) {
                Name importCompUnit = ((BPackageSymbol) entry.symbol).compUnit;
                //importCompUnit is null for predeclared modules
                if (importCompUnit == null) {
                    return entry.symbol;
                } else if (importCompUnit.equals(compUnit)) {
                    ((BPackageSymbol) entry.symbol).isUsed = true;
                    return entry.symbol;
                }
            }
            
            entry = entry.next;
        }

        if (env.enclEnv != null) {
            return resolvePrefixSymbol(env.enclEnv, pkgAlias, compUnit);
        }

        return symTable.notFoundSymbol;
    }

    public BSymbol resolveAnnotation(DiagnosticPos pos, SymbolEnv env, Name pkgAlias, Name annotationName) {
        return this.lookupAnnotationSpaceSymbolInPackage(pos, env, pkgAlias, annotationName);
    }

    public BSymbol resolveStructField(DiagnosticPos pos, SymbolEnv env, Name fieldName, BTypeSymbol structSymbol) {
        return lookupMemberSymbol(pos, structSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BSymbol resolveObjectField(DiagnosticPos pos, SymbolEnv env, Name fieldName, BTypeSymbol objectSymbol) {
        return lookupMemberSymbol(pos, objectSymbol.scope, env, fieldName, SymTag.VARIABLE);
    }

    public BSymbol resolveObjectMethod(DiagnosticPos pos, SymbolEnv env, Name fieldName,
                                       BObjectTypeSymbol objectSymbol) {
        return lookupMemberSymbol(pos, objectSymbol.methodScope, env, fieldName, SymTag.VARIABLE);
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

        // If the typeNode.nullable is true then convert the resultType to a union type
        // if it is not already a union type, JSON type, or any type
        if (typeNode.nullable && this.resultType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) this.resultType;
            unionType.add(symTable.nilType);
        } else if (typeNode.nullable && resultType.tag != TypeTags.JSON && resultType.tag != TypeTags.ANY) {
            this.resultType = BUnionType.create(null, resultType, symTable.nilType);
        }

        typeNode.type = resultType;
        return resultType;
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
                bSymbol = lookupLangLibMethodInModule(symTable.langArrayModuleSymbol, name);
                break;
            case TypeTags.DECIMAL:
                bSymbol = lookupLangLibMethodInModule(symTable.langDecimalModuleSymbol, name);
                break;
            case TypeTags.ERROR:
                bSymbol = lookupLangLibMethodInModule(symTable.langErrorModuleSymbol, name);
                break;
            case TypeTags.FLOAT:
                bSymbol = lookupLangLibMethodInModule(symTable.langFloatModuleSymbol, name);
                break;
            case TypeTags.FUTURE:
                bSymbol = lookupLangLibMethodInModule(symTable.langFutureModuleSymbol, name);
                break;
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                bSymbol = lookupLangLibMethodInModule(symTable.langIntModuleSymbol, name);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                bSymbol = lookupLangLibMethodInModule(symTable.langMapModuleSymbol, name);
                break;
            case TypeTags.OBJECT:
                bSymbol = lookupLangLibMethodInModule(symTable.langObjectModuleSymbol, name);
                break;
            case TypeTags.STREAM:
                bSymbol = lookupLangLibMethodInModule(symTable.langStreamModuleSymbol, name);
                break;
            case TypeTags.TABLE:
                bSymbol = lookupLangLibMethodInModule(symTable.langTableModuleSymbol, name);
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                bSymbol = lookupLangLibMethodInModule(symTable.langStringModuleSymbol, name);
                break;
            case TypeTags.TYPEDESC:
                bSymbol = lookupLangLibMethodInModule(symTable.langTypedescModuleSymbol, name);
                break;
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_PI:
                bSymbol = lookupLangLibMethodInModule(symTable.langXmlModuleSymbol, name);
                break;
            case TypeTags.XML_TEXT:
                bSymbol = lookupLangLibMethodInModule(symTable.langXmlModuleSymbol, name);
                if (bSymbol == symTable.notFoundSymbol) {
                    bSymbol = lookupLangLibMethodInModule(symTable.langStringModuleSymbol, name);
                }
                break;
            case TypeTags.BOOLEAN:
                bSymbol = lookupLangLibMethodInModule(symTable.langBooleanModuleSymbol, name);
                break;
            case TypeTags.UNION:
                Iterator<BType> itr = ((BUnionType) type).getMemberTypes().iterator();

                if (!itr.hasNext()) {
                    throw new IllegalArgumentException(
                            format("Union type '%s' does not have member types", type.toString()));
                }

                BType member = itr.next();
                if (types.isSubTypeOfBaseType(type, member.tag)) {
                    bSymbol = lookupLangLibMethod(member, name);
                } else {
                    bSymbol = symTable.notFoundSymbol;
                }
                break;
            default:
                bSymbol = symTable.notFoundSymbol;
        }
        if (bSymbol == symTable.notFoundSymbol) {
            bSymbol = lookupLangLibMethodInModule(symTable.langValueModuleSymbol, name);
        }

        if (bSymbol == symTable.notFoundSymbol) {
            bSymbol = lookupLangLibMethodInModule(symTable.langInternalModuleSymbol, name);
        }

        if (bSymbol == symTable.notFoundSymbol) {
            bSymbol = lookupLangLibMethodInModule(symTable.langTransactionModuleSymbol, name);
        }

        if (bSymbol == symTable.notFoundSymbol) {
            bSymbol = lookupLangLibMethodInModule(symTable.langQueryModuleSymbol, name);
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

    public BSymbol lookupMainSpaceSymbolInPackage(DiagnosticPos pos,
                                         SymbolEnv env,
                                         Name pkgAlias,
                                         Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInMainSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.getSource().getCompilationUnitName()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.MAIN);
    }

    public BSymbol lookupPrefixSpaceSymbolInPackage(DiagnosticPos pos,
                                         SymbolEnv env,
                                         Name pkgAlias,
                                         Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInPrefixSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.getSource().getCompilationUnitName()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.IMPORT);
    }

    public BSymbol lookupAnnotationSpaceSymbolInPackage(DiagnosticPos pos,
                                         SymbolEnv env,
                                         Name pkgAlias,
                                         Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInAnnotationSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.getSource().getCompilationUnitName()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.ANNOTATION);
    }

    public BSymbol lookupConstructorSpaceSymbolInPackage(DiagnosticPos pos,
                                                        SymbolEnv env,
                                                        Name pkgAlias,
                                                        Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbolInConstructorSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.getSource().getCompilationUnitName()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, SymTag.CONSTRUCTOR);
    }

    public BSymbol lookupLangLibMethodInModule(BPackageSymbol moduleSymbol, Name name) {

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
    public BSymbol lookupMemberSymbol(DiagnosticPos pos,
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
                dlog.error(pos, DiagnosticCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, entry.symbol.name);
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

    public void reloadErrorAndDependentTypes() {

        ScopeEntry entry = symTable.rootPkgSymbol.scope.lookup(Names.ERROR);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) != SymTag.TYPE) {
                entry = entry.next;
                continue;
            }
            symTable.errorType = (BErrorType) entry.symbol.type;
            symTable.detailType = (BMapType) symTable.errorType.detailType;
            symTable.errorConstructor = ((BErrorTypeSymbol) symTable.errorType.tsymbol).ctorSymbol;
            symTable.pureType = BUnionType.create(null, symTable.anydataType, this.symTable.errorType);
            symTable.streamType = new BStreamType(TypeTags.STREAM, symTable.pureType, null, null);
            symTable.tableType = new BTableType(TypeTags.TABLE, symTable.pureType, null);
            symTable.defineOperators(); // Define all operators e.g. binary, unary, cast and conversion
            symTable.pureType = BUnionType.create(null, symTable.anydataType, symTable.errorType);
            symTable.errorOrNilType = BUnionType.create(null, symTable.errorType, symTable.nilType);
            symTable.anyOrErrorType = BUnionType.create(null, symTable.anyType, symTable.errorType);
            symTable.mapAllType = new BMapType(TypeTags.MAP, symTable.anyOrErrorType, null);
            symTable.arrayAllType = new BArrayType(symTable.anyOrErrorType);
            symTable.typeDesc.constraint = symTable.anyOrErrorType;
            symTable.futureType.constraint = symTable.anyOrErrorType;
            return;
        }
        throw new IllegalStateException("built-in error not found ?");
    }

    public void reloadIntRangeType() {

        ScopeEntry entry = symTable.langInternalModuleSymbol.scope.lookup(Names.CREATE_INT_RANGE);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.INVOKABLE) != SymTag.INVOKABLE) {
                entry = entry.next;
                continue;
            }
            symTable.intRangeType = (BObjectType) ((BInvokableType) entry.symbol.type).retType;
            symTable.defineBinaryOperator(OperatorKind.CLOSED_RANGE, symTable.intType, symTable.intType,
                    symTable.intRangeType);
            symTable.defineBinaryOperator(OperatorKind.HALF_OPEN_RANGE, symTable.intType, symTable.intType,
                    symTable.intRangeType);
            return;
        }
        throw new IllegalStateException("built-in Integer Range type not found ?");
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
        for (int i = 0; i < arrayTypeNode.dimensions; i++) {
            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                                                                   env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                   arrayTypeNode.pos, SOURCE);
            BArrayType arrType;
            if (arrayTypeNode.sizes.length == 0) {
                arrType = new BArrayType(resultType, arrayTypeSymbol);
            } else {
                int size = arrayTypeNode.sizes[i];
                arrType = (size == UNSEALED_ARRAY_INDICATOR) ?
                        new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.UNSEALED) :
                        (size == OPEN_SEALED_ARRAY_INDICATOR) ?
                                new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.OPEN_SEALED) :
                                new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.CLOSED_SEALED);
            }
            resultType = arrayTypeSymbol.type = arrType;

            markParameterizedType(arrType, arrType.eType);
        }
    }

    public void visit(BLangUnionTypeNode unionTypeNode) {
        LinkedHashSet<BType> memberTypes = unionTypeNode.memberTypeNodes.stream()
                .map(memTypeNode -> resolveTypeNode(memTypeNode, env))
                .flatMap(memBType ->
                        memBType.tag == TypeTags.UNION && !Symbols.isFlagOn(memBType.tsymbol.flags, Flags.TYPE_PARAM) ?
                                ((BUnionType) memBType).getMemberTypes().stream() :
                                Stream.of(memBType))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                               Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                                                               env.scope.owner, unionTypeNode.pos, SOURCE);

        if (memberTypes.contains(symTable.noType)) {
            resultType = symTable.noType;
            return;
        }

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

        boolean isReadOnly = objectTypeNode.flagSet.contains(Flag.READONLY);
        if (isReadOnly) {
            flags.add(Flag.READONLY);
        }

        BTypeSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(flags), Names.EMPTY,
                env.enclPkg.symbol.pkgID, null, env.scope.owner, objectTypeNode.pos, SOURCE);
        BObjectType objectType;
        if (flags.contains(Flag.SERVICE)) {
            objectType = new BServiceType(objectSymbol);
        } else {
            objectType = isReadOnly ? new BObjectType(objectSymbol, Flags.READONLY) : new BObjectType(objectSymbol);
        }
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
                                                                        env.scope.owner, recordTypeNode.pos, SOURCE);
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
        BType error = streamTypeNode.error != null ? resolveTypeNode(streamTypeNode.error, env) : null;
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            resultType = symTable.noType;
            return;
        }

        BType streamType = new BStreamType(TypeTags.STREAM, constraintType, error, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        streamType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                                                      typeSymbol.pkgID, streamType, typeSymbol.owner,
                                                      streamTypeNode.pos, SOURCE);

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
                                                     typeSymbol.name, env.enclPkg.symbol.pkgID, tableType,
                                                     env.scope.owner, tableTypeNode.pos, SOURCE);
        tableType.constraintPos = tableTypeNode.constraint.pos;

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

        markParameterizedType(tableType, constraintType);

        resultType = tableType;
    }

    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                                                                Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY,
                                                                env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                finiteTypeNode.pos, SOURCE);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        for (BLangExpression literal : finiteTypeNode.valueSpace) {
            ((BLangLiteral) literal).type = symTable.getTypeFromTag(((BLangLiteral) literal).type.tag);
            finiteType.addValue(literal);
        }
        finiteTypeSymbol.type = finiteType;

        resultType = finiteType;
    }

    public void visit(BLangTupleTypeNode tupleTypeNode) {
        List<BType> memberTypes = tupleTypeNode.memberTypeNodes.stream()
                .map(memTypeNode -> resolveTypeNode(memTypeNode, env))
                .collect(Collectors.toList());

        // If at least one member is undefined, return noType as the type.
        if (memberTypes.contains(symTable.noType)) {
            resultType = symTable.noType;
            return;
        }

        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                               Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                                                               env.scope.owner, tupleTypeNode.pos, SOURCE);

        BTupleType tupleType = new BTupleType(tupleTypeSymbol, memberTypes);
        tupleTypeSymbol.type = tupleType;

        if (tupleTypeNode.restParamType != null) {
            tupleType.restType = resolveTypeNode(tupleTypeNode.restParamType, env);
            markParameterizedType(tupleType, tupleType.restType);
        }

        markParameterizedType(tupleType, memberTypes);

        resultType = tupleType;
    }

    public void visit(BLangErrorType errorTypeNode) {
        BType detailType = Optional.ofNullable(errorTypeNode.detailType)
                .map(bLangType -> resolveTypeNode(bLangType, env)).orElse(symTable.detailType);

        boolean distinctErrorDef = errorTypeNode.flagSet.contains(Flag.DISTINCT);
        if (detailType == symTable.detailType && !distinctErrorDef &&
                !this.env.enclPkg.packageID.equals(PackageID.ANNOTATIONS)) {
            resultType = symTable.errorType;
            return;
        }

        // Define user define error type.
        BErrorTypeSymbol errorTypeSymbol = Symbols
                .createErrorSymbol(Flags.asMask(errorTypeNode.flagSet), Names.EMPTY, env.enclPkg.symbol.pkgID,
                                   null, env.scope.owner, errorTypeNode.pos, SOURCE);
        BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;

        markParameterizedType(errorType, detailType);

        errorType.typeIdSet = BTypeIdSet.emptySet();

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
                                                           typeSymbol.pkgID, constrainedType, typeSymbol.owner,
                                                           constrainedTypeNode.pos, SOURCE);
        markParameterizedType(constrainedType, constraintType);
        resultType = constrainedType;
    }

    private void validateXMLConstraintType(BType constraintType, DiagnosticPos pos) {
        if (constraintType.tag == TypeTags.UNION) {
            checkUnionTypeForXMLSubTypes((BUnionType) constraintType, pos);
            return;
        }

        if (!TypeTags.isXMLTypeTag(constraintType.tag) && constraintType.tag != TypeTags.NEVER) {
            dlog.error(pos, DiagnosticCode.INCOMPATIBLE_TYPE_CONSTRAINT, symTable.xmlType, constraintType);
        }
    }

    private void checkUnionTypeForXMLSubTypes(BUnionType constraintUnionType, DiagnosticPos pos) {
        for (BType memberType : constraintUnionType.getMemberTypes()) {
            if (memberType.tag == TypeTags.UNION) {
                checkUnionTypeForXMLSubTypes((BUnionType) memberType, pos);
            }
            if (!TypeTags.isXMLTypeTag(memberType.tag)) {
                dlog.error(pos, DiagnosticCode.INCOMPATIBLE_TYPE_CONSTRAINT, symTable.xmlType, constraintUnionType);
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

            if ((tempSymbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                symbol = tempSymbol;
            } else if (Symbols.isTagOn(tempSymbol, SymTag.VARIABLE) && env.node.getKind() == NodeKind.FUNCTION) {
                BLangFunction func = (BLangFunction) env.node;
                boolean errored = false;

                if (func.returnTypeNode == null || !func.hasBody() ||
                        func.body.getKind() != NodeKind.EXTERN_FUNCTION_BODY) {
                    dlog.error(userDefinedTypeNode.pos, DiagnosticCode.INVALID_USE_OF_TYPEDESC_PARAM);
                    errored = true;
                }

                if (tempSymbol.type.tag != TypeTags.TYPEDESC) {
                    dlog.error(userDefinedTypeNode.pos, DiagnosticCode.INVALID_PARAM_TYPE_FOR_RETURN_TYPE,
                               tempSymbol.type);
                    errored = true;
                }

                if (errored) {
                    this.resultType = symTable.semanticError;
                    return;
                }

                BType paramValType = getTypedescParamValueType(func.requiredParams, tempSymbol);

                if (paramValType == symTable.semanticError) {
                    this.resultType = symTable.semanticError;
                    return;
                }

                if (paramValType != null) {
                    BTypeSymbol tSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PARAMETERIZED | tempSymbol.flags,
                                                          tempSymbol.name, tempSymbol.pkgID, null, func.symbol,
                                                          tempSymbol.pos, VIRTUAL);
                    tSymbol.type = new BParameterizedType(paramValType, (BVarSymbol) tempSymbol,
                                                          tSymbol, tempSymbol.name);
                    tSymbol.type.flags |= Flags.PARAMETERIZED;

                    this.resultType = tSymbol.type;
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

        resultType = symbol.type;
    }

    private BType getTypedescParamValueType(List<BLangSimpleVariable> params, BSymbol varSym) {
        for (BLangSimpleVariable param : params) {
            if (param.name.value.equals(varSym.name.value)) {
                if (param.expr == null) {
                    return ((BTypedescType) varSym.type).constraint;
                }

                NodeKind defaultValueExprKind = param.expr.getKind();

                if (defaultValueExprKind == NodeKind.TYPEDESC_EXPRESSION) {
                    return resolveTypeNode(((BLangTypedescExpr) param.expr).typeNode, this.env);
                }

                if (defaultValueExprKind == NodeKind.SIMPLE_VARIABLE_REF) {
                    Name varName = names.fromIdNode(((BLangSimpleVarRef) param.expr).variableName);
                    BSymbol typeRefSym = lookupSymbolInMainSpace(this.env, varName);
                    return typeRefSym != symTable.notFoundSymbol ? typeRefSym.type : symTable.semanticError;
                }

                dlog.error(param.pos, DiagnosticCode.INVALID_TYPEDESC_PARAM);
                return symTable.semanticError;
            }
        }

        return null;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        resultType = createInvokableType(functionTypeNode.getParams(), functionTypeNode.restParam,
                                         functionTypeNode.returnTypeNode, Flags.asMask(functionTypeNode.flagSet), env,
                                         functionTypeNode.pos);
    }

    public BInvokableType createInvokableType(List<? extends BLangVariable> paramVars, BLangVariable restVariable,
                                              BLangType retTypeVar, int flags, SymbolEnv env, DiagnosticPos pos) {
        List<BType> paramTypes = new ArrayList<>();
        List<BVarSymbol> params = new ArrayList<>();

        boolean foundDefaultableParam = false;
        List<String> paramNames = new ArrayList<>();
        for (BLangVariable paramNode : paramVars) {
            BLangSimpleVariable param = (BLangSimpleVariable) paramNode;
            Name paramName = names.fromIdNode(param.name);
            if (paramName != Names.EMPTY) {
                if (paramNames.contains(paramName.value)) {
                    dlog.error(param.name.pos, DiagnosticCode.REDECLARED_SYMBOL, paramName.value);
                } else {
                    paramNames.add(paramName.value);
                }
            }
            BType type = resolveTypeNode(param.getTypeNode(), env);
            paramNode.type = type;
            paramTypes.add(type);

            if (param.expr != null) {
                foundDefaultableParam = true;
            }

            BVarSymbol symbol = new BVarSymbol(type.flags, paramName, env.enclPkg.symbol.pkgID, type, env.scope.owner,
                                               param.pos);
            param.symbol = symbol;

            if (param.expr == null && foundDefaultableParam) {
                dlog.error(param.pos, DiagnosticCode.REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM);
            }

            if (param.flagSet.contains(Flag.PUBLIC)) {
                symbol.flags |= Flags.PUBLIC;
            }

            if (param.flagSet.contains(Flag.TRANSACTIONAL)) {
                symbol.flags |= Flags.TRANSACTIONAL;
            }

            if (param.expr != null) {
                symbol.flags |= Flags.OPTIONAL;
                symbol.defaultableParam = true;
            }
            params.add(symbol);
        }

        BType retType = resolveTypeNode(retTypeVar, this.env);

        BVarSymbol restParam = null;
        BType restType = null;

        if (restVariable != null) {
            restType = resolveTypeNode(restVariable.typeNode, env);
            restVariable.type = restType;
            restParam = new BVarSymbol(restType.flags, names.fromIdNode(((BLangSimpleVariable) restVariable).name),
                                       env.enclPkg.symbol.pkgID, restType, env.scope.owner, restVariable.pos);
        }

        BInvokableType bInvokableType = new BInvokableType(paramTypes, restType, retType, null);
        bInvokableType.flags = flags;
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE, flags,
                                                                         env.enclPkg.symbol.pkgID, bInvokableType,
                                                                         env.scope.owner, pos, SOURCE);

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
                        if (!scopeEntries.contains(scopeEntry) && !(scopeEntry.symbol instanceof BVarSymbol)) {
                            scopeEntries.add(scopeEntry);
                        }
                    });
                }
            });
        }
        return visibleEntries;
    }

    public BSymbol getBinaryEqualityForTypeSets(OperatorKind opKind, BType lhsType, BType rhsType,
                                                BLangBinaryExpr binaryExpr) {
        boolean validEqualityIntersectionExists;
        switch (opKind) {
            case EQUAL:
            case NOT_EQUAL:
                validEqualityIntersectionExists = types.validEqualityIntersectionExists(lhsType, rhsType);
                break;
            case REF_EQUAL:
            case REF_NOT_EQUAL:
                validEqualityIntersectionExists =
                        types.isAssignable(lhsType, rhsType) || types.isAssignable(rhsType, lhsType);
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

    private void visitBuiltInTypeNode(BLangType typeNode, TypeKind typeKind, SymbolEnv env) {
        Name typeName = names.fromTypeKind(typeKind);
        BSymbol typeSymbol = lookupMemberSymbol(typeNode.pos, symTable.rootScope,
                env, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            dlog.error(typeNode.pos, diagCode, typeName);
        }

        resultType = typeNode.type = typeSymbol.type;
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
            return env.enclType.type.tsymbol == symbol.owner;
        }
        return isMemberAllowed(env, symbol);
    }

    private boolean isMemberAllowed(SymbolEnv env, BSymbol symbol) {
        return env != null && (env.enclInvokable != null
                && env.enclInvokable.symbol.receiverSymbol != null
                && env.enclInvokable.symbol.receiverSymbol.type.tsymbol == symbol.owner
                || isMemberAllowed(env.enclEnv, symbol));
    }

    public void loadFunctionalConstructors() {
        BPackageSymbol xmlModuleSymbol = symTable.langXmlModuleSymbol;
        if (xmlModuleSymbol == null) {
            return;
        }

        BConstructorSymbol elementCtor =
                FunctionalConstructorBuilder
                    .newConstructor("Element", xmlModuleSymbol, symTable.xmlElementType, symTable.builtinPos)
                    .addParam("name", symTable.stringType)
                    .addDefaultableParam("attributes", symTable.mapStringType)
                    .addDefaultableParam("children", symTable.xmlType)
                    .build();
        xmlModuleSymbol.scope.define(elementCtor.name, elementCtor);

        BConstructorSymbol piCtor =
                FunctionalConstructorBuilder.newConstructor("ProcessingInstruction", xmlModuleSymbol,
                                                            symTable.xmlPIType, symTable.builtinPos)
                    .addParam("target", symTable.stringType)
                    .addDefaultableParam("content", symTable.stringType)
                    .build();
        xmlModuleSymbol.scope.define(piCtor.name, piCtor);

        BConstructorSymbol commentCtor =
                FunctionalConstructorBuilder
                    .newConstructor("Comment", xmlModuleSymbol, symTable.xmlCommentType, symTable.builtinPos)
                    .addDefaultableParam("comment", symTable.stringType)
                    .build();
        xmlModuleSymbol.scope.define(commentCtor.name, commentCtor);

        BConstructorSymbol textCtor =
                FunctionalConstructorBuilder
                    .newConstructor("Text", xmlModuleSymbol, symTable.xmlTextType, symTable.builtinPos)
                    .addDefaultableParam("characters", symTable.stringType)
                    .build();
        xmlModuleSymbol.scope.define(textCtor.name, textCtor);
    }

    private BType computeIntersectionType(BLangIntersectionTypeNode intersectionTypeNode) {
        List<BLangType> constituentTypeNodes = intersectionTypeNode.constituentTypeNodes;
        Map<BType, BLangType> typeBLangTypeMap = new HashMap<>();

        boolean validIntersection = true;

        BLangType bLangTypeOne = constituentTypeNodes.get(0);
        BType typeOne = resolveTypeNode(bLangTypeOne, env);

        if (typeOne == symTable.noType) {
            return symTable.noType;
        }
        typeBLangTypeMap.put(typeOne, bLangTypeOne);


        BLangType bLangTypeTwo = constituentTypeNodes.get(1);
        BType typeTwo = resolveTypeNode(bLangTypeTwo, env);

        if (typeTwo == symTable.noType) {
            return symTable.noType;
        }
        typeBLangTypeMap.put(typeTwo, bLangTypeTwo);

        boolean hasReadOnlyType = typeOne == symTable.readonlyType || typeTwo == symTable.readonlyType;

        BType potentialIntersectionType = getPotentialReadOnlyIntersection(typeOne, typeTwo);

        if (potentialIntersectionType == symTable.semanticError) {
            validIntersection = false;
        } else {
            for (int i = 2; i < constituentTypeNodes.size(); i++) {
                BLangType bLangType = constituentTypeNodes.get(i);
                BType type = resolveTypeNode(bLangType, env);
                typeBLangTypeMap.put(type, bLangType);

                if (!hasReadOnlyType) {
                    hasReadOnlyType = type == symTable.readonlyType;
                }

                if (type == symTable.noType) {
                    return symTable.noType;
                }

                potentialIntersectionType = getPotentialReadOnlyIntersection(potentialIntersectionType, type);
                if (potentialIntersectionType == symTable.semanticError) {
                    validIntersection = false;
                    break;
                }
            }
        }

        if (!validIntersection) {
            dlog.error(intersectionTypeNode.pos, DiagnosticCode.INVALID_INTERSECTION_TYPE, intersectionTypeNode);
            return symTable.semanticError;
        }

        if (!hasReadOnlyType) {
            dlog.error(intersectionTypeNode.pos, DiagnosticCode.INVALID_NON_READONLY_INTERSECTION_TYPE,
                       intersectionTypeNode);
            return symTable.semanticError;
        }

        if (types.isInherentlyImmutableType(potentialIntersectionType)) {
            return potentialIntersectionType;
        }

        if (!types.isSelectivelyImmutableType(potentialIntersectionType, true, false)) {
            if (types.isSelectivelyImmutableType(potentialIntersectionType)) {
                // This intersection would have been valid if not for `readonly object`s.
                dlog.error(intersectionTypeNode.pos, DiagnosticCode.INVALID_READONLY_OBJECT_INTERSECTION_TYPE);
            } else {
                dlog.error(intersectionTypeNode.pos, DiagnosticCode.INVALID_READONLY_INTERSECTION_TYPE,
                           potentialIntersectionType);
            }
            return symTable.semanticError;
        }

        return ImmutableTypeCloner.getImmutableIntersectionType(typeBLangTypeMap.get(potentialIntersectionType),
                                                                intersectionTypeNode.pos, types,
                                                                (SelectivelyImmutableReferenceType)
                                                                        potentialIntersectionType,
                                                                env, symTable, anonymousModelHelper, names);
    }

    private BType getPotentialReadOnlyIntersection(BType lhsType, BType rhsType) {
        if (lhsType == symTable.readonlyType) {
            return rhsType;
        }

        if (rhsType == symTable.readonlyType) {
            return lhsType;
        }

        return types.getTypeIntersection(lhsType, rhsType);
    }
}
