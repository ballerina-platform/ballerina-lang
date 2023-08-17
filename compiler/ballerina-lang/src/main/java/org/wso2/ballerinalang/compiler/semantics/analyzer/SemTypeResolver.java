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

import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.HybridType;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            resultType.setSemtype(s);
        } catch (UnsupportedOperationException e) {
            // Do nothing
        }
    }

    void setSemTypeIfEnabled(BFiniteType finiteType) {
        if (!SEMTYPE_ENABLED) {
            return;
        }

        finiteType.setSemtype(resolveSingletonType(new ArrayList<>(finiteType.getValueSpace())));
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
            typeNode.getBType().setSemtype(semType);
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

    private static SemType resolveSingletonType(BLangLiteral literal) {
        Object litVal = literal.value;
        switch (literal.getBType().getKind()) {
            case FLOAT:
                double value;
                if (litVal instanceof Long) {
                    value = ((Long) litVal).doubleValue();
                } else if (litVal instanceof Double) {
                    value = (double) litVal;
                } else {
                    value = Double.parseDouble((String) litVal);
                }
                return SemTypes.floatConst(value);
            case INT:
            case BYTE:
                return SemTypes.intConst((long) litVal);
            case STRING:
                return SemTypes.stringConst((String) litVal);
            case BOOLEAN:
                return SemTypes.booleanConst((Boolean) litVal);
            case DECIMAL:
                return SemTypes.decimalConst((String) litVal);
            case NIL:
                return PredefinedType.NIL;
            default:
                throw new UnsupportedOperationException("Finite type not implemented for: " + literal);
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

    public static HybridType resolveBUnionHybridType(LinkedHashSet<BType> memberTypes) {
        SemType semType = PredefinedType.NEVER;
        LinkedHashSet<BType> bTypes = new LinkedHashSet<>(memberTypes.size());
        for (BType memberType : memberTypes) {
            HybridType ht = getHybridType(memberType);
            semType = SemTypes.union(semType, ht.getSemTypeComponent());
            BType bComponent = ht.getBTypeComponent();
            if (!(bComponent.getKind() == TypeKind.NEVER)) {
                bTypes.add(bComponent);
            }
        }

        BType bType;
        if (bTypes.size() == 0) {
            bType = BType.createNeverType();
        } else if (bTypes.size() == 1) {
            bType = bTypes.iterator().next();
        } else {
            bType = BUnionType.createBTypeComponent(bTypes);
        }

        return new HybridType(semType, bType);
    }

    public static HybridType resolveBIntersectionHybridType(LinkedHashSet<BType> constituentTypes) {
        SemType semType = PredefinedType.TOP;
        LinkedHashSet<BType> bTypes = new LinkedHashSet<>(constituentTypes.size());
        for (BType constituentType : constituentTypes) {
            HybridType hybridType = getHybridType(constituentType);
            semType = SemTypes.intersection(semType, hybridType.getSemTypeComponent());
            BType bComponent = hybridType.getBTypeComponent();
            if (!(bComponent.getKind() == TypeKind.NEVER)) {
                bTypes.add(bComponent);
            }
        }
        return new HybridType(semType, BIntersectionType.createBTypeComponent(bTypes));
    }

    public static HybridType resolveBFiniteTypeHybridType(List<BLangExpression> valueSpace) {
        // In case we encounter unary expressions in finite type, we will be replacing them with numeric literals
        replaceUnaryExprWithNumericLiteral(valueSpace);

        SemType semType = PredefinedType.NEVER;
        Set<BLangExpression> bTypeValSpace = new HashSet<>(valueSpace.size());
        for (BLangExpression bLangExpression : valueSpace) {
            BLangLiteral literal = (BLangLiteral) bLangExpression;
            if (semTypeSupported(literal.getBType().getKind())) {
                semType = SemTypes.union(semType, resolveSingletonType((BLangLiteral) bLangExpression));
            } else {
                bTypeValSpace.add(bLangExpression);
            }
        }

        return new HybridType(semType, BFiniteType.createBTypeComponent(bTypeValSpace));
    }

    public static void addBFiniteValue(BFiniteType bFiniteType, BLangExpression value) {
        HybridType hybridType = bFiniteType.getHybridType();
        SemType sComponent = hybridType.getSemTypeComponent();
        BFiniteType bComponent = (BFiniteType) hybridType.getBTypeComponent();

        if (semTypeSupported(value.getBType().getKind())) {
            sComponent = SemTypes.union(sComponent, resolveSingletonType((BLangLiteral) value));
        } else {
            bComponent.addValue(value, true);
        }

        bFiniteType.setHybridType(new HybridType(sComponent, bComponent));
    }

    public static HybridType getHybridType(BType t) {
        if (t.tag == TypeTags.TYPEREFDESC) {
            return getHybridType(((BTypeReferenceType) t).referredType);
        }

        if (t.tag == TypeTags.UNION || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON) {
            return ((BUnionType) t).getHybridType();
        }

        if (t.tag == TypeTags.INTERSECTION) {
            return ((BIntersectionType) t).getHybridType();
        }

        if (t.tag == TypeTags.FINITE) {
            return ((BFiniteType) t).getHybridType();
        }

        if (t.tag == TypeTags.ANY) {
            return ((BAnyType) t).getHybridType(); // TODO: bTypeComponent is still any
        }

        if (t.tag == TypeTags.READONLY) {
            return ((BReadonlyType) t).getHybridType(); // TODO: bTypeComponent is still readonly
        }

        if (semTypeSupported(t.tag)) {
            return new HybridType(t.getSemtype(), BType.createNeverType());
        }

        return new HybridType(PredefinedType.NEVER, t);
    }

    private static boolean semTypeSupported(TypeKind kind) {
        return switch (kind) {
            case NIL -> true;
            default -> false;
        };
    }

    private static boolean semTypeSupported(int tag) {
        return switch (tag) {
            case TypeTags.NIL -> true;
            default -> false;
        };
    }

    public static final SemType READONLY_SEM_COMPONENT = PredefinedType.NIL;
}
