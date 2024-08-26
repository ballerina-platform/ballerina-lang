/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.semtype.port.test;

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.FunctionQualifiers;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import io.ballerina.types.definition.Member;
import io.ballerina.types.definition.ObjectDefinition;
import io.ballerina.types.definition.ObjectQualifiers;
import io.ballerina.types.definition.StreamDefinition;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.TableSubtype;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.jetbrains.annotations.NotNull;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter.getTypeOrClassName;

/**
 * Resolves sem-types for module definitions.
 *
 * @since 2201.10.0
 */
public class SemTypeResolver {

    private final Map<BLangFunction, Definition> attachedDefinitions = new HashMap<>();

    void defineSemTypes(List<BLangNode> moduleDefs, Context cx) {
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                throw new UnsupportedOperationException("Semtype are not supported for class definitions yet");
            } else if (def.getKind() == NodeKind.CONSTANT) {
                resolveConstant(cx, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefn(cx, modTable, typeDefinition, 0);
            }
        }
    }

    private void resolveConstant(Context cx, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype = evaluateConst(constant);
        addSemTypeBType(constant.getTypeNode(), semtype);
        cx.env.addTypeDef(constant.name.value, semtype);
    }

    private SemType evaluateConst(BLangConstant constant) {
        switch (constant.symbol.value.type.getKind()) {
            case INT:
                return SemTypes.intConst((long) constant.symbol.value.value);
            case BOOLEAN:
                return SemTypes.booleanConst((boolean) constant.symbol.value.value);
            case STRING:
                return SemTypes.stringConst((String) constant.symbol.value.value);
            case FLOAT:
                return SemTypes.floatConst((double) constant.symbol.value.value);
            default:
                throw new UnsupportedOperationException("Expression type not implemented for const semtype");
        }
    }

    private SemType resolveTypeDefn(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth) {
        if (defn.semType != null) {
            return defn.semType;
        }

        if (depth == defn.semCycleDepth) {
            throw new IllegalStateException("cyclic type definition: " + defn.name.value);
        }
        defn.semCycleDepth = depth;
        SemType s = resolveTypeDesc(cx, mod, defn, depth, defn.typeNode);
        addSemTypeBType(defn.getTypeNode(), s);
        if (defn.semType == null) {
            defn.semType = s;
            defn.semCycleDepth = -1;
            cx.env.addTypeDef(defn.name.value, s);
            return s;
        } else {
            return s;
        }
    }

    private void addSemTypeBType(BLangType typeNode, SemType semType) {
        if (typeNode != null) {
            typeNode.getBType().semType(semType);
        }
    }

    public SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                   TypeNode td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc(cx, (BLangValueType) td);
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc(cx, (BLangBuiltInRefTypeNode) td);
            case RECORD_TYPE:
                return resolveTypeDesc(cx, (BLangRecordTypeNode) td, mod, depth, defn);
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                return resolveTypeDesc(cx, (BLangConstrainedType) td, mod, depth, defn);
            case UNION_TYPE_NODE:
                return resolveTypeDesc(cx, (BLangUnionTypeNode) td, mod, depth, defn);
            case INTERSECTION_TYPE_NODE:
                return resolveTypeDesc(cx, (BLangIntersectionTypeNode) td, mod, depth, defn);
            case USER_DEFINED_TYPE:
                return resolveTypeDesc(cx, (BLangUserDefinedType) td, mod, depth);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td);
            case ARRAY_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangArrayType) td);
            case TUPLE_TYPE_NODE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangTupleTypeNode) td);
            case FUNCTION_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangFunctionTypeNode) td);
            case TABLE_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangTableTypeNode) td);
            case ERROR_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangErrorType) td);
            case OBJECT_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangObjectTypeNode) td);
            case STREAM_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangStreamType) td);
            default:
                throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        }
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangObjectTypeNode td) {
        SemType innerType = resolveNonDistinctObject(cx, mod, defn, depth, td);
        if (td.flagSet.contains(Flag.DISTINCT)) {
            return getDistinctObjectType(cx, innerType);
        }
        return innerType;
    }

    private static SemType getDistinctObjectType(Context cx, SemType innerType) {
        return Core.intersect(SemTypes.objectDistinct(cx.env.distinctAtomCountGetAndIncrement()), innerType);
    }

    private SemType resolveNonDistinctObject(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                             int depth, BLangObjectTypeNode td) {
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }
        ObjectDefinition od = new ObjectDefinition();
        Stream<Member> fieldStream = td.fields.stream().map(field -> {
            Set<Flag> flags = field.flagSet;
            Member.Visibility visibility = flags.contains(Flag.PUBLIC) ? Member.Visibility.Public :
                    Member.Visibility.Private;
            SemType ty = resolveTypeDesc(cx, mod, defn, depth + 1, field.typeNode);
            return new Member(field.name.value, ty, Member.Kind.Field, visibility, flags.contains(Flag.READONLY));
        });
        Stream<Member> methodStream = td.getFunctions().stream().map(method -> {
            Member.Visibility visibility = method.flagSet.contains(Flag.PUBLIC) ? Member.Visibility.Public :
                    Member.Visibility.Private;
            SemType ty = resolveTypeDesc(cx, mod, defn, depth + 1, method);
            return new Member(method.name.value, ty, Member.Kind.Method, visibility, true);
        });
        td.defn = od;
        List<Member> members = Stream.concat(fieldStream, methodStream).toList();
        ObjectQualifiers qualifiers = getQualifiers(td);
        return od.define(cx.env, qualifiers, members);
    }

    private static ObjectQualifiers getQualifiers(BLangObjectTypeNode td) {
        Set<Flag> flags = td.symbol.getFlags();
        ObjectQualifiers.NetworkQualifier networkQualifier;
        assert !(flags.contains(Flag.CLIENT) && flags.contains(Flag.SERVICE)) :
                "object can't be both client and service";
        if (flags.contains(Flag.CLIENT)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Client;
        } else if (flags.contains(Flag.SERVICE)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Service;
        } else {
            networkQualifier = ObjectQualifiers.NetworkQualifier.None;
        }
        return new ObjectQualifiers(flags.contains(Flag.ISOLATED), flags.contains(Flag.READONLY), networkQualifier);
    }

    // TODO: should we make definition part of BLangFunction as well?
    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangFunction functionType) {
        Definition attached = attachedDefinitions.get(functionType);
        if (attached != null) {
            return attached.getSemType(cx.env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        attachedDefinitions.put(functionType, fd);
        Map<String, BLangNode> paramScope = new HashMap<>();
        List<SemType> params = getParameters(cx, mod, paramScope, defn, depth, functionType);
        SemType rest;
        if (functionType.getRestParameters() == null) {
            rest = PredefinedType.NEVER;
        } else {
            ArrayTypeNode arrayType = (ArrayTypeNode) functionType.getRestParameters().getTypeNode();
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, arrayType.getElementType());
        }
        SemType returnType = resolveReturnType(cx, mod, paramScope, defn, depth + 1, functionType.getReturnTypeNode());
        ListDefinition paramListDefinition = new ListDefinition();
        FunctionQualifiers qualifiers = FunctionQualifiers.from(cx.env, functionType.flagSet.contains(Flag.ISOLATED),
                functionType.flagSet.contains(Flag.TRANSACTIONAL));
        return fd.define(cx.env, paramListDefinition.defineListTypeWrapped(cx.env, params, params.size(), rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE), returnType, qualifiers);
    }

    @NotNull
    private List<SemType> getParameters(Context cx, Map<String, BLangNode> mod, Map<String, BLangNode> paramScope,
                                        BLangTypeDefinition defn, int depth, BLangFunction functionType) {
        List<SemType> params = new ArrayList<>();
        if (functionType instanceof BLangResourceFunction resourceFunctionType) {
            params.add(SemTypes.stringConst(resourceFunctionType.methodName.value));
            for (var each : resourceFunctionType.resourcePathSegments) {
                params.add(resolveTypeDesc(cx, mod, defn, depth + 1, each.typeNode));
            }
        }
        for (BLangSimpleVariable paramVar : functionType.getParameters()) {
            SemType semType = resolveTypeDesc(cx, mod, defn, depth + 1, paramVar.typeNode);
            if (Core.isSubtypeSimple(semType, PredefinedType.TYPEDESC)) {
                paramScope.put(paramVar.name.value, paramVar);
            }
            params.add(semType);
        }
        return params;
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangFunctionTypeNode td) {
        if (isFunctionTop(td)) {
            if (td.flagSet.contains(Flag.ISOLATED) || td.flagSet.contains(Flag.TRANSACTIONAL)) {
                FunctionQualifiers qualifiers = FunctionQualifiers.from(cx.env, td.flagSet.contains(Flag.ISOLATED),
                        td.flagSet.contains(Flag.TRANSACTIONAL));
                // I think param type here is wrong. It should be the intersection of all list types, but I think
                //  never is close enough
                return new FunctionDefinition().define(cx.env, PredefinedType.NEVER, PredefinedType.VAL, qualifiers);
            }
            return PredefinedType.FUNCTION;
        }
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        td.defn = fd;
        Map<String, BLangNode> tdScope = new HashMap<>();
        List<SemType> params = new ArrayList<>(td.params.size());
        for (BLangSimpleVariable param : td.params) {
            SemType paramType = resolveTypeDesc(cx, mod, defn, depth + 1, param.typeNode);
            params.add(paramType);
            if (Core.isSubtypeSimple(paramType, PredefinedType.TYPEDESC)) {
                tdScope.put(param.name.value, param);
            }
        }
        SemType rest;
        if (td.restParam == null) {
            rest = PredefinedType.NEVER;
        } else {
            BLangArrayType restArrayType = (BLangArrayType) td.restParam.typeNode;
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, restArrayType.elemtype);
        }
        SemType returnType = resolveReturnType(cx, mod, tdScope, defn, depth + 1, td.returnTypeNode);
        ListDefinition paramListDefinition = new ListDefinition();
        FunctionQualifiers qualifiers = FunctionQualifiers.from(cx.env, td.flagSet.contains(Flag.ISOLATED),
                td.flagSet.contains(Flag.TRANSACTIONAL));
        return fd.define(cx.env, paramListDefinition.defineListTypeWrapped(cx.env, params, params.size(), rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE), returnType, qualifiers);
    }

    private SemType resolveReturnType(Context cx, Map<String, BLangNode> mod,
                                      Map<String, BLangNode> mayBeDependentlyTypeNodes, BLangTypeDefinition defn,
                                      int depth, BLangType returnTypeNode) {
        if (returnTypeNode == null) {
            return PredefinedType.NIL;
        }
        SemType innerType;
        // Dependently typed function are quite rare so doing it via exception handling should be faster than actually
        //  checking if it is a dependently typed one.
        boolean isDependentlyType;
        try {
            innerType = resolveTypeDesc(cx, mod, defn, depth + 1, returnTypeNode);
            isDependentlyType = false;
        } catch (IndexOutOfBoundsException err) {
            innerType =
                    resolveDependentlyTypedReturnType(cx, mod, mayBeDependentlyTypeNodes, defn, depth, returnTypeNode);
            isDependentlyType = true;
        }
        ListDefinition ld = new ListDefinition();
        return ld.tupleTypeWrapped(cx.env,
                !isDependentlyType ? PredefinedType.BOOLEAN : SemTypes.booleanConst(true), innerType);
    }

    private SemType resolveDependentlyTypedReturnType(Context cx, Map<String, BLangNode> mod,
                                                      Map<String, BLangNode> mayBeDependentlyTypeNodes,
                                                      BLangTypeDefinition defn, int depth,
                                                      TypeNode returnTypeNode) {
        Map<String, BLangNode> combined = new HashMap<>(mod);
        combined.putAll(mayBeDependentlyTypeNodes);
        return resolveTypeDesc(cx, combined, defn, depth + 1, returnTypeNode);
    }

    private boolean isFunctionTop(BLangFunctionTypeNode td) {
        return td.params.isEmpty() && td.restParam == null && td.returnTypeNode == null;
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangArrayType td) {
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }
        ListDefinition ld = new ListDefinition();
        td.defn = ld;

        int dimensions = td.dimensions;
        SemType accum = resolveTypeDesc(cx, mod, defn, depth + 1, td.elemtype);
        for (int i = 0; i < dimensions; i++) {
            int size = from(mod, td.sizes.get(i));
            if (i == dimensions - 1) {
                accum = resolveListInner(cx, ld, size, accum);
            } else {
                accum = resolveListInner(cx, size, accum);
            }
        }
        return accum;
    }

    private static int from(Map<String, BLangNode> mod, BLangNode expr) {
        if (expr instanceof BLangLiteral literal) {
            return listSize((Number) literal.value);
        } else if (expr instanceof BLangSimpleVarRef varRef) {
            String varName = varRef.variableName.value;
            return from(mod, mod.get(varName));
        } else if (expr instanceof BLangConstant constant) {
            return listSize((Number) constant.symbol.value.value);
        }
        throw new UnsupportedOperationException("Unsupported expr kind " + expr.getKind());
    }

    private static int listSize(Number size) {
        if (size.longValue() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("list sizes greater than " + Integer.MAX_VALUE + " not yet supported");
        }
        return size.intValue();
    }

    private SemType resolveListInner(Context cx, int size, SemType eType) {
        ListDefinition ld = new ListDefinition();
        return resolveListInner(cx, ld, size, eType);
    }

    private static SemType resolveListInner(Context cx, ListDefinition ld, int size, SemType eType) {
        if (size != -1) {
            return ld.defineListTypeWrapped(cx.env, List.of(eType), Math.abs(size), PredefinedType.NEVER,
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        } else {
            return ld.defineListTypeWrapped(cx.env, List.of(), 0, eType,
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        }
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangTupleTypeNode td) {
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }
        ListDefinition ld = new ListDefinition();
        td.defn = ld;
        List<SemType> memberSemTypes =
                td.members.stream().map(member -> resolveTypeDesc(cx, mod, defn, depth + 1, member.typeNode))
                        .toList();
        SemType rest = td.restParamType != null ? resolveTypeDesc(cx, mod, defn, depth + 1, td.restParamType) :
                PredefinedType.NEVER;
        return ld.defineListTypeWrapped(cx.env, memberSemTypes, memberSemTypes.size(), rest);
    }

    private SemType resolveTypeDesc(Context cx, BLangValueType td) {
        switch (td.typeKind) {
            case NIL:
                return PredefinedType.NIL;
            case BOOLEAN:
                return PredefinedType.BOOLEAN;
            case BYTE:
                return PredefinedType.BYTE;
            case INT:
                return PredefinedType.INT;
            case FLOAT:
                return PredefinedType.FLOAT;
            case DECIMAL:
                return PredefinedType.DECIMAL;
            case STRING:
                return PredefinedType.STRING;
            case TYPEDESC:
                return PredefinedType.TYPEDESC;
            case ERROR:
                return PredefinedType.ERROR;
            case HANDLE:
                return PredefinedType.HANDLE;
            case XML:
                return PredefinedType.XML;
            case ANY:
                return PredefinedType.ANY;
            case READONLY:
                return PredefinedType.VAL_READONLY;
            case ANYDATA:
                return Core.createAnydata(cx);
            case JSON:
                return Core.createJson(cx);
            default:
                throw new IllegalStateException("Unknown type: " + td);
        }
    }

    private SemType resolveTypeDesc(Context cx, BLangBuiltInRefTypeNode td) {
        return switch (td.typeKind) {
            case NEVER -> PredefinedType.NEVER;
            case XML -> PredefinedType.XML;
            case JSON -> Core.createJson(cx);
            default -> throw new UnsupportedOperationException("Built-in ref type not implemented: " + td.typeKind);
        };
    }

    private SemType resolveTypeDesc(Context cx, BLangConstrainedType td, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition defn) {
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();
        return switch (typeKind) {
            case MAP -> resolveMapTypeDesc(td, cx, mod, depth, defn);
            case XML -> resolveXmlTypeDesc(td, cx, mod, depth, defn);
            case FUTURE -> resolveFutureTypeDesc(td, cx, mod, depth, defn);
            case TYPEDESC -> resolveTypedescTypeDesc(td, cx, mod, depth, defn);
            default -> throw new IllegalStateException("Unexpected constrained type: " + typeKind);
        };
    }

    private SemType resolveMapTypeDesc(BLangConstrainedType td, Context cx, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }

        MappingDefinition d = new MappingDefinition();
        td.defn = d;

        SemType rest = resolveTypeDesc(cx, mod, typeDefinition, depth + 1, td.constraint);
        return d.defineMappingTypeWrapped(cx.env, Collections.emptyList(), rest == null ? PredefinedType.NEVER : rest);
    }

    private SemType resolveXmlTypeDesc(BLangConstrainedType td, Context cx, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition defn) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return SemTypes.xmlSequence(constraint);
    }

    private SemType resolveFutureTypeDesc(BLangConstrainedType td, Context cx, Map<String, BLangNode> mod, int depth,
                                          BLangTypeDefinition defn) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return SemTypes.futureContaining(cx.env, constraint);
    }

    private SemType resolveTypedescTypeDesc(BLangConstrainedType td, Context cx, Map<String, BLangNode> mod, int depth,
                                            BLangTypeDefinition defn) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return SemTypes.typedescContaining(cx.env, constraint);
    }

    private SemType resolveTypeDesc(Context cx, BLangRecordTypeNode td, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }

        MappingDefinition d = new MappingDefinition();
        td.defn = d;

        List<Field> fields = new ArrayList<>();
        for (BLangSimpleVariable field : td.fields) {
            SemType ty = resolveTypeDesc(cx, mod, typeDefinition, depth + 1, field.typeNode);
            if (Core.isNever(ty)) {
                throw new IllegalStateException("record field can't be never");
            }
            fields.add(Field.from(field.name.value, ty, false, field.flagSet.contains(Flag.OPTIONAL)));
        }

        SemType rest;
        if (!td.isSealed() && td.getRestFieldType() == null) {
            rest = Core.createAnydata(cx);
        } else {
            rest = resolveTypeDesc(cx, mod, typeDefinition, depth + 1, td.restFieldType);
        }

        return d.defineMappingTypeWrapped(cx.env, fields, rest == null ? PredefinedType.NEVER : rest);
    }

    private SemType resolveTypeDesc(Context cx, BLangUnionTypeNode td, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
        SemType u = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            u = Core.union(u, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return u;
    }

    private SemType resolveTypeDesc(Context cx, BLangIntersectionTypeNode td, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
        SemType i = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            i = Core.intersect(i, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return i;
    }

    private SemType resolveTypeDesc(Context cx, BLangUserDefinedType td, Map<String, BLangNode> mod, int depth) {
        String name = td.typeName.value;
        // Need to replace this with a real package lookup
        if (td.pkgAlias.value.equals("int")) {
            return resolveIntSubtype(name);
        } else if (td.pkgAlias.value.equals("string") && name.equals("Char")) {
            return SemTypes.CHAR;
        } else if (td.pkgAlias.value.equals("xml")) {
            return resolveXmlSubtype(name);
        } else if (td.pkgAlias.value.equals("regexp") && name.equals("RegExp")) {
            return PredefinedType.REGEXP;
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            throw new IndexOutOfBoundsException("unknown type " + name);
        }

        switch (moduleLevelDef.getKind()) {
            case TYPE_DEFINITION -> {
                SemType ty = resolveTypeDefn(cx, mod, (BLangTypeDefinition) moduleLevelDef, depth);
                if (td.flagSet.contains(Flag.DISTINCT)) {
                    return getDistinctSemType(cx, ty);
                }
                return ty;
            }
            case CONSTANT -> {
                BLangConstant constant = (BLangConstant) moduleLevelDef;
                return resolveTypeDefn(cx, mod, constant.getAssociatedTypeDefinition(), depth);
            }
            case VARIABLE -> {
                // This happens when the type is a parameter of a dependently typed function
                BLangVariable variable = (BLangVariable) moduleLevelDef;
                BLangConstrainedType typeDescType = (BLangConstrainedType) variable.getTypeNode();
                return resolveTypeDesc(cx, mod, null, depth, typeDescType.constraint);
            }
            default -> throw new UnsupportedOperationException("class defns not implemented");
        }
    }

    private SemType getDistinctSemType(Context cx, SemType innerType) {
        if (Core.isSubtypeSimple(innerType, PredefinedType.OBJECT)) {
            return getDistinctObjectType(cx, innerType);
        } else if (Core.isSubtypeSimple(innerType, PredefinedType.ERROR)) {
            return getDistinctErrorType(cx, innerType);
        }
        throw new IllegalArgumentException("Distinct type not supported for: " + innerType);
    }

    private SemType resolveIntSubtype(String name) {
        // TODO: support MAX_VALUE
        return switch (name) {
            case "Signed8" -> SemTypes.SINT8;
            case "Signed16" -> SemTypes.SINT16;
            case "Signed32" -> SemTypes.SINT32;
            case "Unsigned8" -> SemTypes.UINT8;
            case "Unsigned16" -> SemTypes.UINT16;
            case "Unsigned32" -> SemTypes.UINT32;
            default -> throw new UnsupportedOperationException("Unknown int subtype: " + name);
        };
    }

    private SemType resolveXmlSubtype(String name) {
        return switch (name) {
            case "Element" -> SemTypes.XML_ELEMENT;
            case "Comment" -> SemTypes.XML_COMMENT;
            case "Text" -> SemTypes.XML_TEXT;
            case "ProcessingInstruction" -> SemTypes.XML_PI;
            default -> throw new IllegalStateException("Unknown XML subtype: " + name);
        };
    }

    private SemType resolveSingletonType(BLangFiniteTypeNode td) {
        return resolveSingletonType(td.valueSpace);
    }

    private SemType resolveSingletonType(List<BLangExpression> valueSpace) {
        List<SemType> types = new ArrayList<>();
        for (BLangExpression bLangExpression : valueSpace) {
            types.add(resolveSingletonType((BLangLiteral) bLangExpression));
        }

        Iterator<SemType> iter = types.iterator();
        SemType u = iter.next();
        while (iter.hasNext()) {
            u = SemTypes.union(u, iter.next());
        }
        return u;
    }

    private SemType resolveSingletonType(BLangLiteral literal) {
        return resolveSingletonType(literal.value, literal.getDeterminedType().getKind());
    }

    private SemType resolveSingletonType(Object value, TypeKind targetTypeKind) {
        return switch (targetTypeKind) {
            case NIL -> PredefinedType.NIL;
            case BOOLEAN -> SemTypes.booleanConst((Boolean) value);
            case INT, BYTE -> {
                assert !(value instanceof Byte);
                yield SemTypes.intConst(((Number) value).longValue());
            }
            case FLOAT -> {
                double doubleVal;
                if (value instanceof Long longValue) {
                    doubleVal = longValue.doubleValue();
                } else if (value instanceof Double doubleValue) {
                    doubleVal = doubleValue;
                } else {
                    // literal value will be a string if it wasn't within the bounds of what is supported by Java Long
                    // or Double when it was parsed in BLangNodeBuilder.
                    try {
                        doubleVal = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        // We reach here when there is a syntax error. Mock the flow with default float value.
                        yield FloatSubtype.floatConst(0);
                    }
                }
                yield SemTypes.floatConst(doubleVal);
                // literal value will be a string if it wasn't within the bounds of what is supported by Java Long
                // or Double when it was parsed in BLangNodeBuilder.
                // We reach here when there is a syntax error. Mock the flow with default float value.
            }
            case DECIMAL -> SemTypes.decimalConst((String) value);
            case STRING -> SemTypes.stringConst((String) value);
            default -> throw new UnsupportedOperationException("Finite type not implemented for: " + targetTypeKind);
        };
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangTableTypeNode td) {
        SemType tableConstraint = resolveTypeDesc(cx, mod, defn, depth, td.constraint);

        if (td.tableKeySpecifier != null) {
            List<IdentifierNode> fieldNameIdentifierList = td.tableKeySpecifier.fieldNameIdentifierList;
            String[] fieldNames = fieldNameIdentifierList.stream().map(IdentifierNode::getValue).toArray(String[]::new);
            return TableSubtype.tableContainingKeySpecifier(cx, tableConstraint, fieldNames);
        }

        if (td.tableKeyTypeConstraint != null) {
            SemType keyConstraint = resolveTypeDesc(cx, mod, defn, depth, td.tableKeyTypeConstraint.keyType);
            return TableSubtype.tableContainingKeyConstraint(cx, tableConstraint, keyConstraint);
        }

        return TableSubtype.tableContaining(cx.env, tableConstraint);
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangErrorType td) {
        SemType err;
        if (td.detailType == null) {
            err = PredefinedType.ERROR;
        } else {
            SemType detail = resolveTypeDesc(cx, mod, defn, depth, td.detailType);
            err = SemTypes.errorDetail(detail);
        }

        if (td.flagSet.contains(Flag.DISTINCT)) {
            err = getDistinctErrorType(cx, err);
        }
        return err;
    }

    private static SemType getDistinctErrorType(Context cx, SemType err) {
        return Core.intersect(SemTypes.errorDistinct(cx.env.distinctAtomCountGetAndIncrement()), err);
    }

    private SemType resolveTypeDesc(Context cx, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangStreamType td) {
        if (td.constraint == null) {
            return PredefinedType.STREAM;
        }

        if (td.defn != null) {
            return td.defn.getSemType(cx.env);
        }

        StreamDefinition d = new StreamDefinition();
        td.defn = d;

        SemType valueType = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        SemType completionType = td.error == null ?
                PredefinedType.NIL : resolveTypeDesc(cx, mod, defn, depth + 1, td.error);
        return d.define(cx.env, valueType, completionType);
    }
}
