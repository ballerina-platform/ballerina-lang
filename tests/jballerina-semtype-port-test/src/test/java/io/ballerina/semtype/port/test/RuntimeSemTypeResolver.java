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
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter.getTypeOrClassName;

public class RuntimeSemTypeResolver implements SemTypeResolver<SemType> {

    Map<BLangType, SemType> attachedSemType = new HashMap<>();
    Map<BLangTypeDefinition, SemType> semTypeMemo = new HashMap<>();

    // TODO: may be we need an abstract class
    @Override
    public void defineSemTypes(List<BLangNode> moduleDefs, TypeTestContext<SemType> cx) {
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        // TODO: stream
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
                resolveTypeDefn(cx, modTable, typeDefinition);
            }
        }
    }

    private void resolveTypeDefn(TypeTestContext<SemType> cx, Map<String, BLangNode> modTable,
                                 BLangTypeDefinition typeDefinition) {
        resolveTypeDefnRec(cx, modTable, typeDefinition, 0);
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
                                    int depth, BLangType td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc(cx, (BLangValueType) td);
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc((BLangBuiltInRefTypeNode) td);
            case INTERSECTION_TYPE_NODE:
                return resolveTypeDesc(cx, (BLangIntersectionTypeNode) td, mod, depth, defn);
            case UNION_TYPE_NODE:
                return resolveTypeDesc(cx, (BLangUnionTypeNode) td, mod, depth, defn);
            case USER_DEFINED_TYPE:
                return resolveTypeDesc(cx, (BLangUserDefinedType) td, mod, depth);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td);
            default:
                throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        }
    }

    private SemType resolveSingletonType(BLangFiniteTypeNode td) {
        return td.valueSpace.stream().map(each -> (BLangLiteral) each)
                .map(literal -> resolveSingletonType(literal.value, literal.getDeterminedType().getKind()).get())
                .reduce(Builder.neverType(), Core::union);
    }

    // TODO: common code?
    private Optional<SemType> resolveSingletonType(Object value, TypeKind targetTypeKind) {
        return switch (targetTypeKind) {
            case NIL -> Optional.of(Builder.nilType());
            case BOOLEAN -> Optional.of(Builder.booleanConst((Boolean) value));
            case INT, BYTE -> {
                assert !(value instanceof Byte);
                yield Optional.of(Builder.intConst(((Number) value).longValue()));
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
                        yield Optional.empty();
                    }
                }
                yield Optional.of(Builder.floatConst(doubleVal));
                // literal value will be a string if it wasn't within the bounds of what is supported by Java Long
                // or Double when it was parsed in BLangNodeBuilder.
                // We reach here when there is a syntax error. Mock the flow with default float value.
            }
            case DECIMAL -> {
                String repr = (String) value;
                if (repr.contains("d") || repr.contains("D")) {
                    repr = repr.substring(0, repr.length() - 1);
                }
                BigDecimal d = new BigDecimal(repr);
                yield Optional.of(Builder.decimalConst(d));
            }
            case STRING -> Optional.of(Builder.stringConst((String) value));
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
            return Builder.charType();
        }

        BLangNode moduleLevelDef = mod.get(name);
        if (moduleLevelDef == null) {
            throw new IllegalStateException("unknown type: " + name);
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            return resolveTypeDefnRec(cx, mod, (BLangTypeDefinition) moduleLevelDef, depth);
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            return resolveTypeDefnRec(cx, mod, constant.associatedTypeDefinition, depth);
        } else {
            throw new UnsupportedOperationException("constants and class defns not implemented");
        }
    }

    private SemType resolveIntSubtype(String name) {
        // TODO: support MAX_VALUE
        return switch (name) {
            case "Signed8" -> Builder.intRange(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
            case "Signed16" -> Builder.intRange(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
            case "Signed32" -> Builder.intRange(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
            case "Unsigned8" -> Builder.intRange(0, UNSIGNED8_MAX_VALUE);
            case "Unsigned16" -> Builder.intRange(0, UNSIGNED16_MAX_VALUE);
            case "Unsigned32" -> Builder.intRange(0, UNSIGNED32_MAX_VALUE);
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
            case NEVER -> Builder.neverType();
            default -> throw new UnsupportedOperationException("Built-in ref type not implemented: " + td.typeKind);
        };
    }

    private SemType resolveTypeDesc(TypeTestContext<SemType> cx, BLangValueType td) {
        switch (td.typeKind) {
            case NIL:
                return Builder.nilType();
            case BOOLEAN:
                return Builder.booleanType();
            case BYTE:
                return Builder.intRange(0, UNSIGNED8_MAX_VALUE);
            case INT:
                return Builder.intType();
            case FLOAT:
                return Builder.floatType();
            case DECIMAL:
                return Builder.decimalType();
            case STRING:
                return Builder.stringType();
            default:
                throw new IllegalStateException("Unknown type: " + td);
        }
    }

    private SemType evaluateConst(BLangConstant constant) {
        return switch (constant.symbol.value.type.getKind()) {
            case INT -> Builder.intConst((long) constant.symbol.value.value);
            case BOOLEAN -> Builder.booleanConst((boolean) constant.symbol.value.value);
            case STRING -> Builder.stringConst((String) constant.symbol.value.value);
            case FLOAT -> Builder.floatConst((double) constant.symbol.value.value);
            default -> throw new UnsupportedOperationException("Expression type not implemented for const semtype");
        };
    }

    private void resolveConstant(TypeTestContext<SemType> cx, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype = evaluateConst(constant);
        attachToBType(constant.typeNode, semtype);
        cx.getEnv().addTypeDef(constant.name.value, semtype);
    }

    private void attachToBType(BLangType bType, SemType semType) {
        attachedSemType.put(bType, semType);
    }
}
