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
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import io.ballerina.types.subtypedata.FloatSubtype;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter.getTypeOrClassName;

/**
 * Resolves sem-types for module definitions.
 *
 * @since 2201.10.0
 */
public class CompilerSemTypeResolver implements SemTypeResolver<SemType> {

    @Override
    public void defineSemTypes(List<BLangNode> moduleDefs, TypeTestContext<SemType> cx) {
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

    private void resolveConstant(TypeTestContext<SemType> cx, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype = evaluateConst(constant);
        addSemTypeBType(constant.getTypeNode(), semtype);
        cx.getEnv().addTypeDef(constant.name.value, semtype);
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

    private SemType resolveTypeDefn(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth) {
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
            cx.getEnv().addTypeDef(defn.name.value, s);
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

    public SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                   int depth, BLangType td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc(cx, (BLangValueType) td);
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc((BLangBuiltInRefTypeNode) td);
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
                return resolveTypeDesc(cx, mod, depth, (BLangTableTypeNode) td);
            case ERROR_TYPE:
                return resolveTypeDesc(cx, mod, defn, depth, (BLangErrorType) td);
            default:
                throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        }
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth, BLangFunctionTypeNode td) {
        if (isFunctionTop(td)) {
            return PredefinedType.FUNCTION;
        }
        if (td.defn != null) {
            return td.defn.getSemType((Env) cx.getInnerEnv());
        }
        FunctionDefinition fd = new FunctionDefinition();
        td.defn = fd;
        List<SemType> params =
                td.params.stream().map(param -> resolveTypeDesc(cx, mod, defn, depth + 1, param.typeNode))
                        .toList();
        SemType rest;
        if (td.restParam == null) {
            rest = PredefinedType.NEVER;
        } else {
            BLangArrayType restArrayType = (BLangArrayType) td.restParam.typeNode;
            rest = resolveTypeDesc(cx, mod, defn, depth + 1, restArrayType.elemtype);
        }
        SemType returnType = td.returnTypeNode != null ? resolveTypeDesc(cx, mod, defn, depth + 1, td.returnTypeNode) :
                PredefinedType.NIL;
        ListDefinition paramListDefinition = new ListDefinition();
        Env env = (Env) cx.getInnerEnv();
        return fd.define(env,
                paramListDefinition.defineListTypeWrapped(env, params, params.size(), rest,
                        CellAtomicType.CellMutability.CELL_MUT_NONE), returnType);
    }

    private boolean isFunctionTop(BLangFunctionTypeNode td) {
        return td.params.isEmpty() && td.restParam == null && td.returnTypeNode == null;
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth, BLangArrayType td) {
        if (td.defn != null) {
            return td.defn.getSemType((Env) cx.getInnerEnv());
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

    private SemType resolveListInner(TypeTestContext<SemType> cx, int size, SemType eType) {
        ListDefinition ld = new ListDefinition();
        return resolveListInner(cx, ld, size, eType);
    }

    private static SemType resolveListInner(TypeTestContext<SemType> cx, ListDefinition ld, int size, SemType eType) {
        Env env = (Env) cx.getInnerEnv();
        if (size != -1) {
            return ld.defineListTypeWrapped(env, List.of(eType), Math.abs(size), PredefinedType.NEVER,
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        } else {
            return ld.defineListTypeWrapped(env, List.of(), 0, eType,
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        }
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth,
                                    BLangTupleTypeNode td) {
        Env env = (Env) cx.getInnerEnv();
        if (td.defn != null) {
            return td.defn.getSemType(env);
        }
        ListDefinition ld = new ListDefinition();
        td.defn = ld;
        List<SemType> memberSemTypes =
                td.members.stream().map(member -> resolveTypeDesc(cx, mod, defn, depth + 1, member.typeNode))
                        .toList();
        SemType rest = td.restParamType != null ? resolveTypeDesc(cx, mod, defn, depth + 1, td.restParamType) :
                PredefinedType.NEVER;
        return ld.defineListTypeWrapped(env, memberSemTypes, memberSemTypes.size(), rest);
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangValueType td) {
        Context innerContext = (Context) cx.getInnerContext();
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
                return Core.createAnydata(innerContext);
            case JSON:
                return Core.createJson(innerContext);
            default:
                throw new IllegalStateException("Unknown type: " + td);
        }
    }

    private SemType resolveTypeDesc(BLangBuiltInRefTypeNode td) {
        return switch (td.typeKind) {
            case NEVER -> PredefinedType.NEVER;
            case XML -> PredefinedType.XML;
            default -> throw new UnsupportedOperationException("Built-in ref type not implemented: " + td.typeKind);
        };
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangConstrainedType td, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition defn) {
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();
        return switch (typeKind) {
            case MAP -> resolveMapTypeDesc(td, cx, mod, depth, defn);
            case XML -> resolveXmlTypeDesc(td, cx, mod, depth, defn);
            default -> throw new UnsupportedOperationException("Constrained type not implemented: " + typeKind);
        };
    }

    private SemType resolveMapTypeDesc(BLangConstrainedType td, TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                       int depth, BLangTypeDefinition typeDefinition) {
        Env env = (Env) cx.getInnerEnv();
        if (td.defn != null) {
            return td.defn.getSemType(env);
        }

        MappingDefinition d = new MappingDefinition();
        td.defn = d;

        SemType rest = resolveTypeDesc(cx, mod, typeDefinition, depth + 1, td.constraint);
        return d.defineMappingTypeWrapped(env, Collections.emptyList(), rest == null ? PredefinedType.NEVER : rest);
    }

    private SemType resolveXmlTypeDesc(BLangConstrainedType td, TypeTestContext<SemType> cx, Map<String, BLangNode> mod,
                                       int depth, BLangTypeDefinition defn) {
        if (td.defn != null) {
            return td.defn.getSemType((Env) cx.getInnerEnv());
        }
        return SemTypes.xmlSequence(resolveTypeDesc(cx, mod, defn, depth + 1, td.constraint));
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangRecordTypeNode td, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getSemType((Env) cx.getInnerEnv());
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
            rest = Core.createAnydata((Context) cx.getInnerContext());
        } else {
            rest = resolveTypeDesc(cx, mod, typeDefinition, depth + 1, td.restFieldType);
        }

        return d.defineMappingTypeWrapped((Env) cx.getInnerEnv(), fields, rest == null ? PredefinedType.NEVER : rest);
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangUnionTypeNode td, Map<String, BLangNode> mod,
                                    int depth,
                                    BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
        SemType u = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            u = Core.union(u, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return u;
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangIntersectionTypeNode td,
                                    Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
        SemType i = resolveTypeDesc(cx, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            i = Core.intersect(i, resolveTypeDesc(cx, mod, defn, depth, iterator.next()));
        }
        return i;
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangUserDefinedType td, Map<String, BLangNode> mod,
                                    int depth) {
        String name = td.typeName.value;
        // Need to replace this with a real package lookup
        if (td.pkgAlias.value.equals("int")) {
            return resolveIntSubtype(name);
        } else if (td.pkgAlias.value.equals("string") && name.equals("Char")) {
            return SemTypes.CHAR;
        } else if (td.pkgAlias.value.equals("xml")) {
            return resolveXmlSubtype(name);
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            throw new IllegalStateException("unknown type: " + name);
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            return resolveTypeDefn(cx, mod, (BLangTypeDefinition) moduleLevelDef, depth);
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            return resolveTypeDefn(cx, mod, constant.associatedTypeDefinition, depth);
        } else {
            throw new UnsupportedOperationException("constants and class defns not implemented");
        }
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

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, int depth,
                                    BLangTableTypeNode td) {
        if (td.tableKeySpecifier != null || td.tableKeyTypeConstraint != null) {
            throw new UnsupportedOperationException("table key constraint not supported yet");
        }

        SemType memberType = resolveTypeDesc(cx, mod, (BLangTypeDefinition) td.constraint.defn, depth, td.constraint);
        return SemTypes.tableContaining((Env) cx.getInnerEnv(), memberType);
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                    int depth,
                                    BLangErrorType td) {
        if (td.detailType == null) {
            return PredefinedType.ERROR;
        }

        SemType detail = resolveTypeDesc(cx, mod, defn, depth, td.detailType);
        return SemTypes.errorDetail(detail);
    }
}
