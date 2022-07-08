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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.*;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.types.*;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.*;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * @since 2201.3.0
 */

public class TypeResolver {

    private static final CompilerContext.Key<TypeResolver> TYPE_RESOLVER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final SourceDirectory sourceDirectory;
    private final ConstantValueResolver constResolver;
    private List<BLangNode> unresolvedTypes;
    private Set<BLangNode> unresolvedRecordDueToFields;
    private boolean resolveRecordsUnresolvedDueToFields;
    private List<BLangClassDefinition> unresolvedClasses;
    private HashSet<SymbolEnter.LocationData> unknownTypeRefs;
    private List<PackageID> importedPackages;
    private int typePrecedence;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private PackageCache packageCache;
    private List<BLangNode> intersectionTypes;
    private Map<BType, BLangTypeDefinition> typeToTypeDef;

    private SymbolEnv env;

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
        this.sourceDirectory = context.get(SourceDirectory.class);
        this.importedPackages = new ArrayList<>();
        this.unknownTypeRefs = new HashSet<>();
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.constResolver = ConstantValueResolver.getInstance(context);
        this.intersectionTypes = new ArrayList<>();
    }

    public static TypeResolver getInstance(CompilerContext context) {
        TypeResolver typeResolver = context.get(TYPE_RESOLVER_KEY);
        if (typeResolver == null) {
            typeResolver = new TypeResolver(context);
        }

        return typeResolver;
    }

    public void defineBTypes(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        this.typePrecedence = 0;
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(symEnter.getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

        constResolver.resolve(pkgEnv.enclPkg.constants, pkgEnv.enclPkg.packageID, pkgEnv);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                // throw new IllegalStateException("Semtype are not supported for class definitions yet");
                // define it first
                BLangClassDefinition classDefinition = (BLangClassDefinition) def;
                //
//                symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(classDefinition);
            } else if (def.getKind() == NodeKind.CONSTANT) {
                resolveConstant(pkgEnv, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefn(pkgEnv, modTable, typeDefinition, 0);
            }
        }
    }

    private void resolveConstant(SymbolEnv symEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
//        SemType semtype;
//        if (constant.associatedTypeDefinition != null) {
//            semtype = resolveTypeDefn(semtypeEnv, modTable, constant.associatedTypeDefinition, 0);
//        } else {
//            semtype = evaluateConst(constant);
//        }
//        addSemtypeBType(constant.getTypeNode(), semtype);
//        semtypeEnv.addTypeDef(constant.name.value, semtype);
    }

//    private BType evaluateConst(BLangConstant constant) {
//        switch (constant.symbol.value.type.getKind()) {
//            case INT:
//                return SemTypes.intConst((long) constant.symbol.value.value);
//            case BOOLEAN:
//                return SemTypes.booleanConst((boolean) constant.symbol.value.value);
//            case STRING:
//                return  SemTypes.stringConst((String) constant.symbol.value.value);
//            case FLOAT:
//                return SemTypes.floatConst((double) constant.symbol.value.value);
//            default:
//                throw new AssertionError("Expression type not implemented for const semtype");
//        }
//    }

    private BType resolveTypeDefn(SymbolEnv symEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth) {
        if (defn.getBType() != null) {
            return defn.getBType();
        }

        if (depth == defn.cycleDepth) {
            dlog.error(defn.pos, DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, defn.name);
            return null;
        }
        defn.cycleDepth = depth;
        BType type = resolveTypeDesc(symEnv, mod, defn, depth, defn.typeNode);
        defineTypeDefn(defn, type, symEnv); // swj: define symbols, set flags ...
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(defn);
//        defn.getTypeNode().setBType(type); swj: not required since type is already set by defineTypeDefn().
        if (defn.getBType() == null) {
            defn.setBType(type);
            defn.cycleDepth = -1;
//            env.enclType.addTypeDef(defn.name.value, s);
            return type;
        } else {
            return type;
        }
    }

    private BType resolveTypeDesc(SymbolEnv symEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangType td) {
        if (td == null) {
            return symTable.semanticError;
        }

        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc((BLangValueType) td, symEnv);
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                return resolveTypeDesc((BLangConstrainedType) td, symEnv, mod, depth, defn);
            case ARRAY_TYPE:
                return resolveTypeDesc(((BLangArrayType) td), symEnv, mod, depth, defn);
            case TUPLE_TYPE_NODE:
                return resolveTypeDesc((BLangTupleTypeNode) td, symEnv, mod, depth, defn);
            case RECORD_TYPE:
                return resolveTypeDesc((BLangRecordTypeNode) td, symEnv, mod, depth, defn);
//            case OBJECT_TYPE: // swj: need to implement
//                return resolveTypeDesc((BLangObjectTypeNode) td, symEnv, mod, depth, defn);
//            case FUNCTION_TYPE:
//                return resolveTypeDesc((BLangFunctionTypeNode) td, symEnv, mod, depth, defn);
//            case ERROR_TYPE:
//                return resolveTypeDesc((BLangErrorType) td, symEnv, mod, depth, defn);
            case UNION_TYPE_NODE:
                return resolveTypeDesc((BLangUnionTypeNode) td, symEnv, mod, depth, defn);
            case INTERSECTION_TYPE_NODE:
                return resolveTypeDesc((BLangIntersectionTypeNode) td, symEnv, mod, depth, defn);
            case USER_DEFINED_TYPE:
                return resolveTypeDesc((BLangUserDefinedType) td, symEnv, mod, depth);
//            case BUILT_IN_REF_TYPE:
//                return resolveTypeDesc((BLangBuiltInRefTypeNode) td, symEnv);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td, symEnv);
//            case TABLE_TYPE:
//                return resolveTypeDesc((BLangTableTypeNode) td, symEnv, mod, depth);
            default:
                System.out.println("not implemented");
                return null;
//                throw new AssertionError("not implemented");
        }
    }

    private void defineFieldsOfTypeDefn(BLangNode typeDefOrObject, SymbolEnv symEnv) {
        // Temporarily
        List<BLangNode> typeAndClassDefs = new ArrayList<>();
        typeAndClassDefs.add(typeDefOrObject);
        symEnter.defineFields(typeAndClassDefs, symEnv);
        symEnter.populateTypeToTypeDefMap(typeAndClassDefs);
        symEnter.defineDependentFields(typeAndClassDefs, symEnv);
    }


    private BType resolveSingletonType(BLangFiniteTypeNode td, SymbolEnv symEnv) {
        return symResolver.resolveTypeNode(td, symEnv);
    }

//    private BType resolveTypeDesc(BLangBuiltInRefTypeNode td, SymbolEnv symEnv) {
//        switch (td.typeKind) {
//            case NEVER:
//                return PredefinedType.NEVER;
//            case XML:
//                return PredefinedType.XML;
//            default:
//                throw new AssertionError("Unknown type kind: " + td.typeKind);
//        }
//    }

//    private BType resolveTypeDesc(BLangTableTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth) {
//        SemType memberType =
//                resolveTypeDesc(semtypeEnv, mod, (BLangTypeDefinition) td.constraint.defn, depth, td.constraint);
//        return SemTypes.tableContaining(memberType);
//    }

    private BType resolveTypeDesc(BLangUserDefinedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth) {
        String name = td.typeName.value;
        // Need to replace this with a real package lookup
//        if (td.pkgAlias.value.equals("int")) {
//            return resolveIntSubtype(name);
//        } else if (td.pkgAlias.value.equals("string") && name.equals("Char")) {
//            return SemTypes.CHAR;
//        } else if (td.pkgAlias.value.equals("xml")) {
//            return resolveXmlSubtype(name);
//        }

        BType type = symResolver.resolveTypeNode(td, symEnv);
        if (type.getKind() != TypeKind.OTHER) { // swj: already resolved
            return type;
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            dlog.error(td.pos, DiagnosticErrorCode.UNKNOWN_TYPE, td.typeName);
            return null;
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            BLangTypeDefinition typeDefn = (BLangTypeDefinition) moduleLevelDef;
            BType resolvedType = resolveTypeDefn(symEnv, mod, typeDefn, depth);
            defineTypeDefn(typeDefn, resolvedType, symEnv);
            symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefn);
            return resolvedType;
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            return resolveTypeDefn(symEnv, mod, constant.associatedTypeDefinition, depth);
        } else {
            throw new AssertionError();
        }
    }

//    private BType resolveIntSubtype(String name) {
//        switch (name) {
//            case "Signed8":
//                return SemTypes.SINT8;
//            case "Signed16":
//                return SemTypes.SINT16;
//            case "Signed32":
//                return SemTypes.SINT32;
//            case "Unsigned8":
//                return SemTypes.UINT8;
//            case "Unsigned16":
//                return SemTypes.UINT16;
//            case "Unsigned32":
//                return SemTypes.UINT32;
//            default:
//                throw new IllegalStateException("Unknown int subtype: " + name);
//        }
//    }
//
//    private BType resolveXmlSubtype(String name) {
//        switch(name) {
//            case "Element":
//                return SemTypes.XML_ELEMENT;
//            case "Comment":
//                return SemTypes.XML_COMMENT;
//            case "Text":
//                return SemTypes.XML_TEXT;
//            case "ProcessingInstruction":
//                return SemTypes.XML_PI;
//            default:
//                throw new IllegalStateException("Unknown XML subtype: " + name);
//        }
//    }

    private BType resolveTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition defn) {
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();
        switch (typeKind) {
            case MAP:
                return resolveMapTypeDesc(td, symEnv, mod, depth, defn);
            case XML:
//                return resolveXmlTypeDesc(td, symEnv, mod, depth, defn);
            case TYPEDESC:
            default:
                throw new AssertionError("Unhandled type-kind: " + typeKind);
        }
    }

//    private BType resolveXmlTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
//                                       BLangTypeDefinition defn) {
//        if (td.defn != null) {
//            return td.defn.getSemType(semtypeEnv);
//        }
//        return SemTypes.xmlSequence(resolveTypeDesc(semtypeEnv, mod, defn, depth + 1, td.constraint));
//    }

    private BType resolveMapTypeDesc(BLangConstrainedType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition typeDefinition) {
        if (td.getBType() != null) {
            return td.getBType();
        }

//        MappingDefinition d = new MappingDefinition();
//        td.defn = d;

        resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, td.constraint);
        return symResolver.resolveTypeNode(td, symEnv);
    }

    private BType resolveTypeDesc(BLangRecordTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition typeDefinition) {
        if (td.getBType() != null) {
            return td.getBType();
        }
//
//        MappingDefinition d = new MappingDefinition();
//        td.defn = d;

//        List<Field> fields = new ArrayList<>();
        for (BLangSimpleVariable field : td.fields) {
//            String name = field.name.value;
            resolveTypeDesc(symEnv, mod, typeDefinition, depth + 1, field.typeNode);
//            fields.add(Field.from(name, t));
        }

        BType resolvedType = symResolver.resolveTypeNode(td, symEnv);
        defineTypeDefn(typeDefinition, resolvedType, symEnv); // swj: define symbols, set flags ...
        symEnter.populateDistinctTypeIdsFromIncludedTypeReferences(typeDefinition);
        defineFieldsOfTypeDefn(typeDefinition, symEnv);

        return resolvedType;

//        SemType rest;
//        if (!td.isSealed() && td.getRestFieldType() == null) {
//            throw new AssertionError("Open record not supported yet");
//        } else {
//            rest = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, td.restFieldType);
//        }
//
//        return d.define(semtypeEnv, fields, rest == null ? PredefinedType.NEVER : rest);
    }

//    private BType resolveTypeDesc(BLangFunctionTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
//                                    BLangTypeDefinition typeDefinition) {
//        Definition defn = td.defn;
//        if (defn != null) {
//            return defn.getSemType(semtypeEnv);
//        }
//        FunctionDefinition d = new FunctionDefinition(semtypeEnv);
//        td.defn = d;
//
//        List<SemType> paramTypes = new ArrayList<>();
//        for (BLangVariable p : td.params) {
//            paramTypes.add(resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, p.typeNode));
//        }
//
//        SemType rest = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, td.returnTypeNode);
//        SemType args = SemTypes.tuple(semtypeEnv, paramTypes.toArray(new SemType[]{}));
//        return d.define(semtypeEnv, args, rest);
//    }
//
//    private BType resolveTypeDesc(BLangErrorType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
//                                    BLangTypeDefinition defn) {
//        if (td.detailType == null) {
//            return PredefinedType.ERROR;
//        }
//
//        SemType detail = resolveTypeDesc(semtypeEnv, mod, defn, depth, td.detailType);
//        return SemTypes.errorDetail(detail);
//    }

    private BType resolveTypeDesc(BLangUnionTypeNode td, SymbolEnv symEnv,
                                    Map<String, BLangNode> mod, int depth, BLangTypeDefinition defn) {
//        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
//        SemType u = resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next());
//        while (iterator.hasNext()) {
//            u = SemTypes.union(u, resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next()));
//        }
//        return u;
        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
        resolveTypeDesc(symEnv, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            resolveTypeDesc(symEnv, mod, defn, depth, iterator.next());
        }
        return symResolver.resolveTypeNode(td, symEnv);
    }

    private BType resolveTypeDesc(BLangIntersectionTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
//        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
//        SemType type = resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next());
//        while (iterator.hasNext()) {
//            type = SemTypes.intersection(type, resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next()));
//        }
//        return type;
        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
        resolveTypeDesc(symEnv, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            resolveTypeDesc(symEnv, mod, defn, depth, iterator.next());
        }
        return symResolver.resolveTypeNode(td, symEnv);
    }

    private BType resolveTypeDesc(BLangValueType defn, SymbolEnv symbolEnv) {
        return symResolver.resolveTypeNode(defn, symbolEnv);
    }

    private BType resolveTypeDesc(BLangArrayType td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition moduleDefn) {
//        Definition defn = td.defn;
        if (td.getBType() != null) {
            return td.getBType();//defn.getSemType(semtypeEnv);
        }
//
//        ListDefinition d = new ListDefinition();
//        td.defn = d;
        BType elementType;

//        ArrayList<BLangExpression> reversed = new ArrayList<>(td.sizes);
//        for (BLangExpression t : reversed) {
//            // todo: We need to constFold this expression.
//            int size = constExprToInt(t);
//            if (size >= 0) {
//                elementType = d.define(semtypeEnv, new ArrayList<>(List.of(elementType)), size);
//            } else {
//                elementType = d.define(semtypeEnv, elementType);
//            }
//        }
        resolveTypeDesc(symEnv, mod, moduleDefn, depth + 1, td.elemtype); // swj: resolve the element type
        return symResolver.resolveTypeNode(td, symEnv); // now we can resolve array type
    }

    private int constExprToInt(BLangExpression t) {
        if (t.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BConstantSymbol symbol = (BConstantSymbol) ((BLangSimpleVarRef) t).symbol;
            return ((Long) symbol.value.value).intValue();
        }
        return (int) ((BLangLiteral) t).value;
    }

    private BType resolveTypeDesc(BLangTupleTypeNode td, SymbolEnv symEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition moduleDefn) {
//        Definition defn = td.defn;
        if (td.getBType() != null) {
            return td.getBType();
        }

//        ListDefinition d = new ListDefinition();
//        td.defn = d;
//        List<SemType> members = new ArrayList<>();
        for (BLangType memberTypeNode : td.memberTypeNodes) {
            resolveTypeDesc(symEnv, mod, moduleDefn, depth + 1, memberTypeNode);
        }

        resolveTypeDesc(symEnv, mod, moduleDefn, depth + 1, td.restParamType);

//        if (restType == null) {
//            restType = PredefinedType.NEVER;
//        }

        return symResolver.resolveTypeNode(td, symEnv);
    }

    public void defineTypeDefn(BLangTypeDefinition typeDefinition, BType resolvedType, SymbolEnv env) {
//        BType definedType;
//        if (typeDefinition.hasCyclicReference) {
//            definedType = getCyclicDefinedType(typeDefinition, env);
//        } else {
//            definedType = symResolver.resolveTypeNode(typeDefinition.typeNode, env);
//        }

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
            for (BLangType typeRef : structureTypeNode.typeRefs) {
                hasTypeInclusions = true;
                BType referencedType = symResolver.resolveTypeNode(typeRef, env);
                if (referencedType == symTable.noType) {
                    if (!this.unresolvedTypes.contains(typeDefinition)) {
                        this.unresolvedTypes.add(typeDefinition);
                        return;
                    }
                }
            }
        }

        // check for unresolved fields. This record may be referencing another record
        if (hasTypeInclusions && !this.resolveRecordsUnresolvedDueToFields && typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
            for (BLangSimpleVariable variable : structureTypeNode.fields) {
                Scope scope = new Scope(structureTypeNode.symbol);
                structureTypeNode.symbol.scope = scope;
                SymbolEnv typeEnv = SymbolEnv.createTypeEnv(structureTypeNode, scope, env);
                BType referencedType = symResolver.resolveTypeNode(variable.typeNode, typeEnv);
                if (referencedType == symTable.noType) {
                    if (this.unresolvedRecordDueToFields.add(typeDefinition) &&
                            !this.unresolvedTypes.contains(typeDefinition)) {
                        this.unresolvedTypes.add(typeDefinition);
                        return;
                    }
                }
            }
        }

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
                isIntersectionType && effectiveDefinedType.tsymbol != null;

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
            if (PackageID.isLangLibPackageID(this.env.enclPkg.packageID)) {
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

        if (typeDefinition.hasCyclicReference) {
            // Workaround for https://github.com/ballerina-platform/ballerina-lang/issues/29742
            typeDefinition.getBType().tsymbol = resolvedType.tsymbol;
        } else {
            boolean isLanglibModule = PackageID.isLangLibPackageID(env.enclPkg.packageID);
            if (isLanglibModule) {
                symEnter.handleLangLibTypes(typeDefinition);
                return;
            }
            // We may have already defined error intersection
            if (!isErrorIntersection || symEnter.lookupTypeSymbol(env, typeDefinition.name) == symTable.notFoundSymbol) {
                symEnter.defineSymbol(typeDefinition.name.pos, typeDefSymbol);
            }
        }
    }

}
