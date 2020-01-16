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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
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
    private BLangDiagnosticLog dlog;
    private Types types;

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
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public boolean checkForUniqueSymbol(DiagnosticPos pos, SymbolEnv env, BSymbol symbol, int expSymTag) {
        //lookup symbol
        BSymbol foundSym = lookupSymbol(env, symbol.name, expSymTag);

        //if symbol is not found then it is unique for the current scope
        if (foundSym == symTable.notFoundSymbol) {
            return true;
        }

        if ((foundSym.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            // In order to remove duplicate errors.
            return false;
        }

        // if a symbol is found, then check whether it is unique
        return isDistinctSymbol(pos, symbol, foundSym);
    }

    public boolean checkForUniqueSymbol(SymbolEnv env, BSymbol symbol, int expSymTag) {
        BSymbol foundSym = lookupSymbol(env, symbol.name, expSymTag);
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

        // Type names should be unique and cannot be shadowed
        if ((foundSym.tag & SymTag.TYPE) == SymTag.TYPE) {
            dlog.error(pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
            return false;
        }

        if (isSymbolDefinedInRootPkgLvl(foundSym)) {
            dlog.error(pos, DiagnosticCode.REDECLARED_BUILTIN_SYMBOL, symbol.name);
            return false;
        }

        if (hasSameOwner(symbol, foundSym)) {
            dlog.error(pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
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

        // Type names should be unique and cannot be shadowed
        if ((foundSym.tag & SymTag.TYPE) == SymTag.TYPE) {
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
        }

        // If the symbol being defined is inside a lambda and the existing symbol is defined inside a function, both
        // symbols are in the same block scope.
        return Symbols.isFlagOn(symbol.owner.flags, Flags.LAMBDA) &&
                ((foundSym.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE);
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
            if ((entry.symbol.tag & expSymTag) == expSymTag) {
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

    public BSymbol resolveImplicitCastOp(BType sourceType,
                                         BType targetType) {
        BSymbol symbol = resolveOperator(Names.CAST_OP, Lists.of(sourceType, targetType));
        if (symbol == symTable.notFoundSymbol) {
            return symbol;
        }

        BCastOperatorSymbol castSymbol = (BCastOperatorSymbol) symbol;
        if (castSymbol.implicit) {
            return symbol;
        }

        return symTable.notFoundSymbol;
    }

    public BSymbol resolveConversionOperator(BType sourceType, BType targetType) {
        return types.getConversionOperator(sourceType, targetType);
    }

    public BSymbol resolveCastOperator(BLangExpression expr, BType sourceType, BType targetType) {
        return types.getCastOperator(expr, sourceType, targetType);
    }

    public BSymbol resolveTypeCastOperator(BLangExpression expr, BType sourceType, BType targetType) {
        return types.getTypeCastOperator(expr, sourceType, targetType);
    }

    public BSymbol resolveBinaryOperator(OperatorKind opKind,
                                         BType lhsType,
                                         BType rhsType) {
        return resolveOperator(names.fromString(opKind.value()), Lists.of(lhsType, rhsType));
    }

    public BSymbol resolveBuiltinOperator(Name method, BType... args) {
        BType type = args[0];
        switch (type.tag) {
            case TypeTags.RECORD:
                type = symTable.recordType;
                break;
            case TypeTags.ARRAY:
                type = symTable.arrayType;
                break;
            case TypeTags.TUPLE:
                type = symTable.tupleType;
                break;
            case TypeTags.ERROR:
                type = symTable.errorType;
                break;
            case TypeTags.MAP:
                type = symTable.mapType;
                break;
        }

        List<BType> argsList = Lists.of(type);
        List<BType> paramTypes = Arrays.asList(args).subList(1, args.length);
        argsList.addAll(paramTypes);
        return resolveOperator(method, argsList);
    }

    BSymbol createEqualityOperator(OperatorKind opKind, BType lhsType, BType rhsType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        BType retType = symTable.booleanType;
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        return new BOperatorSymbol(names.fromString(opKind.value()), null, opType, null);
    }

    BOperatorSymbol createTypeCastSymbol(BType type, BType retType) {
        List<BType> paramTypes = Lists.of(type);
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        return new BOperatorSymbol(Names.CAST_OP, null, opType, null);
    }

    BSymbol getNumericConversionOrCastSymbol(BLangExpression expr, BType sourceType,
                                             BType targetType) {
        if (targetType.tag == TypeTags.UNION &&
                ((BUnionType) targetType).getMemberTypes().stream()
                        .filter(memType -> types.isBasicNumericType(memType)).count() > 1) {
            return symTable.notFoundSymbol;
        }

        if (types.isBasicNumericType(sourceType) && types.isBasicNumericType(targetType)) {
            // we only reach here for different numeric types.
            return resolveOperator(Names.CONVERSION_OP, Lists.of(sourceType, targetType));
        } else {
            // Target type is always a union here.
            if (types.isBasicNumericType(sourceType)) {
                // i.e., a conversion from a numeric type to another numeric type in a union.
                // int|string u1 = <int|string> 1.0;
                types.setImplicitCastExpr(expr, sourceType, symTable.anyType);
                return createTypeCastSymbol(sourceType, targetType);
            }

            switch (sourceType.tag) {
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                case TypeTags.JSON:
                    return createTypeCastSymbol(sourceType, targetType);
                case TypeTags.UNION:
                    if (((BUnionType) sourceType).getMemberTypes().stream()
                            .anyMatch(memType -> types.isBasicNumericType(memType) ||
                                    (memType.tag == TypeTags.FINITE &&
                                            types.finiteTypeContainsNumericTypeValues((BFiniteType) memType)))) {
                        return createTypeCastSymbol(sourceType, targetType);
                    }
                    break;
                case TypeTags.FINITE:
                    if (types.finiteTypeContainsNumericTypeValues((BFiniteType) sourceType)) {
                        return createTypeCastSymbol(sourceType, targetType);
                    }
                    break;
            }
        }
        return symTable.notFoundSymbol;
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
        return resolvePkgSymbol(pos, env, pkgAlias, SymTag.PACKAGE);
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

    private BSymbol resolvePkgSymbol(DiagnosticPos pos, SymbolEnv env, Name pkgAlias, int symTag) {
        if (pkgAlias == Names.EMPTY) {
            // Return the current package symbol
            return env.enclPkg.symbol;
        }

        // Lookup for an imported package
        BSymbol pkgSymbol = lookupSymbol(env, pkgAlias, symTag);
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
        }

        return pkgSymbol;
    }

    public BSymbol resolveAnnotation(DiagnosticPos pos, SymbolEnv env, Name pkgAlias, Name annotationName) {
        return this.lookupSymbolInPackage(pos, env, pkgAlias, annotationName, SymTag.ANNOTATION);
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
     * Return the symbol associated with the given name in the current package.
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
            if (symTable.rootPkgSymbol.pkgID.equals(entry.symbol.pkgID) &&
                    (entry.symbol.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME) {
                return entry.symbol;
            }
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
            case TypeTags.STRING:
                bSymbol = lookupLangLibMethodInModule(symTable.langStringModuleSymbol, name);
                break;
            case TypeTags.TABLE:
                bSymbol = lookupLangLibMethodInModule(symTable.langTableModuleSymbol, name);
                break;
            case TypeTags.TYPEDESC:
                bSymbol = lookupLangLibMethodInModule(symTable.langTypedescModuleSymbol, name);
                break;
            case TypeTags.XML:
                bSymbol = lookupLangLibMethodInModule(symTable.langXmlModuleSymbol, name);
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
    BSymbol lookupClosureVarSymbol(SymbolEnv env, Name name, int expSymTag) {
        ScopeEntry entry = env.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if (symTable.rootPkgSymbol.pkgID.equals(entry.symbol.pkgID) &&
                    (entry.symbol.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME) {
                return entry.symbol;
            }
            if ((entry.symbol.tag & expSymTag) == expSymTag) {
                return entry.symbol;
            }
            entry = entry.next;
        }

        if (env.enclEnv == null || env.enclEnv.node == null) {
            return symTable.notFoundSymbol;
        }

        return lookupClosureVarSymbol(env.enclEnv, name, expSymTag);
    }

    /**
     * Return the symbol associated with the given name in the give package.
     *
     * @param pos       symbol position
     * @param env       current symbol environment
     * @param pkgAlias  package alias
     * @param name      symbol name
     * @param expSymTag expected symbol type/tag
     * @return resolved symbol
     */
    public BSymbol lookupSymbolInPackage(DiagnosticPos pos,
                                         SymbolEnv env,
                                         Name pkgAlias,
                                         Name name,
                                         int expSymTag) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return lookupSymbol(env, name, expSymTag);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol =
                resolvePrefixSymbol(env, pkgAlias, names.fromString(pos.getSource().getCompilationUnitName()));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias.value);
            return pkgSymbol;
        }

        // 3) Look up the package scope.
        return lookupMemberSymbol(pos, pkgSymbol.scope, env, name, expSymTag);
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
            symTable.detailType = (BRecordType) symTable.errorType.detailType;
            symTable.errorConstructor = ((BErrorTypeSymbol) symTable.errorType.tsymbol).ctorSymbol;
            symTable.pureType = BUnionType.create(null, symTable.anydataType, this.symTable.errorType);
            symTable.detailType.restFieldType = symTable.pureType;
            symTable.streamType = new BStreamType(TypeTags.STREAM, symTable.pureType, null);
            symTable.defineOperators(); // Define all operators e.g. binary, unary, cast and conversion
            symTable.pureType = BUnionType.create(null, symTable.anydataType, symTable.errorType);
            symTable.errorOrNilType = BUnionType.create(null, symTable.errorType, symTable.nilType);
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
            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.asMask(EnumSet
                    .of(Flag.PUBLIC)), Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);
            if (arrayTypeNode.sizes.length == 0) {
                resultType = new BArrayType(resultType, arrayTypeSymbol);
            } else {
                int size = arrayTypeNode.sizes[i];
                resultType = (size == UNSEALED_ARRAY_INDICATOR) ?
                        new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.UNSEALED) :
                        (size == OPEN_SEALED_ARRAY_INDICATOR) ?
                                new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.OPEN_SEALED) :
                                new BArrayType(resultType, arrayTypeSymbol, size, BArrayState.CLOSED_SEALED);
            }
            arrayTypeSymbol.type = resultType;
        }
    }

    public void visit(BLangUnionTypeNode unionTypeNode) {
        LinkedHashSet<BType> memberTypes = unionTypeNode.memberTypeNodes.stream()
                .map(memTypeNode -> resolveTypeNode(memTypeNode, env))
                .flatMap(memBType ->
                        memBType.tag == TypeTags.UNION ?
                                ((BUnionType) memBType).getMemberTypes().stream() :
                                Stream.of(memBType))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);

        if (memberTypes.contains(symTable.noType)) {
            resultType = symTable.noType;
            return;
        }

        BUnionType unionType = BUnionType.create(unionTypeSymbol, memberTypes);
        unionTypeSymbol.type = unionType;

        resultType = unionType;
    }

    public void visit(BLangObjectTypeNode objectTypeNode) {
        EnumSet<Flag> flags = EnumSet.copyOf(objectTypeNode.flagSet);
        if (objectTypeNode.isAnonymous) {
            flags.add(Flag.PUBLIC);
        }

        BTypeSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(flags), Names.EMPTY,
                env.enclPkg.symbol.pkgID, null, env.scope.owner);
        BObjectType objectType;
        if (flags.contains(Flag.SERVICE)) {
            objectType = new BServiceType(objectSymbol);
        } else {
            objectType = new BObjectType(objectSymbol);
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
            EnumSet<Flag> flags = recordTypeNode.isAnonymous ? EnumSet.of(Flag.PUBLIC) : EnumSet.noneOf(Flag.class);
            BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                    env.enclPkg.symbol.pkgID, null, env.scope.owner);
            BRecordType recordType = new BRecordType(recordSymbol);
            recordSymbol.type = recordType;
            recordTypeNode.symbol = recordSymbol;
            resultType = recordType;
        } else {
            resultType = recordTypeNode.symbol.type;
        }
    }

    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        for (BLangExpression literal : finiteTypeNode.valueSpace) {
            ((BLangLiteral) literal).type = symTable.getTypeFromTag(((BLangLiteral) literal).type.tag);
            finiteType.valueSpace.add(literal);
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
                Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);

        BTupleType tupleType = new BTupleType(tupleTypeSymbol, memberTypes);
        tupleTypeSymbol.type = tupleType;
        if (tupleTypeNode.restParamType !=  null) {
            tupleType.restType = resolveTypeNode(tupleTypeNode.restParamType, env);
        }

        resultType = tupleType;
    }

    public void visit(BLangErrorType errorTypeNode) {
        BType reasonType = Optional.ofNullable(errorTypeNode.reasonType)
                .map(bLangType -> resolveTypeNode(bLangType, env)).orElse(symTable.stringType);
        BType detailType = Optional.ofNullable(errorTypeNode.detailType)
                .map(bLangType -> resolveTypeNode(bLangType, env)).orElse(symTable.detailType);

        // TODO: 7/12/19 FIX ME!!! This is a temporary hack to ensure the detail type is set to the error type when
        //  compiling the annotations module which contains the error def
        if (detailType == null && PackageID.ANNOTATIONS.equals(env.enclPkg.packageID)) {
            BSymbol symbol = this.lookupSymbol(env, Names.ERROR, SymTag.ERROR);
            resultType = symbol.type;
            symTable.errorType = (BErrorType) resultType;
            symTable.detailType = (BRecordType) symTable.errorType.detailType;
            return;
        }

        if (reasonType == symTable.stringType && detailType == symTable.detailType) {
            resultType = symTable.errorType;
            return;
        }

        // Define user define error type.
        BErrorTypeSymbol errorTypeSymbol = Symbols
                .createErrorSymbol(Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY, env.enclPkg.symbol.pkgID,
                        null, env.scope.owner);
        BErrorType errorType = new BErrorType(errorTypeSymbol, reasonType, detailType);
        errorTypeSymbol.type = errorType;

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
        if (type.tag == TypeTags.TABLE) {
            if (constraintType.tag == TypeTags.OBJECT) {
                dlog.error(constrainedTypeNode.pos, DiagnosticCode.OBJECT_TYPE_NOT_ALLOWED);
                resultType = symTable.semanticError;
                return;
            }
            // TODO: Fix to set type symbol with specified constraint, as with other constrained types.
            resultType = new BTableType(TypeTags.TABLE, constraintType, type.tsymbol);
            return;
        } else if (type.tag == TypeTags.STREAM) {
            if (!constraintType.isPureType()) {
                dlog.error(constrainedTypeNode.constraint.pos, DiagnosticCode.STREAM_INVALID_CONSTRAINT,
                           constraintType);
                resultType = symTable.semanticError;
                return;
            }
            constrainedType = new BStreamType(TypeTags.STREAM, constraintType, null);
        } else if (type.tag == TypeTags.FUTURE) {
            constrainedType = new BFutureType(TypeTags.FUTURE, constraintType, null);
        } else if (type.tag == TypeTags.MAP) {
            constrainedType = new BMapType(TypeTags.MAP, constraintType, null);
        } else if (type.tag == TypeTags.TYPEDESC) {
            constrainedType = new BTypedescType(constraintType, null);
        } else {
            return;
        }

        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                                                           typeSymbol.pkgID, constrainedType, typeSymbol.owner);
        resultType = constrainedType;
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
            symbol = lookupSymbolInPackage(userDefinedTypeNode.pos, env, pkgAlias, typeName, SymTag.ANNOTATION);
        }

        // 2) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        if (symbol == symTable.notFoundSymbol) {
            symbol = lookupSymbolInPackage(userDefinedTypeNode.pos, env, pkgAlias, typeName, SymTag.VARIABLE_NAME);
        }

        if (symbol == symTable.notFoundSymbol) {
            // 3) Lookup the root scope for types such as 'error'
            symbol = lookupMemberSymbol(userDefinedTypeNode.pos, symTable.rootScope, this.env, typeName,
                                        SymTag.VARIABLE_NAME);
        }

        if (this.env.logErrors && symbol == symTable.notFoundSymbol) {
            dlog.error(userDefinedTypeNode.pos, diagCode, typeName);
            resultType = symTable.semanticError;
            return;
        }

        resultType = symbol.type;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        resultType = createInvokableType(functionTypeNode.getParams(), functionTypeNode.restParam,
                functionTypeNode.returnTypeNode, Flags.asMask(functionTypeNode.flagSet), env);
    } 

    public BInvokableType createInvokableType(List<? extends BLangVariable> paramVars, BLangVariable restVariable,
                               BLangType retTypeVar, int flags, SymbolEnv env) {
        List<BType> paramTypes = new ArrayList<>();
        List<BVarSymbol> params = new ArrayList<>();

        boolean foundDefaultableParam = false;
        List<String> paramNames = new ArrayList<>();
        for (BLangVariable paramNode :  paramVars) {
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

            BVarSymbol symbol = new BVarSymbol(type.flags, paramName,
                    env.enclPkg.symbol.pkgID, type, env.scope.owner);
            param.symbol = symbol;

            if (param.expr == null && foundDefaultableParam) {
                dlog.error(param.pos, DiagnosticCode.REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM);
            }

            if (param.flagSet.contains(Flag.PUBLIC)) {
                symbol.flags |= Flags.PUBLIC;
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
                    env.enclPkg.symbol.pkgID, restType, env.scope.owner);
        }

        BInvokableType bInvokableType = new BInvokableType(paramTypes, restType, retType, null);
        bInvokableType.flags = flags;
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                flags,
                env.enclPkg.symbol.pkgID,
                bInvokableType, env.scope.owner);

        tsymbol.params = params;
        tsymbol.restParam = restParam;
        tsymbol.returnType = retType;
        bInvokableType.tsymbol = tsymbol;

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
}
