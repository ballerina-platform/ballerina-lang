/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.util.Constants.INFERRED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_ARRAY_INDICATOR;

/**
 * Resolve the types of module-level-constructs.
 *
 * @since 2201.7.0
 */

public class TypeResolver {

    private static final CompilerContext.Key<TypeResolver> TYPE_RESOLVER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private int typePrecedence;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final ConstantTypeChecker constantTypeChecker;
    private final ConstantTypeChecker.ResolveConstantExpressionType resolveConstantExpressionType;
    private final EffectiveTypePopulator effectiveTypePopulator;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

    private List<BLangTypeDefinition> resolvingTypeDefinitions = new ArrayList<>();
    private HashMap<BIntersectionType, BLangIntersectionTypeNode> intersectionTypeList;
    public HashSet<BLangConstant> resolvedConstants = new HashSet<>();
    private ArrayList<BLangConstant> resolvingConstants = new ArrayList<>();
    private Stack<String> resolvingModuleDefs;
    private HashSet<BLangClassDefinition> resolvedClassDef = new HashSet<>();
    private Map<String, BLangNode> modTable = new LinkedHashMap<>();
    private Map<String, BLangConstantValue> constantMap = new HashMap<>();
    private HashSet<LocationData> unknownTypeRefs;
    private SymbolEnv pkgEnv;
    private int currentDepth;
    private Stack<BType> resolvingTypes;
    public HashSet<BStructureType> resolvingStructureTypes = new HashSet<>();

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
        this.constantTypeChecker = ConstantTypeChecker.getInstance(context);
        this.resolveConstantExpressionType = ConstantTypeChecker.ResolveConstantExpressionType.getInstance(context);
        this.effectiveTypePopulator = EffectiveTypePopulator.getInstance(context);
        this.unknownTypeRefs = new HashSet<>();
    }

    public void clearUnknowTypeRefs() {
        unknownTypeRefs.clear();
    }

    private void clear() {
        modTable.clear();
        constantMap.clear();
        resolvingTypeDefinitions.clear();
        resolvedConstants.clear();
        resolvingConstants.clear();
        resolvedClassDef.clear();
        resolvingStructureTypes.clear();
    }

    public static TypeResolver getInstance(CompilerContext context) {
        TypeResolver typeResolver = context.get(TYPE_RESOLVER_KEY);
        if (typeResolver == null) {
            typeResolver = new TypeResolver(context);
        }

        return typeResolver;
    }

    public void defineBTypes(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        this.pkgEnv = pkgEnv;
        typePrecedence = 0;
        for (BLangNode moduleDef : moduleDefs) {
            if (moduleDef.getKind() == NodeKind.XMLNS) {
                continue;
            }
            String typeOrClassName = symEnter.getTypeOrClassName(moduleDef);
            if (!modTable.containsKey(typeOrClassName)) {
                modTable.put(typeOrClassName, moduleDef);
            }
        }

        for (BLangNode def : moduleDefs) {
            resolvingTypes = new Stack<>();
            resolvingModuleDefs = new Stack<>();
            switch (def.getKind()) {
                case CLASS_DEFN -> {
                    intersectionTypeList = new HashMap<>();
                    extracted(pkgEnv, (BLangClassDefinition) def, 0);
                    updateEffectiveTypeOfCyclicIntersectionTypes(pkgEnv);
                }
                case CONSTANT -> resolveConstant(pkgEnv, modTable, (BLangConstant) def);
                case XMLNS -> resolveXMLNS(pkgEnv, (BLangXMLNS) def);
                default -> {
                    BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                    intersectionTypeList = new HashMap<>();
                    resolveTypeDefinition(pkgEnv, modTable, typeDefinition, 0);
                    BType type = typeDefinition.typeNode.getBType();
                    if (typeDefinition.hasCyclicReference) {
                        updateIsCyclicFlag(type);
                    }
                    updateEffectiveTypeOfCyclicIntersectionTypes(pkgEnv);
                }
            }
            resolvingTypes.clear();
            resolvingModuleDefs.clear();
        }
        clear();
    }

    private BType extracted(SymbolEnv pkgEnv, BLangClassDefinition classDefinition, int depth) {
        if (resolvedClassDef.contains(classDefinition)) {
            return classDefinition.getBType();
        }

        String currentDefnName = classDefinition.name.value;
        if (depth == classDefinition.cycleDepth) {
            // We cannot define recursive classDefinitions with same depths.
            logInvalidCyclicReferenceError(currentDefnName, classDefinition.pos);
            return symTable.semanticError;
        }

        currentDepth = depth;
        classDefinition.cycleDepth = depth;
        resolvingModuleDefs.push(currentDefnName);

        if (classDefinition.getBType() != null) {
            return classDefinition.getBType();
        }

        defineClassDef(classDefinition, pkgEnv);
        symEnter.defineDistinctClassAndObjectDefinitionIndividual(classDefinition);

        // Define the class fields
        defineFields(classDefinition, pkgEnv);
        resolvedClassDef.add(classDefinition);
        resolvingModuleDefs.pop();
        BObjectType classDefType = (BObjectType) classDefinition.getBType();
        resolvingStructureTypes.remove(classDefType);

        classDefinition.setPrecedence(this.typePrecedence++);
        return classDefType;
    }

    public void defineFields(BLangNode typeDefNode, SymbolEnv pkgEn) {
        currentDepth++;
        if (typeDefNode.getKind() == NodeKind.CLASS_DEFN) {
            BLangClassDefinition classDefinition = (BLangClassDefinition) typeDefNode;
            if (symEnter.isObjectCtor(classDefinition)) {
                return;
            }
            defineFieldsOfClassDef(classDefinition, pkgEn);
            symEnter.defineReferencedFieldsOfClassDef(classDefinition, pkgEn);
        } else if (typeDefNode.getKind() == NodeKind.TYPE_DEFINITION) {
            symEnter.defineFields((BLangTypeDefinition) typeDefNode, pkgEn);
            symEnter.defineReferencedFieldsOfRecordTypeDef((BLangTypeDefinition) typeDefNode);
        }
        currentDepth--;
    }

    public void defineFieldsOfClassDef(BLangClassDefinition classDefinition, SymbolEnv env) {
        SymbolEnv typeDefEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, env);
        BObjectTypeSymbol tSymbol = (BObjectTypeSymbol) classDefinition.symbol;
        BObjectType objType = (BObjectType) tSymbol.type;

        classDefinition.typeDefEnv = typeDefEnv;

        for (BLangSimpleVariable field : classDefinition.fields) {
            symEnter.defineNode(field, typeDefEnv);
            if (field.expr != null) {
                field.symbol.isDefaultable = true;
            }
            // Unless skipped, this causes issues in negative cases such as duplicate fields.
            if (field.symbol.type == symTable.semanticError) {
                continue;
            }
            objType.fields.put(field.name.value, new BField(names.fromIdNode(field.name), field.pos, field.symbol));
        }
    }

    private void defineRestFields(BLangTypeDefinition typeDef) {
        BStructureType structureType = (BStructureType) typeDef.symbol.type;
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
        SymbolEnv typeDefEnv = recordTypeNode.typeDefEnv;
        BRecordType recordType = (BRecordType) structureType;
        recordType.sealed = recordTypeNode.sealed;
        if (recordTypeNode.sealed && recordTypeNode.restFieldType != null) {
            dlog.error(recordTypeNode.restFieldType.pos, DiagnosticErrorCode.REST_FIELD_NOT_ALLOWED_IN_CLOSED_RECORDS);
            return;
        }

        if (recordTypeNode.restFieldType != null) {
            recordType.restFieldType = symResolver.resolveTypeNode(recordTypeNode.restFieldType, typeDefEnv);
            return;
        }

        if (!recordTypeNode.sealed) {
            recordType.restFieldType = symTable.anydataType;
            return;
        }
        recordType.restFieldType = symTable.noType;
    }

    public void defineClassDef(BLangClassDefinition classDefinition, SymbolEnv env) {
        EnumSet<Flag> flags = EnumSet.copyOf(classDefinition.flagSet);
        boolean isPublicType = flags.contains(Flag.PUBLIC);
        Name className = names.fromIdNode(classDefinition.name);
        Name classOrigName = names.originalNameFromIdNode(classDefinition.name);

        BClassSymbol tSymbol = Symbols.createClassSymbol(Flags.asMask(flags), className, env.enclPkg.symbol.pkgID, null,
                env.scope.owner, classDefinition.name.pos,
                symEnter.getOrigin(className, flags), classDefinition.isServiceDecl);
        tSymbol.originalName = classOrigName;
        tSymbol.scope = new Scope(tSymbol);
        tSymbol.markdownDocumentation =
                symEnter.getMarkdownDocAttachment(classDefinition.markdownDocumentationAttachment);

        long typeFlags = 0;

        if (flags.contains(Flag.READONLY)) {
            typeFlags |= Flags.READONLY;
        }

        if (flags.contains(Flag.ISOLATED)) {
            typeFlags |= Flags.ISOLATED;
        }

        if (flags.contains(Flag.SERVICE)) {
            typeFlags |= Flags.SERVICE;
        }

        if (flags.contains(Flag.OBJECT_CTOR)) {
            typeFlags |= Flags.OBJECT_CTOR;
        }

        BObjectType objectType = new BObjectType(tSymbol, typeFlags);
        resolvingStructureTypes.add(objectType);
        if (classDefinition.isObjectContructorDecl || flags.contains(Flag.OBJECT_CTOR)) {
            classDefinition.oceEnvData.objectType = objectType;
            objectType.classDef = classDefinition;
        }

        if (flags.contains(Flag.DISTINCT)) {
            objectType.typeIdSet = BTypeIdSet.from(env.enclPkg.symbol.pkgID, classDefinition.name.value, isPublicType);
        }

        if (flags.contains(Flag.CLIENT)) {
            objectType.flags |= Flags.CLIENT;
        }

        tSymbol.type = objectType;
        classDefinition.setBType(objectType);
        classDefinition.setDeterminedType(objectType);
        classDefinition.symbol = tSymbol;

        if (symEnter.isDeprecated(classDefinition.annAttachments)) {
            tSymbol.flags |= Flags.DEPRECATED;
        }

        // For each referenced type, check whether the types are already resolved.
        // If not, then that type should get a higher precedence.
        for (BLangType typeRef : classDefinition.typeRefs) {
            BType referencedType = symResolver.resolveTypeNode(typeRef, env);
            objectType.typeInclusions.add(referencedType);
        }

        if (symResolver.checkForUniqueSymbol(classDefinition.pos, env, tSymbol)) {
            env.scope.define(tSymbol.name, tSymbol);
        }
    }


    private void updateEffectiveTypeOfCyclicIntersectionTypes(SymbolEnv symEnv) {
        for (BIntersectionType intersectionType: intersectionTypeList.keySet()) {
            BLangIntersectionTypeNode intersectionTypeNode = intersectionTypeList.get(intersectionType);
            effectiveTypePopulator.updateType(intersectionType, symTable.builtinPos,
                    symEnv.enclPkg.packageID, intersectionTypeNode, symEnv);
            effectiveTypePopulator.visitedImmutableTypes.clear();
        }
    }

    private BType calculateEffectiveType(BLangType typeNode, BLangType bLangTypeOne,
                                         BLangType bLangTypeTwo, BType typeOne, BType typeTwo,
                                         BType typeOneReference, BType typeTwoReference) {

        if (typeOneReference.tag != TypeTags.ERROR || typeTwoReference.tag != TypeTags.ERROR) {
            dlog.error(typeNode.pos, DiagnosticErrorCode.UNSUPPORTED_TYPE_INTERSECTION);
            return symTable.semanticError;
        }

        BType potentialIntersectionType = types.getTypeIntersection(
                Types.IntersectionContext.from(dlog, bLangTypeOne.pos, bLangTypeTwo.pos),
                typeOne, typeTwo, pkgEnv);

        if (potentialIntersectionType.tag == TypeTags.SEMANTIC_ERROR) {
            dlog.error(typeNode.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, typeNode);
            return symTable.semanticError;
        }

        return potentialIntersectionType;
    }

    private void handleDistinctDefinitionOfErrorIntersection(BLangTypeDefinition typeDefinition, BSymbol typeDefSymbol,
                                                             BType definedType) {
        if (definedType.tag == TypeTags.INTERSECTION &&
                ((BIntersectionType) definedType).effectiveType.getKind() == TypeKind.ERROR) {
            boolean distinctFlagPresentInTypeDef = typeDefinition.typeNode.flagSet.contains(Flag.DISTINCT);

            BTypeIdSet typeIdSet = BTypeIdSet.emptySet();
            int numberOfDistinctConstituentTypes = 0;
            BLangIntersectionTypeNode intersectionTypeNode = (BLangIntersectionTypeNode) typeDefinition.typeNode;
            for (BLangType constituentType : intersectionTypeNode.constituentTypeNodes) {
                BType type = Types.getImpliedType(
                        types.getTypeWithEffectiveIntersectionTypes(constituentType.getBType()));

                if (type.getKind() == TypeKind.ERROR) {
                    if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                        numberOfDistinctConstituentTypes++;
                        typeIdSet.addSecondarySet(((BErrorType) type).typeIdSet.getAll());
                    } else {
                        typeIdSet.add(((BErrorType) type).typeIdSet);
                    }
                }
            }

            BErrorType effectiveType = (BErrorType) ((BIntersectionType) definedType).effectiveType;

            // if the distinct keyword is part of a distinct-type-descriptor that is the
            // only distinct-type-descriptor occurring within a module-type-defn,
            // then the local id is the name of the type defined by the module-type-defn.
            if (numberOfDistinctConstituentTypes == 1
                    || (numberOfDistinctConstituentTypes == 0 && distinctFlagPresentInTypeDef)) {
                effectiveType.typeIdSet = BTypeIdSet.from(pkgEnv.enclPkg.packageID, typeDefinition.name.value,
                        true, typeIdSet);
            } else {
                for (BLangType constituentType : intersectionTypeNode.constituentTypeNodes) {
                    if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                        typeIdSet.add(BTypeIdSet.from(pkgEnv.enclPkg.packageID,
                                anonymousModelHelper.getNextAnonymousTypeId(pkgEnv.enclPkg.packageID), true));
                    }
                }
                effectiveType.typeIdSet = typeIdSet;
            }

            //setting the newly created distinct type as the referred type of the definition
            if (((BTypeDefinitionSymbol) typeDefSymbol).referenceType != null) {
                ((BTypeDefinitionSymbol) typeDefSymbol).referenceType.referredType = definedType;
            }

            if (!effectiveType.typeIdSet.isEmpty()) {
                definedType.flags |= Flags.DISTINCT;
            }
        }
    }

    private BType resolveTypeDefinition(SymbolEnv symEnv, Map<String, BLangNode> mod,
                                        BLangTypeDefinition defn, int depth) {
        if (defn.getBType() != null) {
            // Already defined.
            return defn.getBType();
        }

        String currentDefnName = defn.name.value;
        if (depth == defn.cycleDepth) {
            // We cannot define recursive classDefinitions with same depths.
            logInvalidCyclicReferenceError(currentDefnName, defn.pos);
            return symTable.semanticError;
        }

        currentDepth = depth;
        defn.cycleDepth = depth;
        boolean hasAlreadyVisited = false;

        if (resolvingTypeDefinitions.contains(defn)) {
            // Type definition has a cyclic reference.
            boolean logError = true;
            for (int i = resolvingTypeDefinitions.size() - 1; i >= 0; i--) {
                BLangTypeDefinition resolvingTypeDefn = resolvingTypeDefinitions.get(i);
                resolvingTypeDefn.hasCyclicReference = true;

                // TODO: Remove this after runtime allows cyclic array & constraint type.
                BLangType resolvingTypeNode = resolvingTypeDefn.typeNode;
                if (isNotRestrictedCyclicTypeNode(resolvingTypeNode)) {
                    logError = false;
                }
                if (logError) {
                    logErrorForRestrictedCyclicTypes(resolvingTypeNode, resolvingTypeDefn.name.value);
                }

                if (resolvingTypeDefn == defn) {
                    break;
                }
            }
            hasAlreadyVisited = true;
        } else {
            // Add the type into resolvingtypeDefinitions list.
            // This is used to identify types which have cyclic references.
            resolvingTypeDefinitions.add(defn);
            resolvingModuleDefs.push(currentDefnName);
        }

        // Resolve the type
        ResolverData data = new ResolverData();
        data.modTable = mod;
        BType type = resolveTypeDesc(symEnv, defn, depth, defn.typeNode, data, false);

        // Define the typeDefinition. Add symbol, flags etc.
        type = defineTypeDefinition(defn, type, symEnv);
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(defn);

        if (!hasAlreadyVisited) {
            // Remove the typeDefinition from currently resolving typeDefinition map.
            resolvingTypeDefinitions.remove(defn);
            resolvingModuleDefs.pop();
        }

        if (defn.getBType() == null) {
            defn.setBType(type);
            defn.cycleDepth = -1;
        }
        return type;
    }

    private void updateIsCyclicFlag(BType type) {
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

    private void logErrorForRestrictedCyclicTypes(BLangType typeNode, String name) {
        switch (typeNode.getKind()) {
            case ARRAY_TYPE:
            case CONSTRAINED_TYPE:
                dlog.error(typeNode.pos, DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE_NOT_YET_SUPPORTED, name);
                break;
            case INTERSECTION_TYPE_NODE:
                ((BLangIntersectionTypeNode) typeNode).constituentTypeNodes.forEach(
                        t -> logErrorForRestrictedCyclicTypes(t, name));
                break;
        }
    }

    private boolean isNotRestrictedCyclicTypeNode(BLangType typeNode) {
        switch (typeNode.getKind()) {
            case USER_DEFINED_TYPE:
            case ARRAY_TYPE:
            case CONSTRAINED_TYPE:
            case INTERSECTION_TYPE_NODE:
                return false;
            default:
                return true;
        }
    }

    private void logInvalidCyclicReferenceError(String currentDefnName, Location pos) {
        // Here, all the types in the list might not be necessary for the cyclic dependency error message.
        //
        // Eg - A -> B -> C -> B // Last B is what we are currently checking
        //
        // In such case, we create a new list with relevant type names.
        int i = resolvingModuleDefs.indexOf(currentDefnName);
        List<String> dependencyList = new ArrayList<>(resolvingModuleDefs.size() - i);
        for (; i < resolvingModuleDefs.size(); i++) {
            dependencyList.add(resolvingModuleDefs.get(i));
        }
        dependencyList.add(currentDefnName);

        dlog.error(pos, DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, dependencyList);
    }

    public boolean isNotUnknownTypeRef(BLangUserDefinedType td) {
        Location pos = td.pos;
        LocationData locationData = new LocationData(td.typeName.value, pos.lineRange().startLine().line(),
                pos.lineRange().startLine().offset());
        return unknownTypeRefs.add(locationData);
    }

    public BType validateModuleLevelDef(String name, Name pkgAlias, Name typeName, BLangUserDefinedType td) {
        BLangNode moduleLevelDef = pkgAlias == Names.EMPTY ? modTable.get(name) : null;
        if (moduleLevelDef == null) {
            if (missingNodesHelper.isMissingNode(pkgAlias) || missingNodesHelper.isMissingNode(typeName)) {
                return symTable.semanticError;
            }

            if (isNotUnknownTypeRef(td)) {
                dlog.error(td.pos, DiagnosticErrorCode.UNKNOWN_TYPE, name);
            }
            return symTable.semanticError;
        }
        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            BLangTypeDefinition typeDefinition = (BLangTypeDefinition) moduleLevelDef;
            BType resolvedType = resolveTypeDefinition(pkgEnv, modTable, typeDefinition, currentDepth);
            if (resolvedType == symTable.semanticError || resolvedType == symTable.noType) {
                return resolvedType;
            }
            BSymbol symbol = typeDefinition.symbol;
            td.symbol = symbol;
            if (symbol.kind == SymbolKind.TYPE_DEF && !Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
                BType referenceType = ((BTypeDefinitionSymbol) symbol).referenceType;
                referenceType.flags |= symbol.type.flags;
                referenceType.tsymbol.flags |= symbol.type.flags;
                return referenceType;
            }
            return resolvedType;
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            resolveConstant(pkgEnv, modTable, constant);
            return constant.getBType();
        } else {
            return extracted(pkgEnv, (BLangClassDefinition) moduleLevelDef, currentDepth);
        }
    }

    public void getFieldsOfStructureType(String name, List<BLangSimpleVariable> includedFields) {
        BLangNode moduleLevelDef = modTable.get(name);
        if (moduleLevelDef != null && moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            BLangNode typeNode = ((BLangTypeDefinition) moduleLevelDef).typeNode;
            if (typeNode.getKind() == NodeKind.RECORD_TYPE || typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeNode;
                includedFields.addAll(structureTypeNode.fields);
                structureTypeNode.typeRefs.forEach(typeRef -> {
                    if (typeRef.getKind() == NodeKind.USER_DEFINED_TYPE) {
                        getFieldsOfStructureType(((BLangUserDefinedType) typeRef).typeName.value, includedFields);
                    }
                });
            }
        }
    }

    public BLangTypeDefinition getTypeDefinition(String name) {
        BLangNode moduleLevelDef = modTable.get(name);
        return moduleLevelDef != null && moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION ?
                (BLangTypeDefinition) moduleLevelDef : null;
    }

    private BType resolveTypeDesc(SymbolEnv symEnv, BLangTypeDefinition defn, int depth,
                                  BLangType td, ResolverData data) {
        return resolveTypeDesc(symEnv, defn, depth, td, data, true);
    }

    private BType resolveTypeDesc(SymbolEnv symEnv, BLangTypeDefinition defn, int depth,
                                  BLangType td, ResolverData data, boolean anonymous) {
        SymbolEnv prevEnv = data.env;
        BLangTypeDefinition prevDefn = data.typeDefinition;
        int prevDepth = data.depth;
        data.env = symEnv;
        data.typeDefinition = defn;
        data.depth = depth;

        BType resultType;
        switch (td.getKind()) {
            case VALUE_TYPE:
                resultType = resolveTypeDesc((BLangValueType) td, symEnv);
                break;
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                resultType = resolveTypeDesc((BLangConstrainedType) td, data);
                break;
            case ARRAY_TYPE:
                resultType = resolveTypeDesc(((BLangArrayType) td), data);
                break;
            case TUPLE_TYPE_NODE:
                resultType = resolveTypeDesc((BLangTupleTypeNode) td, data);
                break;
            case RECORD_TYPE:
                resultType = resolveTypeDesc((BLangRecordTypeNode) td, data);
                break;
            case OBJECT_TYPE:
                resultType = resolveTypeDesc((BLangObjectTypeNode) td, data);
                break;
            case FUNCTION_TYPE:
                resultType = resolveTypeDesc((BLangFunctionTypeNode) td, data);
                break;
            case ERROR_TYPE:
                resultType = resolveTypeDesc((BLangErrorType) td, data);
                break;
            case UNION_TYPE_NODE:
                resultType = resolveTypeDesc((BLangUnionTypeNode) td, data);
                break;
            case INTERSECTION_TYPE_NODE:
                resultType = resolveTypeDesc((BLangIntersectionTypeNode) td, data, anonymous);
                break;
            case USER_DEFINED_TYPE:
                resultType = resolveTypeDesc((BLangUserDefinedType) td, data);
                break;
            case BUILT_IN_REF_TYPE:
                resultType = resolveTypeDesc((BLangBuiltInRefTypeNode) td, symEnv);
                break;
            case FINITE_TYPE_NODE:
                resultType = resolveSingletonType((BLangFiniteTypeNode) td, symEnv);
                break;
            case TABLE_TYPE:
                resultType = resolveTypeDesc((BLangTableTypeNode) td, data);
                break;
            case STREAM_TYPE:
                resultType = resolveTypeDesc((BLangStreamType) td, data);
                break;
            default:
                throw new AssertionError("Invalid type");
        }

        BType refType = Types.getImpliedType(resultType);
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
        data.env = prevEnv;
        data.typeDefinition = prevDefn;
        data.depth = prevDepth;
        return resultType;
    }

    private BType resolveTypeDesc(BLangValueType td, SymbolEnv symEnv) {
        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);
        return visitBuiltInTypeNode(td, data, td.typeKind);
    }

    private BType resolveTypeDesc(BLangConstrainedType td, ResolverData data) {
        currentDepth = data.depth;
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();

        switch (typeKind) {
            case MAP:
                return resolveMapTypeDesc(td, data);
            case XML:
                return resolveXmlTypeDesc(td, data);
            case FUTURE:
                return resolveFutureTypeDesc(td, data);
            case TYPEDESC:
                return resolveTypedescTypeDesc(td, data);
        }
        throw new IllegalStateException("unknown constrained type found: " + typeKind);
    }

    private BType resolveTypedescTypeDesc(BLangConstrainedType td, ResolverData data) {
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);
        BTypedescType constrainedType = new BTypedescType(symTable.empty, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, constrainedType, typeSymbol.owner,
                td.pos, BUILTIN);
        td.setBType(constrainedType);
        BType constraintType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);
        constrainedType.constraint = constraintType;
        symResolver.markParameterizedType(constrainedType, constraintType);
        return constrainedType;
    }

    private BType resolveFutureTypeDesc(BLangConstrainedType td, ResolverData data) {
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);
        BFutureType constrainedType = new BFutureType(TypeTags.FUTURE, symTable.empty, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, constrainedType, typeSymbol.owner,
                td.pos, BUILTIN);
        td.setBType(constrainedType);
        BType constraintType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);
        constrainedType.constraint = constraintType;
        symResolver.markParameterizedType(constrainedType, constraintType);
        return constrainedType;
    }

    private BType resolveXmlTypeDesc(BLangConstrainedType td, ResolverData data) {
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);
        BXMLType constrainedType = new BXMLType(symTable.empty, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        constrainedType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, constrainedType, typeSymbol.owner,
                td.pos, BUILTIN);

        td.setBType(constrainedType);
        resolvingTypes.push(constrainedType);

        BType constraintType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);

        if (constraintType.tag == TypeTags.PARAMETERIZED_TYPE) {
            BType typedescType = ((BParameterizedType) constraintType).paramSymbol.type;
            BType typedescConstraint = ((BTypedescType) typedescType).constraint;
            symResolver.validateXMLConstraintType(typedescConstraint, td.pos);
        } else {
            symResolver.validateXMLConstraintType(constraintType, td.pos);
        }

        constrainedType.constraint = constraintType;
        symResolver.markParameterizedType(constrainedType, constraintType);
        resolvingTypes.pop();
        return constrainedType;
    }

    private BType resolveMapTypeDesc(BLangConstrainedType td, ResolverData data) {
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);
        BTypeSymbol typeSymbol = type.tsymbol;
        BTypeSymbol tSymbol = Symbols.createTypeSymbol(SymTag.TYPE, typeSymbol.flags, Names.EMPTY,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner,
                td.pos, BUILTIN);
        BMapType constrainedType = new BMapType(TypeTags.MAP, symTable.empty, tSymbol);
        td.setBType(constrainedType);
        tSymbol.type = type;
        resolvingTypes.push(constrainedType);

        BType constraintType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);
        constrainedType.constraint = constraintType;
        symResolver.markParameterizedType(constrainedType, constraintType);
        resolvingTypes.pop();
        return constrainedType;
    }

    private BType resolveTypeDesc(BLangArrayType td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        BType resultType = symTable.empty;
        boolean isError = false;
        BArrayType firstDimArrType = null;
        boolean firstDim = true;

        SymbolEnv symEnv = data.env;
        for (int i = 0; i < td.dimensions; i++) {
            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                    symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, BUILTIN);
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

                    BSymbol sizeSymbol =
                            symResolver.lookupMainSpaceSymbolInPackage(size.pos, symEnv, pkgAlias, typeName);
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
                    if (lengthCheck > SymbolResolver.MAX_ARRAY_SIZE) {
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

        td.setBType(resultType);
        resolvingTypes.push(resultType);

        if (isError) {
            return symTable.semanticError;
        }

        firstDimArrType.eType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.elemtype, data);
        symResolver.markParameterizedType(firstDimArrType, firstDimArrType.eType);
        resolvingTypes.pop();
        return resultType;
    }

    private BType resolveTypeDesc(BLangTupleTypeNode td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, BUILTIN);
        List<BTupleMember> memberTypes = new ArrayList<>();
        BTupleType tupleType = new BTupleType(tupleTypeSymbol, memberTypes);
        tupleTypeSymbol.type = tupleType;
        td.setBType(tupleType);
        resolvingTypes.push(tupleType);

        for (BLangSimpleVariable memberNode: td.getMemberNodes()) {
            BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, memberNode.typeNode, data);
            SymbolEnv tupleEnv = SymbolEnv.createTypeEnv(td, new Scope(tupleTypeSymbol), symEnv);
            symEnter.defineNode(memberNode, tupleEnv);
            BVarSymbol varSymbol = new BVarSymbol(memberNode.getBType().flags, memberNode.symbol.name,
                    memberNode.symbol.pkgID, memberNode.getBType(), memberNode.symbol.owner, memberNode.pos, SOURCE);
            memberTypes.add(new BTupleMember(type, varSymbol));
        }

        if (td.restParamType != null) {
            BType tupleRestType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.restParamType, data);
            if (tupleRestType.tag == TypeTags.SEMANTIC_ERROR) {
                return symTable.semanticError;
            }
            tupleType.restType = tupleRestType;
            symResolver.markParameterizedType(tupleType, tupleType.restType);
        }

        symResolver.markParameterizedType(tupleType, tupleType.getTupleTypes());
        resolvingTypes.pop();
        return tupleType;
    }

    private BType resolveTypeDesc(BLangRecordTypeNode td, ResolverData data) {
        int depth = data.depth;
        currentDepth = depth;

        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BLangTypeDefinition typeDefinition = data.typeDefinition;
        EnumSet<Flag> flags = td.isAnonymous ? EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS)
                : EnumSet.noneOf(Flag.class);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos,
                td.isAnonymous ? VIRTUAL : BUILTIN);
        BRecordType recordType = new BRecordType(recordSymbol);
        resolvingStructureTypes.add(recordType);
        recordSymbol.type = recordType;
        td.symbol = recordSymbol;
        td.setBType(recordType);
        resolvingTypes.push(recordType);

        if (symEnv.node.getKind() != NodeKind.PACKAGE) {
            recordSymbol.name = Names.fromString(
                    anonymousModelHelper.getNextAnonymousTypeKey(symEnv.enclPkg.packageID));
            symEnter.defineSymbol(td.pos, td.symbol, symEnv);
            symEnter.defineNode(td, symEnv);
        }

        if (td.getRestFieldType() != null) {
            resolveTypeDesc(symEnv, typeDefinition, depth + 1, td.restFieldType, data);
            currentDepth = depth;
        }

        boolean errored = false;
        for (BLangType bLangRefType : td.typeRefs) {
            BType refType = resolveTypeDesc(symEnv, typeDefinition, depth, bLangRefType, data);
            currentDepth = depth;
            if (refType == symTable.semanticError) {
                errored = true;
            }
        }

        defineTypeDefinition(typeDefinition, recordType, symEnv);
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefinition);
        currentDepth = depth;
        if (errored) {
            typeDefinition.referencedFieldsDefined = true;
            defineRestFields(typeDefinition);
        }
        defineFields(typeDefinition, symEnv);
        resolvingStructureTypes.remove(recordType);
        resolvingTypes.pop();
        return recordType;
    }

    private BType resolveTypeDesc(BLangObjectTypeNode td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        EnumSet<Flag> flags = EnumSet.copyOf(td.flagSet);
        if (td.isAnonymous) {
            flags.add(Flag.PUBLIC);
        }

        int typeFlags = 0;

        if (flags.contains(Flag.ISOLATED)) {
            typeFlags |= Flags.ISOLATED;
        }

        if (flags.contains(Flag.SERVICE)) {
            typeFlags |= Flags.SERVICE;
        }

        SymbolEnv symEnv = data.env;
        BLangTypeDefinition typeDefinition = data.typeDefinition;
        BTypeSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(flags), Names.EMPTY,
                symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, BUILTIN);

        BObjectType objectType = new BObjectType(objectSymbol, typeFlags);
        resolvingStructureTypes.add(objectType);
        objectSymbol.type = objectType;
        td.symbol = objectSymbol;
        td.setBType(objectType);
        resolvingTypes.push(objectType);

        boolean errored = false;
        for (BLangType bLangRefType : td.typeRefs) {
            BType refType = resolveTypeDesc(symEnv, typeDefinition, data.depth, bLangRefType, data);
            if (refType == symTable.semanticError) {
                errored = true;
            }
        }

        if (errored) {
            typeDefinition.referencedFieldsDefined = true;
        }

        symResolver.validateDistinctType(td, objectType);

        defineTypeDefinition(typeDefinition, objectType, symEnv);
        symEnter.defineDistinctClassAndObjectDefinitionIndividual(typeDefinition);
        defineFields(typeDefinition, symEnv);
        resolvingStructureTypes.remove(objectType);
        resolvingTypes.pop();
        return objectType;
    }

    private BType resolveTypeDesc(BLangFunctionTypeNode td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        Location pos = td.pos;
        BInvokableType bInvokableType = new BInvokableType(null, null, null, null);
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                Flags.asMask(td.flagSet), symEnv.enclPkg.symbol.pkgID, bInvokableType,
                symEnv.scope.owner, pos, BUILTIN);
        tsymbol.name = Names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(symEnv.enclPkg.packageID));
        symEnter.defineSymbol(pos, tsymbol, symEnv);
        bInvokableType.tsymbol = tsymbol;
        td.setBType(bInvokableType);
        resolvingTypes.push(bInvokableType);

        List<BLangSimpleVariable> params = td.getParams();
        BLangType returnTypeNode = td.returnTypeNode;
        BType invokableType = createInvokableType(params, td.restParam, returnTypeNode,
                Flags.asMask(td.flagSet), symEnv, data.modTable, data.depth, data.typeDefinition,
                bInvokableType, data);
        resolvingTypes.pop();
        return symResolver.validateInferTypedescParams(pos, params, returnTypeNode == null ?
                null : returnTypeNode.getBType()) ? invokableType : symTable.semanticError;
    }

    public BType createInvokableType(List<? extends BLangVariable> paramVars,
                                     BLangVariable restVariable,
                                     BLangType retTypeVar,
                                     long flags, SymbolEnv env, Map<String, BLangNode> mod, int depth,
                                     BLangTypeDefinition typeDefinition, BInvokableType bInvokableType,
                                     ResolverData data) {
        List<BType> paramTypes = new ArrayList<>();
        List<BVarSymbol> params = new ArrayList<>();

        boolean foundDefaultableParam = false;
        List<String> paramNames = new ArrayList<>();
        BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) bInvokableType.tsymbol;
        if (Symbols.isFlagOn(flags, Flags.ANY_FUNCTION)) {
            bInvokableType.flags = flags;
            tsymbol.params = null;
            tsymbol.restParam = null;
            tsymbol.returnType = null;
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
            BType type = resolveTypeDesc(env, typeDefinition, depth + 1, param.getTypeNode(), data);
            paramNode.setBType(type);
            paramTypes.add(type);

            long paramFlags = Flags.asMask(paramNode.flagSet);
            BVarSymbol symbol = new BVarSymbol(paramFlags, paramName, paramOrigName, env.enclPkg.symbol.pkgID,
                    type, env.scope.owner, param.pos, BUILTIN);
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

        BType retType = resolveTypeDesc(env, typeDefinition, depth + 1, retTypeVar, data);

        BVarSymbol restParam = null;
        BType restType = null;

        if (restVariable != null) {
            restType = resolveTypeDesc(env, typeDefinition, depth + 1, restVariable.typeNode, data);
            BLangIdentifier id = ((BLangSimpleVariable) restVariable).name;
            restVariable.setBType(restType);
            restParam = new BVarSymbol(Flags.asMask(restVariable.flagSet),
                    names.fromIdNode(id), names.originalNameFromIdNode(id),
                    env.enclPkg.symbol.pkgID, restType, env.scope.owner, restVariable.pos, BUILTIN);
            restVariable.symbol = restParam;
        }

        bInvokableType.paramTypes = paramTypes;
        bInvokableType.restType = restType;
        bInvokableType.retType = retType;
        bInvokableType.flags |= flags;
        tsymbol.params = params;
        tsymbol.restParam = restParam;
        tsymbol.returnType = retType;

        params.forEach(param -> tsymbol.scope.define(param.name, param));
        List<BType> allConstituentTypes = new ArrayList<>(paramTypes);
        allConstituentTypes.add(restType);
        allConstituentTypes.add(retType);
        symResolver.markParameterizedType(bInvokableType, allConstituentTypes);
        return bInvokableType;
    }

    private BType resolveTypeDesc(BLangErrorType td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        if (td.detailType == null) {
            BType errorType = new BErrorType(null, symTable.detailType);
            errorType.tsymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.ERROR,
                    symTable.rootPkgSymbol.pkgID, errorType, symTable.rootPkgSymbol, symTable.builtinPos, BUILTIN);
            return errorType;
        }

        // Define user define error type.
        BErrorTypeSymbol errorTypeSymbol = Symbols.createErrorSymbol(Flags.asMask(td.flagSet),
                Names.EMPTY, data.env.enclPkg.packageID, null, data.env.scope.owner, td.pos, BUILTIN);
        BErrorType errorType = new BErrorType(errorTypeSymbol, symTable.empty);
        td.setBType(errorType);
        resolvingTypes.push(errorType);

        BType detailType = Optional.ofNullable(td.detailType)
                .map(bLangType -> resolveTypeDesc(data.env, data.typeDefinition, data.depth,
                        bLangType, data)).orElse(symTable.detailType);

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

        errorType.detailType = detailType;

        PackageID packageID = data.env.enclPkg.packageID;
        if (data.env.node.getKind() != NodeKind.PACKAGE) {
            errorTypeSymbol.name = Names.fromString(
                    anonymousModelHelper.getNextAnonymousTypeKey(packageID));
            symEnter.defineSymbol(td.pos, errorTypeSymbol, data.env);
        }

        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;

        symResolver.markParameterizedType(errorType, detailType);

        errorType.typeIdSet = BTypeIdSet.emptySet();

        if (td.isAnonymous && td.flagSet.contains(Flag.DISTINCT)) {
            errorType.typeIdSet.add(
                    BTypeIdSet.from(packageID, anonymousModelHelper.getNextAnonymousTypeId(packageID), true));
        }

        resolvingTypes.pop();
        return errorType;
    }

    private BType resolveTypeDesc(BLangUnionTypeNode td, ResolverData data) {
        currentDepth = data.depth;

        SymbolEnv symEnv = data.env;
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, BUILTIN);
        BUnionType unionType = new BUnionType(unionTypeSymbol, memberTypes, false, false);
        unionTypeSymbol.type = unionType;
        td.setBType(unionType);
        resolvingTypes.push(unionType);

        for (BLangType langType : td.memberTypeNodes) {
            BType resolvedType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth, langType, data);
            if (resolvedType == symTable.semanticError) {
                memberTypes.add(symTable.semanticError);
                continue;
            }

            if (resolvedType.isNullable()) {
                unionType.setNullable(true);
            }
            memberTypes.add(resolvedType);
        }

        updateReadOnlyAndNullableFlag(unionType);
        symResolver.markParameterizedType(unionType, memberTypes);
        resolvingTypes.pop();
        return unionType;
    }

    private void updateReadOnlyAndNullableFlag(BUnionType type) {
        LinkedHashSet<BType> memberTypes = type.getMemberTypes();
        LinkedHashSet<BType> flattenMemberTypes = new LinkedHashSet<>(memberTypes.size());
        boolean isImmutable = true;
        boolean hasNilableType = false;

        for (BType memBType : BUnionType.toFlatTypeSet(memberTypes)) {
            if (Types.getImpliedType(memBType).tag != TypeTags.NEVER) {
                flattenMemberTypes.add(memBType);
            }

            if (isImmutable && !Symbols.isFlagOn(memBType.flags, Flags.READONLY)) {
                isImmutable = false;
            }
        }

        if (isImmutable) {
            type.flags |= Flags.READONLY;
            if (type.tsymbol != null) {
                type.tsymbol.flags |= Flags.READONLY;
            }
        }

        for (BType memberType : flattenMemberTypes) {
            if (memberType.isNullable() && memberType.tag != TypeTags.NIL) {
                hasNilableType = true;
                break;
            }
        }

        if (hasNilableType) {
            LinkedHashSet<BType> bTypes = new LinkedHashSet<>(flattenMemberTypes.size());
            for (BType t : flattenMemberTypes) {
                if (t.tag != TypeTags.NIL) {
                    bTypes.add(t);
                }
            }
            flattenMemberTypes = bTypes;
        }

        for (BType memberType : flattenMemberTypes) {
            if (memberType.isNullable()) {
                type.setNullable(true);
            }
        }
        type.setOriginalMemberTypes(memberTypes);
        memberTypes.clear();
        memberTypes.addAll(flattenMemberTypes);
    }

    private BType resolveTypeDesc(BLangIntersectionTypeNode td, ResolverData data, boolean anonymous) {
        currentDepth = data.depth;
        List<BLangType> constituentTypeNodes = td.constituentTypeNodes;
        LinkedHashSet<BType> constituentTypes = new LinkedHashSet<>();
        SymbolEnv symEnv = data.env;
        boolean errored = false;
        boolean hasReadonly = false;
        int numOfNonReadOnlyConstituents = 0;
        BLangTypeDefinition typeDefinition = data.typeDefinition;

        for (BLangType typeNode : constituentTypeNodes) {
            BType constituentType = resolveTypeDesc(symEnv, typeDefinition, data.depth, typeNode, data);
            if (constituentType == symTable.semanticError) {
                errored = true;
                continue;
            }
            if (constituentType.tag == TypeTags.READONLY) {
                hasReadonly = true;
            } else {
                numOfNonReadOnlyConstituents++;
            }
            constituentTypes.add(constituentType);
        }
        if (errored) {
            return symTable.semanticError;
        }
        Name name = anonymous ?
                Names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(data.env.enclPkg.packageID)) :
                Names.fromString(typeDefinition.name.value);
        BTypeSymbol intersectionSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, name,
                symEnv.enclPkg.symbol.pkgID, null, symEnv.scope.owner, td.pos, BUILTIN);
        BIntersectionType intersectionType = new BIntersectionType(intersectionSymbol);
        intersectionSymbol.type = intersectionType;
        intersectionType.setConstituentTypes(constituentTypes);

        if (hasReadonly) {
            intersectionType.flags |= Flags.READONLY;
        }

        // Differ cyclic intersection between more than 2 non-readonly types.
        if (numOfNonReadOnlyConstituents > 1) {
            for (BType constituentType : constituentTypes) {
                if (constituentType.tag == TypeTags.READONLY) {
                    continue;
                }
                if (resolvingTypes.contains(Types.getImpliedType(constituentType))) {
                    dlog.error(td.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, td);
                    return symTable.semanticError;
                }
            }
        }

        fillEffectiveType(intersectionType, td, symEnv);
        return intersectionType;
    }

    private void fillEffectiveType(BIntersectionType intersectionType,
                                   BLangIntersectionTypeNode td, SymbolEnv env) {
        List<BLangType> constituentTypeNodes = td.constituentTypeNodes;
        Iterator<BLangType> bLangTypeItr = constituentTypeNodes.iterator();
        Iterator<BType> iterator = intersectionType.getConstituentTypes().iterator();
        BType effectiveType = iterator.next();
        BLangType bLangEffectiveType = bLangTypeItr.next();

        while (iterator.hasNext()) {
            BType bLangEffectiveImpliedType = Types.getImpliedType(effectiveType);
            if (bLangEffectiveImpliedType.tag == TypeTags.READONLY) {
                intersectionType.flags = intersectionType.flags | TypeTags.READONLY;
                effectiveType = iterator.next();
                bLangEffectiveType = bLangTypeItr.next();
                continue;
            }
            BType type = iterator.next();
            BLangType bLangType = bLangTypeItr.next();
            BType typeReferenceType = Types.getImpliedType(type);
            if (typeReferenceType.tag == TypeTags.READONLY) {
                intersectionType.flags = intersectionType.flags | TypeTags.READONLY;
                continue;
            }
            effectiveType = calculateEffectiveType(td, bLangEffectiveType, bLangType, effectiveType, type,
                    bLangEffectiveImpliedType, typeReferenceType);
            if (effectiveType.tag == TypeTags.SEMANTIC_ERROR) {
                intersectionType.effectiveType = symTable.semanticError;
                return;
            }
        }
        intersectionType.effectiveType = effectiveType;
        intersectionType.flags |= effectiveType.flags;

        if ((intersectionType.flags & Flags.READONLY) == Flags.READONLY) {
            if (types.isInherentlyImmutableType(effectiveType)) {
                return;
            }

            if (!resolvingTypes.isEmpty()) {
                intersectionTypeList.put(intersectionType, td);
            }

            if (!(Types.getImpliedType(effectiveType) instanceof SelectivelyImmutableReferenceType)) {
                intersectionType.effectiveType = symTable.semanticError;
                return;
            }

            BIntersectionType immutableIntersectionType =
                    ImmutableTypeCloner.getImmutableIntersectionType(intersectionType.tsymbol.pos, types,
                            intersectionType.effectiveType, env, symTable, anonymousModelHelper, names,
                            new HashSet<>());
            intersectionType.effectiveType = immutableIntersectionType.effectiveType;
        }
    }

    private BType resolveTypeDesc(BLangUserDefinedType td, ResolverData data) {
        currentDepth = data.depth;
        String name = td.typeName.value;
        SymbolEnv symEnv = data.env;

        BType type;

        // 1) Resolve the package scope using the package alias.
        //    If the package alias is not empty or null, then find the package scope,
        //    if not use the current package scope.
        // 2) lookup the typename in the package scope returned from step 1.
        // 3) If the symbol is not found, then lookup in the root scope. e.g. for types such as 'error'
        SymbolResolver.AnalyzerData analyzerData = new SymbolResolver.AnalyzerData(symEnv);

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
            BSymbol refSymbol = tempSymbol.tag == SymTag.TYPE_DEF ?
                    Types.getImpliedType(tempSymbol.type).tsymbol : tempSymbol;
            // Tsymbol of the effective type can be null for invalid intersections and `xml & readonly` intersections
            if (refSymbol == null) {
                refSymbol = tempSymbol;
            }

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
                        Types.getImpliedType(tempSymbol.type).tag != TypeTags.TYPEDESC) {
                    dlog.error(td.pos, DiagnosticErrorCode.INVALID_PARAM_TYPE_FOR_RETURN_TYPE,
                            tempSymbol.type);
                    errored = true;
                }

                if (errored) {
                    return symTable.semanticError;
                }

                SymbolResolver.ParameterizedTypeInfo parameterizedTypeInfo =
                        symResolver.getTypedescParamValueType(func.requiredParams, analyzerData, refSymbol);
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
                dlog.error(td.pos, analyzerData.diagCode, typeName);
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

        if (symbol != symTable.notFoundSymbol) {
            return type;
        }

        BLangNode moduleLevelDef = pkgAlias == Names.EMPTY ? data.modTable.get(name) : null;
        if (moduleLevelDef == null) {
            if (missingNodesHelper.isMissingNode(pkgAlias) || missingNodesHelper.isMissingNode(typeName)) {
                return symTable.semanticError;
            }

            LocationData locationData = new LocationData(name, td.pos.lineRange().startLine().line(),
                    td.pos.lineRange().startLine().offset());
            if (unknownTypeRefs.add(locationData)) {
                dlog.error(td.pos, DiagnosticErrorCode.UNKNOWN_TYPE, td.typeName);
            }
            return symTable.semanticError;
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            BLangTypeDefinition typeDefinition = (BLangTypeDefinition) moduleLevelDef;
            BType resolvedType = resolveTypeDefinition(symEnv, data.modTable, typeDefinition, data.depth);
            if (resolvedType == symTable.semanticError || resolvedType == symTable.noType) {
                return resolvedType;
            }
            symbol = typeDefinition.symbol;
            td.symbol = symbol;
            if (symbol.kind == SymbolKind.TYPE_DEF && !Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
                BType referenceType = ((BTypeDefinitionSymbol) symbol).referenceType;
                referenceType.flags |= symbol.type.flags;
                referenceType.tsymbol.flags |= symbol.type.flags;
                return referenceType;
            }
            return resolvedType;
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            resolveConstant(symEnv, data.modTable, constant);
            return constant.getBType();
        } else {
            return extracted(symEnv, (BLangClassDefinition) moduleLevelDef, data.depth);
        }
    }

    private BType resolveTypeDesc(BLangBuiltInRefTypeNode td, SymbolEnv symEnv) {
        SymbolResolver.AnalyzerData data = new SymbolResolver.AnalyzerData(symEnv);
        return visitBuiltInTypeNode(td, data, td.typeKind);
    }

    private BType resolveSingletonType(BLangFiniteTypeNode td, SymbolEnv symEnv) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                (Flags.asMask(EnumSet.of(Flag.PUBLIC))), Names.EMPTY, symEnv.enclPkg.symbol.pkgID, null,
                symEnv.scope.owner, td.pos, BUILTIN);

        // In case we encounter unary expressions in finite type, we will be replacing them with numeric literals.
         replaceUnaryExprWithNumericLiteral(td);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        for (BLangExpression literal : td.valueSpace) {
            BType type = blangTypeUpdate(literal);
            if (type != null && type.tag == TypeTags.SEMANTIC_ERROR) {
                return type;
            }
            if (type != null) {
                literal.setBType(symTable.getTypeFromTag(type.tag));
            }
            finiteType.addValue(literal);
        }
        finiteTypeSymbol.type = finiteType;
        td.setBType(finiteType);
        return finiteType;
    }

    private void replaceUnaryExprWithNumericLiteral(BLangFiniteTypeNode finiteTypeNode) {
        List<BLangExpression> valueSpace = finiteTypeNode.valueSpace;
        for (int i = 0; i < valueSpace.size(); i++) {
            BLangExpression value;
            NodeKind valueKind;
            value = valueSpace.get(i);
            valueKind = value.getKind();

            if (valueKind == NodeKind.UNARY_EXPR) {
                BLangUnaryExpr unaryExpr = (BLangUnaryExpr) value;
                if (unaryExpr.expr.getKind() == NodeKind.NUMERIC_LITERAL) {
                    // Replacing unary expression with numeric literal type for + and - numeric values.
                    BLangNumericLiteral newNumericLiteral =
                            Types.constructNumericLiteralFromUnaryExpr(unaryExpr);
                    valueSpace.set(i, newNumericLiteral);
                }
            }
        }
    }

    private BType blangTypeUpdate(BLangExpression expression) {
        BType type;
        switch (expression.getKind()) {
            case UNARY_EXPR:
                type = blangTypeUpdate(((BLangUnaryExpr) expression).expr);
                expression.setBType(type);
                return type;
            case GROUP_EXPR:
                type = blangTypeUpdate(((BLangGroupExpr) expression).expression);
                expression.setBType(type);
                return type;
            case LITERAL:
                return ((BLangLiteral) expression).getBType();
            case BINARY_EXPR:
                type = blangTypeUpdate(((BLangBinaryExpr) expression).lhsExpr);
                expression.setBType(type);
                return type;
            case NUMERIC_LITERAL:
                BLangNumericLiteral expr = (BLangNumericLiteral) expression;
                if (expr.getBType().tag == TypeTags.INT && !(expr.value instanceof Long)) {
                    dlog.error(expression.pos, DiagnosticErrorCode.OUT_OF_RANGE, expr.originalValue,
                            expression.getBType());
                    return symTable.semanticError;
                }
                return expr.getBType();
            default:
                return null;
        }
    }

    private BType resolveTypeDesc(BLangTableTypeNode td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);

        BTableType tableType = new BTableType(TypeTags.TABLE, symTable.empty, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        tableType.tsymbol = Symbols.createTypeSymbol(SymTag.TYPE, Flags.asMask(EnumSet.noneOf(Flag.class)),
                typeSymbol.name, typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID,
                tableType, symEnv.scope.owner, td.pos, BUILTIN);
        tableType.tsymbol.flags = typeSymbol.flags;
        tableType.constraintPos = td.constraint.pos;
        tableType.isTypeInlineDefined = td.isTypeInlineDefined;
        td.setBType(tableType);
        resolvingTypes.push(tableType);

        BType constraintType =
                resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);
        tableType.constraint = constraintType;

        if (td.tableKeyTypeConstraint != null) {
            tableType.keyTypeConstraint =
                    resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1,
                            td.tableKeyTypeConstraint.keyType, data);
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

        if (Types.getImpliedType(constraintType).tag == TypeTags.MAP &&
                (!tableType.fieldNameList.isEmpty() || tableType.keyTypeConstraint != null) &&
                !tableType.tsymbol.owner.getFlags().contains(Flag.LANG_LIB)) {
            dlog.error(tableType.keyPos,
                    DiagnosticErrorCode.KEY_CONSTRAINT_NOT_SUPPORTED_FOR_TABLE_WITH_MAP_CONSTRAINT);
            return symTable.semanticError;
        }

        resolvingTypes.pop();
        symResolver.markParameterizedType(tableType, constraintType);
        td.tableType = tableType;

        return tableType;
    }

    private BType resolveTypeDesc(BLangStreamType td, ResolverData data) {
        currentDepth = data.depth;
        if (td.getBType() != null) {
            return td.getBType();
        }

        SymbolEnv symEnv = data.env;
        BType type = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.type, data);
        BType error = td.error != null ?
                resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.error, data) : symTable.nilType;

        BStreamType streamType = new BStreamType(TypeTags.STREAM, symTable.empty, error, null);
        BTypeSymbol typeSymbol = type.tsymbol;
        streamType.tsymbol = Symbols.createTypeSymbol(typeSymbol.tag, typeSymbol.flags, typeSymbol.name,
                typeSymbol.originalName, symEnv.enclPkg.symbol.pkgID, streamType,
                symEnv.scope.owner, td.pos, BUILTIN);
        td.setBType(streamType);
        resolvingTypes.push(streamType);

        BType constraintType = resolveTypeDesc(symEnv, data.typeDefinition, data.depth + 1, td.constraint, data);
        streamType.constraint = constraintType;

        symResolver.markParameterizedType(streamType, constraintType);
        if (error != null) {
            symResolver.markParameterizedType(streamType, error);
        }
        resolvingTypes.pop();
        return streamType;
    }

    public BType visitBuiltInTypeNode(BLangType typeNode, SymbolResolver.AnalyzerData data, TypeKind typeKind) {
        Name typeName = names.fromTypeKind(typeKind);
        BSymbol typeSymbol =
                symResolver.lookupMemberSymbol(typeNode.pos, symTable.rootScope, data.env, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            dlog.error(typeNode.pos, data.diagCode, typeName);
        }

        typeNode.setBType(typeSymbol.type);
        return typeSymbol.type;
    }

    public BType defineTypeDefinition(BLangTypeDefinition typeDefinition, BType resolvedType, SymbolEnv env) {

        if (resolvedType == null || resolvedType == symTable.noType || typeDefinition.symbol != null) {
            return resolvedType;
        }

        // Check for any circular type references
        NodeKind typeNodeKind = typeDefinition.typeNode.getKind();
        if (typeNodeKind == NodeKind.OBJECT_TYPE || typeNodeKind == NodeKind.RECORD_TYPE) {
            if (resolvedType.tsymbol.scope == null) {
                resolvedType.tsymbol.scope = new Scope(resolvedType.tsymbol);
            }
        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            resolvedType.tsymbol = createEnumSymbol(typeDefinition, resolvedType, env);
        }

        typeDefinition.setPrecedence(this.typePrecedence++);
        BSymbol typeDefSymbol = Symbols.createTypeDefinitionSymbol(Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), env.enclPkg.packageID, resolvedType, env.scope.owner,
                typeDefinition.name.pos, symEnter.getOrigin(typeDefinition.name.value));
        typeDefSymbol.markdownDocumentation =
                symEnter.getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_REF, typeDefSymbol.flags, typeDefSymbol.name,
                typeDefSymbol.pkgID, typeDefSymbol.type, typeDefSymbol.owner, typeDefSymbol.pos, typeDefSymbol.origin);
        typeSymbol.markdownDocumentation = typeDefSymbol.markdownDocumentation;
        ((BTypeDefinitionSymbol) typeDefSymbol).referenceType = new BTypeReferenceType(resolvedType, typeSymbol,
                typeDefSymbol.type.flags);

        if (resolvedType == symTable.semanticError && resolvedType.tsymbol == null) {
            typeDefinition.symbol = typeDefSymbol;
            return resolvedType;
        }

        //todo remove after type ref introduced to runtime
        if (resolvedType.tsymbol.name == Names.EMPTY) {
            resolvedType.tsymbol.name = names.fromIdNode(typeDefinition.name);
            resolvedType.tsymbol.originalName = names.originalNameFromIdNode(typeDefinition.name);
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

        BType referenceConstraintType = Types.getReferredType(resolvedType);
        boolean isIntersectionType = referenceConstraintType.tag == TypeTags.INTERSECTION;

        BType effectiveDefinedType = isIntersectionType ? ((BIntersectionType) referenceConstraintType).effectiveType :
                referenceConstraintType;

        BTypeSymbol effectiveTypeSymbol = effectiveDefinedType.tsymbol;
        if (isIntersectionType && effectiveTypeSymbol != null && effectiveTypeSymbol.name == Names.EMPTY) {
            effectiveTypeSymbol.name = typeDefSymbol.name;
            effectiveTypeSymbol.pkgID = typeDefSymbol.pkgID;
        }

        symEnter.handleDistinctDefinition(typeDefinition, typeDefSymbol, resolvedType, effectiveDefinedType);
        handleDistinctDefinitionOfErrorIntersection(typeDefinition, typeDefSymbol, resolvedType);

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
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
                typeDefinition.typeNode.setBType(typeDefSymbol.type);
                resolvedType = typeDefSymbol.type;
            } else {
                dlog.error(typeDefinition.pos, DiagnosticErrorCode.TYPE_PARAM_OUTSIDE_LANG_MODULE);
            }
        }
        resolvedType.flags |= typeDefSymbol.flags;

        typeDefinition.symbol = typeDefSymbol;

        boolean isLanglibModule = PackageID.isLangLibPackageID(env.enclPkg.packageID);
        if (isLanglibModule) {
            symEnter.handleLangLibTypes(typeDefinition, env);
            return resolvedType;
        }

        symEnter.defineSymbol(typeDefinition.name.pos, typeDefSymbol, env);
        return resolvedType;
    }

    private BEnumSymbol createEnumSymbol(BLangTypeDefinition typeDefinition, BType definedType, SymbolEnv env) {
        List<BConstantSymbol> enumMembers = new ArrayList<>();

        List<BLangType> members = ((BLangUnionTypeNode) typeDefinition.typeNode).memberTypeNodes;
        for (BLangType member : members) {
            enumMembers.add((BConstantSymbol) ((BLangUserDefinedType) member).symbol);
        }

        BEnumSymbol enumSymbol = new BEnumSymbol(enumMembers, Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), names.fromIdNode(typeDefinition.name),
                env.enclPkg.symbol.pkgID, definedType, env.scope.owner,
                typeDefinition.pos, SOURCE);

        enumSymbol.name = names.fromIdNode(typeDefinition.name);
        enumSymbol.originalName = names.fromIdNode(typeDefinition.name);
        enumSymbol.flags |= Flags.asMask(typeDefinition.flagSet);

        enumSymbol.markdownDocumentation =
                symEnter.getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        enumSymbol.pkgID = env.enclPkg.packageID;
        return enumSymbol;
    }

    public void resolveConstant(SymbolEnv symEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        if (resolvingConstants.contains(constant)) { // To identify cycles.
            dlog.error(constant.pos, DiagnosticErrorCode.CONSTANT_CYCLIC_REFERENCE,
                    (this.resolvingConstants).stream().map(constNode -> constNode.symbol)
                            .collect(Collectors.toList()));
            constant.setBType(symTable.semanticError);
            return;
        }
        resolvingConstants.add(constant);
        if (resolvedConstants.contains(constant)) { // Already resolved constant.
            resolvingConstants.remove(constant);
            return;
        }
        defineConstant(symEnv, modTable, constant);
        resolvingConstants.remove(constant);
        resolvedConstants.add(constant);
        checkUniqueness(constant);
    }

    public void resolveXMLNS(SymbolEnv symEnv, BLangXMLNS xmlnsNode) {
        if (xmlnsNode.namespaceURI.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) xmlnsNode.namespaceURI;
            varRef.symbol = getSymbolOfVarRef(varRef.pos, symEnv, names.fromIdNode(varRef.pkgAlias),
                            names.fromIdNode(varRef.variableName));
        }
        symEnter.defineXMLNS(symEnv, xmlnsNode);
    }

    public BSymbol getSymbolOfVarRef(Location pos, SymbolEnv env, Name pkgAlias, Name varName) {
        if (pkgAlias == Names.EMPTY && modTable.containsKey(varName.value)) {
            // modTable contains the available constants in current module.
            BLangNode node = modTable.get(varName.value);
            if (node.getKind() == NodeKind.CONSTANT) {
                if (!resolvedConstants.contains((BLangConstant) node)) {
                    resolveConstant(env, modTable, (BLangConstant) node);
                }
            } else {
                dlog.error(pos, DiagnosticErrorCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                return symTable.notFoundSymbol;
            }
        }

        // Search and get the referenced variable from different module.
        return symResolver.lookupMainSpaceSymbolInPackage(pos, env, pkgAlias, varName);
    }

    private void checkUniqueness(BLangConstant constant) {
        if (constant.symbol.kind != SymbolKind.CONSTANT) {
            return;
        }

        String nameString = constant.name.value;
        BLangConstantValue value = constant.symbol.value;
        if (!constantMap.containsKey(nameString)) {
            constantMap.put(nameString, value);
            return;
        }

        if (value == null) {
            dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL, nameString);
            return;
        }

        BLangConstantValue lastValue = constantMap.get(nameString);
        if (!value.equals(lastValue)) {
            if (lastValue == null) {
                dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL, nameString);
                return;
            }

            dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL_WITH_ANOTHER,
                    nameString, lastValue);
        }
    }

    private void defineConstant(SymbolEnv symEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        BType staticType;
        BConstantSymbol constantSymbol = symEnter.getConstantSymbol(constant);
        constant.symbol = constantSymbol;
        BLangTypeDefinition typeDef = constant.associatedTypeDefinition;
        NodeKind nodeKind = constant.expr.getKind();
        boolean isLiteral = nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL
                || nodeKind == NodeKind.UNARY_EXPR;
        if (typeDef != null && isLiteral) {
            resolveTypeDefinition(symEnv, modTable, typeDef, 0);
        }
        if (constant.typeNode != null) {
            // Type node is available.
            ResolverData data = new ResolverData();
            data.modTable = modTable;
            staticType = resolveTypeDesc(symEnv, typeDef, 0, constant.typeNode, data);
        } else {
            staticType = symTable.noType;
        }

        ConstantTypeChecker.AnalyzerData data = new ConstantTypeChecker.AnalyzerData();
        data.constantSymbol = constantSymbol;
        data.env = symEnv;
        data.modTable = modTable;
        data.expType = staticType;
        data.anonTypeNameSuffixes.push(constant.name.value);
        // Type check and resolve the constant expression.
        BType resolvedType = constantTypeChecker.checkConstExpr(constant.expr, staticType, data);
        data.anonTypeNameSuffixes.pop();

        if (resolvedType == symTable.semanticError) {
            // Constant expression contains errors.
            constant.setBType(symTable.semanticError);
            constantSymbol.type = symTable.semanticError;
            symEnv.scope.define(constantSymbol.name, constantSymbol);
            return;
        }

        if (resolvedType.tag == TypeTags.FINITE) {
            resolvedType.tsymbol.originalName = names.originalNameFromIdNode(constant.name);
        }

        // Get immutable type for the narrowed type.
        BType intersectionType = ImmutableTypeCloner.getImmutableType(constant.pos, types, resolvedType, symEnv,
                symEnv.scope.owner.pkgID, symEnv.scope.owner, symTable, anonymousModelHelper, names,
                new HashSet<>());

        // Fix the constant expr types due to tooling requirements.
        resolveConstantExpressionType.resolveConstExpr(constant.expr, intersectionType, data);

        // Update the final type in necessary fields.
        constantSymbol.type = intersectionType;
        if (intersectionType.tag == TypeTags.FINITE) {
            constantSymbol.literalType = ((BFiniteType) intersectionType).getValueSpace().iterator().next().getBType();
        } else {
            constantSymbol.literalType = intersectionType;
        }
        constant.setBType(intersectionType);

        // Get the constant value from the final type.
        constantSymbol.value = constantTypeChecker.getConstantValue(intersectionType);

        if (isLiteral && constantSymbol.type.tag != TypeTags.TYPEREFDESC && typeDef != null) {
            // Update flags.
            constantSymbol.type.tsymbol.flags |= typeDef.symbol.flags;
        }

        constantSymbol.markdownDocumentation =
                symEnter.getMarkdownDocAttachment(constant.markdownDocumentationAttachment);
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
        addAssociatedTypeDefinition(constant, intersectionType, symEnv);
        constant.setDeterminedType(null);
        symEnv.scope.define(constantSymbol.name, constantSymbol);
    }

    public BLangTypeDefinition findTypeDefinition(List<BLangTypeDefinition> typeDefinitionArrayList, String name) {
        for (int i = typeDefinitionArrayList.size() - 1; i >= 0; i--) {
            BLangTypeDefinition typeDefinition = typeDefinitionArrayList.get(i);
            if (typeDefinition.name.value.equals(name)) {
                return typeDefinition;
            }
        }
        return null;
    }

    private void addAssociatedTypeDefinition(BLangConstant constant, BType type,
                                             SymbolEnv symEnv) {
        if (type.tag == TypeTags.INTERSECTION) {
            BIntersectionType immutableType = (BIntersectionType) type;

            if (immutableType.effectiveType.tag == TypeTags.RECORD) {
                constant.associatedTypeDefinition = findTypeDefinition(symEnv.enclPkg.typeDefinitions,
                        immutableType.effectiveType.tsymbol.name.value);
            }
        }
    }

    /**
     * This class holds the data required for the module type resolving.
     *
     * @since 2201.7.0
     */
    class ResolverData {
        SymbolEnv env;
        Map<String, BLangNode> modTable;
        int depth;
        BLangTypeDefinition typeDefinition;
    }

    /**
     * Used to store location data for encountered unknown types.
     *
     * @since 2201.7.0
     */
    class LocationData {
        private String name;
        private int row;
        private int column;

        LocationData(String name, int row, int column) {
            this.name = name;
            this.row = row;
            this.column = column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LocationData that = (LocationData) o;
            return row == that.row &&
                    column == that.column &&
                    name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, row, column);
        }
    }
}
