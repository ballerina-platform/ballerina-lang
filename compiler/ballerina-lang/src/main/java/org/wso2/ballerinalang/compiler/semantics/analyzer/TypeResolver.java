/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.BTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.*;
import org.wso2.ballerinalang.compiler.semantics.model.types.*;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.tree.types.*;
import org.wso2.ballerinalang.compiler.util.*;
import org.wso2.ballerinalang.util.Flags;

import java.util.*;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.util.Constants.INFERRED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_ARRAY_INDICATOR;

/**
 * @since 2201.4.0
 */

public class TypeResolver {

    private static final CompilerContext.Key<TypeResolver> TYPE_RESOLVER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final ConstantValueResolver constResolver;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final ConstantTypeChecker constantTypeChecker;
    private final ConstantTypeChecker.ResolveConstantExpressionType resolveConstantExpressionType;
    private final ConstantTypeChecker.GetConstantValidType getConstantValidType;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

    private List<BLangTypeDefinition> resolvingtypeDefinitions = new ArrayList<>();
    private List<BIntersectionType> intersectionTypeList;
    public HashSet<BLangConstant> resolvedConstants = new HashSet<>();
    private HashSet<BLangConstant> resolvingConstants = new HashSet<>();
    private BLangPackage pkgNode;

    public TypeResolver(CompilerContext context) {
        context.put(TYPE_RESOLVER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.constResolver = ConstantValueResolver.getInstance(context);
        this.constantTypeChecker = ConstantTypeChecker.getInstance(context);
        this.resolveConstantExpressionType = ConstantTypeChecker.ResolveConstantExpressionType.getInstance(context);
        this.getConstantValidType = ConstantTypeChecker.GetConstantValidType.getInstance(context);
    }

    public static TypeResolver getInstance(CompilerContext context) {
        TypeResolver typeResolver = context.get(TYPE_RESOLVER_KEY);
        if (typeResolver == null) {
            typeResolver = new TypeResolver(context);
        }

        return typeResolver;
    }

    public void defineBTypes(List<BLangNode> moduleDefs, SymbolEnv pkgEnv, BLangPackage pkgNode) {
        this.pkgNode = pkgNode;
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(symEnter.getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

//        constResolver.resolve(pkgEnv.enclPkg.constants, pkgEnv.enclPkg.packageID, pkgEnv);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDefinition = (BLangClassDefinition) def;
                // call the visitor
            } else if (def.getKind() == NodeKind.CONSTANT) {
                // TODO: Support type inferring from the expr
                //       Introduce light weight type checker to validate constants
                //       Resolve constant exprs.
                resolveConstant(pkgEnv, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                intersectionTypeList = new ArrayList<>();
                resolveTypeDefinition(pkgEnv, modTable, typeDefinition, 0);
                BType type = typeDefinition.typeNode.getBType();
                fillEffectiveTypeOfIntersectionTypes(intersectionTypeList, pkgEnv);
                if (typeDefinition.hasCyclicReference) {
                    updateIsCyclicFlag(type);
                }
            }
        }
    }

    private void fillEffectiveTypeOfIntersectionTypes(List<BIntersectionType> typeList, SymbolEnv symEnv) {
        for (BIntersectionType intersectionType: typeList) {
            Iterator<BType> iterator = intersectionType.getConstituentTypes().iterator();
            BType effectiveType = iterator.next();
            if (effectiveType.tag == TypeTags.READONLY && iterator.hasNext()) {
                effectiveType = iterator.next();
            }
            while (iterator.hasNext()) {
                BType type = iterator.next();
                if (type.tag == TypeTags.READONLY) {
                    intersectionType.flags = intersectionType.flags | TypeTags.READONLY;
                    continue;
                }
                effectiveType = calculateEffectiveType(effectiveType, type);
            }

            intersectionType.effectiveType = effectiveType;
        }
        for (BIntersectionType intersectionType: typeList) {
            if ((intersectionType.flags & Flags.READONLY) == Flags.READONLY) {
                BIntersectionType immutableIntersectionType =
                        ImmutableTypeCloner.getImmutableIntersectionType(intersectionType.tsymbol.pos, types,
                                intersectionType.effectiveType, symEnv, symTable, anonymousModelHelper, names, new HashSet<>());
                intersectionType.effectiveType = immutableIntersectionType.effectiveType;
            }
        }
    }

    private BType calculateEffectiveType(BType effectiveType, BType type) {
        return effectiveType;
    }

    private BType resolveTypeDefinition(SymbolEnv symEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth) {
        if (defn.getBType() != null) {
            // Already defined.
            return defn.getBType();
        }

        if (depth == defn.cycleDepth) {
            // We cannot define recursive typeDefinitions with same depths.
            dlog.error(defn.pos, DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, defn.name);
            return null;
        }

        defn.cycleDepth = depth;
        boolean hasAlreadyVisited = false;

        if (resolvingtypeDefinitions.contains(defn)) {
            // Type definition has a cyclic reference.
            for (int i = resolvingtypeDefinitions.size() - 1; i >= 0; i--) {
                resolvingtypeDefinitions.get(i).hasCyclicReference = true;
                if (resolvingtypeDefinitions.get(i) == defn) {
                    break;
                }
            }
            hasAlreadyVisited = true;
        } else {
            // Add the type into resolvingtypeDefinitions list.
            // This is used to identify types which have cyclic references.
            resolvingtypeDefinitions.add(defn);
        }

        // Resolve the type
        BType type = resolveTypeDesc(symEnv, mod, defn, depth, defn.typeNode);

        // Define the typeDefinition. Add symbol, flags etc.
        definetypeDefinition(defn, type, symEnv);
//        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(defn); // disable due to intersections

        if (!hasAlreadyVisited) {
            // Remove the typeDefinition from currently resolving typeDefinition map.
            resolvingtypeDefinitions.remove(defn);
        }

        if (defn.getBType() == null) {
            defn.setBType(type);
            defn.cycleDepth = -1;
            return type;
        } else {
            return type;
        }
    }

    private void updateIsCyclicFlag(BType type) {
        if (type == null) {
            return;
        }
        switch (type.getKind()) {
            case TUPLE:
                ((BTupleType) type).isCyclic = true;
                break;
            case UNION:
                ((BUnionType) type).isCyclic = true;
                break;
            case INTERSECTION:
                updateIsCyclicFlag(((BIntersectionType) type).getEffectiveType());
                break;
        }
    }

    private BType resolveTypeDesc(SymbolEnv symEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangType td) {
        if (td == null) {
            return symTable.semanticError;
        }
        BType resultType;
        switch (td.getKind()) {
            case VALUE_TYPE:
                resultType = resolveTypeDesc((BLangValueType) td, symEnv);
                break;
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                resultType = resolveTypeDesc((BLangConstrainedType) td, symEnv, mod, depth, defn);
                break;
            case ARRAY_TYPE:
                resultType = resolveTypeDesc(((BLangArrayType) td), symEnv, mod, depth, defn);
                break;
            case TUPLE_TYPE_NODE:
                resultType = resolveTypeDesc((BLangTupleTypeNode) td, symEnv, mod, depth, defn);
                break;
            case RECORD_TYPE:
                resultType = resolveTypeDesc((BLangRecordTypeNode) td, symEnv, mod, depth, defn);
                break;
            case OBJECT_TYPE: // Need to implement
                resultType = resolveTypeDesc((BLangObjectTypeNode) td, symEnv, mod, depth, defn);
                break;
            case FUNCTION_TYPE:
                resultType = resolveTypeDesc((BLangFunctionTypeNode) td, symEnv, mod, depth, defn);
                break;
            case ERROR_TYPE:
                resultType = resolveTypeDesc((BLangErrorType) td, symEnv, mod, depth, defn);
                break;
            case UNION_TYPE_NODE:
                resultType = resolveTypeDesc((BLangUnionTypeNode) td, symEnv, mod, depth, defn);
                break;
            case INTERSECTION_TYPE_NODE:
                resultType = resolveTypeDesc((BLangIntersectionTypeNode) td, symEnv, mod, depth, defn);
                break;
            case USER_DEFINED_TYPE:
                resultType = resolveTypeDesc((BLangUserDefinedType) td, symEnv, mod, depth);
                break;
            case BUILT_IN_REF_TYPE:
                resultType = resolveTypeDesc((BLangBuiltInRefTypeNode) td, symEnv);
                break;
            case FINITE_TYPE_NODE:
                resultType = resolveSingletonType((BLangFiniteTypeNode) td, symEnv);
                break;
            case TABLE_TYPE:
                resultType = resolveTypeDesc((BLangTableTypeNode) td, symEnv, mod, depth, defn);
                break;
            case STREAM_TYPE:
                resultType = resolveTypeDesc((BLangStreamType) td, symEnv, mod, depth, defn);
                break;
            default:
                throw new AssertionError("Invalid type");
        }

        BType refType = Types.getReferredType(resultType);
        if (refType != symTable.noType) {
            // If the typeNode.nullable is true then convert the resultType to a union type
            // if it is not already a union type, JSON type, or any type
            if (td.nullable && resultType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) refType;
                unionType.add(symTable.nilType);
            }
        }

        symResolver.validateDistinctType(td, resultType);
        if (td.getBType() == null) {
            td.setBType(resultType);
        }
        return resultType;
    }

    private BType resolveTypeDesc(BLangValueType td, SymbolEnv symEnv) {
        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);
        return visitBuiltInTypeNode(td, data, td.typeKind);
    }

    private BType resolveTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod,
                                  int depth, BLangTypeDefinition defn) {
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();

        if (typeKind == TypeKind.MAP) {
            return resolveMapTypeDesc(td, symEnv, mod, depth, defn);
        } else if (typeKind == TypeKind.XML) {
            return resolveXmlTypeDesc(td, symEnv, mod, depth, defn);
        }

        BType type = resolveTypeDesc(symEnv, mod, defn, depth + 1, td.type);
        BType constraintType = resolveTypeDesc(symEnv, mod, defn, depth + 1, td.constraint);
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            return symTable.noType;
        }

        BType constrainedType;
        switch (typeKind) {
            case FUTURE:
                constrainedType = new BFutureType(TypeTags.FUTURE, constraintType, null);
                break;
            case TYPEDESC:
                constrainedType = new BTypedescType(constraintType, null);
                break;
            default:
                return symTable.neverType;
        }

        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, typeSymbol.pkgID, constrainedType, typeSymbol.owner,
                td.pos, SOURCE);
        symResolver.markParameterizedType(constrainedType, constraintType);
        td.setBType(constrainedType);
        return constrainedType;
    }

    private BType resolveXmlTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getImmutableType();
        }

        BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.type);
        BType constrainedType = new BXMLType(null, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, typeSymbol.pkgID, constrainedType, typeSymbol.owner,
                td.pos, SOURCE);

        BTypeDefinition defn = new BTypeDefinition(constrainedType);
        td.defn = defn;
        td.type.setBType(constrainedType);

        BType constraintType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.constraint);

        if (constraintType == symTable.noType) {
            return symTable.noType;
        }

        if (constraintType.tag == TypeTags.PARAMETERIZED_TYPE) {
            BType typedescType = ((BParameterizedType) constraintType).paramSymbol.type;
            BType typedescConstraint = ((BTypedescType) typedescType).constraint;
            symResolver.validateXMLConstraintType(typedescConstraint, td.pos);
        } else {
            symResolver.validateXMLConstraintType(constraintType, td.pos);
        }

        symResolver.markParameterizedType(constrainedType, constraintType);
        return constrainedType;
    }

    private BType resolveMapTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                     BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getMutableType();
        }

        BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.type);
        BTypeSymbol typeSymbol = type.tsymbol;
        BTypeSymbol tSymbol = Symbols.createTypeSymbol(TypeTags.MAP, typeSymbol.flags, Names.EMPTY,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner,
                td.pos, SOURCE);
        BType constrainedType = new BMapType(TypeTags.MAP, null, tSymbol);
        BTypeDefinition defn = new BTypeDefinition(constrainedType);
        td.defn = defn;
        td.type.setBType(constrainedType);

        BType constraintType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.constraint);
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            return symTable.noType;
        }

        ((BMapType) constrainedType).constraint = constraintType;
        symResolver.markParameterizedType(constrainedType, constraintType);
        return constrainedType;
    }

    private BType resolveTypeDesc(BLangArrayType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getMutableType();
        }

        BType resultType = null;
        boolean isError = false;
        BArrayType firstDimArrType = null;
        boolean firstDim = true;

        for (int i = 0; i < td.dimensions; i++) {
            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                    symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, SOURCE);
            BArrayType arrType;
            if (td.sizes.size() == 0) {
                arrType = new BArrayType(resultType, arrayTypeSymbol);
            } else {
                BLangExpression size = td.sizes.get(i);
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
                    arrType = new BArrayType(resultType, arrayTypeSymbol, sizeIndicator, arrayState);
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

                    BSymbol sizeSymbol = symResolver.lookupMainSpaceSymbolInPackage(size.pos, symEnv, pkgAlias, typeName);
                    sizeReference.symbol = sizeSymbol;

                    if (symTable.notFoundSymbol == sizeSymbol) {
                        dlog.error(td.pos, DiagnosticErrorCode.UNDEFINED_SYMBOL, size);
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
                    if (lengthCheck > symResolver.MAX_ARRAY_SIZE) {
                        length = 0;
                        dlog.error(size.pos,
                                DiagnosticErrorCode.ARRAY_LENGTH_GREATER_THAT_2147483637_NOT_YET_SUPPORTED);
                    } else if (lengthCheck < 0) {
                        length = 0;
                        dlog.error(size.pos, DiagnosticErrorCode.INVALID_ARRAY_LENGTH);
                    } else {
                        length = (int) lengthCheck;
                    }
                    arrType = new BArrayType(resultType, arrayTypeSymbol, length, BArrayState.CLOSED);
                }
            }
            arrayTypeSymbol.type = arrType;
            resultType = arrayTypeSymbol.type;
            if (firstDim) {
                firstDimArrType = arrType;
                firstDim = false;
                continue;
            }
            symResolver.markParameterizedType(arrType, arrType.eType);
        }

//        BIntersectionType immutableType = new BIntersectionType(null, null, null);

        BTypeDefinition defn = new BTypeDefinition(resultType);
//        defn.setImmutableType(immutableType);
        td.defn = defn;
        td.setBType(resultType);

        if (isError) {
            resultType = symTable.semanticError;
        }

        firstDimArrType.eType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.elemtype);
        symResolver.markParameterizedType(firstDimArrType, firstDimArrType.eType);
        if (resultType == symTable.noType) {
            return resultType;
        }

        return resultType;
    }

    private BType resolveTypeDesc(BLangTupleTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getMutableType();
        }

        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, SOURCE);
        List<BType> memberTypes = new ArrayList<>();
        BTupleType tupleType = new BTupleType(tupleTypeSymbol, memberTypes);
        tupleTypeSymbol.type = tupleType;
        BTypeDefinition defn = new BTypeDefinition(tupleType);
//        BIntersectionType immutableType = new BIntersectionType(null, null, null);
//        defn.setImmutableType(immutableType);
        td.defn = defn;
        td.setBType(tupleType);

        for (BLangType memberTypeNode : td.memberTypeNodes) {
            BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, memberTypeNode);
            memberTypes.add(type);
        }

        if (td.restParamType != null) {
            BType tupleRestType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.restParamType);
            tupleType.restType = tupleRestType;
            symResolver.markParameterizedType(tupleType, tupleType.restType);
        }

        symResolver.markParameterizedType(tupleType, memberTypes);
        return tupleType;
    }

    private BType resolveTypeDesc(BLangRecordTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getMutableType();
        }

        EnumSet<Flag> flags = td.isAnonymous ? EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS)
                : EnumSet.noneOf(Flag.class);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos,
                td.isAnonymous ? VIRTUAL : SOURCE);
        BRecordType recordType = new BRecordType(recordSymbol);
        recordSymbol.type = recordType;
        td.symbol = recordSymbol;
        BTypeDefinition defn = new BTypeDefinition(recordType);
        td.defn = defn;
        td.setBType(recordType);

        if (symEnv.node.getKind() != NodeKind.PACKAGE) {
            recordSymbol.name = names.fromString(
                    anonymousModelHelper.getNextAnonymousTypeKey(symEnv.enclPkg.packageID));
            symEnter.defineSymbol(td.pos, td.symbol, symEnv);
            symEnter.defineNode(td, symEnv);
        }

        for (BLangSimpleVariable field : td.fields) {
            resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, field.typeNode);
        }

        if (td.getRestFieldType() != null) {
            resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.restFieldType);
        }

        for (BLangType refType : td.typeRefs) {
            resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, refType);
        }

        definetypeDefinition(typeDefinition, recordType, symEnv); // swj: define symbols, set flags ...
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefinition);
        defineFieldsOftypeDefinition(typeDefinition, symEnv);

        return recordType;
    }

    private BType resolveTypeDesc(BLangObjectTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        EnumSet<Flag> flags = EnumSet.copyOf(td.flagSet);
        if (td.isAnonymous) {
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
                symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, SOURCE);

        BObjectType objectType = new BObjectType(objectSymbol, typeFlags);

        objectSymbol.type = objectType;
        td.symbol = objectSymbol;
        td.setBType(objectType);

        definetypeDefinition(typeDefinition, objectType, symEnv); // swj: define symbols, set flags ...
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefinition);
        defineFieldsOftypeDefinition(typeDefinition, symEnv);

        return objectType;
    }

    private void defineFieldsOftypeDefinition(BLangNode typeDefOrObject, SymbolEnv symEnv) {
        // Temporarily
        List<BLangNode> typeAndClassDefs = new ArrayList<>();
        typeAndClassDefs.add(typeDefOrObject);
        symEnter.defineFields(typeAndClassDefs, symEnv);
        symEnter.populateTypeToTypeDefMap(typeAndClassDefs);
        symEnter.defineDependentFields(typeAndClassDefs, symEnv);
    }

    private BType resolveTypeDesc(BLangFunctionTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getMutableType();
        }

        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);
        List<BLangVariable> params = td.getParams();
        Location pos = td.pos;
        BLangType returnTypeNode = td.returnTypeNode;
        BType invokableType = createInvokableType(params, td.restParam, returnTypeNode, data,
                Flags.asMask(td.flagSet), data.env, pos, symEnv, mod, depth, typeDefinition);
        return symResolver.validateInferTypedescParams(pos, params, returnTypeNode == null ?
                null : returnTypeNode.getBType()) ? invokableType : symTable.semanticError;
    }

    public BType createInvokableType(List<? extends BLangVariable> paramVars,
                                     BLangVariable restVariable,
                                     BLangType retTypeVar,
                                     SymbolResolver.AnalyzerData data,
                                     long flags,
                                     SymbolEnv env,
                                     Location location, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                     BLangTypeDefinition typeDefinition) {
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
            BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, param.getTypeNode());
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

        BType retType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, retTypeVar);//resolveTypeNode(retTypeVar, data, env);
        if (retType == symTable.noType) {
            return symTable.noType;
        }

        BVarSymbol restParam = null;
        BType restType = null;

        if (restVariable != null) {
            restType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, restVariable.typeNode);//resolveTypeNode(restVariable.typeNode, data, env);
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
        symResolver.markParameterizedType(bInvokableType, allConstituentTypes);

        return bInvokableType;
    }

    private BType resolveTypeDesc(BLangErrorType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition defn) {
        if (td.detailType == null) {
            return symTable.errorType;
        }

        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);

        BType detailType = Optional.ofNullable(td.detailType)
                .map(bLangType -> resolveTypeDesc(symEnv, mod, defn, depth, bLangType)).orElse(symTable.detailType);

        if (td.isAnonymous) {
            td.flagSet.add(Flag.PUBLIC);
            td.flagSet.add(Flag.ANONYMOUS);
        }

        // The builtin error type
        BErrorType bErrorType = symTable.errorType;

        boolean distinctErrorDef = td.flagSet.contains(Flag.DISTINCT);
        if (detailType == symTable.detailType && !distinctErrorDef &&
                !data.env.enclPkg.packageID.equals(PackageID.ANNOTATIONS)) {
            return bErrorType;
        }

        // Define user define error type.
        BErrorTypeSymbol errorTypeSymbol = Symbols.createErrorSymbol(Flags.asMask(td.flagSet),
                Names.EMPTY, data.env.enclPkg.packageID, null, data.env.scope.owner, td.pos, SOURCE);

        PackageID packageID = data.env.enclPkg.packageID;
        if (data.env.node.getKind() != NodeKind.PACKAGE) {
            errorTypeSymbol.name = names.fromString(
                    anonymousModelHelper.getNextAnonymousTypeKey(packageID));
            symEnter.defineSymbol(td.pos, errorTypeSymbol, data.env);
        }

        BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;

        symResolver.markParameterizedType(errorType, detailType);

        errorType.typeIdSet = BTypeIdSet.emptySet();

        if (td.isAnonymous && td.flagSet.contains(Flag.DISTINCT)) {
            errorType.typeIdSet.add(
                    BTypeIdSet.from(packageID, anonymousModelHelper.getNextAnonymousTypeId(packageID), true));
        }

        return errorType;
    }

    private BType resolveTypeDesc(BLangUnionTypeNode td, SymbolEnv symEnv,
                                  Map<String, BLangNode> mod, int depth, BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            td.defn.getMutableType();
        }

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, SOURCE);
        BUnionType unionType = new BUnionType(unionTypeSymbol, memberTypes, false, false);
        unionTypeSymbol.type = unionType;
        BTypeDefinition defn = new BTypeDefinition(unionType);
        td.defn = defn;
        td.setBType(unionType);

        for (BLangType langType : td.memberTypeNodes) {
            BType resolvedType = resolveTypeDesc(symEnv, mod, typeDefinition, depth, langType);
            if (resolvedType == symTable.noType) {
                return symTable.noType;
            }
            if (resolvedType.isNullable()) {
                unionType.setNullable(true);
            }
            memberTypes.add(resolvedType);
        }

        symResolver.markParameterizedType(unionType, memberTypes);
        return unionType;
    }

    private BType resolveTypeDesc(BLangIntersectionTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        List<BLangType> constituentTypeNodes = td.constituentTypeNodes;

        BTypeSymbol intersectionSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, SOURCE);
        BIntersectionType intersectionType = new BIntersectionType(intersectionSymbol);
        intersectionSymbol.type = intersectionType;
        BTypeDefinition defn = new BTypeDefinition(intersectionType);
        td.defn = defn;
        LinkedHashSet<BType> constituentTypes = new LinkedHashSet<>();

        for (BLangType typeNode : constituentTypeNodes) {
            BType constituentType = resolveTypeDesc(symEnv, mod, typeDefinition, depth, typeNode);
            constituentTypes.add(constituentType);
        }
        intersectionType.setConstituentTypes(constituentTypes);
        intersectionTypeList.add(intersectionType);
        return intersectionType;

//        Map<BType, BLangType> typeBLangTypeMap = new HashMap<>();
//
//        boolean validIntersection = true;
//        boolean isErrorIntersection = false;
//        boolean isAlreadyExistingType = false;
//
//        BLangType bLangTypeOne = constituentTypeNodes.get(0);
//        BType typeOne = resolveTypeDesc(symEnv, mod, typeDefinition, depth, bLangTypeOne);
//        if (typeOne == symTable.noType) {
//            return symTable.noType;
//        }
//
//        typeBLangTypeMap.put(typeOne, bLangTypeOne);
//
//        BLangType bLangTypeTwo = constituentTypeNodes.get(1);
//        BType typeTwo = resolveTypeDesc(symEnv, mod, typeDefinition, depth, bLangTypeTwo);
//        if (typeTwo == symTable.noType) {
//            return symTable.noType;
//        }
//
//        BType typeOneReference = Types.getReferredType(typeOne);
//        BType typeTwoReference = Types.getReferredType(typeTwo);
//
//        typeBLangTypeMap.put(typeTwo, bLangTypeTwo);
//
//        boolean hasReadOnlyType = typeOneReference == symTable.readonlyType
//                || typeTwoReference == symTable.readonlyType;
//
//        if (typeOneReference.tag == TypeTags.ERROR || typeTwoReference.tag == TypeTags.ERROR) {
//            isErrorIntersection = true;
//        }
//
//        if (!(hasReadOnlyType || isErrorIntersection)) {
//            dlog.error(td.pos,
//                    DiagnosticErrorCode.UNSUPPORTED_TYPE_INTERSECTION, td);
//            return symTable.semanticError;
//        }
//
//        BType potentialIntersectionType = symResolver.getPotentialIntersection(
//                Types.IntersectionContext.from(dlog, bLangTypeOne.pos, bLangTypeTwo.pos),
//                typeOne, typeTwo, symEnv);
//        if (typeOne == potentialIntersectionType || typeTwo == potentialIntersectionType) {
//            isAlreadyExistingType = true;
//        }
//
//        LinkedHashSet<BType> constituentBTypes = new LinkedHashSet<>();
//        constituentBTypes.add(typeOne);
//        constituentBTypes.add(typeTwo);
//
//        if (potentialIntersectionType == symTable.semanticError) {
//            validIntersection = false;
//        } else {
//            for (int i = 2; i < constituentTypeNodes.size(); i++) {
//                BLangType bLangType = constituentTypeNodes.get(i);
//                BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth, bLangType);
//                if (type.tag == TypeTags.ERROR) {
//                    isErrorIntersection = true;
//                }
//                typeBLangTypeMap.put(type, bLangType);
//
//                if (!hasReadOnlyType) {
//                    hasReadOnlyType = type == symTable.readonlyType;
//                }
//
//                if (type == symTable.noType) {
//                    return symTable.noType;
//                }
//
//                BType tempIntersectionType = symResolver.getPotentialIntersection(
//                        Types.IntersectionContext.from(dlog, bLangTypeOne.pos, bLangTypeTwo.pos),
//                        potentialIntersectionType, type, symEnv);
//                if (tempIntersectionType == symTable.semanticError) {
//                    validIntersection = false;
//                    break;
//                }
//
//                if (type == tempIntersectionType) {
//                    potentialIntersectionType = type;
//                    isAlreadyExistingType = true;
//                } else if (potentialIntersectionType != tempIntersectionType) {
//                    potentialIntersectionType = tempIntersectionType;
//                    isAlreadyExistingType = false;
//                }
//                constituentBTypes.add(type);
//            }
//        }
//
//        if (!validIntersection) {
//            dlog.error(td.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, td);
//            return symTable.semanticError;
//        }
//
//        if (isErrorIntersection && !isAlreadyExistingType) {
//            BType detailType = ((BErrorType) potentialIntersectionType).detailType;
//
//            boolean existingErrorDetailType = false;
//            if (detailType.tsymbol != null) {
//                BSymbol detailTypeSymbol = symResolver.lookupSymbolInMainSpace(symEnv, detailType.tsymbol.name);
//                if (detailTypeSymbol != symTable.notFoundSymbol) {
//                    existingErrorDetailType = true;
//                }
//            }
//
//            return symResolver.createIntersectionErrorType((BErrorType) potentialIntersectionType, td.pos,
//                    constituentBTypes, existingErrorDetailType, symEnv);
//        }
//
//        if (types.isInherentlyImmutableType(potentialIntersectionType) ||
//                (Symbols.isFlagOn(potentialIntersectionType.flags, Flags.READONLY) &&
//                        // For objects, a new type has to be created.
//                        !types.isSubTypeOfBaseType(potentialIntersectionType, TypeTags.OBJECT))) {
//            return potentialIntersectionType;
//        }
//
//        PackageID packageID = symEnv.enclPkg.packageID;
//        if (!types.isSelectivelyImmutableType(potentialIntersectionType, false, packageID)) {
//            if (types.isSelectivelyImmutableType(potentialIntersectionType, packageID)) {
//                // This intersection would have been valid if not for `readonly object`s.
//                dlog.error(td.pos, DiagnosticErrorCode.INVALID_READONLY_OBJECT_INTERSECTION_TYPE);
//            } else {
//                dlog.error(td.pos, DiagnosticErrorCode.INVALID_READONLY_INTERSECTION_TYPE,
//                        potentialIntersectionType);
//            }
//            return symTable.semanticError;
//        }
//
//        BLangType typeNode = typeBLangTypeMap.get(potentialIntersectionType);
//        Set<Flag> flagSet;
//        if (typeNode == null) {
//            flagSet = new HashSet<>();
//        } else if (typeNode.getKind() == NodeKind.OBJECT_TYPE) {
//            flagSet = ((BLangObjectTypeNode) typeNode).flagSet;
//        } else if (typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
//            flagSet = typeNode.flagSet;
//        } else {
//            flagSet = new HashSet<>();
//        }
//        return ImmutableTypeCloner.getImmutableIntersectionType(td.pos, types,
//                potentialIntersectionType,
//                symEnv, symTable, anonymousModelHelper, names, flagSet);
    }

    private BType resolveTypeDesc(BLangUserDefinedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth) {
        String name = td.typeName.value;
        // Need to replace this with a real package lookup
        if (td.pkgAlias.value.equals("int")) {
            return resolveIntSubtype(name);
        } else if (td.pkgAlias.value.equals("string") && name.equals("Char")) {
            return symTable.charStringType;
        } else if (td.pkgAlias.value.equals("xml")) {
            return resolveXmlSubtype(name);
        }

        BType type;

        // 1) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        //    if not use the current package scope.
        // 2) lookup the typename in the package scope returned from step 1.
        // 3) If the symbol is not found, then lookup in the root scope. e.g. for types such as 'error'
        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);

        Name pkgAlias = names.fromIdNode(td.pkgAlias);
        Name typeName = names.fromIdNode(td.typeName);
        BSymbol symbol = symTable.notFoundSymbol;

        // 1) Resolve ANNOTATION type if and only current scope inside ANNOTATION definition.
        // Only valued types and ANNOTATION type allowed.
        if (symEnv.scope.owner.tag == SymTag.ANNOTATION) {
            symbol = symResolver.lookupAnnotationSpaceSymbolInPackage(td.pos, symEnv, pkgAlias, typeName);
        }

        // 2) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        if (symbol == symTable.notFoundSymbol) {
            BSymbol tempSymbol = symResolver.lookupMainSpaceSymbolInPackage(td.pos, symEnv, pkgAlias, typeName);

            BSymbol refSymbol = tempSymbol.tag == SymTag.TYPE_DEF ? Types.getReferredType(tempSymbol.type).tsymbol
                    : tempSymbol;
            if ((refSymbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                symbol = tempSymbol;
            } else if (Symbols.isTagOn(refSymbol, SymTag.VARIABLE) && symEnv.node.getKind() == NodeKind.FUNCTION) {
                BLangFunction func = (BLangFunction) symEnv.node;
                boolean errored = false;

                if (func.returnTypeNode == null ||
                        (func.hasBody() && func.body.getKind() != NodeKind.EXTERN_FUNCTION_BODY)) {
                    dlog.error(td.pos,
                            DiagnosticErrorCode.INVALID_NON_EXTERNAL_DEPENDENTLY_TYPED_FUNCTION);
                    errored = true;
                }

                if (tempSymbol.type != null &&
                        Types.getReferredType(tempSymbol.type).tag != TypeTags.TYPEDESC) {
                    dlog.error(td.pos, DiagnosticErrorCode.INVALID_PARAM_TYPE_FOR_RETURN_TYPE,
                            tempSymbol.type);
                    errored = true;
                }

                if (errored) {
                    return symTable.semanticError;
                }

                SymbolResolver.ParameterizedTypeInfo parameterizedTypeInfo =
                        symResolver.getTypedescParamValueType(func.requiredParams, data, refSymbol);
                BType paramValType = parameterizedTypeInfo == null ? null : parameterizedTypeInfo.paramValueType;

                if (paramValType == symTable.semanticError) {
                    return symTable.semanticError;
                }

                if (paramValType != null) {
                    BTypeSymbol tSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PARAMETERIZED | tempSymbol.flags,
                            tempSymbol.name, tempSymbol.originalName, tempSymbol.pkgID,
                            null, func.symbol, tempSymbol.pos, VIRTUAL);
                    tSymbol.type = new BParameterizedType(paramValType, (BVarSymbol) tempSymbol,
                            tSymbol, tempSymbol.name, parameterizedTypeInfo.index);
                    tSymbol.type.flags |= Flags.PARAMETERIZED;

                    td.symbol = tSymbol;
                    return tSymbol.type;
                }
            }
        }

        if (symbol == symTable.notFoundSymbol) {
            // 3) Lookup the root scope for types such as 'error'
            symbol = symResolver.lookupMemberSymbol(td.pos, symTable.rootScope, symEnv, typeName,
                    SymTag.VARIABLE_NAME);
        }

        if (symEnv.logErrors && symbol == symTable.notFoundSymbol) {
            if (!missingNodesHelper.isMissingNode(pkgAlias) && !missingNodesHelper.isMissingNode(typeName) &&
                    !symEnter.isUnknownTypeRef(td)) {
                dlog.error(td.pos, data.diagCode, typeName);
            }
            return symTable.semanticError;
        }

        td.symbol = symbol;

        if (symbol.kind == SymbolKind.TYPE_DEF && !Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
            BType referenceType = ((BTypeDefinitionSymbol) symbol).referenceType;
            referenceType.flags |= symbol.type.flags;
            referenceType.tsymbol.flags |= symbol.type.flags;
            return referenceType;
        }

        type = symbol.type;

        if (type.getKind() != TypeKind.OTHER) {
            return type;
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            dlog.error(td.pos, DiagnosticErrorCode.UNKNOWN_TYPE, td.typeName);
            return null;
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            BLangTypeDefinition typeDefinition = (BLangTypeDefinition) moduleLevelDef;
            BType resolvedType = resolveTypeDefinition(symEnv, mod, typeDefinition, depth);
//            symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefinition); // temporary disable due to intersections
            return resolvedType;
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            return resolveTypeDefinition(symEnv, mod, constant.associatedTypeDefinition, depth);
        } else {
            throw new AssertionError();
        }
    }

    private BType resolveTypeDesc(BLangBuiltInRefTypeNode td, SymbolEnv symEnv) {
        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);
        return visitBuiltInTypeNode(td, data, td.typeKind);
    }

    private BType resolveSingletonType(BLangFiniteTypeNode td, SymbolEnv symEnv) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, SOURCE);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        for (BLangExpression literal : td.valueSpace) {
            literal.setBType(symTable.getTypeFromTag(literal.getBType().tag));
            finiteType.addValue(literal);
        }
        finiteTypeSymbol.type = finiteType;
        td.setBType(finiteType);
        return finiteType;
    }

    private BType resolveTypeDesc(BLangTableTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth, BLangTypeDefinition typeDefinition) {
//        SemType memberType =
//                resolveTypeDesc(semtypeEnv, mod, (BLangTypeDefinition) td.constraint.defn, depth, td.constraint);
//        return SemTypes.tableContaining(memberType);

        BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.type);//resolveTypeNode(td.type, data, data.env);
        BType constraintType =
                resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.constraint);// resolveTypeNode(td.constraint, data, data.env);
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            return symTable.noType;
        }

        BTableType tableType = new BTableType(TypeTags.TABLE, constraintType, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        tableType.tsymbol = Symbols.createTypeSymbol(SymTag.TYPE, Flags.asMask(EnumSet.noneOf(Flag.class)),
                typeSymbol.name, typeSymbol.originalName, typeSymbol.pkgID,
                tableType, symEnv.scope.owner, td.pos, SOURCE);
        tableType.tsymbol.flags = typeSymbol.flags;
        tableType.constraintPos = td.constraint.pos;
        tableType.isTypeInlineDefined = td.isTypeInlineDefined;

        if (td.tableKeyTypeConstraint != null) {
            tableType.keyTypeConstraint = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.tableKeyTypeConstraint.keyType);//resolveTypeNode(td.tableKeyTypeConstraint.keyType, data, symEnv);
            tableType.keyPos = td.tableKeyTypeConstraint.pos;
        } else if (td.tableKeySpecifier != null) {
            BLangTableKeySpecifier tableKeySpecifier = td.tableKeySpecifier;
            List<String> fieldNameList = new ArrayList<>();
            for (IdentifierNode identifier : tableKeySpecifier.fieldNameIdentifierList) {
                fieldNameList.add(((BLangIdentifier) identifier).value);
            }
            tableType.fieldNameList = fieldNameList;
            tableType.keyPos = tableKeySpecifier.pos;
        }

        if (Types.getReferredType(constraintType).tag == TypeTags.MAP &&
                (!tableType.fieldNameList.isEmpty() || tableType.keyTypeConstraint != null) &&
                !tableType.tsymbol.owner.getFlags().contains(Flag.LANG_LIB)) {
            dlog.error(tableType.keyPos,
                    DiagnosticErrorCode.KEY_CONSTRAINT_NOT_SUPPORTED_FOR_TABLE_WITH_MAP_CONSTRAINT);
            return symTable.semanticError;
        }

        symResolver.markParameterizedType(tableType, constraintType);
        td.tableType = tableType;

        return tableType;
    }

    private BType resolveTypeDesc(BLangStreamType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                  BLangTypeDefinition typeDefinition) {
        BType type = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.type);
        BType constraintType = resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.constraint);
        BType error = td.error != null ?
                resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.error) : symTable.nilType;
        // If the constrained type is undefined, return noType as the type.
        if (constraintType == symTable.noType) {
            return symTable.noType;
        }

        BType streamType = new BStreamType(TypeTags.STREAM, constraintType, error, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        streamType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, typeSymbol.pkgID, streamType,
                symEnv.scope.owner, td.pos, SOURCE);

        symResolver.markParameterizedType(streamType, constraintType);
        if (error != null) {
            symResolver.markParameterizedType(streamType, error);
        }

        return streamType;
    }

    private BType resolveIntSubtype(String name) {
        switch (name) {
            case "Signed8":
                return symTable.signed8IntType;
            case "Signed16":
                return symTable.signed16IntType;
            case "Signed32":
                return symTable.signed32IntType;
            case "Unsigned8":
                return symTable.unsigned8IntType;
            case "Unsigned16":
                return symTable.unsigned16IntType;
            case "Unsigned32":
                return symTable.unsigned32IntType;
            default:
                throw new IllegalStateException("Unknown int subtype: " + name);
        }
    }

    private BType resolveXmlSubtype(String name) {
        switch(name) {
            case "Element":
                return symTable.xmlElementType;
            case "Comment":
                return symTable.xmlCommentType;
            case "Text":
                return symTable.xmlTextType;
            case "ProcessingInstruction":
                return symTable.xmlPIType;
            default:
                throw new IllegalStateException("Unknown XML subtype: " + name);
        }
    }

    public BType visitBuiltInTypeNode(BLangType typeNode, SymbolResolver.AnalyzerData data, TypeKind typeKind) {
        Name typeName = names.fromTypeKind(typeKind);
        BSymbol typeSymbol = symResolver.lookupMemberSymbol(typeNode.pos, symTable.rootScope, data.env, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            dlog.error(typeNode.pos, data.diagCode, typeName);
        }

        typeNode.setBType(typeSymbol.type);
        return typeSymbol.type;
    }

    private int constExprToInt(BLangExpression t) {
        if (t.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BConstantSymbol symbol = (BConstantSymbol) ((BLangSimpleVarRef) t).symbol;
            return ((Long) symbol.value.value).intValue();
        }
        return (int) ((BLangLiteral) t).value;
    }

    public void definetypeDefinition(BLangTypeDefinition typeDefinition, BType resolvedType, SymbolEnv env) {
        if (resolvedType == symTable.semanticError) {
            // TODO : Fix this properly. issue #21242

            symEnter.invalidateAlreadyDefinedErrorType(typeDefinition);
            return;
        }

        if (resolvedType == symTable.semanticError || resolvedType == symTable.noType) {
            return;
        }

//        if (definedType == symTable.noType) {
//            // This is to prevent concurrent modification exception.
//            if (!this.unresolvedTypes.contains(typeDefinition)) {
//                this.unresolvedTypes.add(typeDefinition);
//            }
//            return;
//        }

        if (resolvedType == null) {
            return;
        }

        // Check for any circular type references
        boolean hasTypeInclusions = false;
        NodeKind typeNodeKind = typeDefinition.typeNode.getKind();
        if (typeNodeKind == NodeKind.OBJECT_TYPE || typeNodeKind == NodeKind.RECORD_TYPE) {
            if (resolvedType.tsymbol.scope == null) {
                resolvedType.tsymbol.scope = new Scope(resolvedType.tsymbol);
            }
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
            // For each referenced type, check whether the types are already resolved.
            // If not, then that type should get a higher precedence.
//            for (BLangType typeRef : structureTypeNode.typeRefs) {
//                hasTypeInclusions = true;
//                BType referencedType = symResolver.resolveTypeNode(typeRef, env);
//                if (referencedType == symTable.noType) {
//                    if (!this.unresolvedTypes.contains(typeDefinition)) {
//                        this.unresolvedTypes.add(typeDefinition);
//                        return;
//                    }
//                }
//            }
        }

        // check for unresolved fields. This record may be referencing another record
//        if (hasTypeInclusions && !this.resolveRecordsUnresolvedDueToFields && typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
//            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
//            for (BLangSimpleVariable variable : structureTypeNode.fields) {
//                Scope scope = new Scope(structureTypeNode.symbol);
//                structureTypeNode.symbol.scope = scope;
//                SymbolEnv typeEnv = SymbolEnv.createTypeEnv(structureTypeNode, scope, env);
//                BType referencedType = symResolver.resolveTypeNode(variable.typeNode, typeEnv);
//                if (referencedType == symTable.noType) {
//                    if (this.unresolvedRecordDueToFields.add(typeDefinition) &&
//                            !this.unresolvedTypes.contains(typeDefinition)) {
//                        this.unresolvedTypes.add(typeDefinition);
//                        return;
//                    }
//                }
//            }
//        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            resolvedType.tsymbol = symEnter.createEnumSymbol(typeDefinition, resolvedType);
        }

//        typeDefinition.setPrecedence(this.typePrecedence++);

        BSymbol typeDefSymbol = Symbols.createTypeDefinitionSymbol(Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), env.enclPkg.packageID, resolvedType, env.scope.owner,
                typeDefinition.name.pos, symEnter.getOrigin(typeDefinition.name.value));
        typeDefSymbol.markdownDocumentation = symEnter.getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_REF, typeDefSymbol.flags, typeDefSymbol.name,
                typeDefSymbol.pkgID, typeDefSymbol.type, typeDefSymbol.owner, typeDefSymbol.pos, typeDefSymbol.origin);
        typeSymbol.markdownDocumentation = typeDefSymbol.markdownDocumentation;
        ((BTypeDefinitionSymbol) typeDefSymbol).referenceType = new BTypeReferenceType(resolvedType, typeSymbol,
                typeDefSymbol.type.flags);

        boolean isLabel = true;
        //todo remove after type ref introduced to runtime
        if (resolvedType.tsymbol.name == Names.EMPTY) {
            isLabel = false;
            resolvedType.tsymbol.name = names.fromIdNode(typeDefinition.name);
            resolvedType.tsymbol.originalName = names.fromIdNode(typeDefinition.name);
            resolvedType.tsymbol.flags |= typeDefSymbol.flags;

            resolvedType.tsymbol.markdownDocumentation = typeDefSymbol.markdownDocumentation;
            resolvedType.tsymbol.pkgID = env.enclPkg.packageID;
            if (resolvedType.tsymbol.tag == SymTag.ERROR) {
                resolvedType.tsymbol.owner = env.scope.owner;
            }
        }

        if ((((resolvedType.tsymbol.kind == SymbolKind.OBJECT
                && !Symbols.isFlagOn(resolvedType.tsymbol.flags, Flags.CLASS))
                || resolvedType.tsymbol.kind == SymbolKind.RECORD))
                && ((BStructureTypeSymbol) resolvedType.tsymbol).typeDefinitionSymbol == null) {
            ((BStructureTypeSymbol) resolvedType.tsymbol).typeDefinitionSymbol = (BTypeDefinitionSymbol) typeDefSymbol;
        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            typeDefSymbol = resolvedType.tsymbol;
            typeDefSymbol.pos = typeDefinition.name.pos;
        }

        boolean isErrorIntersection = symEnter.isErrorIntersection(resolvedType);
        if (isErrorIntersection) {
            symEnter.populateSymbolNameOfErrorIntersection(resolvedType, typeDefinition.name.value);
            symEnter.populateAllReadyDefinedErrorIntersection(resolvedType, typeDefinition, env);
        }

        BType referenceConstraintType = Types.getReferredType(resolvedType);
        boolean isIntersectionType = referenceConstraintType.tag == TypeTags.INTERSECTION && !isLabel;

        BType effectiveDefinedType = isIntersectionType ? ((BIntersectionType) referenceConstraintType).effectiveType :
                referenceConstraintType;

        boolean isIntersectionTypeWithNonNullEffectiveTypeSymbol =
                isIntersectionType && effectiveDefinedType != null && effectiveDefinedType.tsymbol != null;

        if (isIntersectionTypeWithNonNullEffectiveTypeSymbol) {
            BTypeSymbol effectiveTypeSymbol = effectiveDefinedType.tsymbol;
            effectiveTypeSymbol.name = typeDefSymbol.name;
            effectiveTypeSymbol.pkgID = typeDefSymbol.pkgID;
        }

        symEnter.handleDistinctDefinition(typeDefinition, typeDefSymbol, resolvedType, referenceConstraintType);

        typeDefSymbol.flags |= Flags.asMask(typeDefinition.flagSet);
        // Reset public flag when set on a non public type.
        typeDefSymbol.flags &= symEnter.getPublicFlagResetingMask(typeDefinition.flagSet, typeDefinition.typeNode);
        if (symEnter.isDeprecated(typeDefinition.annAttachments)) {
            typeDefSymbol.flags |= Flags.DEPRECATED;
        }

        // Reset origin for anonymous types
        if (Symbols.isFlagOn(typeDefSymbol.flags, Flags.ANONYMOUS)) {
            typeDefSymbol.origin = VIRTUAL;
        }

        if (typeDefinition.annAttachments.stream()
                .anyMatch(attachment -> attachment.annotationName.value.equals(Names.ANNOTATION_TYPE_PARAM.value))) {
            // TODO : Clean this. Not a nice way to handle this.
            //  TypeParam is built-in annotation, and limited only within lang.* modules.
            if (PackageID.isLangLibPackageID(env.enclPkg.packageID)) {
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol.type, typeDefSymbol.name);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
            } else {
                dlog.error(typeDefinition.pos, DiagnosticErrorCode.TYPE_PARAM_OUTSIDE_LANG_MODULE);
            }
        }
        resolvedType.flags |= typeDefSymbol.flags;

        if (isIntersectionTypeWithNonNullEffectiveTypeSymbol) {
            BTypeSymbol effectiveTypeSymbol = effectiveDefinedType.tsymbol;
            effectiveTypeSymbol.flags |= resolvedType.tsymbol.flags;
            effectiveTypeSymbol.origin = VIRTUAL;
            effectiveDefinedType.flags |= resolvedType.flags;
        }

        typeDefinition.symbol = typeDefSymbol;

//        if (typeDefinition.hasCyclicReference) {
            // Workaround for https://github.com/ballerina-platform/ballerina-lang/issues/29742
//            typeDefinition.getBType().tsymbol = resolvedType.tsymbol;
//        } else {
        boolean isLanglibModule = PackageID.isLangLibPackageID(env.enclPkg.packageID);
        if (isLanglibModule) {
            symEnter.handleLangLibTypes(typeDefinition);
            return;
        }
        // We may have already defined error intersection
        if (!isErrorIntersection || symEnter.lookupTypeSymbol(env, typeDefinition.name) == symTable.notFoundSymbol) {
            symEnter.defineSymbol(typeDefinition.name.pos, typeDefSymbol);
        }
//        }
    }

    public void resolveConstant(SymbolEnv symEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        if (!resolvingConstants.add(constant)) { // To identify cycles.
            dlog.error(constant.pos, DiagnosticErrorCode.CONSTANT_CYCLIC_REFERENCE,
                    (this.resolvingConstants).stream().map(constNode -> constNode.symbol)
                            .collect(Collectors.toList()));
            return;
        }
        if (resolvedConstants.contains(constant)) { // Already resolved constant.
            return;
        }
        defineConstant(symEnv, modTable, constant);
        resolvingConstants.remove(constant);
        resolvedConstants.add(constant);
    }

    private void defineConstant(SymbolEnv symEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        BType staticType = symTable.noType;
        constant.symbol = symEnter.getConstantSymbol(constant);
        BLangTypeDefinition typeDef = constant.associatedTypeDefinition;
        if (typeDef != null) {
            resolveTypeDefinition(symEnv, modTable, typeDef, 0);
        }
        if (constant.typeNode != null) {
            // Type node is available.
            staticType = resolveTypeDesc(symEnv, modTable, typeDef, 0, constant.typeNode);//symResolver.resolveTypeNode(constant.typeNode, symEnv);
        }

        BConstantSymbol constantSymbol = symEnter.getConstantSymbol(constant);
        constant.symbol = constantSymbol;

        ConstantTypeChecker.AnalyzerData data = new ConstantTypeChecker.AnalyzerData();
        data.constantSymbol = constantSymbol;
        data.env = symEnv;
        data.modTable = modTable;
        data.expType = staticType;
        // Type check and resolve the constant expression.
        BType inferredExpType = constantTypeChecker.checkConstExpr(constant.expr, staticType, data);

        BType narrowedType = inferredExpType;
        if (inferredExpType == symTable.semanticError) {
            // Constant expression contains errors.
            return;
        } else if (staticType != symTable.noType) {
            data.diagCode = null;
            // Validate the inferred expr type against static type.
            // Narrow the inferred type according to the static type.
            narrowedType = getConstantValidType.getValidType(staticType, inferredExpType, data);

            if (data.diagCode == DiagnosticErrorCode.AMBIGUOUS_TYPES) {
                dlog.error(constant.expr.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES, staticType);
                return;
            } else if (narrowedType == symTable.semanticError) {
                narrowedType = constantTypeChecker.getNarrowedType(inferredExpType);
                dlog.error(constant.expr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, staticType, narrowedType);
                return;
            }
        }

        // Get immutable type for the narrowed type.
        BType intersectionType = ImmutableTypeCloner.getImmutableType(constant.pos, types, narrowedType, symEnv,
                symEnv.scope.owner.pkgID, symEnv.scope.owner, symTable, anonymousModelHelper, names,
                new HashSet<>());

        // Fix the constant expr types due to tooling requirements.
        resolveConstantExpressionType.resolveConstExpr(constant.expr, intersectionType, data);

        // Update the final type in necessary fields.
        constantSymbol.type = intersectionType;
        constantSymbol.literalType = intersectionType;
        constant.setBType(intersectionType);

        // Get the constant value from the final type.
        constantSymbol.value = constantTypeChecker.getConstantValue(intersectionType);

        if (constantSymbol.type.tag != TypeTags.TYPEREFDESC && typeDef != null) {
            // Update flags.
            constantSymbol.type.tsymbol.flags |= typeDef.symbol.flags;
        }

        constantSymbol.markdownDocumentation = symEnter.getMarkdownDocAttachment(constant.markdownDocumentationAttachment);
        if (symEnter.isDeprecated(constant.annAttachments)) {
            constantSymbol.flags |= Flags.DEPRECATED;
        }
        // Add the symbol to the enclosing scope.
        if (!symResolver.checkForUniqueSymbol(constant.name.pos, symEnv, constantSymbol)) {
            return;
        }

        if (constant.symbol.name == Names.IGNORE) {
            // Avoid symbol definition for constants with name '_'
            return;
        }
        // Add the symbol to the enclosing scope.
        symEnv.scope.define(constantSymbol.name, constantSymbol);
    }
}
