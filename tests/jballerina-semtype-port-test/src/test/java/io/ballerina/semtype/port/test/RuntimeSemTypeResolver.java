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

import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.ErrorUtils;
import io.ballerina.runtime.internal.types.semtype.FunctionDefinition;
import io.ballerina.runtime.internal.types.semtype.FunctionQualifiers;
import io.ballerina.runtime.internal.types.semtype.FutureUtils;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.types.semtype.Member;
import io.ballerina.runtime.internal.types.semtype.ObjectDefinition;
import io.ballerina.runtime.internal.types.semtype.ObjectQualifiers;
import io.ballerina.runtime.internal.types.semtype.StreamDefinition;
import io.ballerina.runtime.internal.types.semtype.TableUtils;
import io.ballerina.runtime.internal.types.semtype.TypedescUtils;
import io.ballerina.runtime.internal.types.semtype.XmlUtils;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;

/**
 * Resolves sem-types for module definitions using runtime side semtype implementation.
 *
 * @since 2201.11.0
 */
class RuntimeSemTypeResolver extends SemTypeResolver<SemType> {

    private static final SemType[] EMPTY_SEMTYPE_ARR = {};
    Map<BLangType, SemType> attachedSemType = new HashMap<>();
    Map<BLangTypeDefinition, SemType> semTypeMemo = new HashMap<>();
    Map<BLangNode, Definition> attachedDefinitions = new HashMap<>();

    @Override
    public void resolveTypeDefn(TypeTestContext<SemType> cx, Map<String, BLangNode> modTable,
                                BLangTypeDefinition typeDefinition) {
        resolveTypeDefnRec(cx, modTable, typeDefinition, 0);
    }

    @Override
    public void resolveConstant(TypeTestContext<SemType> cx, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype = evaluateConst(constant);
        attachToBType(constant.typeNode, semtype);
        cx.getEnv().addTypeDef(constant.name.value, semtype);
    }

    private SemType resolveTypeDefnRec(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                       BLangTypeDefinition defn, int depth) {
        SemType memo = semTypeMemo.get(defn);
        if (memo != null) {
            return memo;
        }
        if (depth == defn.semCycleDepth) {
            throw new IllegalStateException("cyclic type definition: " + defn.name.value);
        }

        defn.semCycleDepth = depth;
        SemType s = resolveTypeDesc(cx, mod, defn, depth, defn.typeNode);
        attachToBType(defn.getTypeNode(), s);
        if (!semTypeMemo.containsKey(defn)) {
            semTypeMemo.put(defn, s);
            defn.semCycleDepth = -1;
            cx.getEnv().addTypeDef(defn.name.value, s);
            return s;
        } else {
            return s;
        }
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth, TypeNode td) {
        if (td == null) {
            return null;
        }
        return switch (td.getKind()) {
            case VALUE_TYPE -> resolveTypeDesc(cx, (BLangValueType) td);
            case BUILT_IN_REF_TYPE -> resolveTypeDesc((BLangBuiltInRefTypeNode) td);
            case INTERSECTION_TYPE_NODE -> resolveTypeDesc(cx, (BLangIntersectionTypeNode) td, mod, depth, defn);
            case UNION_TYPE_NODE -> resolveTypeDesc(cx, (BLangUnionTypeNode) td, mod, depth, defn);
            case USER_DEFINED_TYPE -> resolveTypeDesc(cx, (BLangUserDefinedType) td, mod, depth);
            case FINITE_TYPE_NODE -> resolveSingletonType((BLangFiniteTypeNode) td);
            case ARRAY_TYPE -> resolveArrayTypeDesc(cx, mod, defn, depth, (BLangArrayType) td);
            case TUPLE_TYPE_NODE -> resolveTupleTypeDesc(cx, mod, defn, depth, (BLangTupleTypeNode) td);
            case CONSTRAINED_TYPE -> resolveConstrainedTypeDesc(cx, mod, defn, depth, (BLangConstrainedType) td);
            case RECORD_TYPE -> resolveRecordTypeDesc(cx, mod, defn, depth, (BLangRecordTypeNode) td);
            case FUNCTION_TYPE -> resolveFunctionTypeDesc(cx, mod, defn, depth, (BLangFunctionTypeNode) td);
            case OBJECT_TYPE -> resolveObjectTypeDesc(cx, mod, defn, depth, (BLangObjectTypeNode) td);
            case ERROR_TYPE -> resolveErrorTypeDesc(cx, mod, defn, depth, (BLangErrorType) td);
            case TABLE_TYPE -> resolveTableTypeDesc(cx, mod, defn, depth, (BLangTableTypeNode) td);
            case STREAM_TYPE -> resolveStreamTypeDesc(cx, mod, defn, depth, (BLangStreamType) td);
            default -> throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        };
    }

    private SemType resolveStreamTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                          BLangTypeDefinition defn, int depth, BLangStreamType td) {
        if (td.constraint == null) {
            return Builder.getStreamType();
        }
        Env env = (Env) cx.getInnerEnv();
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }
        StreamDefinition sd = new StreamDefinition();
        attachedDefinitions.put(td, sd);

        SemType valueType = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        SemType completionType = td.error == null ? Builder.getNilType() :
                resolveTypeDesc(cx, mod, defn, depth + 1, td.error);
        return sd.define(env, valueType, completionType);
    }

    private SemType resolveTableTypeDesc(TypeTestContext<SemType> cx,
                                         Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                         int depth, BLangTableTypeNode td) {
        SemType tableConstraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        Context context = (Context) cx.getInnerContext();
        if (td.tableKeySpecifier != null) {
            List<IdentifierNode> fieldNameIdentifierList = td.tableKeySpecifier.fieldNameIdentifierList;
            String[] fieldNames = fieldNameIdentifierList.stream().map(IdentifierNode::getValue).toArray(String[]::new);
            return TableUtils.tableContainingKeySpecifier(context, tableConstraint, fieldNames);
        }
        if (td.tableKeyTypeConstraint != null) {
            SemType keyConstraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.tableKeyTypeConstraint.keyType);
            return TableUtils.tableContainingKeyConstraint(context, tableConstraint, keyConstraint);
        }
        return TableUtils.tableContaining(context.env, tableConstraint);
    }


    private SemType resolveErrorTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                         BLangTypeDefinition defn, int depth, BLangErrorType td) {
        SemType innerType = createErrorType(cx, mod, defn, depth, td);
        if (td.flagSet.contains(Flag.DISTINCT)) {
            Env env = (Env) cx.getInnerEnv();
            return getDistinctErrorType(env, innerType);
        }
        return innerType;
    }

    private static SemType getDistinctErrorType(Env env, SemType innerType) {
        return Core.intersect(ErrorUtils.errorDistinct(env.distinctAtomCountGetAndIncrement()), innerType);
    }

    private SemType createErrorType(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth, BLangErrorType td) {
        if (td.detailType == null) {
            return Builder.getErrorType();
        } else {
            SemType detailType = resolveTypeDesc(cx, mod, defn, depth + 1, td.detailType);
            return ErrorUtils.errorDetail(detailType);
        }
    }

    private SemType resolveObjectTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                          BLangTypeDefinition defn, int depth, BLangObjectTypeNode td) {
        SemType innerType = resolveNonDistinctObject(cx, mod, defn, depth, td);
        if (td.flagSet.contains(Flag.DISTINCT)) {
            return getDistinctObjectType((Env) cx.getInnerEnv(), innerType);
        }
        return innerType;
    }

    private SemType resolveNonDistinctObject(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                             BLangTypeDefinition defn, int depth, BLangObjectTypeNode td) {
        Env env = (Env) cx.getInnerEnv();
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }
        ObjectDefinition od = new ObjectDefinition();
        attachedDefinitions.put(td, od);
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
            SemType ty = resolveFunctionType(cx, mod, defn, depth + 1, method);
            return new Member(method.name.value, ty, Member.Kind.Method, visibility, true);
        });
        List<Member> members = Stream.concat(fieldStream, methodStream).toList();
        ObjectQualifiers qualifiers = getQualifiers(td);
        return od.define(env, qualifiers, members, qualifiers.readonly() ? CELL_MUT_NONE :
                CELL_MUT_LIMITED);
    }

    private ObjectQualifiers getQualifiers(BLangObjectTypeNode td) {
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

    private SemType resolveFunctionType(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                        BLangTypeDefinition defn,
                                        int depth, BLangFunction functionType) {
        Env env = (Env) cx.getInnerEnv();
        Definition attached = attachedDefinitions.get(functionType);
        if (attached != null) {
            return attached.getSemType(env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        attachedDefinitions.put(functionType, fd);
        Map<String, BLangNode> paramScope = new HashMap<>();
        SemType[] params = getParameters(cx, mod, paramScope, defn, depth, functionType);
        SemType rest;
        if (functionType.getRestParameters() == null) {
            rest = Builder.getNeverType();
        } else {
            ArrayTypeNode arrayType = (ArrayTypeNode) functionType.getRestParameters().getTypeNode();
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, arrayType.getElementType());
        }
        SemType returnType = resolveReturnType(cx, mod, paramScope, defn, depth + 1, functionType.getReturnTypeNode());
        ListDefinition paramListDefinition = new ListDefinition();
        FunctionQualifiers qualifiers = FunctionQualifiers.create(functionType.flagSet.contains(Flag.ISOLATED),
                functionType.flagSet.contains(Flag.TRANSACTIONAL));
        return fd.define(env, paramListDefinition.defineListTypeWrapped(env, params, params.length, rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE), returnType, qualifiers);
    }

    private SemType[] getParameters(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                    Map<String, BLangNode> paramScope, BLangTypeDefinition defn, int depth,
                                    BLangFunction functionType) {
        List<SemType> params = new ArrayList<>();
        if (functionType instanceof BLangResourceFunction resourceFunctionType) {
            params.add(Builder.getStringConst(resourceFunctionType.methodName.value));
            for (var each : resourceFunctionType.resourcePathSegments) {
                params.add(resolveTypeDesc(cx, mod, defn, depth + 1, each.typeNode));
            }
        }
        for (BLangSimpleVariable paramVar : functionType.getParameters()) {
            SemType semType = resolveTypeDesc(cx, mod, defn, depth + 1, paramVar.typeNode);
            if (Core.isSubtypeSimple(semType, Builder.getTypeDescType())) {
                paramScope.put(paramVar.name.value, paramVar);
            }
            params.add(semType);
        }
        return params.toArray(SemType[]::new);
    }

    private SemType getDistinctObjectType(Env env, SemType innerType) {
        return Core.intersect(ObjectDefinition.distinct(env.distinctAtomCountGetAndIncrement()), innerType);
    }

    private SemType resolveFunctionTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                            BLangTypeDefinition defn, int depth, BLangFunctionTypeNode td) {
        Env env = (Env) cx.getInnerEnv();
        if (isFunctionTop(td)) {
            if (td.flagSet.contains(Flag.ISOLATED) || td.flagSet.contains(Flag.TRANSACTIONAL)) {
                FunctionDefinition fd = new FunctionDefinition();
                return fd.define(env, Builder.getNeverType(), Builder.getValType(),
                        FunctionQualifiers.create(
                                td.flagSet.contains(Flag.ISOLATED),
                                td.flagSet.contains(Flag.TRANSACTIONAL)));
            }
            return Builder.getFunctionType();
        }
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        attachedDefinitions.put(td, fd);

        Map<String, BLangNode> tdScope = new HashMap<>();
        List<SemType> params = new ArrayList<>(td.params.size());
        for (BLangSimpleVariable param : td.params) {
            SemType paramType = resolveTypeDesc(cx, mod, defn, depth + 1, param.typeNode);
            params.add(paramType);
            if (Core.isSubtypeSimple(paramType, Builder.getTypeDescType())) {
                tdScope.put(param.name.value, param);
            }
        }
        SemType rest;
        if (td.restParam == null) {
            rest = Builder.getNeverType();
        } else {
            BLangArrayType restArrayType = (BLangArrayType) td.restParam.typeNode;
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, restArrayType.elemtype);
        }
        SemType returnType = resolveReturnType(cx, mod, tdScope, defn, depth + 1, td.returnTypeNode);
        ListDefinition paramListDefinition = new ListDefinition();
        return fd.define(env,
                paramListDefinition.defineListTypeWrapped(env, params.toArray(SemType[]::new), params.size(), rest,
                        CELL_MUT_NONE),
                returnType,
                FunctionQualifiers.create(td.flagSet.contains(Flag.ISOLATED), td.flagSet.contains(Flag.TRANSACTIONAL)));
    }

    private SemType resolveReturnType(TypeTestContext<SemType> cx,
                                      Map<String, BLangNode> mod,
                                      Map<String, BLangNode> mayBeDependentlyTypeNodes,
                                      BLangTypeDefinition defn,
                                      int depth, BLangType returnTypeNode) {
        if (returnTypeNode == null) {
            return Builder.getNilType();
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
        return ld.defineListTypeWrapped((Env) cx.getInnerEnv(),
                new SemType[]{!isDependentlyType ? Builder.getBooleanType() : Builder.getBooleanConst(true),
                        innerType}, 2, Builder.getNeverType(),
                CELL_MUT_LIMITED);
    }

    private SemType resolveDependentlyTypedReturnType(TypeTestContext<SemType> cx,
                                                      Map<String, BLangNode> mod,
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

    private SemType resolveRecordTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                          BLangTypeDefinition defn, int depth, BLangRecordTypeNode td) {
        Env env = (Env) cx.getInnerEnv();
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }

        MappingDefinition md = new MappingDefinition();
        attachedDefinitions.put(td, md);

        MappingDefinition.Field[] fields = new MappingDefinition.Field[td.fields.size()];
        for (int i = 0; i < td.fields.size(); i++) {
            BLangSimpleVariable field = td.fields.get(i);
            SemType type = resolveTypeDesc(cx, mod, defn, depth + 1, field.typeNode);
            if (Core.isNever(type)) {
                throw new IllegalStateException("record field can't be never");
            }
            fields[i] =
                    new MappingDefinition.Field(field.name.value, type, field.flagSet.contains(Flag.READONLY),
                            field.flagSet.contains(Flag.OPTIONAL));
        }

        SemType rest;
        if (!td.isSealed() && td.getRestFieldType() == null) {
            rest = Builder.getAnyDataType();
        } else {
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, td.getRestFieldType());
        }
        if (rest == null) {
            rest = Builder.getNeverType();
        }
        return md.defineMappingTypeWrapped((Env) cx.getInnerEnv(), fields, rest, CELL_MUT_LIMITED);
    }

    private SemType resolveConstrainedTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                               BLangTypeDefinition defn, int depth, BLangConstrainedType td) {
        BLangBuiltInRefTypeNode refTypeNode = (BLangBuiltInRefTypeNode) td.getType();
        return switch (refTypeNode.typeKind) {
            case MAP -> resolveMapTypeDesc(cx, mod, defn, depth, td);
            case FUTURE -> resolveFutureTypeDesc(cx, mod, defn, depth, td);
            case XML -> resolveXmlTypeDesc(cx, mod, defn, depth, td);
            case TYPEDESC -> resolveTypedescTypeDesc(cx, mod, defn, depth, td);
            default -> throw new UnsupportedOperationException(
                    "Constrained type not implemented: " + refTypeNode.typeKind);
        };
    }

    private SemType resolveTypedescTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                            BLangTypeDefinition defn, int depth, BLangConstrainedType td) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return TypedescUtils.typedescContaining((Env) cx.getInnerEnv(), constraint);
    }

    private SemType resolveFutureTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                          BLangTypeDefinition defn, int depth, BLangConstrainedType td) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return FutureUtils.futureContaining((Env) cx.getInnerEnv(), constraint);
    }

    private SemType resolveXmlTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                       BLangTypeDefinition defn, int depth, BLangConstrainedType td) {
        SemType constraint = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);
        return XmlUtils.xmlSequence(constraint);
    }

    private SemType resolveMapTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                       BLangTypeDefinition defn, int depth, BLangConstrainedType td) {
        Env env = (Env) cx.getInnerEnv();
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }

        MappingDefinition md = new MappingDefinition();
        attachedDefinitions.put(td, md);

        SemType rest = resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint);

        return md.defineMappingTypeWrapped(env, new MappingDefinition.Field[0], rest, CELL_MUT_LIMITED);
    }

    private SemType resolveTupleTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                         BLangTypeDefinition defn, int depth, BLangTupleTypeNode td) {
        Env env = (Env) cx.getInnerEnv();
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType(env);
        }
        ListDefinition ld = new ListDefinition();
        attachedDefinitions.put(td, ld);
        SemType[] memberSemTypes = td.members.stream()
                .map(member -> resolveTypeDesc(cx, mod, defn, depth + 1, member.typeNode))
                .toArray(SemType[]::new);
        SemType rest =
                td.restParamType != null ? resolveTypeDesc(cx, mod, defn, depth + 1, td.restParamType) :
                        Builder.getNeverType();
        return ld.defineListTypeWrapped(env, memberSemTypes, memberSemTypes.length, rest, CELL_MUT_LIMITED);
    }

    private SemType resolveArrayTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                         BLangTypeDefinition defn, int depth, BLangArrayType td) {
        Definition attachedDefinition = attachedDefinitions.get(td);
        if (attachedDefinition != null) {
            return attachedDefinition.getSemType((Env) cx.getInnerEnv());
        }

        ListDefinition ld = new ListDefinition();
        attachedDefinitions.put(td, ld);

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

    private SemType resolveListInner(TypeTestContext<SemType> cx, int size, SemType eType) {
        ListDefinition ld = new ListDefinition();
        return resolveListInner(cx, ld, size, eType);
    }

    private static SemType resolveListInner(TypeTestContext<SemType> cx, ListDefinition ld, int size, SemType eType) {
        Env env = (Env) cx.getInnerEnv();
        if (size != -1) {
            SemType[] members = {eType};
            return ld.defineListTypeWrapped(env, members, Math.abs(size), Builder.getNeverType(), CELL_MUT_LIMITED);
        } else {
            return ld.defineListTypeWrapped(env, EMPTY_SEMTYPE_ARR, 0, eType, CELL_MUT_LIMITED);
        }
    }


    private SemType resolveSingletonType(BLangFiniteTypeNode td) {
        return td.valueSpace.stream().map(each -> (BLangLiteral) each)
                .map(literal -> resolveSingletonType(literal.value, literal.getDeterminedType().getKind()).get())
                .reduce(Builder.getNeverType(), Core::union);
    }

    private Optional<SemType> resolveSingletonType(Object value, TypeKind targetTypeKind) {
        return switch (targetTypeKind) {
            case NIL -> Optional.of(Builder.getNilType());
            case BOOLEAN -> Optional.of(Builder.getBooleanConst((Boolean) value));
            case INT, BYTE -> {
                assert !(value instanceof Byte);
                yield Optional.of(Builder.getIntConst(((Number) value).longValue()));
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
                        yield Optional.empty();
                    }
                }
                yield Optional.of(Builder.getFloatConst(doubleVal));
            }
            case DECIMAL -> {
                String repr = (String) value;
                if (repr.contains("d") || repr.contains("D")) {
                    repr = repr.substring(0, repr.length() - 1);
                }
                yield Optional.of(Builder.getDecimalConst(new BigDecimal(repr)));
            }
            case STRING -> Optional.of(Builder.getStringConst((String) value));
            case HANDLE -> Optional.of(Builder.getHandleType());
            default -> Optional.empty();
        };
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangUnionTypeNode td, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition defn) {

        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
        SemType res = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            res = Core.union(res, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return res;
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangUserDefinedType td, Map<String, BLangNode> mod,
                                    int depth) {
        String name = td.typeName.value;
        // Need to replace this with a real package lookup
        if (td.pkgAlias.value.equals("int")) {
            return resolveIntSubtype(name);
        } else if (td.pkgAlias.value.equals("string") && name.equals("Char")) {
            return Builder.getCharType();
        } else if (td.pkgAlias.value.equals("xml")) {
            return resolveXmlSubType(name);
        } else if (td.pkgAlias.value.equals("regexp") && name.equals("RegExp")) {
            return Builder.getRegexType();
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            throw new IndexOutOfBoundsException("unknown type " + name);
        }

        switch (moduleLevelDef.getKind()) {
            case TYPE_DEFINITION -> {
                SemType ty = resolveTypeDefnRec(cx, mod, (BLangTypeDefinition) moduleLevelDef, depth);
                if (td.flagSet.contains(Flag.DISTINCT)) {
                    return getDistinctSemType(cx, ty);
                }
                return ty;
            }
            case CONSTANT -> {
                BLangConstant constant = (BLangConstant) moduleLevelDef;
                return resolveTypeDefnRec(cx, mod, constant.getAssociatedTypeDefinition(), depth);
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

    private SemType resolveXmlSubType(String name) {
        return switch (name) {
            case "Element" -> Builder.getXmlElementType();
            case "Comment" -> Builder.getXmlCommentType();
            case "Text" -> Builder.getXmlTextType();
            case "ProcessingInstruction" -> Builder.getXmlPIType();
            default -> throw new IllegalStateException("Unknown XML subtype: " + name);
        };
    }

    private SemType getDistinctSemType(TypeTestContext<SemType> cx, SemType innerType) {
        Env env = (Env) cx.getInnerEnv();
        if (Core.isSubtypeSimple(innerType, Builder.getObjectType())) {
            return getDistinctObjectType(env, innerType);
        } else if (Core.isSubtypeSimple(innerType, Builder.getErrorType())) {
            return getDistinctErrorType(env, innerType);
        }
        throw new IllegalArgumentException("Distinct type not supported for: " + innerType);
    }

    private SemType resolveIntSubtype(String name) {
        // TODO: support MAX_VALUE
        return switch (name) {
            case "Signed8" -> Builder.createIntRange(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
            case "Signed16" -> Builder.createIntRange(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
            case "Signed32" -> Builder.createIntRange(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
            case "Unsigned8" -> Builder.createIntRange(0, UNSIGNED8_MAX_VALUE);
            case "Unsigned16" -> Builder.createIntRange(0, UNSIGNED16_MAX_VALUE);
            case "Unsigned32" -> Builder.createIntRange(0, UNSIGNED32_MAX_VALUE);
            default -> throw new UnsupportedOperationException("Unknown int subtype: " + name);
        };
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangIntersectionTypeNode td,
                                    Map<String, BLangNode> mod, int depth, BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
        SemType res = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            res = Core.intersect(res, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return res;
    }

    private SemType resolveTypeDesc(BLangBuiltInRefTypeNode td) {
        return switch (td.typeKind) {
            case NEVER -> Builder.getNeverType();
            case XML -> Builder.getXmlType();
            case FUTURE -> Builder.getFutureType();
            // TODO: implement json type

            default -> throw new UnsupportedOperationException("Built-in ref type not implemented: " + td.typeKind);
        };
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangValueType td) {
        return switch (td.typeKind) {
            case NIL -> Builder.getNilType();
            case BOOLEAN -> Builder.getBooleanType();
            case BYTE -> Builder.createIntRange(0, UNSIGNED8_MAX_VALUE);
            case INT -> Builder.getIntType();
            case FLOAT -> Builder.getFloatType();
            case DECIMAL -> Builder.getDecimalType();
            case STRING -> Builder.getStringType();
            case READONLY -> Builder.getReadonlyType();
            case ANY -> Builder.getAnyType();
            case ANYDATA -> Builder.getAnyDataType();
            case ERROR -> Builder.getErrorType();
            case XML -> Builder.getXmlType();
            case HANDLE -> Builder.getHandleType();
            case TYPEDESC -> Builder.getTypeDescType();
            default -> throw new IllegalStateException("Unknown type: " + td);
        };
    }

    private SemType evaluateConst(BLangConstant constant) {
        return switch (constant.symbol.value.type.getKind()) {
            case INT -> Builder.getIntConst((long) constant.symbol.value.value);
            case BOOLEAN -> Builder.getBooleanConst((boolean) constant.symbol.value.value);
            case STRING -> Builder.getStringConst((String) constant.symbol.value.value);
            case FLOAT -> Builder.getFloatConst((double) constant.symbol.value.value);
            default -> throw new UnsupportedOperationException("Expression type not implemented for const semtype");
        };
    }

    private void attachToBType(BLangType bType, SemType semType) {
        attachedSemType.put(bType, semType);
    }
}
