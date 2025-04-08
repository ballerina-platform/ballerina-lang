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

import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.types.Atom;
import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.Bdd;
import io.ballerina.types.CombinedRange;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.ListMemberTypes;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypePair;
import io.ballerina.types.SemTypes;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.Range;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.BLangCompilerConstants;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static io.ballerina.types.BasicTypeCode.BT_OBJECT;
import static io.ballerina.types.Core.combineRanges;
import static io.ballerina.types.Core.createIsolatedObject;
import static io.ballerina.types.Core.createServiceObject;
import static io.ballerina.types.Core.isSubtypeSimple;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED16_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED16_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED32_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED32_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED8_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED8_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED16_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED32_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED8_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.OBJECT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.RECORD;
import static org.wso2.ballerinalang.compiler.util.TypeTags.UNION;

/**
 * This class consists of utility methods which operate on types.
 * These utility methods allows you to check the compatibility of two types,
 * i.e. check whether two types are equal, check whether one type is assignable to another type etc.
 *
 * @since 0.94
 */
public class Types {

    private static final CompilerContext.Key<Types> TYPES_KEY =
            new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final BLangDiagnosticLog dlog;
    private final Names names;
    private int finiteTypeCount = 0;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private SymbolEnv env;
    protected final Context semTypeCtx;

    private static final String BASE_16 = "base16";

    private static final BigDecimal DECIMAL_MAX =
            new BigDecimal("9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);

    private static final BigDecimal DECIMAL_MIN =
            new BigDecimal("-9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);

    private static final BigDecimal MIN_DECIMAL_MAGNITUDE =
            new BigDecimal("1.000000000000000000000000000000000e-6143", MathContext.DECIMAL128);

    public static final String AND_READONLY_SUFFIX = " & readonly";

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

    public Types(CompilerContext context) {
        this(context, new Env());
    }

    public Types(CompilerContext context, Env typeEnv) {
        context.put(TYPES_KEY, this);

        this.semTypeCtx = Context.from(typeEnv);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public BType checkType(BLangExpression node,
                           BType actualType,
                           BType expType) {
        return checkType(node, actualType, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
    }

    public BType addNilForNillableAccessType(BType actualType) {
        // index based map/record access always returns a nil-able type for optional/rest fields.
        if (actualType.isNullable()) {
            return actualType;
        }

        return BUnionType.create(typeEnv(), null, actualType, symTable.nilType);
    }

    public BType checkType(BLangExpression expr,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        expr.setDeterminedType(actualType);
        expr.setTypeCheckedType(checkType(expr.pos, actualType, expType, diagCode));

        if (expr.getBType().tag == TypeTags.SEMANTIC_ERROR) {
            return expr.getBType();
        }

        // Set an implicit cast expression, if applicable
        setImplicitCastExpr(expr, actualType, expType);

        return expr.getBType();
    }

    public boolean typeIncompatible(Location pos, BType actualType, BType expType) {
        return checkType(pos, actualType, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES) == symTable.semanticError;
    }

    public SemType getErrorIntersection(SemType t) {
        return SemTypes.intersect(t, PredefinedType.ERROR);
    }

    public BType getErrorTypes(BType bType) {
        bType = Types.getImpliedType(bType);
        if (bType == null) {
            return symTable.semanticError;
        }

        BType errorType = symTable.semanticError;

        int tag = bType.tag;
        if (tag == TypeTags.ERROR) {
            return bType;
        }

        if (tag == TypeTags.READONLY) {
            return symTable.errorType;
        }

        if (tag != TypeTags.UNION) {
            return errorType;
        }

        LinkedHashSet<BType> errTypes = new LinkedHashSet<>();
        Set<BType> memTypes = ((BUnionType) bType).getMemberTypes();
        for (BType memType : memTypes) {
            BType memErrType = getErrorTypes(memType);

            if (memErrType != symTable.semanticError) {
                errTypes.add(memErrType);
            }
        }

        if (errTypes.isEmpty()) {
            return errorType;
        }

        return errTypes.size() == 1 ? errTypes.iterator().next() :
                BUnionType.create(typeEnv(), null, errTypes);
    }

    public BType checkType(Location pos,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        if (expType.tag == TypeTags.SEMANTIC_ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return actualType;
        } else if (actualType.tag == TypeTags.SEMANTIC_ERROR) {
            return actualType;
        } else if (isAssignable(actualType, expType)) {
            return actualType;
        }

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(pos, diagCode, expType, actualType);
        return symTable.semanticError;
    }

    public boolean isLaxFieldAccessAllowed(BType type) {
        if (type.tag == TypeTags.SEMANTIC_ERROR) {
            return false;
        }
        return isLaxFieldAccessAllowed(type.semType());
    }

    public boolean isLaxFieldAccessAllowed(SemType t) {
        if (Core.isNever(t)) {
            return false;
        }
        return isSubtypeSimple(t, PredefinedType.XML) || isLaxType(t);
    }

    /**
     * Checks if the type is a lax type.
     * <p>
     * Rules:
     * <ul>
     *   <li>json and readonly-json are lax</li>
     *   <li>map&lt;T&gt; is lax if T is lax</li>
     *   <li>U = T1|T2...|Tn is lax, if Ti is lax for all i.</li>
     * </ul>
     *
     * @param t type to be checked
     * @return true if t is lax
     */
    private boolean isLaxType(SemType t) {
        SemType json = Core.createJson(semTypeCtx);
        if (SemTypes.isSameType(semTypeCtx, t, json) ||
                SemTypes.isSameType(semTypeCtx, t, SemTypes.intersect(json, PredefinedType.VAL_READONLY))) {
            return true;
        }

        Optional<List<MappingAtomicType>> optMatList = Core.mappingAtomicTypesInUnion(semTypeCtx, t);
        if (optMatList.isEmpty()) {
            return false;
        }

        List<MappingAtomicType> matList = optMatList.get();
        return matList.stream().allMatch(mat -> mat.names().length == 0 && isLaxType(Core.cellInnerVal(mat.rest())));
    }

    boolean isUniqueType(Iterable<BType> typeList, BType type) {
        type = Types.getImpliedType(type);
        boolean isRecord = type.tag == TypeTags.RECORD;

        for (BType bType : typeList) {
            bType = Types.getImpliedType(bType);
            if (isRecord) {
                // Seems defaultable values too are considered when checking uniqueness.
                if (type == bType) {
                    return false;
                }
            } else if (isSameType(type, bType)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSameType(BType source, BType target) {
        return isSameType(source.semType(), target.semType());
    }

    public boolean isSameTypeIncludingTags(BType source, BType target) {
        if (source.tag != target.tag) {
            return false;
        }

        if (source.tag == UNION) {
            boolean notSameType = ((BUnionType) source).getMemberTypes()
                    .stream()
                    .map(sT -> ((BUnionType) target).getMemberTypes()
                            .stream()
                            .anyMatch(it -> Types.getReferredType(it).tag == Types.getReferredType(sT).tag))
                    .anyMatch(foundSameType -> !foundSameType);
            if (notSameType) {
                return false;
            }
        }

        return isSameType(source.semType(), target.semType());
    }

    public boolean isSameType(SemType source, SemType target) {
        return SemTypes.isSameType(semTypeCtx, source, target);
    }

    public SemType anydata() {
        return Core.createAnydata(semTypeCtx);
    }

    public boolean isAnydata(SemType t) {
        return isSubtype(t, anydata());
    }

    public boolean isValueType(BType type) {
        return switch (getImpliedType(type).tag) {
            case TypeTags.BOOLEAN,
                 TypeTags.BYTE,
                 TypeTags.DECIMAL,
                 TypeTags.FLOAT,
                 TypeTags.INT,
                 TypeTags.STRING,
                 TypeTags.SIGNED32_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED8_INT,
                 TypeTags.UNSIGNED32_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED8_INT,
                 TypeTags.CHAR_STRING -> true;
            default -> false;
        };
    }

    boolean isBasicNumericType(BType bType) {
        BType type = getImpliedType(bType);
        return type.tag < TypeTags.STRING || TypeTags.isIntegerTypeTag(type.tag);
    }

    public boolean containsErrorType(BType bType) {
        return SemTypeHelper.containsBasicType(bType, PredefinedType.ERROR);
    }

    public boolean containsNilType(BType bType) {
        return SemTypeHelper.containsBasicType(bType, PredefinedType.NIL);
    }

    public boolean isSubTypeOfList(BType bType) {
        return SemTypeHelper.isSubtypeSimpleNotNever(bType, PredefinedType.LIST);
    }

    BType resolvePatternTypeFromMatchExpr(BLangErrorBindingPattern errorBindingPattern, BLangExpression matchExpr,
                                          SymbolEnv env) {
        if (matchExpr == null) {
            return errorBindingPattern.getBType();
        }
        BType intersectionType = getTypeIntersection(
                IntersectionContext.compilerInternalIntersectionContext(),
                matchExpr.getBType(), errorBindingPattern.getBType(), env);
        if (intersectionType == symTable.semanticError) {
            return symTable.noType;
        }
        return intersectionType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangListBindingPattern listBindingPattern,
                                                 BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern,
                                                 SymbolEnv env) {
        BTupleType listBindingPatternType = (BTupleType) listBindingPattern.getBType();
        if (varBindingPatternMatchPattern.matchExpr == null) {
            return listBindingPatternType;
        }
        BType matchExprType = varBindingPatternMatchPattern.matchExpr.getBType();
        BType intersectionType = getTypeIntersection(
                IntersectionContext.compilerInternalIntersectionContext(),
                matchExprType, listBindingPatternType, env);
        if (intersectionType != symTable.semanticError) {
            return intersectionType;
        }
        return symTable.noType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangListMatchPattern listMatchPattern,
                                                 BTupleType listMatchPatternType, SymbolEnv env) {
        if (listMatchPattern.matchExpr == null) {
            return listMatchPatternType;
        }
        BType matchExprType = listMatchPattern.matchExpr.getBType();
        BType intersectionType = getTypeIntersection(
                IntersectionContext.compilerInternalIntersectionContext(listMatchPattern.pos),
                matchExprType, listMatchPatternType, env);
        if (intersectionType != symTable.semanticError) {
            return intersectionType;
        }
        return symTable.noType;
    }

    BType resolvePatternTypeFromMatchExpr(BLangErrorMatchPattern errorMatchPattern, BLangExpression matchExpr) {
        if (matchExpr == null) {
            return errorMatchPattern.getBType();
        }

        BType matchExprType = matchExpr.getBType();
        BType patternType = errorMatchPattern.getBType();
        if (isAssignable(matchExprType, patternType)) {
            return matchExprType;
        }
        if (isAssignable(patternType, matchExprType)) {
            return patternType;
        }
        return symTable.noType;
    }

    public boolean isExpressionInUnaryValid(BLangExpression expr) {
        if (expr.getKind() == NodeKind.GROUP_EXPR) {
            // To resolve ex: -(45) kind of scenarios
            return ((BLangGroupExpr) expr).expression.getKind() == NodeKind.NUMERIC_LITERAL;
        } else {
            return expr.getKind() == NodeKind.NUMERIC_LITERAL;
        }
    }

    static BLangExpression checkAndReturnExpressionInUnary(BLangExpression expr) {
        if (expr.getKind() == NodeKind.GROUP_EXPR) {
            return ((BLangGroupExpr) expr).expression;
        }
        return expr;
    }

    public static void setValueOfNumericLiteral(BLangNumericLiteral newNumericLiteral, BLangUnaryExpr unaryExpr) {
        Object objectValueInUnary = ((BLangNumericLiteral) (checkAndReturnExpressionInUnary(unaryExpr.expr))).value;
        String strValueInUnary = String.valueOf(objectValueInUnary);
        OperatorKind unaryOperatorKind = unaryExpr.operator;

        if (OperatorKind.ADD.equals(unaryOperatorKind)) {
            strValueInUnary = "+" + strValueInUnary;
        } else if (OperatorKind.SUB.equals(unaryOperatorKind)) {
            strValueInUnary = "-" + strValueInUnary;
        }

        if (objectValueInUnary instanceof Long) {
            objectValueInUnary = Long.parseLong(strValueInUnary);
        } else if (objectValueInUnary instanceof Double) {
            objectValueInUnary = Double.parseDouble(strValueInUnary);
        } else {
            objectValueInUnary = strValueInUnary;
        }

        newNumericLiteral.value = objectValueInUnary;
        newNumericLiteral.originalValue = strValueInUnary;
    }

    public boolean isOperatorKindInUnaryValid(OperatorKind unaryOperator) {
        return OperatorKind.SUB.equals(unaryOperator) || OperatorKind.ADD.equals(unaryOperator);
    }

    public boolean isLiteralInUnaryAllowed(BLangUnaryExpr unaryExpr) {
        return isExpressionInUnaryValid(unaryExpr.expr) && isOperatorKindInUnaryValid(unaryExpr.operator);
    }

    public boolean isExpressionAnAllowedUnaryType(BLangExpression expr, NodeKind nodeKind) {
        if (nodeKind != NodeKind.UNARY_EXPR) {
            return false;
        }
        return isLiteralInUnaryAllowed((BLangUnaryExpr) expr);
    }

    public static BLangNumericLiteral constructNumericLiteralFromUnaryExpr(BLangUnaryExpr unaryExpr) {
        BLangExpression exprInUnary = checkAndReturnExpressionInUnary(unaryExpr.expr);

        BLangNumericLiteral newNumericLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        setValueOfNumericLiteral(newNumericLiteral, unaryExpr);
        newNumericLiteral.kind = ((BLangNumericLiteral) exprInUnary).kind;
        newNumericLiteral.pos = unaryExpr.pos;
        newNumericLiteral.setDeterminedType(exprInUnary.getBType());
        newNumericLiteral.setBType(exprInUnary.getBType());
        newNumericLiteral.expectedType = exprInUnary.getBType();
        newNumericLiteral.typeChecked = unaryExpr.typeChecked;
        newNumericLiteral.constantPropagated = unaryExpr.constantPropagated;

        if (unaryExpr.expectedType != null && getImpliedType(unaryExpr.expectedType).tag == TypeTags.FINITE) {
            newNumericLiteral.isFiniteContext = true;
        }

        return newNumericLiteral;
    }

    BType resolvePatternTypeFromMatchExpr(BLangConstPattern constPattern, BLangExpression constPatternExpr) {
        if (constPattern.matchExpr == null) {
            if (constPatternExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                return ((BLangSimpleVarRef) constPatternExpr).symbol.type;
            } else {
                return constPatternExpr.getBType();
            }
        }

        BType matchExprType = constPattern.matchExpr.getBType();
        BType constMatchPatternExprType = constPatternExpr.getBType();

        if (constPatternExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef constVarRef = (BLangSimpleVarRef) constPatternExpr;
            BType constVarRefSymbolType = constVarRef.symbol.type;
            if (isAssignable(constVarRefSymbolType, matchExprType)) {
                return constVarRefSymbolType;
            }
            return symTable.noType;
        }
        BLangLiteral constPatternLiteral;
        if (constPatternExpr.getKind() == NodeKind.UNARY_EXPR) {
            constPatternLiteral = constructNumericLiteralFromUnaryExpr((BLangUnaryExpr) constPatternExpr);
        } else {
            // After the above checks, according to spec all other const-patterns should be literals.
            constPatternLiteral = (BLangLiteral) constPatternExpr;
        }

        if (containsAnyType(constMatchPatternExprType)) {
            return matchExprType;
        } else if (containsAnyType(matchExprType)) {
            return constMatchPatternExprType;
        }
        // This should handle specially
        BType matchExprReferredType = getImpliedType(matchExprType);

        if (isValidLiteral(constPatternLiteral, matchExprReferredType)) {
            return matchExprType;
        }

        if (isAssignable(constMatchPatternExprType, matchExprType)) {
            return constMatchPatternExprType;
        }
        if (matchExprReferredType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) matchExprReferredType).getMemberTypes()) {
                if (getImpliedType(memberType).tag == TypeTags.FINITE) {
                    if (isAssignableToFiniteType(memberType, constPatternLiteral)) {
                        return memberType;
                    }
                } else {
                    if (isAssignable(constMatchPatternExprType, matchExprType)) {
                        return constMatchPatternExprType;
                    }
                }
            }
        } else if (matchExprReferredType.tag == TypeTags.FINITE) {
            if (isAssignableToFiniteType(matchExprType, constPatternLiteral)) {
                return matchExprType;
            }
        }
        return symTable.noType;
    }

    BType resolvePatternTypeFromMatchExpr(BLangMappingMatchPattern mappingMatchPattern, BType patternType,
                                                 SymbolEnv env) {
        if (mappingMatchPattern.matchExpr == null) {
            return patternType;
        }
        BType intersectionType = getTypeIntersection(IntersectionContext.matchClauseIntersectionContextForMapping(),
                mappingMatchPattern.matchExpr.getBType(), patternType, env);
        if (intersectionType == symTable.semanticError) {
            return symTable.noType;
        }
        return intersectionType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangMappingBindingPattern mappingBindingPattern,
                                                 BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern,
                                                 SymbolEnv env) {
        BRecordType mappingBindingPatternType = (BRecordType) mappingBindingPattern.getBType();
        if (varBindingPatternMatchPattern.matchExpr == null) {
            return mappingBindingPatternType;
        }
        BType intersectionType = getTypeIntersection(IntersectionContext.matchClauseIntersectionContextForMapping(),
                varBindingPatternMatchPattern.matchExpr.getBType(), mappingBindingPatternType, env);
        if (intersectionType == symTable.semanticError) {
            return symTable.noType;
        }
        return intersectionType;
    }

    private boolean containsAnyType(BType type) {
        return SemTypeHelper.containsType(semTypeCtx, type, PredefinedType.ANY);
    }

    private boolean containsAnyDataType(BType type) {
        return SemTypeHelper.containsType(semTypeCtx, type, Core.createAnydata(semTypeCtx));
    }

    BType mergeTypes(BType typeFirst, BType typeSecond) {
        if (containsAnyType(typeFirst) && !containsErrorType(typeSecond)) {
            return typeSecond;
        }
        if (containsAnyType(typeSecond) && !containsErrorType(typeFirst)) {
            return typeFirst;
        }
        if (containsAnyDataType(typeFirst) && !containsErrorType(typeSecond)) {
            return typeSecond;
        }
        if (containsAnyDataType(typeSecond) && !containsErrorType(typeFirst)) {
            return typeFirst;
        }
        if (isSameBasicType(typeFirst, typeSecond)) {
            return typeFirst;
        }
        return BUnionType.create(typeEnv(), null, typeFirst, typeSecond);
    }

    public boolean isSubTypeOfMapping(SemType s) {
        return SemTypes.isSubtypeSimpleNotNever(s, PredefinedType.MAPPING);
    }

    public boolean isSubTypeOfBaseType(BType bType, BasicTypeBitSet bbs) {
        return SemTypeHelper.isSubtypeSimpleNotNever(bType, bbs);
    }

    /**
     * @deprecated Use {@link #isSubTypeOfBaseType(BType, BasicTypeBitSet)} instead.
     */
    @Deprecated
    public boolean isSubTypeOfBaseType(BType bType, int baseTypeTag) {
        BType type = getImpliedType(bType);

        if (type.tag != TypeTags.UNION) {

            if ((TypeTags.isIntegerTypeTag(type.tag) || type.tag == TypeTags.BYTE) && TypeTags.INT == baseTypeTag) {
                return true;
            }

            if (TypeTags.isStringTypeTag(type.tag) && TypeTags.STRING == baseTypeTag) {
                return true;
            }

            if (TypeTags.isXMLTypeTag(type.tag) && TypeTags.XML == baseTypeTag) {
                return true;
            }

            return type.tag == baseTypeTag || (baseTypeTag == TypeTags.TUPLE && type.tag == TypeTags.ARRAY)
                    || (baseTypeTag == TypeTags.ARRAY && type.tag == TypeTags.TUPLE);
        }
        // TODO: Recheck this
        if (TypeTags.isXMLTypeTag(baseTypeTag)) {
            return true;
        }
        return isUnionMemberTypesSubTypeOfBaseType(((BUnionType) type).getMemberTypes(), baseTypeTag);
    }

    private boolean isUnionMemberTypesSubTypeOfBaseType(LinkedHashSet<BType> memberTypes, int baseTypeTag) {
        for (BType type : memberTypes) {
            if (!isSubTypeOfBaseType(type, baseTypeTag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether source type is assignable to the target type.
     *
     * @param source type.
     * @param target type.
     * @return true if source type is assignable to the target type.
     */
    public synchronized boolean isAssignable(BType source, BType target) {
        return isSubtype(source.semType(), target.semType());
    }

    public boolean isAssignableIgnoreObjectTypeIds(BType source, BType target) {
        SemType s = typeIgnoringObjectTypeIds(source.semType());
        SemType t = typeIgnoringObjectTypeIds(target.semType());
        return isSubtype(s, t);
    }

    public SemType typeIgnoringObjectTypeIds(SemType t) {
        SubtypeData objSubTypeData = Core.subtypeData(t, BT_OBJECT);
        if (!(objSubTypeData instanceof Bdd b)) {
            return t;
        }
        Bdd bdd = replaceObjectDistinctAtoms(b);
        SemType newObjSemType = Core.createBasicSemType(BT_OBJECT, bdd);
        SemType diff = Core.diff(t, PredefinedType.OBJECT);
        return Core.union(diff, newObjSemType);
    }

    /**
     * Replaces all distinct atoms in object type's bdd with full object equivalent atom.
     * ({@link PredefinedType#ATOM_MAPPING_OBJECT}).
     * <br>
     * This is to suppress effect coming from distinct atoms.
     * The return bdd will be equivalent to object bdd with no distinct atoms.
     *
     * @param b a bdd belong to object type
     * @return bdd with no distinct atoms
     */
    private Bdd replaceObjectDistinctAtoms(Bdd b) {
        if (b instanceof BddAllOrNothing) {
            return b;
        }

        BddNode bn = (BddNode) b;
        Atom atom = bn.atom();
        if (bn.atom().kind() == Atom.Kind.DISTINCT_ATOM) {
            atom = PredefinedType.ATOM_MAPPING_OBJECT;
        }
        Bdd left = replaceObjectDistinctAtoms(bn.left());
        Bdd middle = replaceObjectDistinctAtoms(bn.middle());
        Bdd right = replaceObjectDistinctAtoms(bn.right());
        return BddNode.create(atom, left, middle, right);
    }

    public boolean isSubtype(SemType t1, SemType t2) {
        return SemTypes.isSubtype(semTypeCtx, t1, t2);
    }

    public boolean isSubtype(BType t1, SemType t2) {
        return SemTypeHelper.isSubtype(semTypeCtx, t1, t2);
    }

    BField getTableConstraintField(BType constraintType, String fieldName) {
        constraintType = getImpliedType(constraintType);
        switch (constraintType.tag) {
            case TypeTags.RECORD:
                Map<String, BField> fieldList = ((BRecordType) constraintType).getFields();
                return fieldList.get(fieldName);
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) constraintType;
                Set<BType> memTypes = unionType.getMemberTypes();
                List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                        .filter(Objects::nonNull).toList();

                if (fields.size() != memTypes.size()) {
                    return null;
                }

                if (fields.stream().allMatch(field -> isAssignable(field.type, fields.get(0).type) &&
                        isAssignable(fields.get(0).type, field.type))) {
                    return fields.get(0);
                }
        }

        return null;
    }

    public boolean isInherentlyImmutableType(BType type) {
        type = getImpliedType(type);
        if (isValueType(type)) {
            return true;
        }

        return switch (type.tag) {
            case TypeTags.XML_TEXT,
                 TypeTags.FINITE, // Assuming a finite type will only have members from simple basic types.
                 TypeTags.READONLY,
                 TypeTags.NIL,
                 TypeTags.NEVER,
                 TypeTags.ERROR,
                 TypeTags.INVOKABLE,
                 TypeTags.TYPEDESC,
                 TypeTags.HANDLE,
                 TypeTags.REGEXP -> true;
            case TypeTags.XML -> getImpliedType(((BXMLType) type).constraint).tag == TypeTags.NEVER;
            default -> false;
        };
    }

    /**
     * Retrieve the referred type if a given type is a type reference type or
     * retrieve the effective type if the given type is an intersection type.
     *
     * @param type type to retrieve the implied type
     * @return the implied type if provided with a type reference type or an intersection type,
     * else returns the original type
     */
    public static BType getImpliedType(BType type) {
        type = getReferredType(type);
        if (type != null && type.tag == TypeTags.INTERSECTION) {
            return getImpliedType(((BIntersectionType) type).effectiveType);
        }

        return type;
    }

    public static BType getReferredType(BType type) {
        if (type != null && type.tag == TypeTags.TYPEREFDESC) {
            return getReferredType(((BTypeReferenceType) type).referredType);
        }

        return type;
    }

    public BLangExpression addConversionExprIfRequired(BLangExpression expr, BType lhsType) {
        if (lhsType.tag == TypeTags.NONE) {
            return expr;
        }

        BType rhsType = expr.getBType();

        if (lhsType.tag == TypeTags.TYPEREFDESC && rhsType.tag != TypeTags.TYPEREFDESC) {
            return addConversionExprIfRequired(expr, Types.getReferredType(lhsType));
        }

        if (rhsType.tag == lhsType.tag && isSameType(rhsType, lhsType)) {
            return expr;
        }

        setImplicitCastExpr(expr, rhsType, lhsType);
        if (expr.impConversionExpr != null) {
            BLangExpression impConversionExpr = expr.impConversionExpr;
            expr.impConversionExpr = null;
            return impConversionExpr;
        }

        if (lhsType.tag == TypeTags.JSON && rhsType.tag == TypeTags.NIL) {
            return expr;
        }

        if (lhsType.tag == TypeTags.NIL && rhsType.isNullable()) {
            return expr;
        }

        if (lhsType.tag == TypeTags.ARRAY && rhsType.tag == TypeTags.TUPLE) {
            return expr;
        }

        // Create a type cast expression
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr)
                TreeBuilder.createTypeConversionNode();
        conversionExpr.expr = expr;
        conversionExpr.targetType = lhsType;
        conversionExpr.setBType(lhsType);
        conversionExpr.pos = expr.pos;
        conversionExpr.checkTypes = false;
        conversionExpr.internal = true;
        return conversionExpr;
    }

    boolean isSelectivelyImmutableType(BType type, PackageID packageID) {
        return isSelectivelyImmutableType(type, new HashSet<>(), false, packageID);
    }

    boolean isSelectivelyImmutableType(BType type, boolean forceCheck, PackageID packageID) {
        return isSelectivelyImmutableType(type, new HashSet<>(), forceCheck, packageID);
    }

    public boolean isSelectivelyImmutableType(BType type, Set<BType> unresolvedTypes, PackageID packageID) {
        return isSelectivelyImmutableType(type, unresolvedTypes, false, packageID);
    }

    private boolean isSelectivelyImmutableType(BType input, Set<BType> unresolvedTypes, boolean forceCheck,
                                               PackageID packageID) {
        BType type = getImpliedType(input);

        if (isInherentlyImmutableType(type) || !(type instanceof SelectivelyImmutableReferenceType)) {
            // Always immutable.
            return false;
        }

        if (!unresolvedTypes.add(type)) {
            return true;
        }

        if (!forceCheck &&
                getImmutableType(symTable, packageID, (SelectivelyImmutableReferenceType) type).isPresent()) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
            case TypeTags.BYTE_ARRAY:
                return true;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) type;
                BType elementType = arrayType.eType;
                if (elementType == symTable.semanticError && arrayType.mutableType != null) {
                    elementType = arrayType.mutableType.eType;
                }
                return isInherentlyImmutableType(elementType) ||
                        isSelectivelyImmutableType(elementType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                List<BType> tupleMemberTypes = tupleType.getTupleTypes();
                if (tupleMemberTypes.isEmpty() && tupleType.mutableType != null) {
                    tupleMemberTypes = tupleType.mutableType.getTupleTypes();
                }
                for (BType memberType : tupleMemberTypes) {
                    if (!isInherentlyImmutableType(memberType) &&
                            !isSelectivelyImmutableType(memberType, unresolvedTypes, forceCheck, packageID)) {
                        return false;
                    }
                }

                BType tupRestType = tupleType.restType;
                if (tupRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(tupRestType) ||
                        isSelectivelyImmutableType(tupRestType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                LinkedHashMap<String, BField> recordFields = recordType.fields;
                if (recordFields.isEmpty() && recordType.mutableType != null) {
                    recordFields = recordType.mutableType.fields;
                }
                for (BField field : recordFields.values()) {
                    BType fieldType = field.type;
                    if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                            !isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck, packageID)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.MAP:
                BMapType mapType = (BMapType) type;
                BType constraintType = mapType.constraint;
                if (constraintType == symTable.semanticError && mapType.mutableType != null) {
                    constraintType = mapType.mutableType.constraint;
                }
                return isInherentlyImmutableType(constraintType) ||
                        isSelectivelyImmutableType(constraintType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.OBJECT:
                BObjectType objectType = (BObjectType) type;
                LinkedHashMap<String, BField> objectFields = objectType.fields;
                if (objectFields.isEmpty() && objectType.mutableType != null) {
                    objectFields = objectType.mutableType.fields;
                }
                for (BField field : objectFields.values()) {
                    BType fieldType = field.type;
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck, packageID)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) type;
                BType tableConstraintType = tableType.constraint;
                if (tableConstraintType == symTable.semanticError && tableType.mutableType != null) {
                    tableConstraintType = tableType.mutableType.constraint;
                }
                return isInherentlyImmutableType(tableConstraintType) ||
                        isSelectivelyImmutableType(tableConstraintType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.UNION:
                boolean readonlyIntersectionExists = false;
                BUnionType unionType = (BUnionType) type;
                LinkedHashSet<BType> memberTypes = unionType.getMemberTypes();
                for (BType memberType : memberTypes) {
                    if (isInherentlyImmutableType(memberType) ||
                            isSelectivelyImmutableType(memberType, unresolvedTypes, forceCheck, packageID)) {
                        readonlyIntersectionExists = true;
                    }
                }
                return readonlyIntersectionExists;
        }
        return false;
    }

    public static boolean containsTypeParams(BInvokableType type) {
        boolean hasParameterizedTypes = type.paramTypes.stream()
                .anyMatch(t -> {
                    t = getImpliedType(t);
                    if (t.tag == TypeTags.FUNCTION_POINTER) {
                        return containsTypeParams((BInvokableType) t);
                    }
                    return TypeParamAnalyzer.isTypeParam(t);
                });

        if (hasParameterizedTypes) {
            return true;
        }

        BType retType = getImpliedType(type.retType);
        if (retType.tag == TypeTags.FUNCTION_POINTER) {
            return containsTypeParams((BInvokableType) retType);
        }

        return TypeParamAnalyzer.isTypeParam(type.retType);
    }

    public void setForeachTypedBindingPatternType(BLangForeach foreachNode) {
        BType collectionType = getImpliedType(foreachNode.collection.getBType());
        BType varType;
        switch (collectionType.tag) {
            case TypeTags.STRING:
                varType = symTable.charStringType;
                break;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) collectionType;
                varType = arrayType.eType;
                break;
            case TypeTags.TUPLE:
                varType = getTupleMemberType((BTupleType) collectionType);
                break;
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                varType = bMapType.constraint;
                break;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                varType = inferRecordFieldType(recordType);
                break;
            case TypeTags.XML:
                BType typedBindingPatternType = getTypedBindingPatternTypeForXmlCollection(collectionType);
                if (typedBindingPatternType == null) {
                    foreachNode.varType = symTable.semanticError;
                    foreachNode.resultType = symTable.semanticError;
                    foreachNode.nillableResultType = symTable.semanticError;
                    return;
                }
                varType = typedBindingPatternType;
                break;
            case TypeTags.XML_TEXT:
                varType = symTable.xmlTextType;
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                varType = tableType.constraint;
                break;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) collectionType;
                if (streamType.constraint.tag == TypeTags.NONE) {
                    varType = symTable.anydataType;
                    break;
                }
                varType = streamType.constraint;
                List<BType> completionType = getAllTypes(streamType.completionType, true);
                if (completionType.stream().anyMatch(type -> getImpliedType(type).tag != TypeTags.NIL)) {
                    BType actualType = BUnionType.create(typeEnv(), null, varType, streamType.completionType);
                    dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                            varType, actualType);
                }
                break;
            case TypeTags.OBJECT:
                // check for iterable objects
                BUnionType nextMethodReturnType = getVarTypeFromIterableObject((BObjectType) collectionType);
                if (nextMethodReturnType != null) {
                    foreachNode.resultType = getRecordType(nextMethodReturnType);
                    BType valueType = (foreachNode.resultType != null)
                            ? ((BRecordType) foreachNode.resultType).fields.get("value").type : null;
                    BType errorType = getErrorType(nextMethodReturnType);
                    if (errorType != null) {
                        BType actualType = BUnionType.create(typeEnv(), null, valueType, errorType);
                        dlog.error(foreachNode.collection.pos,
                                DiagnosticErrorCode.INVALID_ITERABLE_COMPLETION_TYPE_IN_FOREACH_NEXT_FUNCTION,
                                actualType, errorType);
                    }
                    foreachNode.nillableResultType = nextMethodReturnType;
                    foreachNode.varType = valueType;
                    return;
                }
                // fallthrough
            case TypeTags.SEMANTIC_ERROR:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                return;
            default:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.ITERABLE_NOT_SUPPORTED_COLLECTION,
                                 collectionType);
                return;
        }

        BInvokableSymbol iteratorSymbol = (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionType,
                Names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), env);
        BObjectType objectType = (BObjectType) getImpliedType(iteratorSymbol.retType);
        BUnionType nextMethodReturnType =
                (BUnionType) getResultTypeOfNextInvocation(objectType);
        foreachNode.varType = varType;
        foreachNode.resultType = getRecordType(nextMethodReturnType);
        foreachNode.nillableResultType = nextMethodReturnType;
    }

    public void setInputClauseTypedBindingPatternType(BLangInputClause bLangInputClause) {
        if (bLangInputClause.collection == null) {
            //not-possible
            return;
        }

        BType collectionType = bLangInputClause.collection.getBType();
        bLangInputClause.varType = visitCollectionType(bLangInputClause, collectionType);
        if (bLangInputClause.varType.tag == TypeTags.SEMANTIC_ERROR ||
                getImpliedType(collectionType).tag == OBJECT) {
            return;
        }
        
        BInvokableSymbol iteratorSymbol = (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionType,
                Names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), env);
        BUnionType nextMethodReturnType =
                (BUnionType) getResultTypeOfNextInvocation((BObjectType) getImpliedType(iteratorSymbol.retType));
        bLangInputClause.resultType = getRecordType(nextMethodReturnType);
        bLangInputClause.nillableResultType = nextMethodReturnType;
    }

    private BType getTypedBindingPatternTypeForXmlCollection(BType collectionType) {
        BType constraint = getImpliedType(((BXMLType) collectionType).constraint);
        while (constraint.tag == TypeTags.XML) {
            collectionType = constraint;
            constraint = getImpliedType(((BXMLType) collectionType).constraint);
        }

        switch (constraint.tag) {
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_TEXT:
            case TypeTags.XML_PI:
            case TypeTags.NEVER:
                return constraint;
            case TypeTags.UNION:
                Set<BType> collectionTypes = getEffectiveMemberTypes((BUnionType) constraint);
                Set<BType> builtinXMLConstraintTypes = getEffectiveMemberTypes
                        ((BUnionType) ((BXMLType) symTable.xmlType).constraint);
                return collectionTypes.size() == 4 && builtinXMLConstraintTypes.equals(collectionTypes) ?
                        collectionType :
                        BUnionType.create(typeEnv(), null, (LinkedHashSet<BType>) collectionTypes);
            default:
                return null;
        }
    }

    private BType visitCollectionType(BLangInputClause bLangInputClause, BType collectionType) {
        collectionType = getImpliedType(collectionType);
        switch (collectionType.tag) {
            case TypeTags.STRING:
                return symTable.stringType;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) collectionType;
                return arrayType.eType;
            case TypeTags.TUPLE:
                return getTupleMemberType((BTupleType) collectionType);
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                return bMapType.constraint;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                return inferRecordFieldType(recordType);
            case TypeTags.XML:
                BType bindingPatternType = getTypedBindingPatternTypeForXmlCollection(collectionType);
                return bindingPatternType == null ? symTable.semanticError : bindingPatternType;
            case TypeTags.XML_TEXT:
                return symTable.xmlTextType;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                return tableType.constraint;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) collectionType;
                if (streamType.constraint.tag == TypeTags.NONE) {
                    return symTable.anydataType;
                }
                return streamType.constraint;
            case TypeTags.OBJECT:
                // check for iterable objects
                if (!isAssignable(collectionType, symTable.iterableType)) {
                    dlog.error(bLangInputClause.collection.pos, DiagnosticErrorCode.INVALID_ITERABLE_OBJECT_TYPE,
                            bLangInputClause.collection.getBType(), symTable.iterableType);
                    bLangInputClause.varType = symTable.semanticError;
                    bLangInputClause.resultType = symTable.semanticError;
                    bLangInputClause.nillableResultType = symTable.semanticError;
                    break;
                }
                
                BUnionType nextMethodReturnType = getVarTypeFromIterableObject((BObjectType) collectionType);
                if (nextMethodReturnType != null) {
                    bLangInputClause.resultType = getRecordType(nextMethodReturnType);
                    bLangInputClause.nillableResultType = nextMethodReturnType;
                    bLangInputClause.varType = ((BRecordType) bLangInputClause.resultType).fields.get("value").type;
                    return bLangInputClause.varType;
                }
                // fallthrough
            case TypeTags.SEMANTIC_ERROR:
                bLangInputClause.varType = symTable.semanticError;
                bLangInputClause.resultType = symTable.semanticError;
                bLangInputClause.nillableResultType = symTable.semanticError;
                break;
            default:
                bLangInputClause.varType = symTable.semanticError;
                bLangInputClause.resultType = symTable.semanticError;
                bLangInputClause.nillableResultType = symTable.semanticError;
                dlog.error(bLangInputClause.collection.pos, DiagnosticErrorCode.ITERABLE_NOT_SUPPORTED_COLLECTION,
                        collectionType);
        }
        return symTable.semanticError;
    }

    private BType getTupleMemberType(BTupleType tupleType) {
        LinkedHashSet<BType> tupleTypes = new LinkedHashSet<>(tupleType.getTupleTypes());
        if (tupleType.restType != null) {
            tupleTypes.add(tupleType.restType);
        }
        int tupleTypesSize = tupleTypes.size();
        if (tupleTypesSize == 0) {
            return symTable.neverType;
        }
        return tupleTypesSize == 1 ?
                tupleTypes.iterator().next() : BUnionType.create(typeEnv(), null, tupleTypes);
    }

    public BUnionType getVarTypeFromIterableObject(BObjectType collectionType) {
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) collectionType.tsymbol;
        for (BAttachedFunction func : objectTypeSymbol.attachedFuncs) {
            if (func.funcName.value.equals(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC)) {
                return getVarTypeFromIteratorFunc(func);
            }
        }

        return null;
    }

    private BUnionType getVarTypeFromIteratorFunc(BAttachedFunction candidateIteratorFunc) {
        if (!candidateIteratorFunc.type.paramTypes.isEmpty()) {
            return null;
        }

        BType returnType = candidateIteratorFunc.type.retType;
        // abstract object {public function next() returns record {|int value;|}?;}
        return getVarTypeFromIteratorFuncReturnType(returnType);
    }

    public BUnionType getVarTypeFromIteratorFuncReturnType(BType type) {
        BObjectTypeSymbol objectTypeSymbol;
        BType returnType = getImpliedType(type);
        if (returnType.tag != TypeTags.OBJECT) {
            return null;
        }

        objectTypeSymbol = (BObjectTypeSymbol) returnType.tsymbol;
        for (BAttachedFunction func : objectTypeSymbol.attachedFuncs) {
            if (func.funcName.value.equals(BLangCompilerConstants.NEXT_FUNC)) {
                return getVarTypeFromNextFunc(func);
            }
        }

        return null;
    }

    private BUnionType getVarTypeFromNextFunc(BAttachedFunction nextFunc) {
        BType returnType;
        if (!nextFunc.type.paramTypes.isEmpty()) {
            return null;
        }

        returnType = nextFunc.type.retType;
        // Check if the next function return type has the union type,
        // record {|int value;|}|error|();
        if (checkNextFuncReturnType(returnType)) {
            return (BUnionType) returnType;
        }

        return null;
    }

    private boolean checkNextFuncReturnType(BType returnType) {
        if (getImpliedType(returnType).tag != TypeTags.UNION) {
            return false;
        }

        List<BType> types = getAllTypes(returnType, true);
        boolean containsCompletionType = types.removeIf(type -> type.tag == TypeTags.NIL);
        containsCompletionType = types.removeIf(type -> type.tag == TypeTags.ERROR) || containsCompletionType;
        if (!containsCompletionType) {
            return false;
        }

        if (types.size() != 1) {
            //TODO: print error
            return false;
        }

        if (types.get(0).tag != TypeTags.RECORD) {
            return false;
        }

        BRecordType recordType = (BRecordType) types.get(0);
        // Check if the union type has the record type,
        // record {|int value;|};
        return checkRecordTypeInNextFuncReturnType(recordType);
    }

    private boolean checkRecordTypeInNextFuncReturnType(BRecordType recordType) {
        if (!recordType.sealed) {
            return false;
        }

        if (recordType.fields.size() != 1) {
            return false;
        }

        return recordType.fields.containsKey(BLangCompilerConstants.VALUE_FIELD);
    }

    private BRecordType getRecordType(BUnionType type) {
        for (BType member : type.getMemberTypes()) {
            BType referredRecordType = getImpliedType(member);
            if (referredRecordType.tag == TypeTags.RECORD) {
                return (BRecordType) referredRecordType;
            }
        }
        return null;
    }

    public BErrorType getErrorType(BUnionType type) {
        for (BType member : type.getMemberTypes()) {
            member = getImpliedType(member);

            if (member.tag == TypeTags.ERROR) {
                return (BErrorType) member;
            } else if (member.tag == TypeTags.UNION) {
                BErrorType e = getErrorType((BUnionType) member);
                if (e != null) {
                    return e;
                }
            }
        }
        return null;
    }

    public BType getResultTypeOfNextInvocation(BObjectType iteratorType) {
        BAttachedFunction nextFunc = getAttachedFuncFromObject(iteratorType, BLangCompilerConstants.NEXT_FUNC);
        return Objects.requireNonNull(nextFunc).type.retType;
    }

    public BAttachedFunction getAttachedFuncFromObject(BObjectType objectType, String funcName) {
        BObjectTypeSymbol iteratorSymbol = (BObjectTypeSymbol) objectType.tsymbol;
        for (BAttachedFunction bAttachedFunction : iteratorSymbol.attachedFuncs) {
            if (funcName.equals(bAttachedFunction.funcName.value)) {
                return bAttachedFunction;
            }
        }
        return null;
    }

    public BType inferRecordFieldType(BRecordType recordType) {
        Map<String, BField> fields = recordType.fields;
        BUnionType unionType = BUnionType.create(typeEnv(), null);

        if (!recordType.sealed) {
            unionType.add(recordType.restFieldType);
        } else if (fields.isEmpty()) {
            unionType.add(symTable.neverType);
        }

        for (BField field : fields.values()) {
            if (isAssignable(field.type, unionType)) {
                continue;
            }

            if (isAssignable(unionType, field.type)) {
                unionType = BUnionType.create(typeEnv(), null);
            }

            unionType.add(field.type);
        }

        if (unionType.getMemberTypes().size() > 1) {
            unionType.tsymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                         Names.EMPTY, recordType.tsymbol.pkgID, null,
                                                         recordType.tsymbol.owner, symTable.builtinPos, VIRTUAL);
            return unionType;
        }

        return unionType.getMemberTypes().iterator().next();
    }


    public BType getTypeWithEffectiveIntersectionTypes(BType bType) {
        // TODO Can remove this method since this unwraps the referred type and intersection type. #40958
        BType type = getReferredType(bType);
        BType effectiveType = null;
        if (type.tag == TypeTags.INTERSECTION) {
            effectiveType = ((BIntersectionType) type).effectiveType;
            type = effectiveType;
        }

        if (type.tag != TypeTags.UNION) {
            return Objects.requireNonNullElse(effectiveType, bType);
        }

        LinkedHashSet<BType> members = new LinkedHashSet<>();
        boolean hasDifferentMember = false;

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            effectiveType = getTypeWithEffectiveIntersectionTypes(memberType);
            effectiveType = getImpliedType(effectiveType);
            if (effectiveType != memberType) {
                hasDifferentMember = true;
            }
            members.add(effectiveType);
        }

        if (hasDifferentMember) {
            return BUnionType.create(typeEnv(), null, members);
        }
        return bType;
    }

    /**
     * Enum to represent type test result.
     *
     * @since 1.2.0
     */
    enum TypeTestResult {
        NOT_FOUND,
        TRUE,
        FALSE
    }

    TypeTestResult isBuiltInTypeWidenPossible(BType actualType, BType targetType) { // TODO: can we remove?
        int targetTag = getImpliedType(targetType).tag;
        int actualTag = getImpliedType(actualType).tag;

        if (actualTag < TypeTags.JSON && targetTag < TypeTags.JSON) {
            // Fail Fast for value types.
            switch (actualTag) {
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                    if (targetTag == TypeTags.BOOLEAN || targetTag == TypeTags.STRING) {
                        return TypeTestResult.FALSE;
                    }
                    break;
                case TypeTags.BOOLEAN:
                    if (targetTag == TypeTags.INT || targetTag == TypeTags.BYTE || targetTag == TypeTags.FLOAT
                            || targetTag == TypeTags.DECIMAL || targetTag == TypeTags.STRING) {
                        return TypeTestResult.FALSE;
                    }
                    break;
                case TypeTags.STRING:
                    if (targetTag == TypeTags.INT || targetTag == TypeTags.BYTE || targetTag == TypeTags.FLOAT
                            || targetTag == TypeTags.DECIMAL || targetTag == TypeTags.BOOLEAN) {
                        return TypeTestResult.FALSE;
                    }
                    break;
            }
        }
        switch (actualTag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.CHAR_STRING:
                if (targetTag == TypeTags.JSON || targetTag == TypeTags.ANYDATA || targetTag == TypeTags.ANY ||
                        targetTag == TypeTags.READONLY) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.ANYDATA:
            case TypeTags.TYPEDESC:
                if (targetTag == TypeTags.ANY) {
                    return TypeTestResult.TRUE;
                }
                break;
            default:
        }

        if (TypeTags.isIntegerTypeTag(targetTag) && actualTag == targetTag) {
            return TypeTestResult.FALSE; // No widening.
        }

        // Validate for Integers subtypes.
        if ((TypeTags.isIntegerTypeTag(actualTag) || actualTag == TypeTags.BYTE)
                && (TypeTags.isIntegerTypeTag(targetTag) || targetTag == TypeTags.BYTE)) {
            return checkBuiltInIntSubtypeWidenPossible(actualType, targetType);
        }

        if (actualTag == TypeTags.CHAR_STRING && TypeTags.STRING == targetTag) {
            return TypeTestResult.TRUE;
        }
        return TypeTestResult.NOT_FOUND;
    }

    private TypeTestResult checkBuiltInIntSubtypeWidenPossible(BType actualType, BType targetType) {
        int actualTag = getImpliedType(actualType).tag;
        targetType = getImpliedType(targetType);
        switch (targetType.tag) {
            case TypeTags.INT:
                if (actualTag == TypeTags.BYTE || TypeTags.isIntegerTypeTag(actualTag)) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.SIGNED32_INT:
                if (actualTag == TypeTags.SIGNED16_INT || actualTag == TypeTags.SIGNED8_INT ||
                        actualTag == TypeTags.UNSIGNED16_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.SIGNED16_INT:
                if (actualTag == TypeTags.SIGNED8_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED32_INT:
                if (actualTag == TypeTags.UNSIGNED16_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED16_INT:
                if (actualTag == TypeTags.UNSIGNED8_INT || actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.BYTE:
                if (actualTag == TypeTags.UNSIGNED8_INT) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED8_INT:
                if (actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
        }
        return TypeTestResult.NOT_FOUND;
    }

    public boolean isImplicitlyCastable(BType actual, BType target) {
        /* The word Builtin refers for Compiler known types. */

        BType targetType = getImpliedType(target);
        BType actualType = getImpliedType(actual);
        BType newTargetType = targetType;
        int targetTypeTag = targetType.tag;
        if ((targetTypeTag == TypeTags.UNION || targetTypeTag == TypeTags.FINITE) && isValueType(actualType)) {
            newTargetType = symTable.anyType;   // TODO : Check for correctness.
        }

        TypeTestResult result = isBuiltInTypeWidenPossible(actualType, newTargetType);
        if (result != TypeTestResult.NOT_FOUND) {
            return result == TypeTestResult.TRUE;
        }

        if (isValueType(targetType) &&
                (actualType.tag == TypeTags.FINITE ||
                        (actualType.tag == TypeTags.UNION && ((BUnionType) actualType).getMemberTypes().stream()
                                .anyMatch(type -> getImpliedType(type).tag == TypeTags.FINITE &&
                                        isAssignable(type, targetType))))) {
            // for nil, no cast is required
            return TypeTags.isIntegerTypeTag(targetTypeTag) ||  targetType.tag == TypeTags.BYTE ||
                    targetTypeTag == TypeTags.FLOAT ||
                    targetTypeTag == TypeTags.DECIMAL ||
                    TypeTags.isStringTypeTag(targetTypeTag) ||
                    targetTypeTag == TypeTags.BOOLEAN;

        } else if (isValueType(targetType) && actualType.tag == TypeTags.UNION &&
                ((BUnionType) actualType).getMemberTypes().stream().allMatch(type -> isAssignable(type, targetType))) {
            return true;

        } else if (targetTypeTag == TypeTags.ERROR
                && (actualType.tag == TypeTags.UNION
                && isAllErrorMembers((BUnionType) actualType))) {
            return true;
        }
        return false;
    }

    public boolean isTypeCastable(BType source, BType target) {
        BType sourceType = getImpliedType(source);
        BType targetType = getImpliedType(target);
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
                sourceType == targetType) {
            return true;
        }

        SemType sourceSemType = sourceType.semType();
        SemType targetSemType = targetType.semType();

        // Disallow casting away error, this forces user to handle the error via type-test, check, or checkpanic
        if (containsErrorType(sourceSemType) && !containsErrorType(targetSemType)) {
            return false;
        }

        if (isNumericConversionPossible(sourceType, targetType)) {
            return true;
        }

        return intersectionExists(sourceSemType, targetSemType);
    }

    public boolean containsErrorType(SemType t) {
        return SemTypes.containsBasicType(t, PredefinedType.ERROR);
    }

    boolean isNumericConversionPossible(BType sourceType, BType targetType) {
        Optional<BasicTypeBitSet> targetNumericType = Core.singleNumericType(targetType.semType());
        if (targetNumericType.isEmpty()) {
            return false;
        }

        return !Core.isEmpty(semTypeCtx, SemTypes.intersect(sourceType.semType(), PredefinedType.NUMBER));
    }

    public boolean isAllErrorMembers(BUnionType actualType) {
        return isSubtype(actualType, PredefinedType.ERROR);
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType targetType) {
        BType expType = getImpliedType(targetType);
        if (!isImplicitlyCastable(actualType, expType)) {
            return;
        }
        BLangTypeConversionExpr implicitConversionExpr =
                (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        implicitConversionExpr.pos = expr.pos;
        implicitConversionExpr.expr = expr.impConversionExpr == null ? expr : expr.impConversionExpr;
        implicitConversionExpr.setBType(expType);
        implicitConversionExpr.targetType = expType;
        implicitConversionExpr.internal = true;
        expr.impConversionExpr = implicitConversionExpr;
    }

    public boolean checkListenerCompatibilityAtServiceDecl(BType type) {
        if (getImpliedType(type).tag == TypeTags.UNION) {
            // There should be at least one listener compatible type and all the member types, except error type
            // should be listener compatible.
            int listenerCompatibleTypeCount = 0;
            for (BType memberType : getAllTypes(type, true)) {
                if (memberType.tag != TypeTags.ERROR) {
                    if (!checkListenerCompatibility(memberType)) {
                        return false;
                    }
                    listenerCompatibleTypeCount++;
                }
            }
            return listenerCompatibleTypeCount > 0;
        }
        return checkListenerCompatibility(type);
    }

    public boolean checkListenerCompatibility(BType bType) {
        BType type = getImpliedType(bType);
        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            for (BType memberType : unionType.getMemberTypes()) {
                if (!checkListenerCompatibility(memberType)) {
                    return false;
                }
            }
            return true;
        }

        if (type.tag != TypeTags.OBJECT) {
            return false;
        }

        BObjectType rhsType = (BObjectType) type;
        List<BAttachedFunction> rhsFuncs = ((BStructureTypeSymbol) rhsType.tsymbol).attachedFuncs;

        ListenerValidationModel listenerValidationModel = new ListenerValidationModel(this, symTable);
        return listenerValidationModel.checkMethods(rhsFuncs);

    }

    public boolean isValidErrorDetailType(BType detailType) {
        return switch (getImpliedType(detailType).tag) {
            case TypeTags.MAP, TypeTags.RECORD -> isAssignable(detailType, symTable.detailType);
            default -> false;
        };
    }

    // private methods

    private Set<BType> getEffectiveMemberTypes(BUnionType unionType) {
        Set<BType> memTypes = new LinkedHashSet<>();

        for (BType memberType : unionType.getMemberTypes()) {
            switch (memberType.tag) {
                case TypeTags.UNION:
                    memTypes.addAll(getEffectiveMemberTypes((BUnionType) memberType));
                    break;
                case TypeTags.TYPEREFDESC:
                case TypeTags.INTERSECTION:
                    BType constraint = getImpliedType(memberType);
                    if (constraint.tag == TypeTags.UNION) {
                        memTypes.addAll(getEffectiveMemberTypes((BUnionType) constraint));
                        continue;
                    }
                    memTypes.add(constraint);
                    break;
                default:
                    memTypes.add(memberType);
                    break;
            }
        }
        return memTypes;
    }

    boolean isAssignableToFiniteType(BType type, BLangLiteral literalExpr) {
        type = getImpliedType(type);
        if (type.tag != TypeTags.FINITE) {
            return false;
        }

        BFiniteType expType = (BFiniteType) type;
        return checkLiteralAssignabilityBasedOnType(literalExpr, expType, literalExpr.getBType().tag);
    }

    /**
     * Method to check the literal assignability based on the types of the literals. For numeric literals the
     * assignability depends on the equivalency of the literals. If the candidate literal could either be a simple
     * literal or a constant. In case of a constant, it is assignable to the base literal if and only if both
     * literals have same type and equivalent values.
     *
     * @param literal Literal to be tested whether it is assignable to the base literal or not.
     * @param finiteType
     * @param targetTypeTag
     * @return true if assignable; false otherwise.
     */
    boolean checkLiteralAssignabilityBasedOnType(BLangLiteral literal, BFiniteType finiteType, int targetTypeTag) {
        Object value = literal.value;
        int literalTypeTag = literal.getBType().tag;

        // Numeric literal assignability is based on assignable type and numeric equivalency of values.
        // If the base numeric literal is,
        // (1) byte: we can assign byte or a int simple literal (Not an int constant) with the same value.
        // (2) int: we can assign int literal or int constants with the same value.
        // (3) float: we can assign int simple literal(Not an int constant) or a float literal/constant with same value.
        // (4) decimal: we can assign int simple literal or float simple literal (Not int/float constants) or decimal
        // with the same value.
        SemType t = finiteType.semType();
        switch (targetTypeTag) {
            case TypeTags.INT:
                if (literalTypeTag == TypeTags.INT) {
                    if (value instanceof String) {
                        return false;
                    }
                    return Core.containsConstInt(t, ((Number) value).longValue());
                }
                break;
            case TypeTags.FLOAT:
                double doubleValue;
                if (literalTypeTag == TypeTags.INT && !literal.isConstant) {
                    if (literal.value instanceof Double) {
                        // Out of range value for int but in range for float
                        doubleValue = Double.parseDouble(String.valueOf(value));
                    } else {
                        doubleValue = ((Long) value).doubleValue();
                    }
                    return Core.containsConstFloat(t, doubleValue);
                } else if (literalTypeTag == TypeTags.FLOAT) {
                    try {
                        doubleValue = Double.parseDouble(String.valueOf(value));
                        return Core.containsConstFloat(t, doubleValue);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                break;
            case TypeTags.DECIMAL:
                BigDecimal decimalValue;
                if (literalTypeTag == TypeTags.INT && !literal.isConstant) {
                    if (literal.value instanceof String) {
                        // out of range value for float but in range for decimal
                        decimalValue = NumericLiteralSupport.parseBigDecimal(value);
                    } else if (literal.value instanceof Double) {
                        // out of range value for int in range for decimal and float
                        decimalValue = new BigDecimal((Double) value, MathContext.DECIMAL128);
                    } else {
                        decimalValue = new BigDecimal((long) value, MathContext.DECIMAL128);
                    }
                    return Core.containsConstDecimal(t, decimalValue);
                } else if (literalTypeTag == TypeTags.FLOAT && !literal.isConstant ||
                        literalTypeTag == TypeTags.DECIMAL) {
                    if (NumericLiteralSupport.isFloatDiscriminated(String.valueOf(value))) {
                        return false;
                    }
                    try {
                        decimalValue = NumericLiteralSupport.parseBigDecimal(value);
                        return Core.containsConstDecimal(t, decimalValue);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                break;
            default:
                // Non-numeric literal kind.
                return Core.containsConst(t, value);
        }
        return false;
    }

    boolean validateFloatLiteral(Location pos, String numericLiteral) {
        double value;
        try {
             value = Double.parseDouble(numericLiteral);
        } catch (Exception e) {
            // We may reach here if a floating point literal has syntax diagnostics.
            return false;
        }

        if (Double.isInfinite(value)) {
            dlog.error(pos, DiagnosticErrorCode.OUT_OF_RANGE, numericLiteral, "float");
            return false;
        }
        if (value != 0.0) {
            return true;
        }

        List<Character> exponentIndicator = List.of('e', 'E', 'p', 'P');
        for (int i = 0; i < numericLiteral.length(); i++) {
            char character = numericLiteral.charAt(i);
            if (exponentIndicator.contains(character)) {
                break;
            }
            if (numericLiteral.charAt(i) >= '1' && numericLiteral.charAt(i) <= '9') {
                dlog.error(pos, DiagnosticErrorCode.OUT_OF_RANGE, numericLiteral, "float");
                return false;
            }

        }
        return true;
    }

    boolean isValidDecimalNumber(Location pos, String decimalLiteral) {
        BigDecimal bd;
        try {
            bd = new BigDecimal(decimalLiteral, MathContext.DECIMAL128);
        } catch (NumberFormatException e) {
            // If there is an error, that means there is a parsing error.
            if (dlog.errorCount() == 0) {
                dlog.error(pos, DiagnosticErrorCode.OUT_OF_RANGE, decimalLiteral, symTable.decimalType);
            }
            return false;
        }
        if (bd.compareTo(DECIMAL_MAX) > 0 || bd.compareTo(DECIMAL_MIN) < 0) {
            dlog.error(pos, DiagnosticErrorCode.OUT_OF_RANGE, decimalLiteral, symTable.decimalType);
            return false;
        }
        return true;
    }

    boolean isByteLiteralValue(Long longObject) {

        return (longObject >= BBYTE_MIN_VALUE && longObject <= BBYTE_MAX_VALUE);
    }

    boolean isSigned32LiteralValue(Long longObject) {

        return (longObject >= SIGNED32_MIN_VALUE && longObject <= SIGNED32_MAX_VALUE);
    }

    BigDecimal getValidDecimalNumber(Location pos, BigDecimal bd) {
        if (bd.compareTo(DECIMAL_MAX) > 0 || bd.compareTo(DECIMAL_MIN) < 0) {
            dlog.error(pos, DiagnosticErrorCode.OUT_OF_RANGE, bd.toString(), symTable.decimalType);
            return null;
        } else if (bd.abs(MathContext.DECIMAL128).compareTo(MIN_DECIMAL_MAGNITUDE) < 0 &&
                bd.abs(MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.ZERO;
        }
        return bd;
    }

    boolean isSigned16LiteralValue(Long longObject) {

        return (longObject >= SIGNED16_MIN_VALUE && longObject <= SIGNED16_MAX_VALUE);
    }

    boolean isSigned8LiteralValue(Long longObject) {

        return (longObject >= SIGNED8_MIN_VALUE && longObject <= SIGNED8_MAX_VALUE);
    }

    boolean isUnsigned32LiteralValue(Long longObject) {

        return (longObject >= 0 && longObject <= UNSIGNED32_MAX_VALUE);
    }

    boolean isUnsigned16LiteralValue(Long longObject) {

        return (longObject >= 0 && longObject <= UNSIGNED16_MAX_VALUE);
    }

    boolean isUnsigned8LiteralValue(Long longObject) {

        return (longObject >= 0 && longObject <= UNSIGNED8_MAX_VALUE);
    }

    boolean isCharLiteralValue(String literal) {

        return (literal.codePoints().count() == 1);
    }

    /**
     * Method to retrieve a type representing all the values in the value space of a finite type that are assignable to
     * the target type.
     *
     * @param finiteType finite type
     * @param targetType target type
     * @return a new finite type if at least one value in the value space of the specified finiteType is
     * assignable to targetType (the same if all are assignable), else semanticError
     */
    private Optional<BType> getFiniteTypeForAssignableValues(BType finiteType, BType targetType) {
        BFiniteType bFiniteType = (BFiniteType) finiteType;
        List<SemNamedType> newValueSpace = new ArrayList<>(bFiniteType.valueSpace.length);

        for (SemNamedType semNamedType : bFiniteType.valueSpace) {
            if (SemTypes.isSubtype(semTypeCtx, semNamedType.semType(), targetType.semType())) {
                newValueSpace.add(semNamedType);
            }
        }

        if (newValueSpace.isEmpty()) {
            return Optional.empty();
        }

        // Create a new finite type representing the assignable values.
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, finiteType.tsymbol.flags,
                Names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                finiteType.tsymbol.pkgID, null,
                finiteType.tsymbol.owner, finiteType.tsymbol.pos,
                VIRTUAL);
        BFiniteType ft = new BFiniteType(finiteTypeSymbol, newValueSpace.toArray(SemNamedType[]::new));
        finiteTypeSymbol.type = ft;
        return Optional.of(ft);
    }

    /**
     * Method to retrieve a type representing all the member types of a union type that are assignable to
     * the target type.
     *
     * @param unionType  the union type
     * @param targetType the target type
     * @param intersectionContext
     * @param visitedTypes cache to capture visited types
     * @return           a single type or a new union type if at least one member type of the union type is
     *                      assignable to targetType, else semanticError
     */
    BType getTypeForUnionTypeMembersAssignableToType(BUnionType unionType, BType targetType, SymbolEnv env,
                                                     IntersectionContext intersectionContext,
                                                     LinkedHashSet<BType> visitedTypes) {
        List<BType> intersection = new LinkedList<>();
        if (!visitedTypes.add(unionType)) {
            return unionType;
        }

        unionType.getMemberTypes().forEach(memType -> {
            BType memberIntersectionType = getTypeIntersection(intersectionContext, memType, targetType,
                        env, visitedTypes);
            if (memberIntersectionType != symTable.semanticError) {
                intersection.add(memberIntersectionType);
            }
        });

        if (intersection.isEmpty()) {
            return symTable.semanticError;
        }

        if (intersection.size() == 1) {
            return intersection.get(0);
        } else {
            return BUnionType.create(typeEnv(), null, new LinkedHashSet<>(intersection));
        }
    }

    boolean validEqualityIntersectionExists(BType lhsType, BType rhsType) {
        SemType intersect = Core.intersect(lhsType.semType(), rhsType.semType());
        if (Core.isEmpty(semTypeCtx, intersect)) {
            return false;
        }

        return isAnydata(intersect);
    }

    /**
     * Checks where a type is subtype of either string or xml.
     *
     * @param type type to be checked
     * @return a boolean
     */
    boolean validStringOrXmlTypeExists(BType type) {
        return isStringSubtype(type) || isXmlSubType(type);
    }

    /**
     * Checks whether a type is a subtype of xml.
     *
     * @param type type to be checked
     * @return a boolean
     */
    boolean isXmlSubType(BType type) {
        return SemTypeHelper.isSubtypeSimple(type, PredefinedType.XML);
    }

    /**
     * Checks whether a type is a subtype of string.
     *
     * @param type type to be checked
     * @return a boolean
     */
    boolean isStringSubtype(BType type) {
        return SemTypeHelper.isSubtypeSimple(type, PredefinedType.STRING);
    }

    /**
     * Checks whether a type is a subtype of one of int?, float? or decimal?.
     *
     * @param type type to be checked
     * @return a boolean
     */
    boolean validNumericTypeExists(BType type) {
        SemType tButNil = Core.diff(type.semType(), PredefinedType.NIL); // nil lift
        BasicTypeBitSet basicTypeBitSet = Core.widenToBasicTypes(tButNil);
        return basicTypeBitSet.equals(PredefinedType.INT) ||
                basicTypeBitSet.equals(PredefinedType.FLOAT) ||
                basicTypeBitSet.equals(PredefinedType.DECIMAL);
    }

    boolean validIntegerTypeExists(BType bType) {
        SemType s = bType.semType();
        s = Core.diff(s, PredefinedType.NIL); // nil lift
        return SemTypes.isSubtypeSimpleNotNever(s, PredefinedType.INT);
    }

    public boolean isStringSubType(BType type) {
        return SemTypeHelper.isSubtypeSimpleNotNever(type, PredefinedType.STRING);
    }

    /**
     * Retrieves member types of the specified type, expanding maps/arrays of/constrained by unions types to individual
     * maps/arrays.
     *
     * e.g., (string|int)[] would cause three entries as string[], int[], (string|int)[]
     *
     * @param bType the type for which member types needs to be identified
     * @return  a set containing all the retrieved member types
     */
    public Set<BType> expandAndGetMemberTypesRecursive(BType bType) {
        HashSet<BType> visited = new HashSet<>();
        return expandAndGetMemberTypesRecursiveHelper(bType, visited);
    }

    private Set<BType> expandAndGetMemberTypesRecursiveHelper(BType bType,
                                                              HashSet<BType> visited) {
        BType referredType = getImpliedType(bType);
        Set<BType> memberTypes = new LinkedHashSet<>();
        switch (referredType.tag) {
            case TypeTags.BYTE:
            case TypeTags.INT:
                memberTypes.add(symTable.intType);
                memberTypes.add(symTable.byteType);
                break;
            case TypeTags.FINITE:
                Set<BType> broadTypes = SemTypeHelper.broadTypes((BFiniteType) referredType, symTable);
                memberTypes.addAll(broadTypes);
                break;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) referredType;
                if (!visited.add(unionType)) {
                    return memberTypes;
                }
                unionType.getMemberTypes().forEach(member ->
                    memberTypes.addAll(expandAndGetMemberTypesRecursiveHelper(member, visited))
                );
                break;
            case TypeTags.ARRAY:
                BType arrayElementType = ((BArrayType) referredType).getElementType();

                // add an unsealed array to allow comparison between closed and open arrays
                // TODO: 10/16/18 improve this, since it will allow comparison between sealed arrays of different sizes
                if (((BArrayType) referredType).getSize() != -1) {
                    memberTypes.add(new BArrayType(typeEnv(), arrayElementType));
                }

                if (getImpliedType(arrayElementType).tag == TypeTags.UNION) {
                    Set<BType> elementUnionTypes = expandAndGetMemberTypesRecursiveHelper(arrayElementType, visited);
                    elementUnionTypes.forEach(
                            elementUnionType -> memberTypes.add(new BArrayType(typeEnv(), elementUnionType)));
                }
                memberTypes.add(bType);
                break;
            case TypeTags.MAP:
                BType mapConstraintType = ((BMapType) referredType).getConstraint();
                if (getImpliedType(mapConstraintType).tag == TypeTags.UNION) {
                    Set<BType> constraintUnionTypes =
                            expandAndGetMemberTypesRecursiveHelper(mapConstraintType, visited);
                    constraintUnionTypes.forEach(constraintUnionType -> memberTypes.add(
                            new BMapType(symTable.typeEnv(), TypeTags.MAP, constraintUnionType,
                                    symTable.mapType.tsymbol)));
                }
                memberTypes.add(bType);
                break;
            default:
                memberTypes.add(bType);
        }
        return memberTypes;
    }

    public BType getRemainingMatchExprType(BType originalType, BType typeToRemove, SymbolEnv env) {
        originalType = getImpliedType(originalType);
        return switch (originalType.tag) {
            case TypeTags.UNION -> getRemainingType((BUnionType) originalType, getAllTypes(typeToRemove, true));
            case TypeTags.FINITE -> getRemainingType((BFiniteType) originalType, getAllTypes(typeToRemove, true));
            case TypeTags.TUPLE -> getRemainingType((BTupleType) originalType, typeToRemove, env);
            default -> originalType;
        };
    }

    private BType getRemainingType(BTupleType originalType, BType typeToRemove, SymbolEnv env) {
        typeToRemove = getImpliedType(typeToRemove);
        return switch (typeToRemove.tag) {
            case TypeTags.TUPLE -> getRemainingType(originalType, (BTupleType) typeToRemove, env);
            case TypeTags.ARRAY -> getRemainingType(originalType, (BArrayType) typeToRemove, env);
            default -> originalType;
        };
    }

    private BType getRemainingType(BTupleType originalType, BTupleType typeToRemove, SymbolEnv env) {
        if (originalType.restType != null) {
            return originalType;
        }

        List<BType> originalTupleTypes = new ArrayList<>(originalType.getTupleTypes());
        List<BType> typesToRemove = new ArrayList<>(typeToRemove.getTupleTypes());
        if (originalTupleTypes.size() < typesToRemove.size()) {
            return originalType;
        }
        List<BTupleMember> tupleTypes = new ArrayList<>();
        for (int i = 0; i < originalTupleTypes.size(); i++) {
            BType type = getRemainingMatchExprType(originalTupleTypes.get(i), typesToRemove.get(i), env);
            BVarSymbol varSymbol = new BVarSymbol(type.getFlags(), null, null, type, null, null, null);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        if (typeToRemove.restType == null) {
            return new BTupleType(typeEnv(), tupleTypes);
        }
        if (originalTupleTypes.size() == typesToRemove.size()) {
            return originalType;
        }
        for (int i = typesToRemove.size(); i < originalTupleTypes.size(); i++) {
            BType type = getRemainingMatchExprType(originalTupleTypes.get(i), typeToRemove.restType, env);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        return new BTupleType(typeEnv(), tupleTypes);
    }

    private BType getRemainingType(BTupleType originalType, BArrayType typeToRemove, SymbolEnv env) {
        BType eType = typeToRemove.eType;
        List<BTupleMember> tupleTypes = new ArrayList<>();
        for (BType tupleMemberType : originalType.getTupleTypes()) {
            BType type = getRemainingMatchExprType(tupleMemberType, eType, env);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        BTupleType remainingType = new BTupleType(typeEnv(), tupleTypes);
        if (originalType.restType != null) {
            remainingType.restType = getRemainingMatchExprType(originalType.restType, eType, env);
        }
        return remainingType;
    }

    public BType getRemainingType(BType originalType, BType typeToRemove, SymbolEnv env) {
        BType remainingType = originalType;

        if (originalType.tag == TypeTags.INTERSECTION) {
            originalType = ((BIntersectionType) originalType).effectiveType;
        }

        boolean unionOriginalType = false;

        switch (originalType.tag) {
            case TypeTags.UNION:
                unionOriginalType = true;
                remainingType = getRemainingType((BUnionType) originalType, getAllTypes(typeToRemove, true));

                BType typeRemovedFromOriginalUnionType = getImpliedType(getRemainingType((BUnionType) originalType,
                                                                                          getAllTypes(remainingType,
                                                                                                      true)));
                if (typeRemovedFromOriginalUnionType == symTable.nullSet ||
                        isSubTypeOfReadOnly(typeRemovedFromOriginalUnionType) ||
                        isSubTypeOfReadOnly(remainingType) ||
                        narrowsToUnionOfImmutableTypesOrDistinctBasicTypes(remainingType, typeToRemove, env)) {
                    return remainingType;
                }

                break;
            case TypeTags.FINITE:
                return getRemainingType((BFiniteType) originalType, getAllTypes(typeToRemove, true));
            case TypeTags.READONLY:
                remainingType = getRemainingType((BReadonlyType) originalType, typeToRemove);
                break;
            case TypeTags.TYPEREFDESC:
                BType refType = getImpliedType(originalType);
                if (refType.tag != TypeTags.UNION && refType.tag != TypeTags.FINITE) {
                    return originalType;
                }
                return getRemainingType(refType, typeToRemove, env);
        }

        if (Symbols.isFlagOn(getImpliedType(originalType).getFlags(), Flags.READONLY)) {
            return remainingType;
        }

        BType referredTypeToRemove = getImpliedType(typeToRemove);
        if (isClosedRecordTypes(referredTypeToRemove) && removesDistinctRecords(typeToRemove, remainingType)) {
            return remainingType;
        }

        if (removesDistinctBasicTypes(typeToRemove, remainingType)) {
            return remainingType;
        }

        if (unionOriginalType && referredTypeToRemove.tag == UNION) {
            BType typeToRemoveFrom = originalType;
            for (BType memberTypeToRemove : ((BUnionType) referredTypeToRemove).getMemberTypes()) {
                remainingType =  getRemainingType(typeToRemoveFrom, memberTypeToRemove, env);
                typeToRemoveFrom = remainingType;
            }

            return remainingType;
        }

        return originalType;
    }

    public boolean isSubTypeOfReadOnly(SemType t) {
        return isSubtype(t, PredefinedType.VAL_READONLY);
    }

    public boolean isSubTypeOfReadOnly(BType type) {
        return isSubTypeOfReadOnly(type.semType());
    }

    private boolean isClosedRecordTypes(BType type) {
        type = getImpliedType(type);
        return switch (type.tag) {
            case RECORD -> {
                BRecordType recordType = (BRecordType) type;
                yield recordType.sealed || recordType.restFieldType == symTable.neverType;
            }
            case UNION -> {
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (!isClosedRecordTypes(getImpliedType(memberType))) {
                        yield false;
                    }
                }
                yield true;
            }
            default -> false;
        };
    }

    private boolean removesDistinctRecords(BType typeToRemove, BType remainingType) {
        List<Set<String>> fieldsInRemainingTypes = new ArrayList<>();

        remainingType = getImpliedType(remainingType);
        switch (remainingType.tag) {
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.ANYDATA:
            case TypeTags.ANY:
                return false;
            case RECORD:
                BRecordType recordType = (BRecordType) remainingType;
                if (!recordType.sealed && recordType.restFieldType != symTable.neverType) {
                    return false;
                }
                fieldsInRemainingTypes.add(recordType.fields.keySet());
                break;
            case UNION:
                for (BType memberType : ((BUnionType) remainingType).getMemberTypes()) {
                    BType referredMemberType = getImpliedType(memberType);
                    int tag = referredMemberType.tag;
                    if (tag == RECORD) {
                        BRecordType memberRecordType = (BRecordType) referredMemberType;
                        if (!memberRecordType.sealed && memberRecordType.restFieldType != symTable.neverType) {
                            return false;
                        }

                        fieldsInRemainingTypes.add(memberRecordType.fields.keySet());
                        continue;
                    }

                    if (tag == TypeTags.MAP || tag == TypeTags.JSON || tag == TypeTags.ANYDATA || tag == TypeTags.ANY) {
                        return false;
                    }
                }
        }

        List<Set<String>> fieldsInRemovingTypes = new ArrayList<>();
        typeToRemove = getImpliedType(typeToRemove);
        switch (typeToRemove.tag) {
            case RECORD:
                fieldsInRemovingTypes.add(((BRecordType) typeToRemove).fields.keySet());
                break;
            case UNION:
                for (BType memberType : ((BUnionType) typeToRemove).getMemberTypes()) {
                    BType referredType = getImpliedType(memberType);

                    if (referredType.tag != RECORD) {
                        continue;
                    }

                    fieldsInRemovingTypes.add(((BRecordType) referredType).fields.keySet());
                }
        }

        for (Set<String> fieldsInRemovingType : fieldsInRemovingTypes) {
            if (fieldsInRemainingTypes.contains(fieldsInRemovingType)) {
                return false;
            }
        }

        return true;
    }

    private boolean removesDistinctBasicTypes(BType typeToRemove, BType remainingType) {
        Set<BasicTypes> removedBasicTypes = new HashSet<>();
        Set<BasicTypes> remainingBasicTypes = new HashSet<>();

        populateBasicTypes(typeToRemove, removedBasicTypes);
        populateBasicTypes(remainingType, remainingBasicTypes);

        if (remainingBasicTypes.contains(BasicTypes.ANY)) {
            for (BasicTypes removedBasicType : removedBasicTypes) {
                if (removedBasicType != BasicTypes.ERROR) {
                    return false;
                }
            }
        }

        for (BasicTypes remainingBasicType : remainingBasicTypes) {
            if (removedBasicTypes.contains(remainingBasicType)) {
                return false;
            }
        }
        return true;
    }

    private boolean narrowsToUnionOfImmutableTypesOrDistinctBasicTypes(BType remainingType, BType typeToRemove,
                                                                       SymbolEnv env) {
        BType referredRemainingType = getImpliedType(remainingType);
        if (referredRemainingType.tag != UNION) {
            return false;
        }

        LinkedHashSet<BType> mutableRemainingTypes =
                filterMutableMembers(((BUnionType) referredRemainingType).getMemberTypes(), env);
        remainingType = mutableRemainingTypes.size() == 1 ? mutableRemainingTypes.iterator().next() :
                BUnionType.create(typeEnv(), null, mutableRemainingTypes);

        BType referredTypeToRemove = getImpliedType(typeToRemove);

        if (referredTypeToRemove.tag == UNION) {
            LinkedHashSet<BType> mutableTypesToRemove =
                    filterMutableMembers(((BUnionType) referredTypeToRemove).getMemberTypes(), env);
            typeToRemove = mutableTypesToRemove.size() == 1 ? mutableTypesToRemove.iterator().next() :
                    BUnionType.create(typeEnv(), null, mutableTypesToRemove);
        } else {
            typeToRemove = referredTypeToRemove;
        }

        return removesDistinctBasicTypes(typeToRemove, remainingType);
    }

    private LinkedHashSet<BType> filterMutableMembers(LinkedHashSet<BType> types, SymbolEnv env) {
        LinkedHashSet<BType> remainingMemberTypes = new LinkedHashSet<>();

        for (BType type : types) {
            BType referredType = getImpliedType(type);
            if (!isSubTypeOfReadOnly(referredType)) {
                remainingMemberTypes.add(referredType);
            }
        }

        return remainingMemberTypes;
    }

    // TODO: now only works for error. Probably we need to properly define readonly types here.
    private BType getRemainingType(BReadonlyType originalType, BType removeType) {
        if (getImpliedType(removeType).tag == TypeTags.ERROR) {
            return symTable.anyAndReadonly;
        }

        return  originalType;
    }

    public boolean intersectionExists(SemType t1, SemType t2) {
        return !Core.isEmpty(semTypeCtx, Core.intersect(t1, t2));
    }

    public BType getTypeIntersection(IntersectionContext intersectionContext, BType lhsType, BType rhsType,
                                     SymbolEnv env) {
        return getTypeIntersection(intersectionContext, lhsType, rhsType, env, new LinkedHashSet<>());
    }

    private BType getTypeIntersection(IntersectionContext intersectionContext, BType lhsType, BType rhsType,
                                     SymbolEnv env,
                                     LinkedHashSet<BType> visitedTypes) {
        List<BType> rhsTypeComponents = getAllTypes(rhsType, true);
        LinkedHashSet<BType> intersection = new LinkedHashSet<>(rhsTypeComponents.size());
        for (BType rhsComponent : rhsTypeComponents) {
            BType it = getIntersection(intersectionContext, lhsType, env, rhsComponent,
                    new LinkedHashSet<>(visitedTypes));
            if (it != null) {
                intersection.add(it);
            }
        }

        if (intersection.isEmpty()) {
            if (getImpliedType(lhsType).tag == TypeTags.NULL_SET) {
                return lhsType;
            }
            return symTable.semanticError;
        }

        if (intersection.size() == 1) {
            return intersection.toArray(new BType[0])[0];
        } else {
            return BUnionType.create(typeEnv(), null, intersection);
        }
    }

    private BType getIntersection(IntersectionContext intersectionContext, BType lhsType, SymbolEnv env, BType type,
                                  LinkedHashSet<BType> visitedTypes) {

        BType referredType = getImpliedType(type);
        BType referredLhsType = getReferredType(lhsType);

        if (intersectionContext.preferNonGenerativeIntersection) {
            if (isAssignable(referredType, referredLhsType)) {
                return type;
            } else if (isAssignable(referredLhsType, referredType)) {
                return lhsType;
            }
        }

        // TODO: intersections with readonly types are not handled properly. Here, the logic works as follows.
        // Say we have an intersection called A & readonly and we have another type called B. As per the current
        // implementation, we cannot easily find the intersection between (A & readonly) and B. Instead, what we
        // do here is, first find the intersection between A and B then re-create the immutable type out of it.

        if (Symbols.isFlagOn(referredLhsType.getFlags(), Flags.READONLY) && referredLhsType.tag == TypeTags.INTERSECTION
                && getImpliedType(((BIntersectionType) referredLhsType).effectiveType).tag == TypeTags.UNION) {
            BIntersectionType intersectionType = (BIntersectionType) referredLhsType;
            BType finalType = type;
            List<BType> types = intersectionType.getConstituentTypes().stream()
                    .filter(t -> getImpliedType(t).tag != TypeTags.READONLY)
                    .map(t -> getIntersection(intersectionContext, t, env, finalType, visitedTypes))
                    .filter(Objects::nonNull)
                    .toList();
            if (types.size() == 1) {
                BType bType = types.get(0);

                if (isInherentlyImmutableType(bType) || Symbols.isFlagOn(bType.getFlags(), Flags.READONLY)) {
                    return bType;
                }

                if (!isSelectivelyImmutableType(bType, new HashSet<>(), env.enclPkg.packageID)) {
                    return symTable.semanticError;
                }

                return ImmutableTypeCloner.getEffectiveImmutableType(intersectionContext.pos, this, bType,
                                                                     env, symTable, anonymousModelHelper, names);
            }
        }

        referredLhsType = getImpliedType(lhsType);
        if (referredType.tag == TypeTags.ERROR && referredLhsType.tag == TypeTags.ERROR) {
            BType intersectionType = getIntersectionForErrorTypes(intersectionContext, referredLhsType, referredType,
                    env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.RECORD && referredLhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) referredLhsType,
                                                              (BRecordType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.MAP && referredLhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) referredLhsType,
                                                              getEquivalentRecordType((BMapType) referredType), env,
                                                                visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.RECORD && referredLhsType.tag == TypeTags.MAP) {
            BType intersectionType = createRecordIntersection(intersectionContext,
                                                              getEquivalentRecordType((BMapType) referredLhsType),
                                                              (BRecordType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (!intersectionContext.preferNonGenerativeIntersection &&
                isAssignable(referredType, referredLhsType)) {
            return type;
        } else if (!intersectionContext.preferNonGenerativeIntersection &&
                isAssignable(referredLhsType, referredType)) {
            return lhsType;
        } else if (referredLhsType.tag == TypeTags.FINITE) {
            Optional<BType> intersectionType = getFiniteTypeForAssignableValues(referredLhsType, type);
            if (intersectionType.isPresent()) {
                return intersectionType.get();
            }
        } else if (referredType.tag == TypeTags.FINITE) {
            Optional<BType> intersectionType = getFiniteTypeForAssignableValues(referredType, lhsType);
            if (intersectionType.isPresent()) {
                return intersectionType.get();
            }
        } else if (referredLhsType.tag == TypeTags.UNION) {
            BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) referredLhsType, type, env,
                    intersectionContext, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.UNION) {
            BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) referredType, lhsType, env,
                    intersectionContext, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.MAP && referredLhsType.tag == TypeTags.MAP) {
            BType intersectionConstraintTypeType = getIntersection(intersectionContext,
                    ((BMapType) referredLhsType).constraint, env, ((BMapType) referredType).constraint, visitedTypes);
            if (intersectionConstraintTypeType == null || intersectionConstraintTypeType == symTable.semanticError) {
                return null;
            }
            return new BMapType(symTable.typeEnv(), TypeTags.MAP, intersectionConstraintTypeType, null);
        } else if (referredType.tag == TypeTags.ARRAY && referredLhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    (BArrayType) referredType, (BTupleType) referredLhsType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.TUPLE && referredLhsType.tag == TypeTags.ARRAY) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    (BArrayType) referredLhsType, (BTupleType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.TUPLE && referredLhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createTupleAndTupleIntersection(intersectionContext,
                    (BTupleType) referredLhsType, (BTupleType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(referredType) && referredLhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) referredLhsType,
                    getEquivalentRecordType(getMapTypeForAnydataOrJson(referredType, env)), env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.RECORD && isAnydataOrJson(referredLhsType)) {
            BType intersectionType = createRecordIntersection(intersectionContext,
                    getEquivalentRecordType(getMapTypeForAnydataOrJson(referredLhsType, env)),
                    (BRecordType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(referredType) && referredLhsType.tag == TypeTags.MAP) {
            return getIntersection(intersectionContext, lhsType, env, getMapTypeForAnydataOrJson(referredType, env),
                    visitedTypes);
        } else if (referredType.tag == TypeTags.MAP && isAnydataOrJson(referredLhsType)) {
            return getIntersection(intersectionContext, getMapTypeForAnydataOrJson(referredLhsType, env), env,
                    referredType, visitedTypes);
        } else if (isAnydataOrJson(referredType) && referredLhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    getArrayTypeForAnydataOrJson(referredType, env), (BTupleType) referredLhsType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (referredType.tag == TypeTags.TUPLE && isAnydataOrJson(referredLhsType)) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    getArrayTypeForAnydataOrJson(referredLhsType, env), (BTupleType) referredType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(referredType) && referredLhsType.tag == TypeTags.ARRAY) {
            BType elementIntersection = getIntersection(intersectionContext, ((BArrayType) referredLhsType).eType, env,
                                                        type, visitedTypes);
            if (elementIntersection == null) {
                return null;
            }
            return new BArrayType(typeEnv(), elementIntersection);
        } else if (referredType.tag == TypeTags.ARRAY && isAnydataOrJson(referredLhsType)) {
            BType elementIntersection = getIntersection(intersectionContext, lhsType, env,
                    ((BArrayType) referredType).eType, visitedTypes);
            if (elementIntersection == null) {
                return null;
            }
            return new BArrayType(typeEnv(), elementIntersection);
        } else if (referredType.tag == TypeTags.NULL_SET) {
            return type;
        }
        return null;
    }

    private boolean isAnydataOrJson(BType type) {
        return switch (getImpliedType(type).tag) {
            case TypeTags.ANYDATA, TypeTags.JSON -> true;
            default -> false;
        };
    }

    private BMapType getMapTypeForAnydataOrJson(BType type, SymbolEnv env) {
        BMapType mapType = getImpliedType(type).tag == TypeTags.ANYDATA ?
                symTable.mapAnydataType : symTable.mapJsonType;

        if (isImmutable(type)) {
            return (BMapType) ImmutableTypeCloner.getEffectiveImmutableType(null, this, mapType, env, symTable,
                    anonymousModelHelper, names);
        }
        return mapType;
    }

    private BArrayType getArrayTypeForAnydataOrJson(BType type, SymbolEnv env) {
        BArrayType arrayType = getImpliedType(type).tag == TypeTags.ANYDATA ?
                symTable.arrayAnydataType : symTable.arrayJsonType;

        if (isImmutable(type)) {
            return (BArrayType) ImmutableTypeCloner.getEffectiveImmutableType(null, this, arrayType, env, symTable,
                    anonymousModelHelper, names);
        }
        return arrayType;
    }

    private BType createArrayAndTupleIntersection(IntersectionContext intersectionContext,
                                                  BArrayType arrayType, BTupleType tupleType, SymbolEnv env,
                                                  LinkedHashSet<BType> visitedTypes) {
        if (!visitedTypes.add(tupleType)) {
            return tupleType;
        }
        List<BType> tupleTypes = tupleType.getTupleTypes();
        if (arrayType.state == BArrayState.CLOSED && tupleTypes.size() != arrayType.getSize()) {
            if (tupleTypes.size() > arrayType.getSize()) {
                return symTable.semanticError;
            }

            if (tupleType.restType == null) {
                return symTable.semanticError;
            }
        }

        List<BTupleMember> tupleMemberTypes = new ArrayList<>(tupleTypes.size());
        BType eType = arrayType.eType;
        for (BType memberType : tupleTypes) {
            BType intersectionType = getTypeIntersection(intersectionContext, memberType, eType, env,
                    visitedTypes);
            if (intersectionType == symTable.semanticError) {
                return symTable.semanticError;
            }
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(intersectionType);
            tupleMemberTypes.add(new BTupleMember(intersectionType, varSymbol));
        }

        if (tupleType.restType == null) {
            return new BTupleType(typeEnv(), tupleMemberTypes);
        }

        BType restIntersectionType = getTypeIntersection(intersectionContext, tupleType.restType, eType, env,
                visitedTypes);
        if (restIntersectionType == symTable.semanticError) {
            return new BTupleType(typeEnv(), tupleMemberTypes);
        }
        return new BTupleType(typeEnv(), null, tupleMemberTypes, restIntersectionType, 0);
    }

    private BType createTupleAndTupleIntersection(IntersectionContext intersectionContext,
                                                  BTupleType lhsTupleType, BTupleType tupleType, SymbolEnv env,
                                                  LinkedHashSet<BType> visitedTypes) {
        if (lhsTupleType.restType == null && tupleType.restType != null) {
            return symTable.semanticError;
        }

        if (lhsTupleType.restType == null &&
                lhsTupleType.getMembers().size() != tupleType.getMembers().size()) {
            return symTable.semanticError;
        }

        List<BType> lhsTupleTypes = lhsTupleType.getTupleTypes();
        List<BType> tupleTypes = tupleType.getTupleTypes();

        if (lhsTupleTypes.size() > tupleTypes.size()) {
            return symTable.semanticError;
        }

        List<BTupleMember> tupleMemberTypes = new ArrayList<>(tupleTypes.size());
        for (int i = 0; i < tupleTypes.size(); i++) {
            BType lhsType = (lhsTupleTypes.size() > i) ? lhsTupleTypes.get(i) : lhsTupleType.restType;
            BType intersectionType = getTypeIntersection(intersectionContext, tupleTypes.get(i), lhsType, env,
                    visitedTypes);
            if (intersectionType == symTable.semanticError) {
                return symTable.semanticError;
            }
            BVarSymbol varSymbol = new BVarSymbol(intersectionType.getFlags(), null, null, intersectionType,
                    null, null, null);
            tupleMemberTypes.add(new BTupleMember(intersectionType, varSymbol));
        }

        if (lhsTupleType.restType != null && tupleType.restType != null) {
            BType restIntersectionType = getTypeIntersection(intersectionContext, tupleType.restType,
                    lhsTupleType.restType, env, visitedTypes);
            if (restIntersectionType == symTable.semanticError) {
                return new BTupleType(typeEnv(), tupleMemberTypes);
            }
            return new BTupleType(typeEnv(), null, tupleMemberTypes, restIntersectionType, 0);
        }

        return new BTupleType(typeEnv(), tupleMemberTypes);
    }

    private BType getIntersectionForErrorTypes(IntersectionContext intersectionContext,
                                               BType lhsType, BType rhsType, SymbolEnv env,
                                               LinkedHashSet<BType> visitedTypes) {

        BType detailIntersectionType = getTypeIntersection(intersectionContext,
                ((BErrorType) lhsType).detailType, ((BErrorType) rhsType).detailType, env, visitedTypes);
        if (detailIntersectionType == symTable.semanticError) {
            return symTable.semanticError;
        }

        BErrorType intersectionErrorType = createErrorType(lhsType, rhsType, detailIntersectionType, env);

        if (intersectionContext.createTypeDefs) {
            BTypeSymbol errorTSymbol = intersectionErrorType.tsymbol;
            BLangErrorType bLangErrorType = TypeDefBuilderHelper.createBLangErrorType(symTable.builtinPos,
                    intersectionErrorType, env, anonymousModelHelper);
            BLangTypeDefinition errorTypeDefinition = TypeDefBuilderHelper.addTypeDefinition(
                    intersectionErrorType, errorTSymbol, bLangErrorType, env);
            errorTypeDefinition.pos = symTable.builtinPos;
        }

        return intersectionErrorType;
    }

    private BType createRecordIntersection(IntersectionContext intersectionContext,
                                           BRecordType recordTypeOne, BRecordType recordTypeTwo, SymbolEnv env,
                                           LinkedHashSet<BType> visitedTypes) {
        if (!visitedTypes.add(recordTypeOne)) {
            return recordTypeOne;
        }
        LinkedHashMap<String, BField> recordOneFields = recordTypeOne.fields;
        LinkedHashMap<String, BField> recordTwoFields = recordTypeTwo.fields;

        Set<String> recordOneKeys = recordOneFields.keySet();
        Set<String> recordTwoKeys = recordTwoFields.keySet();

        boolean isRecordOneClosed = recordTypeOne.sealed;
        boolean isRecordTwoClosed = recordTypeTwo.sealed;

        BType effectiveRecordOneRestFieldType = getConstraint(recordTypeOne);
        BType effectiveRecordTwoRestFieldType = getConstraint(recordTypeTwo);

        BRecordType newType = createAnonymousRecord(env);
        BTypeSymbol newTypeSymbol = newType.tsymbol;
        Set<String> addedKeys = new HashSet<>();

        LinkedHashMap<String, BField> newTypeFields = newType.fields;

        if (!populateFields(intersectionContext.switchLeft(), recordTypeOne, env, recordOneFields, recordTwoFields,
                            recordOneKeys, recordTwoKeys, isRecordTwoClosed, effectiveRecordTwoRestFieldType,
                            newTypeSymbol, addedKeys, newTypeFields, visitedTypes)) {
            return symTable.semanticError;
        }

        if (!populateFields(intersectionContext.switchRight(), recordTypeTwo, env, recordTwoFields, recordOneFields,
                            recordTwoKeys, recordOneKeys, isRecordOneClosed, effectiveRecordOneRestFieldType,
                            newTypeSymbol, addedKeys, newTypeFields, visitedTypes)) {
            return symTable.semanticError;
        }

        BType restFieldType = getTypeIntersection(intersectionContext, effectiveRecordOneRestFieldType,
                effectiveRecordTwoRestFieldType, env, visitedTypes);
        if (setRestType(newType, restFieldType) == symTable.semanticError) {
            return symTable.semanticError;
        }

        if ((newType.sealed || newType.restFieldType == symTable.neverType) &&
                (newTypeFields.isEmpty() || allReadOnlyFields(newTypeFields))) {
            newType.addFlags(Flags.READONLY);
            newTypeSymbol.flags |= Flags.READONLY;
        }

        if (intersectionContext.createTypeDefs) {
            BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(
                    newType, env.enclPkg.packageID, symTable, symTable.builtinPos);
            BLangTypeDefinition recordTypeDef = TypeDefBuilderHelper.addTypeDefinition(
                    newType, newType.tsymbol, recordTypeNode, env);
            env.enclPkg.symbol.scope.define(newType.tsymbol.name, newType.tsymbol);
            recordTypeDef.pos = symTable.builtinPos;
        }

        return newType;
    }

    private boolean populateFields(IntersectionContext intersectionContext, BRecordType lhsRecord, SymbolEnv env,
                                   LinkedHashMap<String, BField> lhsRecordFields,
                                   LinkedHashMap<String, BField> rhsRecordFields,
                                   Set<String> lhsRecordKeys, Set<String> rhsRecordKeys,
                                   boolean isRhsRecordClosed, BType effectiveRhsRecordRestFieldType,
                                   BTypeSymbol newTypeSymbol, Set<String> addedKeys,
                                   LinkedHashMap<String, BField> newTypeFields,
                                   LinkedHashSet<BType> visitedTypes) {
        for (String key : lhsRecordKeys) {
            BField lhsRecordField = lhsRecordFields.get(key);

            if (!validateRecordFieldDefaultValueForIntersection(intersectionContext, lhsRecordField, lhsRecord)) {
                return false;
            }

            if (!addedKeys.add(key)) {
                continue;
            }
            
            BType intersectionFieldType;

            long intersectionFlags = lhsRecordField.symbol.flags;

            BType recordOneFieldType = lhsRecordField.type;
            if (!rhsRecordKeys.contains(key)) {
                if (isRhsRecordClosed) {
                    if (!Symbols.isFlagOn(lhsRecordField.symbol.flags, Flags.OPTIONAL)) {
                        return false;
                    }
                    continue;
                }

                if (isNeverTypeOrStructureTypeWithARequiredNeverMember(effectiveRhsRecordRestFieldType) &&
                        !isNeverTypeOrStructureTypeWithARequiredNeverMember(recordOneFieldType)) {
                    return false;
                }

                intersectionFieldType = getIntersection(intersectionContext, recordOneFieldType, env,
                                                        effectiveRhsRecordRestFieldType, visitedTypes);

                if (intersectionFieldType == null || intersectionFieldType == symTable.semanticError) {
                    if (Symbols.isFlagOn(lhsRecordField.symbol.flags, Flags.OPTIONAL)) {
                        continue;
                    }

                    return false;
                }
            } else {
                BField rhsRecordField = rhsRecordFields.get(key);
                intersectionFieldType = getIntersection(intersectionContext, recordOneFieldType, env,
                                                        rhsRecordField.type, visitedTypes);

                long rhsFieldFlags = rhsRecordField.symbol.flags;
                if (Symbols.isFlagOn(rhsFieldFlags, Flags.READONLY)) {
                    intersectionFlags |= Flags.READONLY;
                }

                if (!Symbols.isFlagOn(rhsFieldFlags, Flags.OPTIONAL) &&
                        Symbols.isFlagOn(intersectionFlags, Flags.OPTIONAL)) {
                    intersectionFlags &= ~Flags.OPTIONAL;
                }

                if (Symbols.isFlagOn(rhsFieldFlags, Flags.REQUIRED) &&
                        !Symbols.isFlagOn(intersectionFlags, Flags.REQUIRED)) {
                    intersectionFlags |= Flags.REQUIRED;
                }
            }

            if (intersectionFieldType == null || intersectionFieldType == symTable.semanticError) {
                return false;
            }

            org.wso2.ballerinalang.compiler.util.Name name = lhsRecordField.name;
            BVarSymbol recordFieldSymbol;

            if (getImpliedType(intersectionFieldType).tag == TypeTags.INVOKABLE &&
                    intersectionFieldType.tsymbol != null) {
                recordFieldSymbol = new BInvokableSymbol(lhsRecordField.symbol.tag, intersectionFlags,
                                                         name, env.enclPkg.packageID, intersectionFieldType,
                                                         newTypeSymbol, lhsRecordField.pos, SOURCE);
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) intersectionFieldType.tsymbol;
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) recordFieldSymbol;
                invokableSymbol.params = tsymbol == null ? null : new ArrayList<>(tsymbol.params);
                invokableSymbol.restParam = tsymbol.restParam;
                invokableSymbol.retType = tsymbol.returnType;
                invokableSymbol.flags = tsymbol.flags;
            } else {
                recordFieldSymbol = new BVarSymbol(intersectionFlags, name, env.enclPkg.packageID,
                        intersectionFieldType, newTypeSymbol, lhsRecordField.symbol.pos, SOURCE);
            }

            newTypeFields.put(key, new BField(name, recordFieldSymbol.pos, recordFieldSymbol));
            newTypeSymbol.scope.define(name,  recordFieldSymbol);
        }
        return true;
    }

    private boolean allReadOnlyFields(LinkedHashMap<String, BField> fields) {
        for (BField field : fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                return false;
            }
        }
        return true;
    }

    private BType setRestType(BRecordType recordType, BType restType) {
        if (restType == symTable.semanticError) {
            recordType.restFieldType = symTable.semanticError;
            return symTable.semanticError;
        }

        if (restType == symTable.neverType) {
            recordType.sealed = true;
            recordType.restFieldType = symTable.noType;
            return symTable.noType;
        }

        recordType.restFieldType = restType;
        return restType;
    }

    private BType getConstraint(BRecordType recordType) {
        if (recordType.sealed) {
            return symTable.neverType;
        }

        return recordType.restFieldType;
    }

    private BRecordType createAnonymousRecord(SymbolEnv env) {
        EnumSet<Flag> flags = EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                                                                                env.enclPkg.packageID, null,
                                                                                env.scope.owner, null, VIRTUAL);
        recordSymbol.name = Names.fromString(
                anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID));
        BInvokableType bInvokableType = new BInvokableType(typeEnv(), List.of(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.scope = new Scope(recordSymbol);

        BRecordType recordType = new BRecordType(symTable.typeEnv(), recordSymbol);
        recordType.tsymbol = recordSymbol;
        recordSymbol.type = recordType;

        return recordType;
    }

    private BRecordType getEquivalentRecordType(BMapType mapType) {
        BRecordType equivalentRecordType = new BRecordType(symTable.typeEnv(), null);
        equivalentRecordType.sealed = false;
        equivalentRecordType.restFieldType = mapType.constraint;
        return equivalentRecordType;
    }

    private BErrorType createErrorType(BType lhsType, BType rhsType, BType detailType, SymbolEnv env) {
        BErrorType lhsErrorType = (BErrorType) lhsType;
        BErrorType rhsErrorType = (BErrorType) rhsType;

        // Anonymous (generated) types are marked as public.
        BErrorType errorType = createErrorType(detailType, lhsType.getFlags() | rhsType.getFlags() | Flags.PUBLIC, env);

        // This is to propagate same distinctId to effective type
        lhsErrorType.setDistinctId();
        rhsErrorType.setDistinctId();
        if (lhsErrorType.distinctId != -1) {
            errorType.distinctId = lhsErrorType.distinctId;
        } else if (rhsErrorType.distinctId != -1) {
            errorType.distinctId = rhsErrorType.distinctId;
        }

        errorType.typeIdSet = BTypeIdSet.getIntersection(lhsErrorType.typeIdSet, rhsErrorType.typeIdSet);
        return errorType;
    }

    public BErrorType createErrorType(BType detailType, long flags, SymbolEnv env) {
        String name = anonymousModelHelper.getNextAnonymousIntersectionErrorTypeName(env.enclPkg.packageID);
        BErrorTypeSymbol errorTypeSymbol = Symbols.createErrorSymbol(flags | Flags.ANONYMOUS, Names.fromString(name),
                                                                     env.enclPkg.symbol.pkgID, null,
                                                                     env.scope.owner, symTable.builtinPos, VIRTUAL);
        errorTypeSymbol.scope = new Scope(errorTypeSymbol);
        BErrorType errorType = new BErrorType(symTable.typeEnv(), errorTypeSymbol, detailType);
        errorType.addFlags(errorTypeSymbol.flags);
        errorTypeSymbol.type = errorType;
        errorType.typeIdSet = BTypeIdSet.emptySet();

        return errorType;
    }


    private boolean validateRecordFieldDefaultValueForIntersection(IntersectionContext diagnosticContext,
                                                                   BField field, BRecordType recordType) {
        if (field.symbol != null && field.symbol.isDefaultable && !diagnosticContext.ignoreDefaultValues) {
            diagnosticContext.logError(DiagnosticErrorCode.INTERSECTION_NOT_ALLOWED_WITH_TYPE, recordType, field.name);
            return false;
        }
        return true;
    }

    private void removeErrorFromReadonlyType(List<BType> remainingTypes) {
        Iterator<BType> remainingIterator = remainingTypes.listIterator();
        boolean addAnyAndReadOnly = false;
        while (remainingIterator.hasNext()) {
            BType remainingType = remainingIterator.next();
            if (getImpliedType(remainingType).tag != TypeTags.READONLY) {
                continue;
            }
            remainingIterator.remove();
            addAnyAndReadOnly = true;
        }
        if (addAnyAndReadOnly) {
            remainingTypes.add(symTable.anyAndReadonly);
        }
    }

    private BType getRemainingType(BUnionType originalType, List<BType> removeTypes) {
        List<BType> remainingTypes = getAllTypes(originalType, true);
        boolean hasErrorToRemove = false;
        for (BType removeType : removeTypes) {
            remainingTypes.removeIf(type -> isAssignable(type, removeType));
            if (!hasErrorToRemove && getImpliedType(removeType).tag == TypeTags.ERROR) {
                hasErrorToRemove = true;
            }
        }

        if (hasErrorToRemove) {
            removeErrorFromReadonlyType(remainingTypes);
        }

        List<BType> finiteTypesToRemove = new ArrayList<>();
        List<BType> finiteTypesToAdd = new ArrayList<>();
        for (BType remainingType : remainingTypes) {
            if (remainingType.tag == TypeTags.FINITE) {
                BFiniteType finiteType = (BFiniteType) remainingType;
                finiteTypesToRemove.add(finiteType);
                BType remainingTypeWithMatchesRemoved = getRemainingType(finiteType, removeTypes);
                if (remainingTypeWithMatchesRemoved != symTable.semanticError) {
                    finiteTypesToAdd.add(remainingTypeWithMatchesRemoved);
                }
            }
        }
        remainingTypes.removeAll(finiteTypesToRemove);
        remainingTypes.addAll(finiteTypesToAdd);

        if (remainingTypes.size() == 1) {
            return remainingTypes.get(0);
        }

        if (remainingTypes.isEmpty()) {
            return symTable.nullSet;
        }

        return BUnionType.create(typeEnv(), null, new LinkedHashSet<>(remainingTypes));
    }

    private BType getRemainingType(BFiniteType originalType, List<BType> removeTypes) {
        SemType removeSemType = PredefinedType.NEVER;
        for (BType removeType : removeTypes) {
            removeSemType = SemTypes.union(removeSemType, removeType.semType());
        }

        List<SemNamedType> newValueSpace = new ArrayList<>();
        for (SemNamedType semNamedType : originalType.valueSpace) {
            if (!SemTypes.isSubtype(semTypeCtx, semNamedType.semType(), removeSemType)) {
                newValueSpace.add(semNamedType);
            }
        }

        if (newValueSpace.isEmpty()) {
            return symTable.semanticError;
        }

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, originalType.tsymbol.flags,
                Names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                originalType.tsymbol.pkgID, null,
                originalType.tsymbol.owner, originalType.tsymbol.pos,
                VIRTUAL);
        BFiniteType ft = new BFiniteType(finiteTypeSymbol, newValueSpace.toArray(SemNamedType[]::new));
        finiteTypeSymbol.type = ft;
        return ft;
    }

    public SemType getNilLiftType(SemType t) {
        return Core.diff(t, PredefinedType.NIL);
    }

    public SemType getErrorLiftType(SemType t) {
        return Core.diff(t, PredefinedType.ERROR);
    }

    public SemType getNilAndErrorLiftType(SemType t) {
        return Core.diff(t, Core.union(PredefinedType.NIL, PredefinedType.ERROR));
    }

    public BType getSafeType(BType bType, boolean liftNil, boolean liftError) {
        BType type = getImpliedType(bType);
        // Since JSON, ANY and ANYDATA by default contain null, we need to create a new respective type which
        // is not-nullable.
        if (liftNil) {
            switch (type.tag) {
                case TypeTags.JSON:
                    return BJSONType.newNilLiftedBJSONType((BJSONType) type);
                case TypeTags.ANY:
                    return BAnyType.newNilLiftedBAnyType();
                case TypeTags.ANYDATA:
                    return BAnydataType.newNilLiftedBAnydataType((BAnydataType) type);
                case TypeTags.READONLY:
                    if (liftError) {
                        return symTable.anyAndReadonly;
                    }
                    return BReadonlyType.newNilLiftedBReadonlyType();
            }
        }

        if (type.tag != TypeTags.UNION) {
            return bType;
        }

        BUnionType unionType = (BUnionType) type;
        LinkedHashSet<BType> memTypes = new LinkedHashSet<>(unionType.getMemberTypes());
        BUnionType errorLiftedType = BUnionType.create(typeEnv(), null, memTypes);

        if (liftNil) {
            errorLiftedType.remove(symTable.nilType);
        }

        if (liftError) {
            LinkedHashSet<BType> bTypes = new LinkedHashSet<>();
            for (BType t : errorLiftedType.getMemberTypes()) {
                if (getImpliedType(t).tag != TypeTags.ERROR) {
                    bTypes.add(t);
                }
            }
            memTypes = bTypes;
            errorLiftedType = BUnionType.create(typeEnv(), null, memTypes);
        }

        if (errorLiftedType.getMemberTypes().size() == 1) {
            return errorLiftedType.getMemberTypes().toArray(new BType[0])[0];
        }

        if (errorLiftedType.getMemberTypes().isEmpty()) {
            return symTable.semanticError;
        }

        return errorLiftedType;
    }

    public List<BType> getAllTypes(BType type, boolean getReferenced) {
        if (type.tag != TypeTags.UNION) {
            if (getReferenced && type.tag == TypeTags.TYPEREFDESC) {
                return getAllTypes(((BTypeReferenceType) type).referredType, true);
            }

            if (getReferenced && type.tag == TypeTags.INTERSECTION) {
                return getAllTypes(((BIntersectionType) type).effectiveType, true);
            }
            return Lists.of(type);
        }

        List<BType> memberTypes = new LinkedList<>();
        ((BUnionType) type).getMemberTypes().forEach(memberType -> memberTypes.addAll(getAllTypes(memberType, true)));
        return memberTypes;
    }

    public boolean isAllowedConstantType(BType type) {
        type = getImpliedType(type);
        return switch (type.tag) {
            case TypeTags.BOOLEAN,
                 TypeTags.INT,
                 TypeTags.SIGNED32_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED8_INT,
                 TypeTags.UNSIGNED32_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED8_INT,
                 TypeTags.BYTE,
                 TypeTags.FLOAT,
                 TypeTags.DECIMAL,
                 TypeTags.STRING,
                 // TODO : Fix this, Issue : #21542
                 //TypeTags.CHAR_STRING:
                 TypeTags.NIL,
                 TypeTags.UNION,
                 TypeTags.ANY,
                 TypeTags.ANYDATA -> true;
            case TypeTags.MAP -> isAllowedConstantType(((BMapType) type).constraint);
            case TypeTags.RECORD -> {
                for (BField field : ((BRecordType) type).fields.values()) {
                    if (field.symbol.isDefaultable || !isAllowedConstantType(field.type)) {
                        yield false;
                    }
                }
                yield true;
            }
            case TypeTags.ARRAY -> isAllowedConstantType(((BArrayType) type).eType);
            case TypeTags.TUPLE -> {
                for (BType memberType : ((BTupleType) type).getTupleTypes()) {
                    if (!isAllowedConstantType(memberType)) {
                        yield false;
                    }
                }
                yield true;
            }
            case TypeTags.FINITE -> {
                yield isAllowedConstantType(SemTypeHelper.broadTypes(type.semType(), symTable).iterator().next());
            }
            default -> false;
        };
    }

    public boolean isValidLiteral(BLangLiteral literal, BType targetType) {
        BType literalType = literal.getBType();
        if (literalType.tag == targetType.tag) {
            return true;
        }

        return switch (targetType.tag) {
            case TypeTags.BYTE -> literalType.tag == TypeTags.INT && isByteLiteralValue((Long) literal.value);
            case TypeTags.DECIMAL -> literalType.tag == TypeTags.FLOAT || literalType.tag == TypeTags.INT;
            case TypeTags.FLOAT -> literalType.tag == TypeTags.INT;
            case TypeTags.SIGNED32_INT ->
                    literalType.tag == TypeTags.INT && isSigned32LiteralValue((Long) literal.value);
            case TypeTags.SIGNED16_INT ->
                    literalType.tag == TypeTags.INT && isSigned16LiteralValue((Long) literal.value);
            case TypeTags.SIGNED8_INT -> literalType.tag == TypeTags.INT && isSigned8LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED32_INT ->
                    literalType.tag == TypeTags.INT && isUnsigned32LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED16_INT ->
                    literalType.tag == TypeTags.INT && isUnsigned16LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED8_INT ->
                    literalType.tag == TypeTags.INT && isUnsigned8LiteralValue((Long) literal.value);
            case TypeTags.CHAR_STRING ->
                    literalType.tag == TypeTags.STRING && isCharLiteralValue((String) literal.value);
            default -> false;
        };
    }

    /**
     * Validate if the return type of the given function is a subtype of `error?`, containing `()`.
     *
     * @param function          The function of which the return type should be validated
     * @param diagnosticCode    The code to log if the return type is invalid
     */
    public void validateErrorOrNilReturn(BLangFunction function, DiagnosticCode diagnosticCode) {
        BType returnType = getImpliedType(function.returnTypeNode.getBType());

        if (returnType.tag == TypeTags.NIL ||
                (returnType.tag == TypeTags.UNION &&
                        isSubTypeOfErrorOrNilContainingNil((BUnionType) returnType))) {
            return;
        }

        dlog.error(function.returnTypeNode.pos, diagnosticCode, function.returnTypeNode.getBType().toString());
    }

    public boolean isSubTypeOfErrorOrNilContainingNil(BUnionType type) {
        if (!type.isNullable()) {
            return false;
        }

        BasicTypeBitSet nilOrError = (BasicTypeBitSet) Core.union(PredefinedType.NIL, PredefinedType.ERROR);
        return SemTypeHelper.isSubtypeSimpleNotNever(type, nilOrError);
    }

    public boolean hasFillerValue(BType type) {
        type = getImpliedType(type);
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_TEXT:
            case TypeTags.NIL:
            case TypeTags.TABLE:
            case TypeTags.ANYDATA:
            case TypeTags.MAP:
            case TypeTags.ANY:
                return true;
            case TypeTags.ARRAY:
                return checkFillerValue((BArrayType) type);
            case TypeTags.FINITE:
                return hasFiller(type.semType());
            case TypeTags.UNION:
                return checkFillerValue((BUnionType) type);
            case TypeTags.OBJECT:
                return checkFillerValue((BObjectType) type);
            case TypeTags.RECORD:
                return checkFillerValue((BRecordType) type);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                if (tupleType.isCyclic) {
                    return false;
                }
                return tupleType.getTupleTypes().stream().allMatch(eleType -> hasFillerValue(eleType));
            default:
                // check whether the type is an integer subtype which has filler value 0
                return TypeTags.isIntegerTypeTag(type.tag);
        }
    }

    private boolean checkFillerValue(BObjectType type) {
        if ((type.tsymbol.flags & Flags.CLASS) != Flags.CLASS) {
            return false;
        }

        BAttachedFunction initFunction = ((BObjectTypeSymbol) type.tsymbol).initializerFunc;
        if (initFunction == null) {
            return true;
        }
        if (initFunction.symbol.getReturnType().getKind() != TypeKind.NIL) {
            return false;
        }

        for (BVarSymbol bVarSymbol : initFunction.symbol.getParameters()) {
            if (!bVarSymbol.isDefaultable) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a SemType has a filler value.
     * <p>
     * <i>Note: this is similar to <code>computeFiller()</code> in nBallerina</i>
     * </p><p>
     * 1. if type contains nil, nil is the filler value.<br/>
     * 2. if all values belong to a single basic type B, and the filler value for B also included in the values.<br/>
     * 3. if type is a singleton, it is the filler value.
     * </p>
     *
     * @param t SemType to be checked
     * @return whether there is a filler value
     */
    private boolean hasFiller(SemType t) {
        if (Core.containsNil(t)) {
            return true;
        }

        return hasImplicitDefaultValue(t) || Core.singleShape(t).isPresent();
    }

    private boolean hasImplicitDefaultValue(SemType t) {
        BasicTypeBitSet bitSet = Core.widenToBasicTypes(t);
        Object value = null;
        if (bitSet.equals(PredefinedType.BOOLEAN)) {
            value = false;
        } else if (bitSet.equals(PredefinedType.INT)) {
            value = (long) 0;
        } else if (bitSet.equals(PredefinedType.DECIMAL)) {
            value = BigDecimal.valueOf(0);
        } else if (bitSet.equals(PredefinedType.FLOAT)) {
            value = (double) 0;
        } else if (bitSet.equals(PredefinedType.STRING)) {
            value = "";
        }

        return value != null && (t instanceof BasicTypeBitSet || Core.containsConst(t, value));
    }

    private boolean checkFillerValue(BUnionType type) {
        if (type.isNullable()) {
            return true;
        }

        if (type.isCyclic) {
            return false;
        }

        Set<BType> memberTypes = new HashSet<>();
        boolean hasFillerValue = false;

        for (BType member : getAllTypes(type, true)) {
            if (member.tag == TypeTags.FINITE) {
                Set<BType> broadTypes = SemTypeHelper.broadTypes((BFiniteType) member, symTable);
                memberTypes.addAll(broadTypes);
                if (!hasFillerValue && hasImplicitDefaultValue(member.semType())) {
                    hasFillerValue = true;
                }
            } else {
                memberTypes.add(member);
                if (!hasFillerValue && hasFillerValue(member)) {
                    hasFillerValue = true;
                }
            }
        }
        if (!hasFillerValue) {
            return false;
        }

        Iterator<BType> iterator = memberTypes.iterator();
        BType firstMember = iterator.next();
        while (iterator.hasNext()) {
            if (!isSameBasicType(firstMember, iterator.next())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameBasicType(BType source, BType target) {
        if (isSameType(source, target)) {
            return true;
        }
        int sourceTag = getImpliedType(source).tag;
        int targetTag = getImpliedType(target).tag;
        if (TypeTags.isStringTypeTag(sourceTag) && TypeTags.isStringTypeTag(targetTag)) {
            return true;
        }
        if (TypeTags.isXMLTypeTag(sourceTag) && TypeTags.isXMLTypeTag(targetTag)) {
            return true;
        }
        return isIntegerSubTypeTag(sourceTag) && isIntegerSubTypeTag(targetTag);
    }

    private boolean isIntegerSubTypeTag(int typeTag) {
        return TypeTags.isIntegerTypeTag(typeTag) || typeTag == TypeTags.BYTE;
    }

    private boolean checkFillerValue(BRecordType type) {
        for (BField field : type.fields.values()) {
            if (Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                continue;
            }
            if (Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFillerValue(BArrayType type) {
        if (type.getSize() == -1) {
            return true;
        }
        return hasFillerValue(type.eType);
    }

    /**
     * Check whether a type is an ordered type.
     *
     * @param type type to be checked
     * @return boolean whether the type is an ordered type or not
     */
    public boolean isOrderedType(BType type) {
        return isOrderedType(type.semType());
    }

    /**
     * Checks whether a SemType is an ordered type.
     * <br/>
     * <p>
     * A type is an ordered type if all values belong to one of (), int?, boolean?, decimal?, float?, string? types.
     * Additionally,
     * <ul>
     *   <li>[T...] is ordered, if T is ordered;</li>
     *   <li>[] is ordered;</li>
     *   <li>[T, rest] is ordered if T is ordered and [rest] is ordered.</li>
     * </ul>
     *
     * @param t SemType to be checked
     * @return boolean
     */
    public boolean isOrderedType(SemType t) {
        assert !Core.isNever(t);
        SemType tButNil = Core.diff(t, PredefinedType.NIL);
        BasicTypeBitSet basicTypeBitSet = Core.widenToBasicTypes(tButNil);
        if (SemTypes.isSubtypeSimple(basicTypeBitSet, PredefinedType.SIMPLE_OR_STRING)) {
            int bitCount = SemTypeHelper.bitCount(basicTypeBitSet.bitset);
            return bitCount <= 1;
        }

        if (SemTypes.isSubtypeSimple(tButNil, PredefinedType.LIST)) {
            ListMemberTypes lmTypes = Core.listAllMemberTypesInner(typeCtx(), t);
            for (SemType lmType : lmTypes.semTypes()) {
                if (!isOrderedType(lmType)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    boolean comparable(BType t1, BType t2) {
        return comparable(t1.semType(), t2.semType());
    }

    /**
     * Checks whether a SemType pair is comparable.
     * <br/>
     * <p>
     * <i>Note: this is similar to <code>comparable()</code> in nBallerina. However, nBallerina API does not have
     * "There must be an ordered type to which the static type of both operands belong" part from spec, implemented</i>
     * </p>
     *
     * @param t1 first semType
     * @param t2 second semType
     * @return boolean
     */
    boolean comparable(SemType t1, SemType t2) {
        assert !Core.isNever(t1) && !Core.isNever(t2);
        if (PredefinedType.NIL.equals(t1)) {
            return isOrderedType(t2);
        }

        if (PredefinedType.NIL.equals(t2)) {
            return isOrderedType(t1);
        }

        SemType tButNil = Core.diff(Core.union(t1, t2), PredefinedType.NIL);
        BasicTypeBitSet basicTypeBitSet = Core.widenToBasicTypes(tButNil);
        if (SemTypes.isSubtypeSimple(basicTypeBitSet, PredefinedType.SIMPLE_OR_STRING)) {
            int bitCount = SemTypeHelper.bitCount(basicTypeBitSet.bitset);
            return bitCount <= 1;
        }
        if (SemTypes.isSubtypeSimple(tButNil, PredefinedType.LIST)) {
            return comparableNillableList(typeCtx(), t1, t2);
        }
        return false;
    }

    private boolean comparableNillableList(Context cx, SemType t1, SemType t2) {
        SemTypePair semPair = SemTypePair.from(t1, t2);
        Boolean b = cx.comparableMemo.get(semPair);
        if (b != null) {
            return b;
        }

        ListMemberTypes lmTypes1 = Core.listAllMemberTypesInner(cx, t1);
        ListMemberTypes lmTypes2 = Core.listAllMemberTypesInner(cx, t2);
        CombinedRange[] combinedRanges = combineRanges(
                lmTypes1.ranges().toArray(Range[]::new),
                lmTypes2.ranges().toArray(Range[]::new)
        );
        SemType accum = PredefinedType.NIL;
        for (CombinedRange combinedRange : combinedRanges) {
            Long i1 = combinedRange.i1();
            Long i2 = combinedRange.i2();
            if (i1 == null) {
                SemType lmType = lmTypes2.semTypes().get(Math.toIntExact(i2));
                if (!comparable(accum, lmType)) {
                    return false;
                }
                accum = Core.union(accum, lmType);
                continue;
            }

            if (i2 == null) {
                SemType lmType = lmTypes1.semTypes().get(Math.toIntExact(i1));
                if (!comparable(accum, lmType)) {
                    return false;
                }
                accum = Core.union(accum, lmType);
                continue;
            }


            if (!comparable(lmTypes1.semTypes().get(Math.toIntExact(i1)),
                    lmTypes2.semTypes().get(Math.toIntExact(i2)))) {
                cx.comparableMemo.put(semPair, false);
                return false;
            }
        }
        cx.comparableMemo.put(semPair, true);
        return true;
    }

    public boolean isSubTypeOfSimpleBasicTypeOrString(BType bType) {
        return isAssignable(getImpliedType(bType),
                BUnionType.create(typeEnv(), null, symTable.nilType, symTable.booleanType, symTable.intType,
                                              symTable.floatType, symTable.decimalType, symTable.stringType));
    }

    public BType findCompatibleType(BType type) {
        type = getImpliedType(type);
        return switch (type.tag) {
            case TypeTags.DECIMAL,
                 TypeTags.FLOAT,
                 TypeTags.XML,
                 TypeTags.XML_TEXT,
                 TypeTags.XML_ELEMENT,
                 TypeTags.XML_PI,
                 TypeTags.XML_COMMENT -> type;
            case TypeTags.INT,
                 TypeTags.BYTE,
                 TypeTags.SIGNED32_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED8_INT,
                 TypeTags.UNSIGNED32_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED8_INT -> symTable.intType;
            case TypeTags.STRING,
                 TypeTags.CHAR_STRING -> symTable.stringType;
            case TypeTags.UNION -> {
                LinkedHashSet<BType> memberTypes = ((BUnionType) type).getMemberTypes();
                yield findCompatibleType(memberTypes.iterator().next());
            }
            default -> {
                Set<BType> broadTypes = SemTypeHelper.broadTypes(type.semType(), symTable);
                assert broadTypes.size() == 1; // all values should belong to a single basic type
                yield broadTypes.iterator().next();
            }
        };
    }

    public boolean isNonNilSimpleBasicTypeOrString(BType bType) {
        return SemTypeHelper.isSubtypeSimpleNotNever(bType,
                (BasicTypeBitSet) Core.diff(PredefinedType.SIMPLE_OR_STRING, PredefinedType.NIL));
    }

    public boolean isSubTypeOfReadOnlyOrIsolatedObjectUnion(BType bType) {
        return SemTypes.isSubtype(semTypeCtx, bType.semType(),
                SemTypes.union(PredefinedType.VAL_READONLY, createIsolatedObject(semTypeCtx)));
    }

    private boolean isImmutable(BType type) {
        return Symbols.isFlagOn(type.getFlags(), Flags.READONLY);
    }

    BType getTypeWithoutNil(BType type) {
        BType constraint = getImpliedType(type);
        if (constraint.tag != TypeTags.UNION) {
            return constraint;
        }

        BUnionType unionType = (BUnionType) constraint;
        if (!unionType.isNullable()) {
            return unionType;
        }

        List<BType> nonNilTypes = new ArrayList<>();
        for (BType memberType : unionType.getMemberTypes()) {
            if (!isAssignable(memberType, symTable.nilType)) {
                nonNilTypes.add(memberType);
            }
        }

        if (nonNilTypes.size() == 1) {
            return nonNilTypes.get(0);
        }

        return BUnionType.create(typeEnv(), null, new LinkedHashSet<>(nonNilTypes));
    }

    public boolean isFixedLengthTuple(BTupleType bTupleType) {
        return isFixedLengthList(bTupleType);
    }

    public boolean isFixedLengthList(BType type) {
        // Using int:MIN_VALUE to project the rest type.
        // This checks the type of effectively infinite list member, which should be the rest type.
        SemType rest = Core.listMemberTypeInnerVal(semTypeCtx, type.semType(),
                IntSubtype.intConst(Long.MAX_VALUE));
        return Core.isNever(rest);
    }

    public boolean isNeverTypeOrStructureTypeWithARequiredNeverMember(BType type) {
        if (type == null) {
            return false;
        }
        Set<BType> visitedTypeSet = new HashSet<>();
        visitedTypeSet.add(type);
        return isNeverTypeOrStructureTypeWithARequiredNeverMember(type, visitedTypeSet);
    }

    boolean isNeverTypeOrStructureTypeWithARequiredNeverMember(BType type, Set<BType> visitedTypeSet) {
        switch (type.tag) {
            case TypeTags.NEVER:
                return true;
            case TypeTags.RECORD:
                for (BField field : ((BRecordType) type).fields.values()) {
                    // skip check for fields with self referencing type and not required fields.
                    if ((SymbolFlags.isFlagOn(field.symbol.flags, SymbolFlags.REQUIRED) ||
                            !SymbolFlags.isFlagOn(field.symbol.flags, SymbolFlags.OPTIONAL)) &&
                            visitedTypeSet.add(field.type) &&
                            isNeverTypeOrStructureTypeWithARequiredNeverMember(field.type, visitedTypeSet)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                List<BType> tupleTypes = tupleType.getTupleTypes();
                for (BType memType : tupleTypes) {
                    if (!visitedTypeSet.add(memType)) {
                        continue;
                    }
                    if (isNeverTypeOrStructureTypeWithARequiredNeverMember(memType, visitedTypeSet)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) type;
                visitedTypeSet.add(arrayType.eType);
                return arrayType.state != BArrayState.OPEN &&
                        isNeverTypeOrStructureTypeWithARequiredNeverMember(arrayType.eType, visitedTypeSet);
            case TypeTags.TYPEREFDESC:
                visitedTypeSet.add(type);
                return isNeverTypeOrStructureTypeWithARequiredNeverMember(getImpliedType(type), visitedTypeSet);
            case UNION:
                BUnionType unionType = (BUnionType) type;
                for (BType memType : unionType.getMemberTypes()) {
                    if (!isNeverTypeOrStructureTypeWithARequiredNeverMember(memType, visitedTypeSet)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.INTERSECTION:
                visitedTypeSet.add(type);
                return isNeverTypeOrStructureTypeWithARequiredNeverMember(((BIntersectionType) type).effectiveType,
                        visitedTypeSet);
            default:
                return false;
        }
    }

    public boolean isNeverType(BType type) {
        return Core.isNever(type.semType());
    }

    boolean isSingletonType(BType bType) {
        BType type = getImpliedType(bType);
        return type.tag == TypeTags.FINITE && Core.singleShape(type.semType()).isPresent();
    }

    boolean isSameSingletonType(BFiniteType type1, BFiniteType type2) {
        SemType t1 = type1.semType();
        SemType t2 = type2.semType();
        return SemTypes.isSameType(semTypeCtx, t1, t2);
    }

    public static void addImmutableType(SymbolTable symTable, PackageID packageId,
                                        SelectivelyImmutableReferenceType type, BIntersectionType immutableType) {

        Map<String, Map<SelectivelyImmutableReferenceType, BIntersectionType>> immutableTypeMaps =
                symTable.immutableTypeMaps;

        String packageIdString = getPackageIdString(packageId);

        Map<SelectivelyImmutableReferenceType, BIntersectionType> moduleImmutableTypeMap =
                immutableTypeMaps.get(packageIdString);

        if (moduleImmutableTypeMap == null) {
            moduleImmutableTypeMap = new HashMap<>();
            immutableTypeMaps.put(packageIdString, moduleImmutableTypeMap);
        }

        moduleImmutableTypeMap.put(type, immutableType);
    }

    public static Optional<BIntersectionType> getImmutableType(SymbolTable symTable, PackageID packageId,
                                                               SelectivelyImmutableReferenceType type) {
        Map<String, Map<SelectivelyImmutableReferenceType, BIntersectionType>> immutableTypeMaps =
                symTable.immutableTypeMaps;

        String packageIdString = getPackageIdString(packageId);

        Map<SelectivelyImmutableReferenceType, BIntersectionType> moduleImmutableTypeMap =
                immutableTypeMaps.get(packageIdString);

        if (moduleImmutableTypeMap == null) {
            return Optional.empty();
        }

        if (moduleImmutableTypeMap.containsKey(type)) {
            return Optional.of(moduleImmutableTypeMap.get(type));
        }

        return Optional.empty();
    }

    public static Name getImmutableTypeName(String origName) {
        if (origName.isEmpty()) {
            return Names.EMPTY;
        }

        return Names.fromString("(".concat(origName).concat(AND_READONLY_SUFFIX).concat(")"));
    }

    public static String getPackageIdString(PackageID packageID) {
        return packageID.isTestPkg ? packageID.toString() + "_testable" : packageID.toString();
    }

    private class ListenerValidationModel {
        private final Types types;
        private final SymbolTable symtable;
        private final BType serviceNameType;
        boolean attachFound;
        boolean detachFound;
        boolean startFound;
        boolean gracefulStopFound;
        boolean immediateStopFound;

        public ListenerValidationModel(Types types, SymbolTable symTable) {
            this.types = types;
            this.symtable = symTable;
            this.serviceNameType =
                    BUnionType.create(symTable.typeEnv(), null, symtable.stringType, symtable.arrayStringType,
                            symtable.nilType);
        }

        boolean isValidListener() {
            return attachFound && detachFound && startFound && gracefulStopFound && immediateStopFound;
        }

        private boolean checkMethods(List<BAttachedFunction> rhsFuncs) {
            for (BAttachedFunction func : rhsFuncs) {
                switch (func.funcName.value) {
                    case "attach":
                        if (!checkAttachMethod(func)) {
                            return false;
                        }
                        break;
                    case "detach":
                        if (!checkDetachMethod(func)) {
                            return false;
                        }
                        break;
                    case "start":
                        if (!checkStartMethod(func)) {
                            return true;
                        }
                        break;
                    case "gracefulStop":
                        if (!checkGracefulStop(func)) {
                            return false;
                        }
                        break;
                    case "immediateStop":
                        if (!checkImmediateStop(func)) {
                            return false;
                        }
                        break;
                }
            }
            return isValidListener();
        }

        private boolean emptyParamList(BAttachedFunction func) {
            return func.type.paramTypes.isEmpty() && func.type.restType != symtable.noType;
        }

        private boolean publicAndReturnsErrorOrNil(BAttachedFunction func) {
            if (!Symbols.isPublic(func.symbol)) {
                return false;
            }

            return types.isAssignable(func.type.retType, symtable.errorOrNilType);
        }

        private boolean isPublicNoParamReturnsErrorOrNil(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            return emptyParamList(func);
        }

        private boolean checkImmediateStop(BAttachedFunction func) {
            return immediateStopFound = isPublicNoParamReturnsErrorOrNil(func);
        }

        private boolean checkGracefulStop(BAttachedFunction func) {
            return gracefulStopFound = isPublicNoParamReturnsErrorOrNil(func);
        }

        private boolean checkStartMethod(BAttachedFunction func) {
            return startFound = publicAndReturnsErrorOrNil(func);
        }

        private boolean checkDetachMethod(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            if (func.type.paramTypes.size() != 1) {
                return false;
            }

            return detachFound = isServiceObject(func.type.paramTypes.get(0));
        }

        private boolean checkAttachMethod(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            if (func.type.paramTypes.size() != 2) {
                return false;
            }

            BType firstParamType = func.type.paramTypes.get(0);
            if (!isServiceObject(firstParamType)) {
                return false;
            }

            BType secondParamType = func.type.paramTypes.get(1);
            boolean sameType = types.isAssignable(secondParamType, this.serviceNameType);
            return attachFound = sameType;

        }

        private boolean isServiceObject(BType bType) {
            return types.isSubtype(bType, createServiceObject(semTypeCtx));
        }
    }

    /**
     * Intersection type validation helper.
     *
     * @since 2.0.0
     */
    public static class IntersectionContext {
        Location lhsPos;
        Location rhsPos;
        // The location of the intersection
        Location pos;
        BLangDiagnosticLog dlog;
        ContextOption contextOption;
        // Intersection test only care about intersection of types (ignoring default values).
        boolean ignoreDefaultValues;
        // Whether type definitions should be defined for newly-created types.
        boolean createTypeDefs;
        // Try to avoid creating new intersection types.
        boolean preferNonGenerativeIntersection;

        private IntersectionContext(BLangDiagnosticLog diaglog, Location left, Location right) {
            this.dlog = diaglog;
            this.lhsPos = left;
            this.rhsPos = right;
            this.contextOption = ContextOption.NON;
            this.ignoreDefaultValues = false;
            this.createTypeDefs = true;
            this.preferNonGenerativeIntersection = false;
        }

        private IntersectionContext(BLangDiagnosticLog diaglog, Location left, Location right, Location pos) {
            this(diaglog, left, right);
            this.pos = pos;
        }

        /**
         * Create {@link IntersectionContext} used for calculating the intersection type when user
         * explicitly write intersection type. This will produce error messages explaining why there is no intersection
         * between two types.
         *
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext from(BLangDiagnosticLog diaglog, Location left, Location right) {
            return new IntersectionContext(diaglog, left, right);
        }

        /**
         * Create {@link IntersectionContext} used for calculating the intersection type to see if there
         * is a intersection between the types. This does not emit error messages explaning why there is no intersection
         * between two types. This also does not generate type-def for the calculated intersection type.
         * Do not use this context to create a intersection type that uses the calculated type for any purpose other
         * than seeing if there is a interserction.
         *
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext compilerInternalIntersectionTestContext() {
            IntersectionContext intersectionContext = new IntersectionContext(null, null, null);
            intersectionContext.ignoreDefaultValues = true;
            intersectionContext.createTypeDefs = false;
            return intersectionContext;
        }

        /**
         * Create {@link IntersectionContext} used for calculating the intersection type.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext compilerInternalIntersectionContext() {
            IntersectionContext diagnosticContext = new IntersectionContext(null, null, null);
            return diagnosticContext;
        }

        /**
         * Create {@link IntersectionContext} used for calculating the intersection type.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @param intersectionPos Location of the intersection
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext compilerInternalIntersectionContext(Location intersectionPos) {
            return new IntersectionContext(null, null, null, intersectionPos);
        }

        /**
         * Create {@link IntersectionContext} used for checking the existence of a valid intersection, irrespective
         * of default values.
         * Type definitions are not created.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @param intersectionPos Location of the intersection
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext typeTestIntersectionExistenceContext(Location intersectionPos) {
            IntersectionContext intersectionContext = new IntersectionContext(null, null, null, intersectionPos);
            intersectionContext.ignoreDefaultValues = true;
            intersectionContext.preferNonGenerativeIntersection = true;
            intersectionContext.createTypeDefs = false;
            return intersectionContext;
        }

        /**
         * Create {@link IntersectionContext} used for creating effective types for the intersection of types,
         * irrespective of default values.
         * Type definitions are created.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext typeTestIntersectionCalculationContext() {
            IntersectionContext intersectionContext = new IntersectionContext(null, null, null);
            intersectionContext.ignoreDefaultValues = true;
            intersectionContext.preferNonGenerativeIntersection = true;
            intersectionContext.createTypeDefs = true;
            return intersectionContext;
        }

        /**
         * Create {@link IntersectionContext} used for creating effective types for the intersection of types,
         * irrespective of default values.
         * Type definitions are created.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @param intersectionPos Location of the intersection
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext typeTestIntersectionCalculationContext(Location intersectionPos) {
            IntersectionContext intersectionContext = typeTestIntersectionCalculationContext();
            intersectionContext.pos = intersectionPos;
            return intersectionContext;
        }

        /**
         * Create {@link IntersectionContext} used for calculating the intersection type with mappings, ignoring
         * default values.
         * This does not emit error messages explaining why there is no intersection between two types.
         *
         * @return a {@link IntersectionContext}
         */
        public static IntersectionContext matchClauseIntersectionContextForMapping() {
            IntersectionContext intersectionContext = new IntersectionContext(null, null, null);
            intersectionContext.ignoreDefaultValues = true;
            return intersectionContext;
        }

        public IntersectionContext switchLeft() {
            this.contextOption = ContextOption.LEFT;
            return this;
        }

        public IntersectionContext switchRight() {
            this.contextOption = ContextOption.RIGHT;
            return this;
        }

        private boolean logError(DiagnosticErrorCode diagnosticCode, Object... args) {
            Location pos = null;
            if (contextOption == ContextOption.LEFT && lhsPos != null) {
                pos = lhsPos;
            } else if (contextOption == ContextOption.RIGHT && rhsPos != null) {
                pos = rhsPos;
            }

            if (pos != null) {
                dlog.error(pos, diagnosticCode, args);
                return true;
            }

            return false;
        }
    }

    private enum ContextOption {
        LEFT, RIGHT, NON
    }

    private void populateBasicTypes(BType type, Set<BasicTypes> basicTypes) {
        type = getImpliedType(type);

        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                basicTypes.add(BasicTypes.INT);
                return;
            case TypeTags.FLOAT:
                basicTypes.add(BasicTypes.FLOAT);
                return;
            case TypeTags.DECIMAL:
                basicTypes.add(BasicTypes.DECIMAL);
                return;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                basicTypes.add(BasicTypes.STRING);
                return;
            case TypeTags.BOOLEAN:
                basicTypes.add(BasicTypes.BOOLEAN);
                return;
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_TEXT:
                basicTypes.add(BasicTypes.XML);
                return;
            case TypeTags.TABLE:
                basicTypes.add(BasicTypes.TABLE);
                return;
            case TypeTags.NIL:
                basicTypes.add(BasicTypes.NIL);
                return;
            case TypeTags.RECORD:
            case TypeTags.MAP:
                basicTypes.add(BasicTypes.MAPPING);
                return;
            case TypeTags.TYPEDESC:
                basicTypes.add(BasicTypes.TYPEDESC);
                return;
            case TypeTags.STREAM:
                basicTypes.add(BasicTypes.STREAM);
                return;
            case TypeTags.INVOKABLE:
            case TypeTags.FUNCTION_POINTER:
                basicTypes.add(BasicTypes.FUNCTION);
                return;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
            case TypeTags.BYTE_ARRAY:
                basicTypes.add(BasicTypes.LIST);
                return;
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.ANYDATA:
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    populateBasicTypes(memberType, basicTypes);
                }
                return;
            case TypeTags.ANY:
                basicTypes.add(BasicTypes.ANY);
                return;
            case TypeTags.ERROR:
                basicTypes.add(BasicTypes.ERROR);
                return;
            case TypeTags.ITERATOR:
            case TypeTags.FUTURE:
                basicTypes.add(BasicTypes.FUTURE);
                return;
            case TypeTags.OBJECT:
                basicTypes.add(BasicTypes.OBJECT);
                return;
            case TypeTags.FINITE:
                SemType semType = type.semType();
                populateBasicTypes(semType, basicTypes);
                return;
            case TypeTags.HANDLE:
                basicTypes.add(BasicTypes.HANDLE);
                return;
            case TypeTags.READONLY:
                basicTypes.add(BasicTypes.READONLY);
                return;
            case TypeTags.NEVER:
                basicTypes.add(BasicTypes.NEVER);
        }
    }

    private void populateBasicTypes(SemType t, Set<BasicTypes> basicTypes) {
        int bitset;
        if (t instanceof BasicTypeBitSet b) {
            bitset = b.bitset;
        } else {
            ComplexSemType cst = (ComplexSemType) t;
            bitset = cst.all() | cst.some();
        }

        if ((bitset & PredefinedType.NIL.bitset) != 0) {
            basicTypes.add(BasicTypes.NIL);
        }
        if ((bitset & PredefinedType.BOOLEAN.bitset) != 0) {
            basicTypes.add(BasicTypes.BOOLEAN);
        }
        if ((bitset & PredefinedType.INT.bitset) != 0) {
            basicTypes.add(BasicTypes.INT);
        }
        if ((bitset & PredefinedType.FLOAT.bitset) != 0) {
            basicTypes.add(BasicTypes.FLOAT);
        }
        if ((bitset & PredefinedType.DECIMAL.bitset) != 0) {
            basicTypes.add(BasicTypes.DECIMAL);
        }
        if ((bitset & PredefinedType.STRING.bitset) != 0) {
            basicTypes.add(BasicTypes.STRING);
        }
    }

    private enum BasicTypes {
        NIL,
        BOOLEAN,
        INT,
        FLOAT,
        DECIMAL,
        STRING,
        XML,
        LIST,
        MAPPING,
        TABLE,
        ERROR,
        FUNCTION,
        FUTURE,
        OBJECT,
        TYPEDESC,
        HANDLE,
        STREAM,
        READONLY,
        ANY,
        NEVER,
        ANYDATA,
        JSON
    }

    /**
     * Holds common analyzer data between {@link TypeChecker} and {@link SemanticAnalyzer}.
     */
    public static class CommonAnalyzerData {
        Deque<SymbolEnv> queryEnvs = new ArrayDeque<>();
        Deque<BLangNode> queryFinalClauses = new ArrayDeque<>();
        HashSet<BType> checkedErrorList = new HashSet<>();
        boolean breakToParallelQueryEnv = false;
        int letCount = 0;
        boolean nonErrorLoggingCheck = false;

        Deque<LinkedHashSet<BType>> errorTypes = new ArrayDeque<>();
    }

    /**
     * Enum to represent query construct type.
     *
     * @since 2201.3.0
     */
    enum QueryConstructType {
        DEFAULT,
        STREAM,
        MAP,
        TABLE,
        ACTION
    }

    QueryConstructType getQueryConstructType(BLangQueryExpr queryExpr) {
        if (queryExpr.isMap) {
            return QueryConstructType.MAP;
        } else if (queryExpr.isTable) {
            return QueryConstructType.TABLE;
        } else if (queryExpr.isStream) {
            return QueryConstructType.STREAM;
        }
        return QueryConstructType.DEFAULT;
    }

    public byte[] convertToByteArray(String literalExpr) {
        String[] elements = getLiteralTextValue(literalExpr);
        if (elements[0].contains(BASE_16)) {
            return hexStringToByteArray(elements[1]);
        }
        return Base64.getDecoder().decode(elements[1].getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hexStringToByteArray(String base16String) {
        int arrayLength = base16String.length();
        byte[] byteArray = new byte[arrayLength / 2];
        for (int i = 0; i < arrayLength; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(base16String.charAt(i), 16) << 4)
                    + Character.digit(base16String.charAt(i + 1), 16));
        }
        return byteArray;
    }

    private static String[] getLiteralTextValue(String literalExpr) {
        String nodeText = literalExpr.replace("\t", "").replace("\n", "").replace("\r", "")
                .replace(" ", "");
        String[] result = new String[2];
        result[0] = nodeText.substring(0, nodeText.indexOf('`'));
        result[1] = nodeText.substring(nodeText.indexOf('`') + 1, nodeText.lastIndexOf('`'));
        return result;
    }

    public boolean isFunctionVarRef(BLangExpression expr) {
        return expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF && ((BLangSimpleVarRef) expr).symbol != null
                && (((BLangSimpleVarRef) expr).symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION;
    }

    public boolean isCloneableType(BUnionType type) {
        LinkedHashSet<BType> cloneableMemberTypes = symTable.cloneableType.getMemberTypes();
        Iterator<BType> memItr = type.getMemberTypes().iterator();
        for (BType memberType : cloneableMemberTypes) {
            if (!memItr.hasNext() || memberType.tag != memItr.next().tag) {
                return false;
            }
        }
        return true;
    }

    public boolean isContainSubtypeOfInt(BType type) {
        return switch (type.tag) {
            case TypeTags.BYTE,
                 TypeTags.SIGNED32_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED8_INT,
                 TypeTags.UNSIGNED32_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED8_INT -> true;
            case TypeTags.UNION -> {
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (isContainSubtypeOfInt(memberType)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }

    public boolean isMappingConstructorCompatibleType(BType type) {
        int tag = getImpliedType(type).tag;
        return tag == TypeTags.RECORD || tag == TypeTags.MAP;
    }

    // Maybe it is a better idea to directly make Env accessible via the CompilerContext but that means SemType module
    //  will have a dependency on compiler
    public Env typeEnv() {
        return semTypeCtx.env;
    }

    public Context typeCtx() {
        return semTypeCtx;
    }
}
