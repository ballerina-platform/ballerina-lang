/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
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
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.Core.widenToBasicTypes;
import static io.ballerina.types.UniformTypeCode.UT_BOOLEAN;
import static io.ballerina.types.UniformTypeCode.UT_DECIMAL;
import static io.ballerina.types.UniformTypeCode.UT_FLOAT;
import static io.ballerina.types.UniformTypeCode.UT_INT;
import static io.ballerina.types.UniformTypeCode.UT_STRING;
import static org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter.getTypeOrClassName;
import static org.wso2.ballerinalang.compiler.semantics.analyzer.Types.getReferredType;

/**
 * Responsible for resolving sem-types.
 *
 * @since 2201.8.0
 */
public class SemTypeResolver {

    private static final CompilerContext.Key<SemTypeResolver> SEM_TYPE_RESOLVER_KEY = new CompilerContext.Key<>();
    private final ConstantValueResolver constResolver;
    private final BLangDiagnosticLog dlog;
    private Map<String, BLangNode> modTable = new LinkedHashMap<>();

    private static final String PROPERTY_SEMTYPE_ENABLED = "ballerina.experimental.semtype.enabled";
    private static final String PROPERTY_SEMTYPE_TEST_SUITE = "ballerina.experimental.semtype.test.suite";
    static final boolean SEMTYPE_ENABLED = Boolean.parseBoolean(System.getProperty(PROPERTY_SEMTYPE_ENABLED));
    static final boolean SEMTYPE_TEST_SUITE = Boolean.parseBoolean(System.getProperty(PROPERTY_SEMTYPE_TEST_SUITE));

    private SemTypeResolver(CompilerContext context) {
        this.constResolver = ConstantValueResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static SemTypeResolver getInstance(CompilerContext context) {
        SemTypeResolver semTypeResolver = context.get(SEM_TYPE_RESOLVER_KEY);
        if (semTypeResolver == null) {
            semTypeResolver = new SemTypeResolver(context);
        }
        return semTypeResolver;
    }

    void defineSemTypesIfEnabled(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        if (SEMTYPE_ENABLED) {
            defineSemTypesSubset(moduleDefs, pkgEnv);
        } else if (SEMTYPE_TEST_SUITE) {
            defineSemTypes(moduleDefs, pkgEnv);
        }
    }

    void resolveSemTypeIfEnabled(BLangType typeNode, SymbolEnv env, BType resultType) {
        if (!SEMTYPE_ENABLED) {
            return;
        }

        try {
            SemType s = resolveTypeDescSubset(env.enclPkg.semtypeEnv, modTable, null, 0, typeNode);
            resultType.setSemType(s);
        } catch (UnsupportedOperationException e) {
            // Do nothing
        }
    }

    // --------------------------------------- Subset suffixed methods ----------------------------------------------

    // All methods end with suffix "Subset", support only subset of ported sem-types.
    // Once we extend sem-type enabled types we can get rid of these methods.
    private void defineSemTypesSubset(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.TYPE_DEFINITION) {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefnSubset(pkgEnv.enclPkg.semtypeEnv, modTable, typeDefinition, 0);
            }
        }
    }

    private SemType resolveTypeDefnSubset(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                          int depth) {
        if (defn.semType != null) {
            return defn.semType;
        }

        if (depth == defn.semCycleDepth) {
            // no error is logged since we handle this in the normal type resolver
            return null;
        }
        defn.semCycleDepth = depth;

        SemType s;
        try {
            s = resolveTypeDescSubset(semtypeEnv, mod, defn, depth, defn.typeNode);
        } catch (UnsupportedOperationException e) {
            return null;
        }

        addSemtypeBType(defn.getTypeNode(), s);
        if (defn.semType == null) {
            defn.semType = s;
            defn.semCycleDepth = -1;
            semtypeEnv.addTypeDef(defn.name.value, s);
            return s;
        } else {
            return s;
        }
    }

    private SemType resolveTypeDescSubset(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn,
                                          int depth, BLangType td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc((BLangBuiltInRefTypeNode) td, semtypeEnv);
            case VALUE_TYPE:
                return resolveTypeDesc((BLangValueType) td, semtypeEnv);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td, semtypeEnv);
            case USER_DEFINED_TYPE:
                return resolveTypeDescSubset((BLangUserDefinedType) td, semtypeEnv, mod, depth);
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
            case RECORD_TYPE:
            case ARRAY_TYPE:
            case TUPLE_TYPE_NODE:
            case ERROR_TYPE:
            case TABLE_TYPE:
            case FUNCTION_TYPE:
            case OBJECT_TYPE:
            case STREAM_TYPE:
            case UNION_TYPE_NODE:
            case INTERSECTION_TYPE_NODE:
            default:
                throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        }
    }

    private SemType resolveTypeDescSubset(BLangUserDefinedType td, Env semtypeEnv, Map<String, BLangNode> mod,
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
            // no error is logged since we handle this in the normal type resolver
            throw new UnsupportedOperationException("Reference to undefined type: " + name);
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            return resolveTypeDefnSubset(semtypeEnv, mod, (BLangTypeDefinition) moduleLevelDef, depth);
        } else {
            throw new UnsupportedOperationException("constants and class defns not implemented");
        }
    }

    // ------------------------------------ End of subset suffixed methods -------------------------------------------

    private void defineSemTypes(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);
        constResolver.resolve(pkgEnv.enclPkg.constants, pkgEnv.enclPkg.packageID, pkgEnv);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                // TODO: semType: support class definitions
                throw new UnsupportedOperationException("Semtype are not supported for class definitions yet");
            } else if (def.getKind() == NodeKind.CONSTANT) {
                resolveConstant(pkgEnv.enclPkg.semtypeEnv, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefn(pkgEnv.enclPkg.semtypeEnv, modTable, typeDefinition, 0);
            }
        }
    }

    private void resolveConstant(Env semtypeEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype = evaluateConst(constant);
        addSemtypeBType(constant.getTypeNode(), semtype);
        semtypeEnv.addTypeDef(constant.name.value, semtype);
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

    private SemType resolveTypeDefn(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth) {
        if (defn.semType != null) {
            return defn.semType;
        }

        if (depth == defn.semCycleDepth) {
            dlog.error(defn.pos, DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, defn.name);
            return null;
        }
        defn.semCycleDepth = depth;
        SemType s = resolveTypeDesc(semtypeEnv, mod, defn, depth, defn.typeNode);
        addSemtypeBType(defn.getTypeNode(), s);
        if (defn.semType == null) {
            defn.semType = s;
            defn.semCycleDepth = -1;
            semtypeEnv.addTypeDef(defn.name.value, s);
            return s;
        } else {
            return s;
        }
    }

    private void addSemtypeBType(BLangType typeNode, SemType semType) {
        if (typeNode != null) {
            typeNode.getBType().setSemType(semType);
        }
    }

    public SemType resolveTypeDesc(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                   BLangType td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc((BLangValueType) td, semtypeEnv);
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                return resolveTypeDesc((BLangConstrainedType) td, semtypeEnv, mod, depth, defn);
            case ARRAY_TYPE:
                return resolveTypeDesc(((BLangArrayType) td), semtypeEnv, mod, depth, defn);
            case TUPLE_TYPE_NODE:
                return resolveTypeDesc((BLangTupleTypeNode) td, semtypeEnv, mod, depth, defn);
            case RECORD_TYPE:
                return resolveTypeDesc((BLangRecordTypeNode) td, semtypeEnv, mod, depth, defn);
            case FUNCTION_TYPE:
                return resolveTypeDesc((BLangFunctionTypeNode) td, semtypeEnv, mod, depth, defn);
            case ERROR_TYPE:
                return resolveTypeDesc((BLangErrorType) td, semtypeEnv, mod, depth, defn);
            case UNION_TYPE_NODE:
                return resolveTypeDesc((BLangUnionTypeNode) td, semtypeEnv, mod, depth, defn);
            case INTERSECTION_TYPE_NODE:
                return resolveTypeDesc((BLangIntersectionTypeNode) td, semtypeEnv, mod, depth, defn);
            case USER_DEFINED_TYPE:
                return resolveTypeDesc((BLangUserDefinedType) td, semtypeEnv, mod, depth);
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc((BLangBuiltInRefTypeNode) td, semtypeEnv);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td, semtypeEnv);
            case TABLE_TYPE:
                return resolveTypeDesc((BLangTableTypeNode) td, semtypeEnv, mod, depth);
            case OBJECT_TYPE:
            case STREAM_TYPE:
            default:
                // TODO: semType: support. e.g. STREAM_TYPE
                throw new UnsupportedOperationException("type not implemented: " + td.getKind());
        }
    }

    private SemType resolveSingletonType(BLangFiniteTypeNode td, Env semtypeEnv) {
        return resolveSingletonType(td.valueSpace);
    }

    private SemType resolveSingletonType(List<BLangExpression> valueSpace) {
        // In case we encounter unary expressions in finite type, we will be replacing them with numeric literals
        replaceUnaryExprWithNumericLiteral(valueSpace);

        if (valueSpace.size() > 1) {
            return resolveFiniteTypeUnion(valueSpace);
        }
        return resolveSingletonType((BLangLiteral) valueSpace.get(0));
    }

    public static SemType resolveSingletonType(BLangLiteral literal) {
        return resolveSingletonType(literal.value, literal.getDeterminedType().getKind());
    }

    public static SemType resolveSingletonType(Object value, TypeKind targetTypeKind) {
        switch (targetTypeKind) {
            case FLOAT:
                double doubleVal;
                if (value instanceof Long) {
                    doubleVal = ((Long) value).doubleValue();
                } else if (value instanceof Double) {
                    doubleVal = (double) value;
                } else {
                    // literal value will be a string if it wasn't within the bounds of what is supported by Java Long
                    // or Double when it was parsed in BLangNodeBuilder.
                    try {
                        doubleVal = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        // We reach here when there is a syntax error. Mock the flow with default float value.
                        return FloatSubtype.floatConst(0);
                    }
                }
                return SemTypes.floatConst(doubleVal);
            case INT:
            case BYTE:
                return SemTypes.intConst((Long) value);
            case STRING:
                return SemTypes.stringConst((String) value);
            case BOOLEAN:
                return SemTypes.booleanConst((Boolean) value);
            case DECIMAL:
                return SemTypes.decimalConst((String) value);
            case NIL:
                return PredefinedType.NIL;
            case OTHER:
                // We reach here when there is a semantic error
                return PredefinedType.NEVER;
            default:
                throw new UnsupportedOperationException("Finite type not implemented for: " + targetTypeKind);
        }
    }

    private static void replaceUnaryExprWithNumericLiteral(List<BLangExpression> valueSpace) {
        for (int i = 0; i < valueSpace.size(); i++) {
            BLangExpression value = valueSpace.get(i);
            if (value.getKind() == NodeKind.UNARY_EXPR) {
                BLangUnaryExpr unaryExpr = (BLangUnaryExpr) value;
                if (unaryExpr.expr.getKind() == NodeKind.NUMERIC_LITERAL) {
                    // Replacing unary expression with numeric literal type for + and - numeric values
                    BLangNumericLiteral newNumericLiteral =
                            Types.constructNumericLiteralFromUnaryExpr(unaryExpr);
                    valueSpace.set(i, newNumericLiteral);
                }
            }
        }
    }

    private SemType resolveFiniteTypeUnion(List<BLangExpression> valueSpace) {
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

    private SemType resolveTypeDesc(BLangBuiltInRefTypeNode td, Env semtypeEnv) {
        switch (td.typeKind) {
            case NEVER:
                return PredefinedType.NEVER;
            case XML:
                return PredefinedType.XML;
            default:
                // TODO: semType: support e.g. MAP
                throw new UnsupportedOperationException("Built-in reference type not implemented: " + td.typeKind);
        }
    }

    private SemType resolveTypeDesc(BLangTableTypeNode td, Env semtypeEnv, Map<String, BLangNode> mod, int depth) {
        SemType memberType = resolveTypeDesc(semtypeEnv, mod, (BLangTypeDefinition) td.constraint.defn, depth,
                td.constraint);
        return SemTypes.tableContaining(memberType);
    }

    private SemType resolveTypeDesc(BLangUserDefinedType td, Env semtypeEnv, Map<String, BLangNode> mod, int depth) {
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
            dlog.error(td.pos, DiagnosticErrorCode.UNKNOWN_TYPE, td.typeName);
            return null;
        }

        if (moduleLevelDef.getKind() == NodeKind.TYPE_DEFINITION) {
            return resolveTypeDefn(semtypeEnv, mod, (BLangTypeDefinition) moduleLevelDef, depth);
        } else if (moduleLevelDef.getKind() == NodeKind.CONSTANT) {
            BLangConstant constant = (BLangConstant) moduleLevelDef;
            return resolveTypeDefn(semtypeEnv, mod, constant.associatedTypeDefinition, depth);
        } else {
            throw new UnsupportedOperationException("constants and class defns not implemented");
        }
    }

    private SemType resolveIntSubtype(String name) {
        switch (name) {
            case "Signed8":
                return SemTypes.SINT8;
            case "Signed16":
                return SemTypes.SINT16;
            case "Signed32":
                return SemTypes.SINT32;
            case "Unsigned8":
                return SemTypes.UINT8;
            case "Unsigned16":
                return SemTypes.UINT16;
            case "Unsigned32":
                return SemTypes.UINT32;
            default:
                // TODO: semtype: support MAX_VALUE
                throw new UnsupportedOperationException("Unknown int subtype: " + name);
        }
    }

    private SemType resolveXmlSubtype(String name) {
        switch (name) {
            case "Element":
                return SemTypes.XML_ELEMENT;
            case "Comment":
                return SemTypes.XML_COMMENT;
            case "Text":
                return SemTypes.XML_TEXT;
            case "ProcessingInstruction":
                return SemTypes.XML_PI;
            default:
                throw new IllegalStateException("Unknown XML subtype: " + name);
        }
    }

    private SemType resolveTypeDesc(BLangConstrainedType td, Env semtypeEnv, Map<String, BLangNode> mod,
                                    int depth, BLangTypeDefinition defn) {
        TypeKind typeKind = ((BLangBuiltInRefTypeNode) td.getType()).getTypeKind();
        switch (typeKind) {
            case MAP:
                return resolveMapTypeDesc(td, semtypeEnv, mod, depth, defn);
            case XML:
                return resolveXmlTypeDesc(td, semtypeEnv, mod, depth, defn);
            case TYPEDESC:
            case FUTURE:
            default:
                // TODO: semType: support. e.g. TYPEDESC
                throw new UnsupportedOperationException("Constrained type not implemented: " + typeKind);
        }
    }

    private SemType resolveXmlTypeDesc(BLangConstrainedType td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition defn) {
        if (td.defn != null) {
            return td.defn.getSemType(semtypeEnv);
        }
        return SemTypes.xmlSequence(resolveTypeDesc(semtypeEnv, mod, defn, depth + 1, td.constraint));
    }

    private SemType resolveMapTypeDesc(BLangConstrainedType td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                       BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getSemType(semtypeEnv);
        }

        MappingDefinition d = new MappingDefinition();
        td.defn = d;
        try {
            SemType rest = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, td.constraint);

            return d.define(semtypeEnv, Collections.emptyList(), rest);
        } catch (Exception e) {
            td.defn = null;
            throw new UnsupportedOperationException("error resolving map type");
        }
    }

    private SemType resolveTypeDesc(BLangRecordTypeNode td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition typeDefinition) {
        if (td.defn != null) {
            return td.defn.getSemType(semtypeEnv);
        }

        MappingDefinition d = new MappingDefinition();
        td.defn = d;

        try {
            List<Field> fields = new ArrayList<>();
            for (BLangSimpleVariable field : td.fields) {
                String name = field.name.value;
                SemType t = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, field.typeNode);
                fields.add(Field.from(name, t));
            }

            SemType rest;
            if (!td.isSealed() && td.getRestFieldType() == null) {
                // TODO: semType: handle open records
                throw new UnsupportedOperationException("Open record not supported yet");
            } else {
                rest = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, td.restFieldType);
            }

            return d.define(semtypeEnv, fields, rest == null ? PredefinedType.NEVER : rest);
        } catch (Exception e) {
            td.defn = null;
            throw new UnsupportedOperationException("error resolving record type");
        }
    }

    private SemType resolveTypeDesc(BLangFunctionTypeNode td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition typeDefinition) {
        Definition defn = td.defn;
        if (defn != null) {
            return defn.getSemType(semtypeEnv);
        }
        FunctionDefinition d = new FunctionDefinition(semtypeEnv);
        td.defn = d;

        try {
            List<SemType> paramTypes = new ArrayList<>();
            for (BLangVariable p : td.params) {
                paramTypes.add(resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, p.typeNode));
            }

            SemType rest = resolveTypeDesc(semtypeEnv, mod, typeDefinition, depth + 1, td.returnTypeNode);
            SemType args = SemTypes.tuple(semtypeEnv, paramTypes.toArray(new SemType[]{}));
            return d.define(semtypeEnv, args, rest);
        } catch (Exception e) {
            td.defn = null;
            throw new UnsupportedOperationException("error resolving function type");
        }
    }

    private SemType resolveTypeDesc(BLangErrorType td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
        if (td.detailType == null) {
            return PredefinedType.ERROR;
        }

        SemType detail = resolveTypeDesc(semtypeEnv, mod, defn, depth, td.detailType);
        return SemTypes.errorDetail(detail);
    }

    private SemType resolveTypeDesc(BLangUnionTypeNode td, Env semtypeEnv,
                                    Map<String, BLangNode> mod, int depth, BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.memberTypeNodes.iterator();
        SemType u = resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            u = SemTypes.union(u, resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next()));
        }
        return u;
    }

    private SemType resolveTypeDesc(BLangIntersectionTypeNode td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition defn) {
        Iterator<BLangType> iterator = td.constituentTypeNodes.iterator();
        SemType type = resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next());
        while (iterator.hasNext()) {
            type = SemTypes.intersection(type, resolveTypeDesc(semtypeEnv, mod, defn, depth, iterator.next()));
        }
        return type;
    }

    private SemType resolveTypeDesc(BLangValueType td, Env semtypeEnv) {
        switch (td.typeKind) {
            case ANY:
                return PredefinedType.ANY;
            case ANYDATA:
                return SemTypes.createAnydata(Context.from(semtypeEnv));
            case BOOLEAN:
                return PredefinedType.BOOLEAN;
            case DECIMAL:
                return PredefinedType.DECIMAL;
            case ERROR:
                return PredefinedType.ERROR;
            case FLOAT:
                return PredefinedType.FLOAT;
            case HANDLE:
                return PredefinedType.HANDLE;
            case INT:
                return PredefinedType.INT;
            case READONLY:
                return PredefinedType.READONLY;
            case STRING:
                return PredefinedType.STRING;
            case TYPEDESC:
                return PredefinedType.TYPEDESC;
            case XML:
                return PredefinedType.XML;
            case JSON:
                return Core.createJson(semtypeEnv);
            case NIL:
                return PredefinedType.NIL;
            case BYTE:
                return PredefinedType.BYTE;
            default:
                throw new IllegalStateException("Unknown type: " + td.toString());
        }
    }

    private SemType resolveTypeDesc(BLangArrayType td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition moduleDefn) {
        Definition defn = td.defn;
        if (defn != null) {
            return defn.getSemType(semtypeEnv);
        }

        ListDefinition d = new ListDefinition();
        td.defn = d;
        try {
            SemType elementType = resolveTypeDesc(semtypeEnv, mod, moduleDefn, depth + 1, td.elemtype);

            ArrayList<BLangExpression> reversed = new ArrayList<>(td.sizes);
            for (BLangExpression t : reversed) {
                // todo: We need to constFold this expression.
                int size = constExprToInt(t);
                if (size >= 0) {
                    elementType = d.define(semtypeEnv, new ArrayList<>(List.of(elementType)), size);
                } else {
                    elementType = d.define(semtypeEnv, elementType);
                }
            }

            return elementType;
        } catch (Exception e) {
            td.defn = null;
            throw new UnsupportedOperationException("error resolving array type");
        }
    }

    private int constExprToInt(BLangExpression t) {
        if (t.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BConstantSymbol symbol = (BConstantSymbol) ((BLangSimpleVarRef) t).symbol;
            return ((Long) symbol.value.value).intValue();
        }
        return (int) ((BLangLiteral) t).value;
    }

    private SemType resolveTypeDesc(BLangTupleTypeNode td, Env semtypeEnv, Map<String, BLangNode> mod, int depth,
                                    BLangTypeDefinition moduleDefn) {
        Definition defn = td.defn;
        if (defn != null) {
            return defn.getSemType(semtypeEnv);
        }

        ListDefinition d = new ListDefinition();
        td.defn = d;
        try {
            List<SemType> members = new ArrayList<>();
            for (BLangType memberTypeNode : td.getMemberTypeNodes()) {
                members.add(resolveTypeDesc(semtypeEnv, mod, moduleDefn, depth + 1, memberTypeNode));
            }
            SemType restType = resolveTypeDesc(semtypeEnv, mod, moduleDefn, depth + 1, td.restParamType);
            if (restType == null) {
                restType = PredefinedType.NEVER;
            }

            return d.define(semtypeEnv, members, restType);
        } catch (Exception e) {
            td.defn = null;
            throw new UnsupportedOperationException("error resolving tuple type");
        }
    }

    static boolean isSemTypeEnabled(BType source, BType target) {
        return isSemTypeEnabled(source) && isSemTypeEnabled(target);
    }

    static boolean isSemTypeEnabled(BType bType) {
        switch (bType.tag) {
            case TypeTags.NEVER:
            case TypeTags.NIL:
            case TypeTags.BOOLEAN:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.FINITE:
                return true;
            case TypeTags.TYPEREFDESC:
                return isSemTypeEnabled(getReferredType(bType));
            default:
                return false;
        }
    }

    // --------------------------------------- Utility methods ----------------------------------------------

    public static void resolveBUnionSemTypeComponent(BUnionType type) {
        LinkedHashSet<BType> memberTypes = type.getMemberTypes();
        LinkedHashSet<BType> nonSemMemberTypes = new LinkedHashSet<>();

        SemType semType = PredefinedType.NEVER;
        for (BType memberType : memberTypes) {
            semType = SemTypes.union(semType, getSemTypeComponent(memberType));

            if (memberType.tag == TypeTags.UNION) {
                nonSemMemberTypes.addAll(((BUnionType) memberType).nonSemMemberTypes);
            } else if (getBTypeComponent(memberType).tag != TypeTags.NEVER) {
                nonSemMemberTypes.add(memberType);
            }
        }

        type.nonSemMemberTypes = nonSemMemberTypes;
        type.setSemTypeComponent(semType);
    }

    public static void resolveBIntersectionSemTypeComponent(BIntersectionType type) {
        SemType semType = PredefinedType.TOP;
        for (BType constituentType : type.getConstituentTypes()) {
            semType = SemTypes.intersection(semType, getSemTypeComponent(constituentType));
        }
        type.setSemTypeComponent(semType);
    }

    public static SemType getSemTypeComponent(BType t) {
        if (t == null) {
            return PredefinedType.NEVER;
        }

        if (t.tag == TypeTags.TYPEREFDESC) {
            return getSemTypeComponent(((BTypeReferenceType) t).referredType);
        }

        if (t.tag == TypeTags.UNION || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON) {
            return ((BUnionType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.INTERSECTION) {
            return ((BIntersectionType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.ANY) {
            return ((BAnyType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.READONLY) {
            return ((BReadonlyType) t).getSemTypeComponent();
        }

        if (semTypeSupported(t.tag)) {
            return t.getSemType();
        }

        return PredefinedType.NEVER;
    }

    /**
     * This method returns the same instance if the given type is not fully sem-type supported.
     * Hence, should be called very carefully.
     */
    @Deprecated
    public static BType getBTypeComponent(BType t) {
        if (t == null) {
            BType neverType = BType.createNeverType();
            neverType.isBTypeComponent = true;
            return neverType;
        }

        if (t.tag == TypeTags.TYPEREFDESC) {
            return getBTypeComponent(((BTypeReferenceType) t).referredType);
        }

        if (semTypeSupported(t.tag)) {
            BType neverType = BType.createNeverType();
            neverType.isBTypeComponent = true;
            return neverType;
        }

        return t;
    }

    public static boolean includesNonSemTypes(BType t) {
        if (t.tag == TypeTags.TYPEREFDESC) {
            return includesNonSemTypes(((BTypeReferenceType) t).referredType);
        }

        if (semTypeSupported(t.tag)) {
            return false;
        }

        if (t.tag == TypeTags.ANY || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON ||
                t.tag == TypeTags.READONLY) {
            return true;
        }

        if (t.tag == TypeTags.UNION) { // TODO: Handle intersection
            return !((BUnionType) t).nonSemMemberTypes.isEmpty();
        }

        return true;
    }

    protected static boolean semTypeSupported(TypeKind kind) {
        return switch (kind) {
            case NIL, BOOLEAN, INT, BYTE, FLOAT, DECIMAL, STRING, FINITE -> true;
            default -> false;
        };
    }

    protected static boolean semTypeSupported(int tag) {
        return switch (tag) {
            case TypeTags.NIL, TypeTags.BOOLEAN, TypeTags.INT, TypeTags.BYTE,
                    TypeTags.SIGNED32_INT, TypeTags.SIGNED16_INT, TypeTags.SIGNED8_INT,
                    TypeTags.UNSIGNED32_INT, TypeTags.UNSIGNED16_INT, TypeTags.UNSIGNED8_INT ,
                    TypeTags.FLOAT, TypeTags.DECIMAL,
                    TypeTags.STRING, TypeTags.CHAR_STRING,
                    TypeTags.FINITE-> true;
            default -> false;
        };
    }

    public static final SemType READONLY_SEM_COMPONENT = SemTypes.union(PredefinedType.NIL,
                                            SemTypes.union(PredefinedType.BOOLEAN,
                                            SemTypes.union(PredefinedType.INT,
                                            SemTypes.union(PredefinedType.FLOAT,
                                            SemTypes.union(PredefinedType.DECIMAL, PredefinedType.STRING)))));

    /**
     * Returns the basic type of singleton.
     * <p>
     * This will replace the existing <code>finiteType.getValueSpace().iterator().next().getBType()</code> calls
     *
     * @param t SemType component of BFiniteType
     */
    public static Optional<BType> singleShapeBroadType(SemType t, SymbolTable symTable) {
        if (PredefinedType.NIL.equals(t)) {
            return Optional.of(symTable.nilType);
        } else if (t instanceof UniformTypeBitSet) {
            return Optional.empty();
        } else if (Core.isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.intType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.floatType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.stringType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.booleanType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.DECIMAL)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_DECIMAL);
            Optional<BigDecimal> value = DecimalSubtype.decimalSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.decimalType);
        }
        return Optional.empty();
    }

    /**
     * Returns the basic types of singleton/union of singleton.
     * <p>
     * This will replace the existing <code>finiteType.getValueSpace().iterator()</code> calls
     *
     * @param t SemType component of BFiniteType
     */
    public static Set<BType> singletonBroadTypes(SemType t, SymbolTable symTable) { // Equivalent to getValueTypes()
        Set<BType> types = new LinkedHashSet<>(7);
        UniformTypeBitSet uniformTypeBitSet = widenToBasicTypes(t);
        if ((uniformTypeBitSet.bitset & PredefinedType.NIL.bitset) != 0) {
            types.add(symTable.nilType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.BOOLEAN.bitset) != 0) {
            types.add(symTable.booleanType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.INT.bitset) != 0) {
            types.add(symTable.intType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.FLOAT.bitset) != 0) {
            types.add(symTable.floatType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.DECIMAL.bitset) != 0) {
            types.add(symTable.decimalType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.STRING.bitset) != 0) {
            types.add(symTable.stringType);
        }

        return types;
    }

    /**
     * Counts number of bits set in bitset.
     * <p>
     * <i>Note: this is similar to <code>lib:bitCount()</code> in nBallerina</i>
     * </p><p>
     * This is the Brian Kernighan algorithm.
     * This won't work if bits is < 0.
     * <p/>
     *
     * @param bitset bitset for bits to be counted
     * @return the count
     */
    public static int bitCount(int bitset) {
        int n = 0;
        int v = bitset;
        while (v != 0) {
            v &= v - 1;
            n += 1;
        }
        return n;
    }
}
