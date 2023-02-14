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
import org.ballerinalang.model.Name;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
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
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
import java.util.Stack;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
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
import static org.wso2.ballerinalang.compiler.util.TypeTags.NEVER;
import static org.wso2.ballerinalang.compiler.util.TypeTags.OBJECT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.RECORD;
import static org.wso2.ballerinalang.compiler.util.TypeTags.UNION;
import static org.wso2.ballerinalang.compiler.util.TypeTags.isSimpleBasicType;

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
    private final Unifier unifier;
    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private BLangDiagnosticLog dlog;
    private Names names;
    private int finiteTypeCount = 0;
    private BUnionType expandedXMLBuiltinSubtypes;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private int recordCount = 0;
    private SymbolEnv env;
    private boolean ignoreObjectTypeIds = false;
    private static final String BASE_16 = "base16";

    private static final BigDecimal DECIMAL_MAX =
            new BigDecimal("9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);

    private static final BigDecimal DECIMAL_MIN =
            new BigDecimal("-9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);

    private static final BigDecimal MIN_DECIMAL_MAGNITUDE =
            new BigDecimal("1.000000000000000000000000000000000e-6143", MathContext.DECIMAL128);

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

    public Types(CompilerContext context) {
        context.put(TYPES_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.expandedXMLBuiltinSubtypes = BUnionType.create(null,
                                                            symTable.xmlElementType, symTable.xmlCommentType,
                                                            symTable.xmlPIType, symTable.xmlTextType);
        this.unifier = new Unifier();
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public List<BType> checkTypes(BLangExpression node,
                                  List<BType> actualTypes,
                                  List<BType> expTypes) {
        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < actualTypes.size(); i++) {
            resTypes.add(checkType(node, actualTypes.get(i), expTypes.size() > i ? expTypes.get(i) : symTable.noType));
        }
        return resTypes;
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

        return BUnionType.create(null, actualType, symTable.nilType);
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

    public boolean isLax(BType type) {
        Set<BType> visited = new HashSet<>();
        int result = isLaxType(type, visited);
        if (result == 1) {
            return true;
        }
        return false;
    }

    // TODO : clean
    public int isLaxType(BType type, Set<BType> visited) {
        if (!visited.add(type)) {
            return -1;
        }
        switch (type.tag) {
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
                return 1;
            case TypeTags.MAP:
                return isLaxType(((BMapType) type).constraint, visited);
            case TypeTags.UNION:
                if (isSameType(type, symTable.jsonType)) {
                    visited.add(type);
                    return 1;
                }
                boolean atleastOneLaxType = false;
                for (BType member : ((BUnionType) type).getMemberTypes()) {
                    int result = isLaxType(member, visited);
                    if (result == -1) {
                        continue;
                    }
                    if (result == 0) {
                        return 0;
                    }
                    atleastOneLaxType = true;
                }
                return atleastOneLaxType ? 1 : 0;
            case TypeTags.TYPEREFDESC:
                return isLaxType(getReferredType(type), visited);
        }
        return 0;
    }

    public boolean isLaxType(BType type, Map<BType, Boolean> visited) {
        if (visited.containsKey(type)) {
            return visited.get(type);
        }
        switch (type.tag) {
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
                visited.put(type, true);
                return true;
            case TypeTags.MAP:
                boolean result = isLaxType(((BMapType) type).constraint, visited);
                visited.put(type, result);
                return result;
            case TypeTags.UNION:
                // TODO: remove
                if (type == symTable.jsonType || isSameType(type, symTable.jsonType)) {
                    visited.put(type, true);
                    return true;
                }
                for (BType member : ((BUnionType) type).getMemberTypes()) {
                    if (!isLaxType(member, visited)) {
                        visited.put(type, false);
                        return false;
                    }
                }
                visited.put(type, true);
                return true;
            case TypeTags.TYPEREFDESC:
                return isLaxType(getReferredType(type), visited);
            }
        visited.put(type, false);
        return false;
    }

    public boolean isSameType(BType source, BType target) {
        return isSameType(source, target, new HashSet<>());
    }

    public boolean isSameOrderedType(BType source, BType target) {
        return isSameOrderedType(source, target, new HashSet<>());
    }

    private boolean isSameOrderedType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        source = getReferredType(source);
        target = getReferredType(target);
        if (source.tag == TypeTags.INTERSECTION) {
            source = getEffectiveTypeForIntersection(source);
        }
        if (isNil(source) || isNil(target)) {
            // If type T is ordered, then type T? Is also ordered.
            // Both source and target are ordered types since they were checked in previous stage.
            // Ex. Let take target -> T, source -> (). T? is ordered type where the static type of both operands belong.
            return true;
        }
        if (!unresolvedTypes.add(new TypePair(source, target))) {
            return true;
        }
        BTypeVisitor<BType, Boolean> orderedTypeVisitor = new BOrderedTypeVisitor(unresolvedTypes);
        return target.accept(orderedTypeVisitor, source);
    }

    public boolean isPureType(BType type) {
        IsPureTypeUniqueVisitor visitor = new IsPureTypeUniqueVisitor();
        return visitor.visit(type);
    }

    public boolean isAnydata(BType type) {
        IsAnydataUniqueVisitor visitor = new IsAnydataUniqueVisitor();
        return visitor.visit(type);
    }

    private boolean isSameType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = null;
        if (!isValueType(source) && !isValueType(target)) {
            pair = new TypePair(source, target);
            if (!unresolvedTypes.add(pair)) {
                return true;
            }
        }

        BTypeVisitor<BType, Boolean> sameTypeVisitor = new BSameTypeVisitor(unresolvedTypes, this::isSameType);

        if (target.accept(sameTypeVisitor, source)) {
            return true;
        }

        if (pair != null) {
            unresolvedTypes.remove(pair);
        }

        return false;
    }

    public boolean isValueType(BType type) {
        switch (type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.BYTE:
            case TypeTags.DECIMAL:
            case TypeTags.FLOAT:
            case TypeTags.INT:
            case TypeTags.STRING:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.CHAR_STRING:
                return true;
            case TypeTags.TYPEREFDESC:
                return isValueType(getReferredType(type));
            default:
                return false;
        }
    }

    boolean isBasicNumericType(BType bType) {
        BType type = getReferredType(bType);
        return type.tag < TypeTags.STRING || TypeTags.isIntegerTypeTag(type.tag);
    }

    boolean finiteTypeContainsNumericTypeValues(BFiniteType finiteType) {
        return finiteType.getValueSpace().stream().anyMatch(valueExpr -> isBasicNumericType(valueExpr.getBType()));
    }

    public boolean containsErrorType(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream()
                    .anyMatch(this::containsErrorType);
        }

        if (type.tag == TypeTags.READONLY) {
            return true;
        }

        return type.tag == TypeTags.ERROR;
    }

    public boolean containsNilType(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (containsNilType(memberType)) {
                    return true;
                }
            }
            return false;
        }

        if (type.tag == TypeTags.READONLY) {
            return true;
        }

        return type.tag == TypeTags.NIL;
    }

    public boolean isSubTypeOfList(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag != TypeTags.UNION) {
            return isSubTypeOfBaseType(type, TypeTags.ARRAY) || isSubTypeOfBaseType(type, TypeTags.TUPLE);
        }

        return ((BUnionType) type).getMemberTypes().stream().allMatch(this::isSubTypeOfList);
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

        if (unaryExpr.expectedType != null && unaryExpr.expectedType.tag == TypeTags.FINITE) {
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
        BType matchExprReferredType = getReferredType(matchExprType);

        if (isValidLiteral(constPatternLiteral, matchExprReferredType)) {
            return matchExprType;
        }

        if (isAssignable(constMatchPatternExprType, matchExprType)) {
            return constMatchPatternExprType;
        }
        if (matchExprReferredType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) matchExprReferredType).getMemberTypes()) {
                if (getReferredType(memberType).tag == TypeTags.FINITE) {
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
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ANY;
        }

        for (BType memberTypes : ((BUnionType) type).getMemberTypes()) {
            if (memberTypes.tag == TypeTags.ANY) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnyDataType(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ANYDATA;
        }

        for (BType memberTypes : ((BUnionType) type).getMemberTypes()) {
            if (memberTypes.tag == TypeTags.ANYDATA) {
                return true;
            }
        }
        return false;
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
        return BUnionType.create(null, typeFirst, typeSecond);
    }

    public boolean isSubTypeOfMapping(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.INTERSECTION) {
            return isSubTypeOfMapping(((BIntersectionType) type).effectiveType);
        }
        if (type.tag != TypeTags.UNION) {
            return isSubTypeOfBaseType(type, TypeTags.MAP) || isSubTypeOfBaseType(type, TypeTags.RECORD);
        }
        return ((BUnionType) type).getMemberTypes().stream().allMatch(this::isSubTypeOfMapping);
    }

    public boolean isSubTypeOfBaseType(BType bType, int baseTypeTag) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.INTERSECTION) {
            type = ((BIntersectionType) type).effectiveType;
        }

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
     * <p>
     * Source type is assignable to the target type if,
     * 1) the target type is any and the source type is not a value type.
     * 2) there exists an implicit cast symbol from source to target.
     * 3) both types are JSON and the target constraint is no type.
     * 4) both types are array type and both array types are assignable.
     * 5) both types are MAP and the target constraint is any type or constraints are structurally equivalent.
     *
     * @param source type.
     * @param target type.
     * @return true if source type is assignable to the target type.
     */
    public boolean isAssignable(BType source, BType target) {
        return isAssignable(source, target, new HashSet<>());
    }

    public boolean isAssignableIgnoreObjectTypeIds(BType source, BType target) {
        this.ignoreObjectTypeIds = true;
        boolean result = isAssignable(source, target);
        this.ignoreObjectTypeIds = false;
        return result;
    }

    private boolean isAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {

        if (isSameType(source, target)) {
            return true;
        }

        int sourceTag = source.tag;
        int targetTag = target.tag;

        if (sourceTag == TypeTags.TYPEREFDESC || targetTag == TypeTags.TYPEREFDESC) {
            return isAssignable(getReferredType(source), getReferredType(target),
                    unresolvedTypes);
        }

        if (isNeverTypeOrStructureTypeWithARequiredNeverMember(source)) {
            return true;
        }

        if (!Symbols.isFlagOn(source.flags, Flags.PARAMETERIZED) &&
                !isInherentlyImmutableType(target) && Symbols.isFlagOn(target.flags, Flags.READONLY) &&
                !isInherentlyImmutableType(source) && isMutable(source)) {
            return false;
        }

        if (sourceTag == TypeTags.INTERSECTION) {
            return isAssignable(((BIntersectionType) source).effectiveType,
                                targetTag != TypeTags.INTERSECTION ? target :
                                        ((BIntersectionType) target).effectiveType, unresolvedTypes);
        }

        if (targetTag == TypeTags.INTERSECTION) {
            return isAssignable(source, ((BIntersectionType) target).effectiveType, unresolvedTypes);
        }

        if (sourceTag == TypeTags.PARAMETERIZED_TYPE) {
            return isParameterizedTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.BYTE && targetTag == TypeTags.INT) {
            return true;
        }

        if (TypeTags.isXMLTypeTag(sourceTag) && TypeTags.isXMLTypeTag(targetTag)) {
            return isXMLTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.CHAR_STRING && targetTag == TypeTags.STRING) {
            return true;
        }

        if (sourceTag == TypeTags.ERROR && targetTag == TypeTags.ERROR) {
            return isErrorTypeAssignable((BErrorType) source, (BErrorType) target, unresolvedTypes);
        } else if (sourceTag == TypeTags.ERROR && targetTag == TypeTags.ANY) {
            return false;
        }

        if (sourceTag == TypeTags.NIL && (isNullable(target) || targetTag == TypeTags.JSON)) {
            return true;
        }

        // TODO: Remove the isValueType() check
        if (targetTag == TypeTags.ANY && !containsErrorType(source) && !isValueType(source)) {
            return true;
        }

        if (targetTag == TypeTags.ANYDATA && !containsErrorType(source) && isAnydata(source)) {
            return true;
        }

        if (targetTag == TypeTags.READONLY) {
            if ((isInherentlyImmutableType(source) || Symbols.isFlagOn(source.flags, Flags.READONLY))) {
                return true;
            }
            if (isAssignable(source, symTable.anyAndReadonlyOrError, unresolvedTypes)) {
                return true;
            }
        }

        if (sourceTag == TypeTags.READONLY && isAssignable(symTable.anyAndReadonlyOrError, target, unresolvedTypes)) {
            return true;
        }

        if (targetTag == TypeTags.MAP && sourceTag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) source;
            return isAssignableRecordType(recordType, target, unresolvedTypes);
        }

        if (targetTag == TypeTags.RECORD && sourceTag == TypeTags.MAP) {
            return isAssignableMapType((BMapType) source, (BRecordType) target);
        }

        if (targetTag == TypeTags.TYPEDESC && sourceTag == TypeTags.TYPEDESC) {
            return isAssignable(((BTypedescType) source).constraint, (((BTypedescType) target).constraint),
                                unresolvedTypes);
        }

        if (targetTag == TypeTags.TABLE && sourceTag == TypeTags.TABLE) {
            return isAssignableTableType((BTableType) source, (BTableType) target, unresolvedTypes);
        }

        if (targetTag == TypeTags.STREAM && sourceTag == TypeTags.STREAM) {
            return isAssignableStreamType((BStreamType) source, (BStreamType) target, unresolvedTypes);
        }

        if (isBuiltInTypeWidenPossible(source, target) == TypeTestResult.TRUE) {
            return true;
        }

        if (sourceTag == TypeTags.FINITE) {
            return isFiniteTypeAssignable((BFiniteType) source, target, unresolvedTypes);
        }

        if ((targetTag == TypeTags.UNION || sourceTag == TypeTags.UNION) &&
                isAssignableToUnionType(source, target, unresolvedTypes)) {
            return true;
        }

        if (targetTag == TypeTags.JSON) {
            if (sourceTag == TypeTags.JSON) {
                return true;
            }

            if (sourceTag == TypeTags.TUPLE) {
                return isTupleTypeAssignable(source, target, unresolvedTypes);
            }

            if (sourceTag == TypeTags.ARRAY) {
                return isArrayTypesAssignable((BArrayType) source, target, unresolvedTypes);
            }

            if (sourceTag == TypeTags.MAP) {
                return isAssignable(((BMapType) source).constraint, target, unresolvedTypes);
            }

            if (sourceTag == TypeTags.RECORD) {
                return isAssignableRecordType((BRecordType) source, target, unresolvedTypes);
            }
        }

        if (targetTag == TypeTags.FUTURE && sourceTag == TypeTags.FUTURE) {
            if (((BFutureType) target).constraint.tag == TypeTags.NONE) {
                return true;
            }
            return isAssignable(((BFutureType) source).constraint, ((BFutureType) target).constraint, unresolvedTypes);
        }

        if (targetTag == TypeTags.MAP && sourceTag == TypeTags.MAP) {
            // Here source condition is added for prevent assigning map union constrained
            // to map any constrained.
            if (((BMapType) target).constraint.tag == TypeTags.ANY &&
                    ((BMapType) source).constraint.tag != TypeTags.UNION) {
                return true;
            }

            return isAssignable(((BMapType) source).constraint, ((BMapType) target).constraint, unresolvedTypes);
        }

        if ((sourceTag == TypeTags.OBJECT || sourceTag == TypeTags.RECORD)
                && (targetTag == TypeTags.OBJECT || targetTag == TypeTags.RECORD)) {
            return checkStructEquivalency(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.TUPLE && targetTag == TypeTags.ARRAY) {
            return isTupleTypeAssignableToArrayType((BTupleType) source, (BArrayType) target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.ARRAY && targetTag == TypeTags.TUPLE) {
            return isArrayTypeAssignableToTupleType((BArrayType) source, (BTupleType) target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.TUPLE || targetTag == TypeTags.TUPLE) {
            return isTupleTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.INVOKABLE && targetTag == TypeTags.INVOKABLE) {
            return isFunctionTypeAssignable((BInvokableType) source, (BInvokableType) target, new HashSet<>());
        }

        return sourceTag == TypeTags.ARRAY && targetTag == TypeTags.ARRAY &&
                isArrayTypesAssignable((BArrayType) source, target, unresolvedTypes);
    }

    private boolean isMutable(BType type) {
        if (Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return false;
        }

        if (type.tag != TypeTags.UNION) {
            return true;
        }

        BUnionType unionType = (BUnionType) type;
        for (BType memberType : unionType.getMemberTypes()) {
            if (!Symbols.isFlagOn(memberType.flags, Flags.READONLY)) {
                return true;
            }
        }

        unionType.flags |= Flags.READONLY;
        BTypeSymbol tsymbol = unionType.tsymbol;
        if (tsymbol != null) {
            tsymbol.flags |= Flags.READONLY;
        }
        return false;
    }

    private boolean isParameterizedTypeAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {
        BType resolvedSourceType = unifier.build(source);

        if (target.tag != TypeTags.PARAMETERIZED_TYPE) {
            return isAssignable(resolvedSourceType, target, unresolvedTypes);
        }

        if (((BParameterizedType) source).paramIndex != ((BParameterizedType) target).paramIndex) {
            return false;
        }

        return isAssignable(resolvedSourceType, unifier.build(target), unresolvedTypes);
    }

    private boolean isAssignableRecordType(BRecordType recordType, BType type, Set<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(recordType, type);
        if (!unresolvedTypes.add(pair)) {
            return true;
        }

        BType targetType;
        switch (type.tag) {
            case TypeTags.MAP:
                targetType = ((BMapType) type).constraint;
                break;
            case TypeTags.JSON:
                targetType = type;
                break;
            default:
                throw new IllegalArgumentException("Incompatible target type: " + type.toString());
        }
        return recordFieldsAssignableToType(recordType, targetType, unresolvedTypes);
    }

    private boolean isAssignableStreamType(BStreamType sourceStreamType, BStreamType targetStreamType,
                                           Set<TypePair> unresolvedTypes) {
        return isAssignable(sourceStreamType.constraint, targetStreamType.constraint, unresolvedTypes)
                && isAssignable(sourceStreamType.completionType, targetStreamType.completionType, unresolvedTypes);
    }

    private boolean recordFieldsAssignableToType(BRecordType recordType, BType targetType,
                                                 Set<TypePair> unresolvedTypes) {
        for (BField field : recordType.fields.values()) {
            if (!isAssignable(field.type, targetType, unresolvedTypes)) {
                return false;
            }
        }

        if (!recordType.sealed) {
            return isAssignable(recordType.restFieldType, targetType, unresolvedTypes);
        }

        return true;
    }

    private boolean isAssignableTableType(BTableType sourceTableType, BTableType targetTableType,
                                          Set<TypePair> unresolvedTypes) {
        if (!isAssignable(sourceTableType.constraint, targetTableType.constraint, unresolvedTypes)) {
            return false;
        }

        if (targetTableType.keyTypeConstraint == null && targetTableType.fieldNameList.isEmpty()) {
            return true;
        }

        if (targetTableType.keyTypeConstraint != null) {
            if (sourceTableType.keyTypeConstraint != null &&
                    (isAssignable(sourceTableType.keyTypeConstraint, targetTableType.keyTypeConstraint,
                            unresolvedTypes))) {
                return true;
            }

            if (sourceTableType.fieldNameList.isEmpty()) {
                return false;
            }

            List<BTupleMember> fieldTypes = new ArrayList<>();
            sourceTableType.fieldNameList.stream()
                    .map(f -> getTableConstraintField(sourceTableType.constraint, f))
                    .filter(Objects::nonNull).map(f -> new BTupleMember(f.type,
                            Symbols.createVarSymbolForTupleMember(f.type))).forEach(fieldTypes::add);
            if (fieldTypes.size() == 1) {
                return isAssignable(fieldTypes.get(0).type, targetTableType.keyTypeConstraint, unresolvedTypes);
            }

            BTupleType tupleType = new BTupleType(fieldTypes);
            return isAssignable(tupleType, targetTableType.keyTypeConstraint, unresolvedTypes);
        }

        return targetTableType.fieldNameList.equals(sourceTableType.fieldNameList);
    }


    BField getTableConstraintField(BType constraintType, String fieldName) {

        switch (constraintType.tag) {
            case TypeTags.RECORD:
                Map<String, BField> fieldList = ((BRecordType) constraintType).getFields();
                return fieldList.get(fieldName);
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) constraintType;
                Set<BType> memTypes = unionType.getMemberTypes();
                List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                        .filter(Objects::nonNull).collect(Collectors.toList());

                if (fields.size() != memTypes.size()) {
                    return null;
                }

                if (fields.stream().allMatch(field -> isAssignable(field.type, fields.get(0).type) &&
                        isAssignable(fields.get(0).type, field.type))) {
                    return fields.get(0);
                }
                break;
            case TypeTags.INTERSECTION:
                return getTableConstraintField(((BIntersectionType) constraintType).effectiveType, fieldName);
            case TypeTags.TYPEREFDESC:
                return getTableConstraintField(((BTypeReferenceType) constraintType).referredType, fieldName);
        }

        return null;
    }

    private boolean isAssignableMapType(BMapType sourceMapType, BRecordType targetRecType) {
        if (targetRecType.sealed) {
            return false;
        }

        for (BField field : targetRecType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                return false;
            }

            if (hasIncompatibleReadOnlyFlags(field.symbol.flags, sourceMapType.flags)) {
                return false;
            }

            if (!isAssignable(sourceMapType.constraint, field.type)) {
                return false;
            }
        }

        return isAssignable(sourceMapType.constraint, targetRecType.restFieldType);
    }

    private boolean hasIncompatibleReadOnlyFlags(long targetFlags, long sourceFlags) {
        return Symbols.isFlagOn(targetFlags, Flags.READONLY) && !Symbols.isFlagOn(sourceFlags, Flags.READONLY);
    }

    private boolean isErrorTypeAssignable(BErrorType source, BErrorType target, Set<TypePair> unresolvedTypes) {
        if (target == symTable.errorType) {
            return true;
        }
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);
        return isAssignable(source.detailType, target.detailType, unresolvedTypes)
                && target.typeIdSet.isAssignableFrom(source.typeIdSet);
    }

    private boolean isXMLTypeAssignable(BType sourceT, BType targetT, Set<TypePair> unresolvedTypes) {
        BType sourceType = getReferredType(sourceT);
        BType targetType = getReferredType(targetT);
        int sourceTag = sourceType.tag;
        int targetTag = targetType.tag;

        if (targetTag == TypeTags.XML) {
            BXMLType target = (BXMLType) targetType;
            if (target.constraint != null) {
                if (TypeTags.isXMLNonSequenceType(sourceTag)) {
                    return isAssignable(sourceType, target.constraint, unresolvedTypes);
                }
                BXMLType source = (BXMLType) sourceType;
                if (source.constraint.tag == TypeTags.NEVER) {
                    if (sourceTag == targetTag) {
                        return true;
                    }
                    return isAssignable(source, target.constraint, unresolvedTypes);
                }
                return isAssignable(source.constraint, target, unresolvedTypes);
            }
            return true;
        }
        if (sourceTag == TypeTags.XML) {
            BXMLType source = (BXMLType) sourceType;
            if (targetTag == TypeTags.XML_TEXT) {
                if (source.constraint != null) {
                    if (source.constraint.tag == TypeTags.NEVER ||
                            source.constraint.tag == TypeTags.XML_TEXT) {
                        return true;
                    } else {
                        return isAssignable(source.constraint, targetType, unresolvedTypes);
                    }
                }
                return false;
            }
        }
        return sourceTag == targetTag;
    }

    private boolean isTupleTypeAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }

        if (source.tag == TypeTags.TUPLE && ((BTupleType) source).isCyclic) {
            // add cyclic source to target pair to avoid recursive calls
            unresolvedTypes.add(pair);
        }

        if (target.tag == TypeTags.JSON && source.tag == TypeTags.TUPLE) {
            BTupleType rhsTupleType = (BTupleType) source;
            for (BType tupleType : rhsTupleType.getTupleTypes()) {
                if (!isAssignable(tupleType, target, unresolvedTypes)) {
                    return false;
                }
            }
            if (rhsTupleType.restType != null) {
                return isAssignable(rhsTupleType.restType, target, unresolvedTypes);
            }
            return true;
        }

        if (source.tag != TypeTags.TUPLE || target.tag != TypeTags.TUPLE) {
            return false;
        }

        BTupleType lhsTupleType = (BTupleType) target;
        BTupleType rhsTupleType = (BTupleType) source;

        if (lhsTupleType.restType == null && rhsTupleType.restType != null) {
            return false;
        }

        if (lhsTupleType.restType == null &&
                lhsTupleType.getMembers().size() != rhsTupleType.getMembers().size()) {
            return false;
        }

        if (lhsTupleType.restType != null && rhsTupleType.restType != null) {
            if (!isAssignable(rhsTupleType.restType, lhsTupleType.restType, unresolvedTypes)) {
                return false;
            }
        }
        List<BType> lhsTupleMemberTypes = lhsTupleType.getTupleTypes();
        List<BType> rhsTupleMemberTypes = rhsTupleType.getTupleTypes();

        if (lhsTupleMemberTypes.size() > rhsTupleMemberTypes.size()) {
            return false;
        }

        for (int i = 0; i < rhsTupleMemberTypes.size(); i++) {
            BType lhsType = (lhsTupleMemberTypes.size() > i)
                    ? lhsTupleMemberTypes.get(i) : lhsTupleType.restType;
            if (!isAssignable(rhsTupleMemberTypes.get(i), lhsType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAllTupleMembersBelongNoType(List<BType> tupleTypes) {
        boolean isNoType = false;
        for (BType type : tupleTypes) {
            switch (type.tag) {
                case TypeTags.NONE:
                    isNoType = true;
                    break;
                case TypeTags.TUPLE:
                    isNoType = checkAllTupleMembersBelongNoType(((BTupleType) type).getTupleTypes());
                    if (!isNoType) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return isNoType;
    }

    private boolean isTupleTypeAssignableToArrayType(BTupleType source, BArrayType target,
                                                     Set<TypePair> unresolvedTypes) {
        if (target.state != BArrayState.OPEN
                && (source.restType != null || source.getMembers().size() != target.size)) {
            return false;
        }

        List<BType> sourceTypes = new ArrayList<>(source.getTupleTypes());
        if (source.restType != null) {
            BType type = source.restType;
            sourceTypes.add(type);
        }
        return sourceTypes.stream()
                .allMatch(tupleElemType -> isAssignable(tupleElemType, target.eType, unresolvedTypes));
    }

    private boolean isArrayTypeAssignableToTupleType(BArrayType source, BTupleType target,
                                                     Set<TypePair> unresolvedTypes) {
        BType restType = target.restType;
        List<BType> tupleTypes = target.getTupleTypes();
        if (source.state == BArrayState.OPEN) {
            if (restType == null || !tupleTypes.isEmpty()) {
                // [int, int] = int[] || [int, int...] = int[]
                return false;
            }

            return isAssignable(source.eType, restType, unresolvedTypes);
        }

        int targetTupleMemberSize = tupleTypes.size();
        int sourceArraySize = source.size;

        if (targetTupleMemberSize > sourceArraySize) {
            // [int, int, int...] = int[1]
            return false;
        }

        if (restType == null && targetTupleMemberSize < sourceArraySize) {
            // [int, int] = int[3]
            return false;
        }

        BType sourceElementType = source.eType;
        for (BType memType : tupleTypes) {
            if (!isAssignable(sourceElementType, memType, unresolvedTypes)) {
                return false;
            }
        }

        if (restType == null) {
            return true;
        }

        return sourceArraySize == targetTupleMemberSize || isAssignable(sourceElementType, restType, unresolvedTypes);
    }

    private boolean isArrayTypesAssignable(BArrayType source, BType target, Set<TypePair> unresolvedTypes) {
        BType sourceElementType = source.getElementType();
        if (target.tag == TypeTags.ARRAY) {
            BArrayType targetArrayType = (BArrayType) target;
            BType targetElementType = targetArrayType.getElementType();
            if (targetArrayType.state == BArrayState.OPEN) {
                return isAssignable(sourceElementType, targetElementType, unresolvedTypes);
            }

            if (targetArrayType.size != source.size) {
                return false;
            }

            return isAssignable(sourceElementType, targetElementType, unresolvedTypes);
        } else if (target.tag == TypeTags.JSON) {
            return isAssignable(sourceElementType, target, unresolvedTypes);
        } else if (target.tag == TypeTags.ANYDATA) {
            return isAssignable(sourceElementType, target, unresolvedTypes);
        }
        return false;
    }

    private boolean isFunctionTypeAssignable(BInvokableType source, BInvokableType target,
                                             Set<TypePair> unresolvedTypes) {
        if (hasIncompatibleIsolatedFlags(source, target) || hasIncompatibleTransactionalFlags(source, target)) {
            return false;
        }

        if (Symbols.isFlagOn(target.flags, Flags.ANY_FUNCTION)) {
            return true;
        }

        // For invokable types with typeParam parameters, we have to check whether the source param types are
        // covariant with the target param types.
        if (containsTypeParams(target)) {
            // TODO: 7/4/19 See if the below code can be generalized to avoid code duplication
            if (source.paramTypes.size() != target.paramTypes.size()) {
                return false;
            }

            for (int i = 0; i < source.paramTypes.size(); i++) {
                BType sourceParam = source.paramTypes.get(i);
                BType targetParam = target.paramTypes.get(i);
                boolean isTypeParam = TypeParamAnalyzer.isTypeParam(targetParam);

                if (isTypeParam) {
                    if (!isAssignable(sourceParam, targetParam)) {
                        return false;
                    }
                } else {
                    if (!isAssignable(targetParam, sourceParam)) {
                        return false;
                    }
                }
            }

            if (source.retType == null && target.retType == null) {
                return true;
            } else if (source.retType == null || target.retType == null) {
                return false;
            }

            // Source return type should be covariant with target return type
            return isAssignable(source.retType, target.retType, unresolvedTypes);
        }

        // Source param types should be contravariant with target param types. Hence s and t switched when checking
        // assignability.
        return checkFunctionTypeEquality(source, target, unresolvedTypes, (s, t, ut) -> isAssignable(t, s, ut));
    }

    public boolean isInherentlyImmutableType(BType type) {
        if (isValueType(type)) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.XML_TEXT:
            case TypeTags.FINITE: // Assuming a finite type will only have members from simple basic types.
            case TypeTags.READONLY:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ERROR:
            case TypeTags.INVOKABLE:
            case TypeTags.TYPEDESC:
            case TypeTags.HANDLE:
                return true;
            case TypeTags.XML:
                return ((BXMLType) type).constraint.tag == TypeTags.NEVER;
            case TypeTags.TYPEREFDESC:
                return isInherentlyImmutableType(((BTypeReferenceType) type).referredType);
        }
        return false;
    }

    public static BType getReferredType(BType type) {
        BType constraint = type;
        if (type != null && type.tag == TypeTags.TYPEREFDESC) {
            constraint = getReferredType(((BTypeReferenceType) type).referredType);
        }
        return constraint;
    }

    public static BType getEffectiveType(BType type) {
        if (type.tag == TypeTags.INTERSECTION) {
            return ((BIntersectionType) type).effectiveType;
        }
        return type;
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

    private boolean isSelectivelyImmutableType(BType type, Set<BType> unresolvedTypes, boolean forceCheck,
                                               PackageID packageID) {
        return isSelectivelyImmutableType(type, false, unresolvedTypes, forceCheck, packageID);
    }

    private boolean isSelectivelyImmutableType(BType input, boolean disallowReadOnlyObjects, Set<BType> unresolvedTypes,
                                               boolean forceCheck, PackageID packageID) {
        BType type = getReferredType(input);

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
                return true;
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                return isInherentlyImmutableType(elementType) ||
                        isSelectivelyImmutableType(elementType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                for (BType memberType : tupleType.getTupleTypes()) {
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
                for (BField field : recordType.fields.values()) {
                    BType fieldType = field.type;
                    if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                            !isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck, packageID)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.MAP:
                BType constraintType = ((BMapType) type).constraint;
                return isInherentlyImmutableType(constraintType) ||
                        isSelectivelyImmutableType(constraintType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.OBJECT:
                BObjectType objectType = (BObjectType) type;

                for (BField field : objectType.fields.values()) {
                    BType fieldType = field.type;
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck, packageID)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TABLE:
                BType tableConstraintType = ((BTableType) type).constraint;
                return isInherentlyImmutableType(tableConstraintType) ||
                        isSelectivelyImmutableType(tableConstraintType, unresolvedTypes, forceCheck, packageID);
            case TypeTags.UNION:
                boolean readonlyIntersectionExists = false;
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (isInherentlyImmutableType(memberType) ||
                            isSelectivelyImmutableType(memberType, unresolvedTypes, forceCheck, packageID)) {
                        readonlyIntersectionExists = true;
                    }
                }
                return readonlyIntersectionExists;
            case TypeTags.INTERSECTION:
                return isSelectivelyImmutableType(((BIntersectionType) type).effectiveType, unresolvedTypes,
                                                  forceCheck, packageID);
            case TypeTags.TYPEREFDESC:
                return isSelectivelyImmutableType(((BTypeReferenceType) type).referredType, unresolvedTypes,
                        forceCheck, packageID);

        }
        return false;
    }

    private boolean containsTypeParams(BInvokableType type) {
        boolean hasParameterizedTypes = type.paramTypes.stream()
                .anyMatch(t -> {
                    if (t.tag == TypeTags.FUNCTION_POINTER) {
                        return containsTypeParams((BInvokableType) t);
                    }
                    return TypeParamAnalyzer.isTypeParam(t);
                });

        if (hasParameterizedTypes) {
            return hasParameterizedTypes;
        }

        if (type.retType.tag == TypeTags.FUNCTION_POINTER) {
            return containsTypeParams((BInvokableType) type.retType);
        }

        return TypeParamAnalyzer.isTypeParam(type.retType);
    }

    private boolean checkFunctionTypeEquality(BInvokableType source, BInvokableType target,
                                              Set<TypePair> unresolvedTypes, TypeEqualityPredicate equality) {
        if (hasIncompatibleIsolatedFlags(source, target) || hasIncompatibleTransactionalFlags(source, target)) {
            return false;
        }

        if (Symbols.isFlagOn(target.flags, Flags.ANY_FUNCTION) && Symbols.isFlagOn(source.flags, Flags.ANY_FUNCTION)) {
            return true;
        }

        if (Symbols.isFlagOn(target.flags, Flags.ANY_FUNCTION) || Symbols.isFlagOn(source.flags, Flags.ANY_FUNCTION)) {
            return false;
        }

        if (source.paramTypes.size() != target.paramTypes.size()) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.size(); i++) {
            if (!equality.test(source.paramTypes.get(i), target.paramTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }

        if ((source.restType != null && target.restType == null) ||
                target.restType != null && source.restType == null) {
            return false;
        } else if (source.restType != null && !equality.test(source.restType, target.restType, unresolvedTypes)) {
            return false;
        }

        if (source.retType == null && target.retType == null) {
            return true;
        } else if (source.retType == null || target.retType == null) {
            return false;
        }

        // Source return type should be covariant with target return type
        return isAssignable(source.retType, target.retType, unresolvedTypes);
    }

    private boolean hasIncompatibleIsolatedFlags(BInvokableType source, BInvokableType target) {
        return Symbols.isFlagOn(target.flags, Flags.ISOLATED) && !Symbols.isFlagOn(source.flags, Flags.ISOLATED);
    }

    private boolean hasIncompatibleTransactionalFlags(BInvokableType source, BInvokableType target) {
        return Symbols.isFlagOn(source.flags, Flags.TRANSACTIONAL) &&
                !Symbols.isFlagOn(target.flags, Flags.TRANSACTIONAL);
    }

    public boolean isSameArrayType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        if (target.tag != TypeTags.ARRAY || source.tag != TypeTags.ARRAY) {
            return false;
        }

        BArrayType lhsArrayType = (BArrayType) target;
        BArrayType rhsArrayType = (BArrayType) source;
        boolean hasSameTypeElements = isSameType(lhsArrayType.eType, rhsArrayType.eType, unresolvedTypes);
        if (lhsArrayType.state == BArrayState.OPEN) {
            return (rhsArrayType.state == BArrayState.OPEN) && hasSameTypeElements;
        }

        return checkSealedArraySizeEquality(rhsArrayType, lhsArrayType) && hasSameTypeElements;
    }

    public boolean checkSealedArraySizeEquality(BArrayType rhsArrayType, BArrayType lhsArrayType) {
        return lhsArrayType.size == rhsArrayType.size;
    }

    public boolean checkStructEquivalency(BType rhsType, BType lhsType) {
        return checkStructEquivalency(rhsType, lhsType, new HashSet<>());
    }

    private boolean checkStructEquivalency(BType rhsType, BType lhsType, Set<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(rhsType, lhsType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        if (rhsType.tag == TypeTags.OBJECT && lhsType.tag == TypeTags.OBJECT) {
            return checkObjectEquivalency((BObjectType) rhsType, (BObjectType) lhsType, unresolvedTypes);
        }

        if (rhsType.tag == TypeTags.RECORD && lhsType.tag == TypeTags.RECORD) {
            return checkRecordEquivalency((BRecordType) rhsType, (BRecordType) lhsType, unresolvedTypes);
        }

        return false;
    }

    public boolean checkObjectEquivalency(BObjectType rhsType, BObjectType lhsType, Set<TypePair> unresolvedTypes) {
        if (Symbols.isFlagOn(lhsType.flags, Flags.ISOLATED) && !Symbols.isFlagOn(rhsType.flags, Flags.ISOLATED)) {
            return false;
        }

        BObjectTypeSymbol lhsStructSymbol = (BObjectTypeSymbol) lhsType.tsymbol;
        BObjectTypeSymbol rhsStructSymbol = (BObjectTypeSymbol) rhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BObjectTypeSymbol) rhsType.tsymbol).attachedFuncs;
        int lhsAttachedFuncCount = getObjectFuncCount(lhsStructSymbol);
        int rhsAttachedFuncCount = getObjectFuncCount(rhsStructSymbol);

        // If LHS is a service obj, then RHS must be a service object in order to assignable
        boolean isLhsAService = Symbols.isService(lhsStructSymbol);
        if (isLhsAService && !Symbols.isService(rhsStructSymbol)) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size() || lhsAttachedFuncCount > rhsAttachedFuncCount) {
            return false;
        }

        // The LHS type cannot have any private members. 
        for (BField bField : lhsType.fields.values()) {
            if (Symbols.isPrivate(bField.symbol)) {
                return false;
            }
        }

        for (BAttachedFunction func : lhsFuncs) {
            if (Symbols.isPrivate(func.symbol)) {
                return false;
            }
        }

        for (BField lhsField : lhsType.fields.values()) {
            BField rhsField = rhsType.fields.get(lhsField.name.value);
            if (rhsField == null ||
                    !isInSameVisibilityRegion(lhsField.symbol, rhsField.symbol) ||
                    !isAssignable(rhsField.type, lhsField.type, unresolvedTypes)) {
                return false;
            }
        }

        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsStructSymbol.initializerFunc) {
                continue;
            }

            Optional<BAttachedFunction> rhsFunction = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunction.isEmpty()) {
                return false;
            }

            BAttachedFunction rhsFunc = rhsFunction.get();
            if (!isInSameVisibilityRegion(lhsFunc.symbol, rhsFunc.symbol)) {
                return false;
            }
            
            if (Symbols.isRemote(lhsFunc.symbol) != Symbols.isRemote(rhsFunc.symbol)) {
                return false;
            }
        }

        return lhsType.typeIdSet.isAssignableFrom(rhsType.typeIdSet) || this.ignoreObjectTypeIds;
    }

    private int getObjectFuncCount(BObjectTypeSymbol sym) {
        int count = sym.attachedFuncs.size();
        // If an explicit initializer is available, it could mean,
        // 1) User explicitly defined an initializer
        // 2) The object type is coming from an already compiled source, hence the initializer is already set.
        //    If it's coming from a compiled binary, the attached functions list of the symbol would already contain
        //    the initializer in it.
        if (sym.initializerFunc != null && sym.attachedFuncs.contains(sym.initializerFunc)) {
            return count - 1;
        }
        return count;
    }

    public boolean checkRecordEquivalency(BRecordType rhsType, BRecordType lhsType, Set<TypePair> unresolvedTypes) {
        // If the LHS record is closed and the RHS record is open and the rest field type of RHS is not a 'never'
        // type, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed && rhsType.restFieldType.tag != TypeTags.NEVER) {
            return false;
        }

        // If both are open records, the rest field type of the RHS record should be assignable to the rest field
        // type of the LHS type.
        if (!rhsType.sealed && !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    public void setForeachTypedBindingPatternType(BLangForeach foreachNode) {
        BType collectionType = getReferredType(foreachNode.collection.getBType());
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
                if (completionType.stream().anyMatch(type -> type.tag != TypeTags.NIL)) {
                    BType actualType = BUnionType.create(null, varType, streamType.completionType);
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
                        BType actualType = BUnionType.create(null, valueType, errorType);
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
                names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), env);
        BObjectType objectType = (BObjectType) getReferredType(iteratorSymbol.retType);
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
        if (bLangInputClause.varType.tag == TypeTags.SEMANTIC_ERROR || collectionType.tag == OBJECT) {
            return;
        }
        
        BInvokableSymbol iteratorSymbol = (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionType,
                names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), env);
        BUnionType nextMethodReturnType =
                (BUnionType) getResultTypeOfNextInvocation((BObjectType) getReferredType(iteratorSymbol.retType));
        bLangInputClause.resultType = getRecordType(nextMethodReturnType);
        bLangInputClause.nillableResultType = nextMethodReturnType;
    }

    private BType getTypedBindingPatternTypeForXmlCollection(BType collectionType) {
        BType constraint = getReferredType(((BXMLType) collectionType).constraint);
        while (constraint.tag == TypeTags.XML) {
            collectionType = constraint;
            constraint = getReferredType(((BXMLType) collectionType).constraint);
        }
        switch (constraint.tag) {
            case TypeTags.XML_ELEMENT:
                return symTable.xmlElementType;
            case TypeTags.XML_COMMENT:
                return symTable.xmlCommentType;
            case TypeTags.XML_TEXT:
                return symTable.xmlTextType;
            case TypeTags.XML_PI:
                return symTable.xmlPIType;
            case TypeTags.NEVER:
                return symTable.neverType;
            case TypeTags.INTERSECTION:
                return getReferredType(((BIntersectionType) constraint).getEffectiveType());
            case TypeTags.UNION:
                Set<BType> collectionTypes = getEffectiveMemberTypes((BUnionType) constraint);
                Set<BType> builtinXMLConstraintTypes = getEffectiveMemberTypes
                        ((BUnionType) ((BXMLType) symTable.xmlType).constraint);
                if (collectionTypes.size() == 4 && builtinXMLConstraintTypes.equals(collectionTypes)) {
                    return symTable.xmlType;
                } else {
                    LinkedHashSet<BType> collectionTypesInSymTable = new LinkedHashSet<>();
                    for (BType subType : collectionTypes) {
                        switch (subType.tag) {
                            case TypeTags.XML_ELEMENT:
                                collectionTypesInSymTable.add(symTable.xmlElementType);
                                break;
                            case TypeTags.XML_COMMENT:
                                collectionTypesInSymTable.add(symTable.xmlCommentType);
                                break;
                            case TypeTags.XML_TEXT:
                                collectionTypesInSymTable.add(symTable.xmlTextType);
                                break;
                            case TypeTags.XML_PI:
                                collectionTypesInSymTable.add(symTable.xmlPIType);
                                break;
                        }
                    }
                    return BUnionType.create(null, collectionTypesInSymTable);
                }
            default:
                return null;
        }
    }

    private BType visitCollectionType(BLangInputClause bLangInputClause, BType collectionType) {
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
            case TypeTags.TYPEREFDESC:
                return visitCollectionType(bLangInputClause, getReferredType(collectionType));
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
                tupleTypes.iterator().next() : BUnionType.create(null, tupleTypes);
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
        BType returnType = getReferredType(type);
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
        if (returnType.tag != TypeTags.UNION) {
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
            BType referredRecordType = getReferredType(member);
            if (referredRecordType.tag == TypeTags.RECORD) {
                return (BRecordType) referredRecordType;
            }
        }
        return null;
    }

    public BErrorType getErrorType(BUnionType type) {
        for (BType member : type.getMemberTypes()) {
            member = getEffectiveTypeForIntersection(getReferredType(member));

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
        BUnionType unionType = BUnionType.create(null);

        if (!recordType.sealed) {
            unionType.add(recordType.restFieldType);
        } else if (fields.size() == 0) {
            unionType.add(symTable.neverType);
        }

        for (BField field : fields.values()) {
            if (isAssignable(field.type, unionType)) {
                continue;
            }

            if (isAssignable(unionType, field.type)) {
                unionType = BUnionType.create(null);
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
            effectiveType = getReferredType(effectiveType);
            if (effectiveType != memberType) {
                hasDifferentMember = true;
            }
            members.add(effectiveType);
        }

        if (hasDifferentMember) {
            return BUnionType.create(null, members);
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

    TypeTestResult isBuiltInTypeWidenPossible(BType actualType, BType targetType) {

        int targetTag = getReferredType(targetType).tag;
        int actualTag = getReferredType(actualType).tag;

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
        int actualTag = getReferredType(actualType).tag;
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
                break;
            case TypeTags.TYPEREFDESC:
                return checkBuiltInIntSubtypeWidenPossible(actualType, getReferredType(targetType));
        }
        return TypeTestResult.NOT_FOUND;
    }

    public boolean isImplicitlyCastable(BType actual, BType target) {
        /* The word Builtin refers for Compiler known types. */

        BType targetType = getReferredType(target);
        BType actualType = getReferredType(actual);
        BType newTargetType = targetType;
        int targetTypeTag = targetType.tag;
        if ((targetTypeTag == TypeTags.UNION || targetTypeTag == TypeTags.FINITE) && isValueType(actualType)) {
            newTargetType = symTable.anyType;   // TODO : Check for correctness.
        } else if (targetTypeTag == TypeTags.INTERSECTION) {
            newTargetType = ((BIntersectionType) targetType).effectiveType;
        }

        TypeTestResult result = isBuiltInTypeWidenPossible(actualType, newTargetType);
        if (result != TypeTestResult.NOT_FOUND) {
            return result == TypeTestResult.TRUE;
        }

        if (isValueType(targetType) &&
                (actualType.tag == TypeTags.FINITE ||
                        (actualType.tag == TypeTags.UNION && ((BUnionType) actualType).getMemberTypes().stream()
                                .anyMatch(type -> type.tag == TypeTags.FINITE && isAssignable(type, targetType))))) {
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

    public boolean isTypeCastable(BLangExpression expr, BType source, BType target, SymbolEnv env) {
        BType sourceType = getReferredType(source);
        BType targetType = getReferredType(target);
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
                sourceType == targetType) {
            return true;
        }

        // Disallow casting away error, this forces user to handle the error via type-test, check, or checkpanic
        IntersectionContext intersectionContext = IntersectionContext.compilerInternalIntersectionTestContext();
        BType errorIntersection = getTypeIntersection(intersectionContext, sourceType, symTable.errorType, env);
        if (errorIntersection != symTable.semanticError &&
                getTypeIntersection(intersectionContext, symTable.errorType, targetType, env)
                        == symTable.semanticError) {
            return false;
        }

        if (isAssignable(sourceType, targetType) || isAssignable(targetType, sourceType)) {
            return true;
        }
        if (isNumericConversionPossible(expr, sourceType, targetType)) {
            return true;
        }
        if (sourceType.tag == TypeTags.ANY && targetType.tag == TypeTags.READONLY) {
            return true;
        }

        boolean validTypeCast = false;

        // Use instanceof to check for anydata and json.
        if (sourceType instanceof BUnionType) {
            if (getTypeForUnionTypeMembersAssignableToType((BUnionType) sourceType, targetType, env,
                    intersectionContext, new LinkedHashSet<>())
                    != symTable.semanticError) {
                // string|typedesc v1 = "hello world";
                // json|table<Foo> v2 = <json|table<Foo>> v1;
                validTypeCast = true;
            }
        }

        // Use instanceof to check for anydata and json.
        if (targetType instanceof BUnionType) {
            if (getTypeForUnionTypeMembersAssignableToType((BUnionType) targetType, sourceType, env,
                    intersectionContext, new LinkedHashSet<>())
                    != symTable.semanticError) {
                // string|int v1 = "hello world";
                // string|boolean v2 = <string|boolean> v1;
                validTypeCast = true;
            }
        }

        if (sourceType.tag == TypeTags.FINITE) {
            if (getTypeForFiniteTypeValuesAssignableToType((BFiniteType) sourceType, targetType)
                    != symTable.semanticError) {
                validTypeCast = true;
            }
        }

        if (targetType.tag == TypeTags.FINITE) {
            if (getTypeForFiniteTypeValuesAssignableToType((BFiniteType) targetType, sourceType)
                    != symTable.semanticError) {
                validTypeCast = true;
            }
        }

        if (validTypeCast) {
            if (isValueType(sourceType)) {
                setImplicitCastExpr(expr, sourceType, symTable.anyType);
            }
            return true;
        }

        return false;
    }

    boolean isNumericConversionPossible(BLangExpression expr, BType sourceType,
                                        BType targetType) {

        final boolean isSourceNumericType = isBasicNumericType(sourceType);
        final boolean isTargetNumericType = isBasicNumericType(targetType);
        if (isSourceNumericType && isTargetNumericType) {
            // We only reach here for different numeric types.
            // 2019R3 Spec defines numeric conversion between each type.
            return true;
        }
        if (targetType.tag == TypeTags.UNION) {
            HashSet<Integer> typeTags = new HashSet<>();
            for (BType bType : ((BUnionType) targetType).getMemberTypes()) {
                if (isBasicNumericType(bType)) {
                    typeTags.add(bType.tag);
                    if (typeTags.size() > 1) {
                        // Multiple Basic numeric types found in the union.
                        return false;
                    }
                }
            }
        }

        if (!isTargetNumericType && targetType.tag != TypeTags.UNION) {
            return false;
        }

        // Target type has at least one numeric type member.

        if (isSourceNumericType) {
            // i.e., a conversion from a numeric type to another numeric type in a union.
            // int|string u1 = <int|string> 1.0;
            // TODO : Fix me. This doesn't belong here.
            setImplicitCastExpr(expr, sourceType, symTable.anyType);
            return true;
        }

        // TODO : Do we need this? This doesn't belong here.
        switch (sourceType.tag) {
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
                // This
                return true;
            case TypeTags.UNION:
                for (BType memType : ((BUnionType) sourceType).getMemberTypes()) {
                    BType referredType = getReferredType(memType);
                    if (isBasicNumericType(referredType) ||
                            (referredType.tag == TypeTags.FINITE &&
                                    finiteTypeContainsNumericTypeValues((BFiniteType) referredType))) {
                        return true;
                    }
                }
                break;
            case TypeTags.FINITE:
                if (finiteTypeContainsNumericTypeValues((BFiniteType) sourceType)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean isAllErrorMembers(BUnionType actualType) {
        return actualType.getMemberTypes().stream().allMatch(t -> isAssignable(t, symTable.errorType));
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType targetType) {
        BType expType = getReferredType(targetType);
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

    public BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    public boolean checkListenerCompatibilityAtServiceDecl(BType type) {
        if (type.tag == TypeTags.UNION) {
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
        BType type = getReferredType(bType);
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
        switch (detailType.tag) {
            case TypeTags.TYPEREFDESC:
                return isValidErrorDetailType(((BTypeReferenceType) detailType).referredType);
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return isAssignable(detailType, symTable.detailType);
        }
        return false;
    }

    // private methods

    private boolean isSealedRecord(BType recordType) {
        return recordType.getKind() == TypeKind.RECORD && ((BRecordType) recordType).sealed;
    }

    private boolean isNullable(BType fieldType) {
        return fieldType.isNullable();
    }

    private class BSameTypeVisitor implements BTypeVisitor<BType, Boolean> {

        Set<TypePair> unresolvedTypes;

        TypeEqualityPredicate equality;

        BSameTypeVisitor(Set<TypePair> unresolvedTypes, TypeEqualityPredicate equality) {
            this.unresolvedTypes = unresolvedTypes;
            this.equality = equality;
        }

        @Override
        public Boolean visit(BType target, BType source) {
            BType t = getReferredType(target);
            BType s = getReferredType(source);
            if (t == s) {
                return true;
            }

            switch (t.tag) {
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                case TypeTags.STRING:
                case TypeTags.BOOLEAN:
                    return t.tag == s.tag
                            && ((TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s)) ||
                            (t.tag == TypeTags.TYPEREFDESC || s.tag == TypeTags.TYPEREFDESC));
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    return t.tag == s.tag && hasSameReadonlyFlag(s, t)
                            && (TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s));
                default:
                    return false;
            }
        }

        @Override
        public Boolean visit(BBuiltInRefType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BAnyType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BAnydataType t, BType s) {
            return t == s || t.tag == s.tag;
        }

        @Override
        public Boolean visit(BMapType t, BType s) {
            return s.tag == TypeTags.MAP && hasSameReadonlyFlag(s, t) &&
                    equality.test(((BMapType) s).constraint, t.constraint, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BFutureType t, BType s) {
            return s.tag == TypeTags.FUTURE &&
                    equality.test(((BFutureType) s).constraint, t.constraint, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BXMLType t, BType s) {
            return visit((BBuiltInRefType) t, s);
        }

        @Override
        public Boolean visit(BJSONType t, BType s) {
            return s.tag == TypeTags.JSON && hasSameReadonlyFlag(s, t);
        }

        @Override
        public Boolean visit(BArrayType t, BType s) {
            return s.tag == TypeTags.ARRAY && hasSameReadonlyFlag(s, t) && isSameArrayType(s, t, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BObjectType t, BType s) {
            return t == s || (s.tag == TypeTags.OBJECT && t.tsymbol.pkgID.equals(s.tsymbol.pkgID) &&
                    t.tsymbol.name.equals(s.tsymbol.name));
        }

        @Override
        public Boolean visit(BRecordType t, BType s) {
            if (t == s) {
                return true;
            }

            if (s.tag != TypeTags.RECORD || !hasSameReadonlyFlag(s, t)) {
                return false;
            }

            BRecordType source = (BRecordType) s;
            LinkedHashMap<String, BField> sFields = source.fields;
            LinkedHashMap<String, BField> tFields = t.fields;

            if (sFields.size() != tFields.size()) {
                return false;
            }

            for (BField sourceField : sFields.values()) {
                if (tFields.containsKey(sourceField.name.value)) {
                    BField targetField = tFields.get(sourceField.name.value);
                    if ((!Symbols.isFlagOn(targetField.symbol.flags, Flags.READONLY) ||
                            Symbols.isFlagOn(sourceField.symbol.flags, Flags.READONLY)) &&
                            hasSameOptionalFlag(sourceField.symbol, targetField.symbol) &&
                            equality.test(sourceField.type, targetField.type, new HashSet<>(this.unresolvedTypes))) {
                        continue;
                    }
                }
                return false;
            }
            return equality.test(source.restFieldType, t.restFieldType, new HashSet<>(this.unresolvedTypes));
        }

        private boolean hasSameOptionalFlag(BVarSymbol s, BVarSymbol t) {
            return ((s.flags & Flags.OPTIONAL) ^ (t.flags & Flags.OPTIONAL)) != Flags.OPTIONAL;
        }

        boolean hasSameReadonlyFlag(BType source, BType target) {
            return Symbols.isFlagOn(target.flags, Flags.READONLY) == Symbols.isFlagOn(source.flags, Flags.READONLY);
        }

        @Override
        public Boolean visit(BTupleType t, BType s) {
            List<BType> tTupleTypes = t.getTupleTypes();
            if (((!tTupleTypes.isEmpty() && checkAllTupleMembersBelongNoType(tTupleTypes)) ||
                    (t.restType != null && t.restType.tag == TypeTags.NONE)) &&
                    !(s.tag == TypeTags.ARRAY && ((BArrayType) s).state == BArrayState.OPEN)) {
                return true;
            }

            if (s.tag != TypeTags.TUPLE || !hasSameReadonlyFlag(s, t)) {
                return false;
            }

            BTupleType source = (BTupleType) s;
            List<BType> sTupleTypes = source.getTupleTypes();
            if (sTupleTypes.size() != tTupleTypes.size()) {
                return false;
            }

            BType sourceRestType = source.restType;
            BType targetRestType = t.restType;
            if ((sourceRestType == null || targetRestType == null) && sourceRestType != targetRestType) {
                return false;
            }

            for (int i = 0; i < sTupleTypes.size(); i++) {
                if (tTupleTypes.get(i) == symTable.noType) {
                    continue;
                }
                if (!equality.test(sTupleTypes.get(i), tTupleTypes.get(i),
                        new HashSet<>(this.unresolvedTypes))) {
                    return false;
                }
            }

            if (sourceRestType == null || targetRestType == symTable.noType) {
                return true;
            }

            return equality.test(sourceRestType, targetRestType, new HashSet<>(this.unresolvedTypes));
        }

        @Override
        public Boolean visit(BStreamType t, BType s) {
            if (s.tag != TypeTags.STREAM) {
                return false;
            }

            BStreamType source = (BStreamType) s;
            return equality.test(source.constraint, t.constraint, unresolvedTypes)
                    && equality.test(source.completionType, t.completionType, unresolvedTypes);
        }

        @Override
        public Boolean visit(BTableType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BInvokableType t, BType s) {
            return s.tag == TypeTags.INVOKABLE &&
                    checkFunctionTypeEquality((BInvokableType) s, t, this.unresolvedTypes, equality);
        }

        @Override
        public Boolean visit(BUnionType tUnionType, BType s) {
            if (s.tag != TypeTags.UNION || !hasSameReadonlyFlag(s, tUnionType)) {
                return false;
            }

            BUnionType sUnionType = (BUnionType) s;

            if (sUnionType.getMemberTypes().size()
                    != tUnionType.getMemberTypes().size()) {
                return false;
            }

            Set<BType> sourceTypes = new LinkedHashSet<>(sUnionType.getMemberTypes().size());
            Set<BType> targetTypes = new LinkedHashSet<>(tUnionType.getMemberTypes().size());

            if (sUnionType.isCyclic) {
                sourceTypes.add(sUnionType);
            }
            if (tUnionType.isCyclic) {
                targetTypes.add(tUnionType);
            }

            sourceTypes.addAll(sUnionType.getMemberTypes());
            targetTypes.addAll(tUnionType.getMemberTypes());

            boolean notSameType = sourceTypes
                    .stream()
                    .map(sT -> targetTypes
                            .stream()
                            .anyMatch(it -> equality.test(sT, it, new HashSet<>(this.unresolvedTypes))))
                    .anyMatch(foundSameType -> !foundSameType);
            return !notSameType;
        }

        @Override
        public Boolean visit(BIntersectionType tIntersectionType, BType s) {
            if (s.tag != TypeTags.INTERSECTION || !hasSameReadonlyFlag(s, tIntersectionType)) {
                return false;
            }

            BIntersectionType sIntersectionType = (BIntersectionType) s;

            if (sIntersectionType.getConstituentTypes().size() != tIntersectionType.getConstituentTypes().size()) {
                return false;
            }

            Set<BType> sourceTypes = new LinkedHashSet<>(sIntersectionType.getConstituentTypes());
            Set<BType> targetTypes = new LinkedHashSet<>(tIntersectionType.getConstituentTypes());

            for (BType sourceType : sourceTypes) {
                boolean foundSameType = false;

                for (BType targetType : targetTypes) {
                    if (equality.test(sourceType, targetType, new HashSet<>(this.unresolvedTypes))) {
                        foundSameType = true;
                        break;
                    }
                }

                if (!foundSameType) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Boolean visit(BErrorType t, BType s) {
            if (s.tag != TypeTags.ERROR) {
                return false;
            }
            BErrorType source = (BErrorType) s;

            if (!source.typeIdSet.equals(t.typeIdSet)) {
                return false;
            }

            if (source.detailType == t.detailType) {
                return true;
            }

            return equality.test(source.detailType, t.detailType, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BTypedescType t, BType s) {
            if (s.tag != TypeTags.TYPEDESC) {
                return false;
            }
            BTypedescType sType = ((BTypedescType) s);
            return equality.test(sType.constraint, t.constraint, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BFiniteType t, BType s) {
            return s == t;
        }

        @Override
        public Boolean visit(BParameterizedType t, BType s) {
            if (s.tag != TypeTags.PARAMETERIZED_TYPE) {
                return false;
            }

            BParameterizedType sType = (BParameterizedType) s;
            return sType.paramSymbol.equals(t.paramSymbol) &&
                    equality.test(sType.paramValueType, t.paramValueType, new HashSet<>());
        }

        public Boolean visit(BTypeReferenceType t, BType s) {
            BType constraint = s;
            if (s.tag == TypeTags.TYPEREFDESC) {
                constraint = getReferredType(((BTypeReferenceType) s).referredType);
            }
            BType target = getReferredType(t.referredType);
            return equality.test(target, constraint, new HashSet<>());
        }
    }

    @Deprecated
    public boolean isSameBIRShape(BType source, BType target) {
        return isSameBIRShape(source, target, new HashSet<>());
    }

    private boolean isSameBIRShape(BType source, BType target, Set<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(source, target);
        if (!unresolvedTypes.add(pair)) {
            return true;
        }

        BIRSameShapeVisitor birSameShapeVisitor = new BIRSameShapeVisitor(unresolvedTypes, this::isSameBIRShape);

        if (target.accept(birSameShapeVisitor, source)) {
            return true;
        }

        unresolvedTypes.remove(pair);
        return false;
    }

    @Deprecated
    private class BIRSameShapeVisitor extends BSameTypeVisitor {

        BIRSameShapeVisitor(Set<TypePair> unresolvedTypes, TypeEqualityPredicate equality) {
            super(unresolvedTypes, equality);
        }

        @Override
        public Boolean visit(BType target, BType source) {
            if (source.tag == TypeTags.TYPEREFDESC || target.tag == TypeTags.TYPEREFDESC) {
                if (source.tag != target.tag) {
                    return false;
                }

                BTypeReferenceType sourceRefType = (BTypeReferenceType) source;
                BTypeReferenceType targetRefType = (BTypeReferenceType) target;

                BTypeSymbol sourceTSymbol = sourceRefType.tsymbol;
                BTypeSymbol targetTSymbol = targetRefType.tsymbol;
                String sourcePkgId = CompilerUtils.getPackageIDStringWithMajorVersion(sourceTSymbol.pkgID);
                String targetPkgId = CompilerUtils.getPackageIDStringWithMajorVersion(targetTSymbol.pkgID);
                return sourcePkgId.equals(targetPkgId) && sourceTSymbol.name.equals(targetTSymbol.name);
            }

            BType t = getReferredType(target);
            BType s = getReferredType(source);
            if (t == s) {
                return true;
            }
            switch (t.tag) {
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                case TypeTags.STRING:
                case TypeTags.BOOLEAN:
                    return t.tag == s.tag
                            && ((TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s)) ||
                            (t.tag == TypeTags.TYPEREFDESC || s.tag == TypeTags.TYPEREFDESC));
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    return t.tag == s.tag && hasSameReadonlyFlag(s, t)
                            && (TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s));
                default:
                    break;
            }
            return false;

        }

        @Override
        public Boolean visit(BFiniteType t, BType s) {
            if (s.tag != TypeTags.FINITE) {
                return false;
            }

            Set<BLangExpression> sourceValueSpace = ((BFiniteType) s).getValueSpace();
            Set<BLangExpression> targetValueSpace = t.getValueSpace();

            if (sourceValueSpace.size() != targetValueSpace.size()) {
                return false;
            }

            return hasSameMembers(sourceValueSpace, targetValueSpace);
        }

        public Boolean visit(BTypeReferenceType t, BType s) {
            if (s.tag != TypeTags.TYPEREFDESC) {
                return false;
            }

            BTypeReferenceType sTypeRefType = (BTypeReferenceType) s;

            BTypeSymbol sourceTSymbol = sTypeRefType.tsymbol;
            BTypeSymbol targetTSymbol = t.tsymbol;
            String sourcePkgId = CompilerUtils.getPackageIDStringWithMajorVersion(sourceTSymbol.pkgID);
            String targetPkgId = CompilerUtils.getPackageIDStringWithMajorVersion(targetTSymbol.pkgID);
            return sourcePkgId.equals(targetPkgId) && sourceTSymbol.name.equals(targetTSymbol.name);
        }
    }

    private boolean hasSameMembers(Set<BLangExpression> sourceValueSpace, Set<BLangExpression> targetValueSpace) {
        Set<BLangExpression> setOne = new HashSet<>(sourceValueSpace);
        Set<BLangExpression> setTwo = new HashSet<>(targetValueSpace);

        Iterator<BLangExpression> setOneIterator = setOne.iterator();
        Iterator<BLangExpression> setTwoIterator = setTwo.iterator();

        while (setOneIterator.hasNext()) {
            BLangLiteral setOneMem = (BLangLiteral) setOneIterator.next();

            if (!setTwoIterator.hasNext()) {
                return false;
            }

            boolean hasEqualValue = false;
            while (setTwoIterator.hasNext()) {
                BLangLiteral setTwoMem = (BLangLiteral) setTwoIterator.next();
                if (setOneMem.value.equals(setTwoMem.value) && setOneMem.getBType() == setTwoMem.getBType()) {
                    hasEqualValue = true;
                    setOneIterator.remove();
                    setTwoIterator.remove();
                    break;
                }
            }

            if (!hasEqualValue) {
                return false;
            }
        }

        return !setTwoIterator.hasNext();
    }

    private class BOrderedTypeVisitor implements BTypeVisitor<BType, Boolean> {

        Set<TypePair> unresolvedTypes;

        BOrderedTypeVisitor(Set<TypePair> unresolvedTypes) {
            this.unresolvedTypes = unresolvedTypes;
        }

        @Override
        public Boolean visit(BType target, BType source) {
            int sourceTag = source.tag;
            int targetTag = target.tag;
            if (isSimpleBasicType(sourceTag) && isSimpleBasicType(targetTag)) {
                // If type T is ordered, then type T? Is also ordered.
                return (source == target) || sourceTag == TypeTags.NIL || targetTag == TypeTags.NIL ||
                        isIntOrStringType(sourceTag, targetTag);
            }
            if (sourceTag == TypeTags.FINITE) {
                return checkValueSpaceHasSameType(((BFiniteType) source), target);
            }
            return isSameOrderedType(target, source, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BArrayType target, BType source) {
            if (source.tag != TypeTags.ARRAY) {
                if (source.tag == TypeTags.TUPLE || source.tag == TypeTags.UNION) {
                    return isSameOrderedType(target, source);
                }
                return false;
            }
            return isSameOrderedType(target.eType, ((BArrayType) source).eType, unresolvedTypes);
        }

        @Override
        public Boolean visit(BTupleType target, BType source) {
            if (source.tag == TypeTags.UNION) {
                return isSameOrderedType(target, source);
            }
            if (source.tag != TypeTags.TUPLE && source.tag != TypeTags.ARRAY) {
                return false;
            }
            List<BType> targetTupleTypes = target.getTupleTypes();
            BType targetRestType = target.restType;

            if (source.tag == TypeTags.ARRAY) {
                // Check whether the element type of the source array has same ordered type with each member type in
                // target tuple type.
                BType eType = ((BArrayType) source).eType;
                for (BType memberType : targetTupleTypes) {
                    if (!isSameOrderedType(eType, memberType, this.unresolvedTypes)) {
                        return false;
                    }
                }
                if (targetRestType == null) {
                    return true;
                }
                return isSameOrderedType(targetRestType, eType, this.unresolvedTypes);
            }

            BTupleType sourceT = (BTupleType) source;
            List<BType> sourceTupleTypes = sourceT.getTupleTypes();

            BType sourceRestType = sourceT.restType;

            int sourceTupleCount = sourceTupleTypes.size();
            int targetTupleCount = targetTupleTypes.size();

            int len = Math.min(sourceTupleCount, targetTupleCount);
            for (int i = 0; i < len; i++) {
                // Check whether the corresponding member types are same ordered type.
                if (!isSameOrderedType(sourceTupleTypes.get(i), targetTupleTypes.get(i),
                        this.unresolvedTypes)) {
                    return false;
                }
            }

            if (sourceTupleCount == targetTupleCount) {
                if (sourceRestType == null || targetRestType == null) {
                    return true;
                }
                return isSameOrderedType(sourceRestType, targetRestType, this.unresolvedTypes);
            } else if (sourceTupleCount > targetTupleCount) {
                // Source tuple has higher number of member types.
                // Check whether the excess member types can be narrowed to an ordered rest type in source tuple.
                // Ex. source tuple -> [string, (), float, int, byte]
                //     target tuple -> [string, (), float]
                //     here, source tuple can be represented as [string, (), float, int...]
                //     since [string, (), float] & [string, (), float, int...] are individually order types and
                //     [string, (), float, int...] can be taken as the ordered type which the static type of both
                //     operands belong.
                if (!hasCommonOrderedTypeForTuples(sourceTupleTypes, targetTupleCount + 1)) {
                    return false;
                }
                return checkSameOrderedTypeInTuples(sourceT, sourceTupleCount, targetTupleCount, sourceRestType,
                        targetRestType);
            } else {
                // Target tuple has higher number of member types.
                if (!hasCommonOrderedTypeForTuples(targetTupleTypes, sourceTupleCount + 1)) {
                    return false;
                }
                return checkSameOrderedTypeInTuples(target, targetTupleCount, sourceTupleCount, targetRestType,
                        sourceRestType);
            }
        }

        private boolean hasCommonOrderedTypeForTuples(List<BType> typeList, int startIndex) {
            BType baseType = typeList.get(startIndex - 1);
            for (int i = startIndex; i < typeList.size(); i++) {
                if (isNil(baseType)) {
                    baseType = typeList.get(i);
                    continue;
                }
                if (!isSameOrderedType(baseType, typeList.get(i), this.unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private boolean checkSameOrderedTypeInTuples(BTupleType source, int sourceTupleCount,
                                                     int targetTupleCount,
                                                     BType sourceRestType, BType targetRestType) {
            if (targetRestType == null) {
                return true;
            }
            for (int i = targetTupleCount; i < sourceTupleCount; i++) {
                if (!isSameOrderedType(source.getTupleTypes().get(i), targetRestType, this.unresolvedTypes)) {
                    return false;
                }
            }
            if (sourceRestType == null) {
                return true;
            }
            return isSameOrderedType(sourceRestType, targetRestType, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BUnionType target, BType source) {
            if (source.tag != TypeTags.UNION || !hasSameReadonlyFlag(source, target)) {
                return checkUnionHasSameType(target, source);
            }

            BUnionType sUnionType = (BUnionType) source;
            LinkedHashSet<BType> sourceTypes = sUnionType.getMemberTypes();
            LinkedHashSet<BType> targetTypes = target.getMemberTypes();

            if (checkUnionHasAllFiniteOrNilMembers(sourceTypes) &&
                    checkUnionHasAllFiniteOrNilMembers(targetTypes)) {
                BType type = target.getMemberTypes().iterator().next();
                return checkValueSpaceHasSameType(((BFiniteType) getReferredType(type)),
                        sUnionType.getMemberTypes().iterator().next());
            }

            return checkSameOrderedTypesInUnionMembers(sourceTypes, targetTypes);
        }

        private boolean checkSameOrderedTypesInUnionMembers(LinkedHashSet<BType> sourceTypes,
                                                            LinkedHashSet<BType> targetTypes) {

            for (BType sourceT : sourceTypes) {
                boolean foundSameOrderedType = false;
                if (isNil(sourceT)) {
                    continue;
                }
                for (BType targetT : targetTypes) {
                    if (isNil(targetT)) {
                        foundSameOrderedType = true;
                        continue;
                    }
                    if (isSameOrderedType(targetT, sourceT, this.unresolvedTypes)) {
                        foundSameOrderedType = true;
                    } else {
                        return false;
                    }
                }
                if (!foundSameOrderedType) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visit(BFiniteType t, BType s) {
            return checkValueSpaceHasSameType(t, s);
        }

        private boolean hasSameReadonlyFlag(BType source, BType target) {
            return Symbols.isFlagOn(target.flags, Flags.READONLY) == Symbols.isFlagOn(source.flags, Flags.READONLY);
        }

        @Override
        public Boolean visit(BBuiltInRefType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BAnyType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BAnydataType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BMapType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BFutureType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BXMLType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BJSONType t, BType s) {
            return false;
        }


        @Override
        public Boolean visit(BObjectType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BRecordType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BStreamType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BTableType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BInvokableType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BIntersectionType tIntersectionType, BType s) {
            return this.visit(getEffectiveTypeForIntersection(tIntersectionType), s);
        }

        @Override
        public Boolean visit(BErrorType t, BType s) {
            return false;
        }

        @Override
        public Boolean visit(BTypedescType t, BType s) {
            return false;
        }

        public Boolean visit(BTypeReferenceType t, BType s) {
            return this.visit(getReferredType(t), t);
        }

        @Override
        public Boolean visit(BParameterizedType t, BType s) {
            return false;
        }
    }

    private boolean isNil(BType type) {
        // Currently, type reference for `null` literal is taken as Finite type and type reference for `()` literal
        // taken as Nil type.
        BType referredType = getReferredType(type);
        TypeKind referredTypeKind = referredType.getKind();
        if (referredTypeKind == TypeKind.NIL) {
            return true;
        } else if (referredTypeKind == TypeKind.FINITE) {
            Set<BLangExpression> valueSpace = ((BFiniteType) referredType).getValueSpace();
            return valueSpace.size() == 1 && valueSpace.iterator().next().getBType().getKind() == TypeKind.NIL;
        }
        return false;
    }

    private boolean checkUnionHasSameType(BUnionType unionType, BType baseType) {
        LinkedHashSet<BType> memberTypes = unionType.getMemberTypes();
        for (BType type : memberTypes) {
            type = getReferredType(type);
            if (type.tag == TypeTags.FINITE) {
                for (BLangExpression expr : ((BFiniteType) type).getValueSpace()) {
                    if (!isSameOrderedType(expr.getBType(), baseType)) {
                        return false;
                    }
                }
//            } else if (type.tag == TypeTags.UNION) {
//                if (!checkUnionHasSameType((BUnionType) type, baseType)) {
//                    return false;
//                }
            } else if (type.tag == TypeTags.TUPLE || type.tag == TypeTags.ARRAY) {
                if (!isSameOrderedType(type, baseType)) {
                    return false;
                }
            } else if (type.tag == TypeTags.INTERSECTION) {
                if (!isSameOrderedType(getEffectiveTypeForIntersection(type), baseType)) {
                    return false;
                }
            } else if (isSimpleBasicType(type.tag)) {
                if (!isSameOrderedType(type, baseType) && !isNil(type)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkValueSpaceHasSameType(BFiniteType finiteType, BType type) {
        BType baseType = getReferredType(type);
        if (baseType.tag == TypeTags.FINITE) {
            BType baseExprType = finiteType.getValueSpace().iterator().next().getBType();
            return checkValueSpaceHasSameType(((BFiniteType) baseType), baseExprType);
        }
        boolean isValueSpaceSameType = false;
        for (BLangExpression expr : finiteType.getValueSpace()) {
            isValueSpaceSameType = isSameOrderedType(expr.getBType(), baseType);
            if (!isValueSpaceSameType) {
                break;
            }
        }
        return isValueSpaceSameType;
    }

    private boolean checkUnionHasAllFiniteOrNilMembers(LinkedHashSet<BType> memberTypes) {
        for (BType bType : memberTypes) {
            BType type = getReferredType(bType);
            if (type.tag != TypeTags.FINITE && !isNil(type)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFieldEquivalency(BRecordType lhsType, BRecordType rhsType, Set<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = new LinkedHashMap<>(rhsType.fields);

        // Check if the RHS record has corresponding fields to those of the LHS record.
        for (BField lhsField : lhsType.fields.values()) {
            BField rhsField = rhsFields.get(lhsField.name.value);

            // If LHS field is required, there should be a corresponding RHS field
            // If LHS field is never typed, RHS rest field type should include never type
            if (rhsField == null) {
                if (!Symbols.isOptional(lhsField.symbol) || isInvalidNeverField(lhsField, rhsType)) {
                    return false;
                }
                continue;
            }
            if (hasIncompatibleReadOnlyFlags(lhsField.symbol.flags, rhsField.symbol.flags)) {
                return false;
            }

            // If LHS field is required, so should the RHS field
            if (!Symbols.isOptional(lhsField.symbol) && Symbols.isOptional(rhsField.symbol)) {
                return false;
            }

            // The corresponding RHS field should be assignable to the LHS field.
            if (!isAssignable(rhsField.type, lhsField.type, unresolvedTypes)) {
                return false;
            }

            rhsFields.remove(lhsField.name.value);
        }

        if (lhsType.sealed) {
            for (BField field : rhsFields.values()) {
                if (!isNeverTypeOrStructureTypeWithARequiredNeverMember(field.type)) {
                    return false;
                }
            }
            return true;
        }

        // If there are any remaining RHS fields, the types of those should be assignable to the rest field type of
        // the LHS record.
        BType lhsRestFieldType = lhsType.restFieldType;
        for (BField field : rhsFields.values()) {
            if (!isAssignable(field.type, lhsRestFieldType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInvalidNeverField(BField lhsField, BRecordType rhsType) {
        if (lhsField.type.tag != NEVER || rhsType.sealed) {
            return false;
        }
        switch (rhsType.restFieldType.tag) {
            case TypeTags.UNION:
                for (BType member : ((BUnionType) rhsType.restFieldType).getOriginalMemberTypes()) {
                    if (member.tag == NEVER) {
                        return false;
                    }
                }
                return true;
            case NEVER:
                return false;
            default:
                return true;
        }
    }

    private Optional<BAttachedFunction> getMatchingInvokableType(List<BAttachedFunction> rhsFuncList,
                                                                 BAttachedFunction lhsFunc,
                                                                 Set<TypePair> unresolvedTypes) {
        Optional<BAttachedFunction> matchingFunction = rhsFuncList.stream()
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> isFunctionTypeAssignable(rhsFunc.type, lhsFunc.type, unresolvedTypes))
                .findFirst();
        
        if (matchingFunction.isEmpty()) {
            return matchingFunction;
        }
        // For resource function match, we need to check whether lhs function resource path type belongs to 
        // rhs function resource path type
        BAttachedFunction matchingFunc = matchingFunction.get();
        // Todo: We could include this logic in `isFunctionTypeAssignable` if we have `resourcePathType` information in
        // `BInvokableType` issue #37502
        boolean lhsFuncIsResource = Symbols.isResource(lhsFunc.symbol);
        boolean matchingFuncIsResource = Symbols.isResource(matchingFunc.symbol);

        if (!lhsFuncIsResource && !matchingFuncIsResource) {
            return matchingFunction;
        }

        if ((lhsFuncIsResource && !matchingFuncIsResource) || (matchingFuncIsResource && !lhsFuncIsResource)) {
            return Optional.empty();
        }

        List<BResourcePathSegmentSymbol> lhsFuncPathTypes = ((BResourceFunction) lhsFunc).pathSegmentSymbols;
        List<BResourcePathSegmentSymbol> rhsFuncPathTypes = ((BResourceFunction) matchingFunc).pathSegmentSymbols;

        int lhsFuncResourcePathTypesSize = lhsFuncPathTypes.size();
        if (lhsFuncResourcePathTypesSize != rhsFuncPathTypes.size()) {
            return Optional.empty();
        }
        
        for (int i = 0; i < lhsFuncResourcePathTypesSize; i++) {
            if (!isAssignable(lhsFuncPathTypes.get(i).type, rhsFuncPathTypes.get(i).type)) {
                return Optional.empty();
            }
        }
        
        return matchingFunction;
    }

    private boolean isInSameVisibilityRegion(BSymbol lhsSym, BSymbol rhsSym) {
        if (Symbols.isPrivate(lhsSym)) {
            return Symbols.isPrivate(rhsSym) && lhsSym.pkgID.equals(rhsSym.pkgID)
                    && lhsSym.owner.name.equals(rhsSym.owner.name);
        } else if (Symbols.isPublic(lhsSym)) {
            return Symbols.isPublic(rhsSym);
        }
        return !Symbols.isPrivate(rhsSym) && !Symbols.isPublic(rhsSym) && lhsSym.pkgID.equals(rhsSym.pkgID);
    }

    private boolean isAssignableToUnionType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }

        if (isJsonAnydataOrUserDefinedUnion(source) && ((BUnionType) source).isCyclic) {
            // add cyclic source to target pair to avoid recursive calls
            unresolvedTypes.add(pair);
        }

        Set<BType> sourceTypes = new LinkedHashSet<>();
        Set<BType> targetTypes = new LinkedHashSet<>();

        if (isJsonAnydataOrUserDefinedUnion(source)) {
            sourceTypes.addAll(getEffectiveMemberTypes((BUnionType) source));
        } else {
            sourceTypes.add(source);
        }

        boolean targetIsAUnion = false;
        if (target.tag == TypeTags.UNION) {
            targetIsAUnion = true;
            targetTypes.addAll(getEffectiveMemberTypes((BUnionType) target));
        } else {
            targetTypes.add(target);
        }

        // check if all the value types are assignable between two unions
        var sourceIterator = sourceTypes.iterator();
        while (sourceIterator.hasNext()) {
            BType sMember = sourceIterator.next();
            if (sMember.tag == TypeTags.NEVER) {
                sourceIterator.remove();
                continue;
            }
            if (sMember.tag == TypeTags.FINITE && isAssignable(sMember, target, unresolvedTypes)) {
                sourceIterator.remove();
                continue;
            }
            if (sMember.tag == TypeTags.XML &&
                    isAssignableToUnionType(expandedXMLBuiltinSubtypes, target, unresolvedTypes)) {
                sourceIterator.remove();
                continue;
            }

            if (!isValueType(sMember)) {
                if (!targetIsAUnion) {
                    continue;
                }
                BUnionType targetUnion = (BUnionType) target;
                // prevent cyclic unions being compared as individual items
                if (sMember instanceof BUnionType) {
                    BUnionType sUnion = (BUnionType) sMember;
                    if (sUnion.isCyclic && targetUnion.isCyclic) {
                        unresolvedTypes.add(new TypePair(sUnion, targetUnion));
                         if (isAssignable(sUnion, targetUnion, unresolvedTypes)) {
                             sourceIterator.remove();
                             continue;
                         }
                    }
                    if (sMember.tag == TypeTags.JSON && isAssignable(sUnion, targetUnion, unresolvedTypes)) {
                        sourceIterator.remove();
                        continue;
                    }
                }
                // readonly can match to a union similar to any|error
                if (sMember.tag == TypeTags.READONLY && isAssignable(symTable.anyAndReadonlyOrError, targetUnion)) {
                    sourceIterator.remove();
                    continue;
                }
                continue;
            }

            boolean sourceTypeIsNotAssignableToAnyTargetType = true;
            var targetIterator = targetTypes.iterator();
            while (targetIterator.hasNext()) {
                BType t = targetIterator.next();
                if (isAssignable(sMember, t, unresolvedTypes)) {
                    sourceIterator.remove();
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }
            }
            if (sourceTypeIsNotAssignableToAnyTargetType) {
                unresolvedTypes.remove(pair);
                return false;
            }
        }

        // check the structural values for similarity
        sourceIterator = sourceTypes.iterator();
        while (sourceIterator.hasNext()) {
            BType sourceMember = sourceIterator.next();
            boolean sourceTypeIsNotAssignableToAnyTargetType = true;
            var targetIterator = targetTypes.iterator();

            boolean selfReferencedSource = (sourceMember != source) &&
                    isSelfReferencedStructuredType(source, sourceMember);

            while (targetIterator.hasNext()) {
                BType targetMember = targetIterator.next();

                boolean selfReferencedTarget = isSelfReferencedStructuredType(target, targetMember);
                if (selfReferencedTarget && selfReferencedSource && (sourceMember.tag == targetMember.tag)) {
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }

                if (isAssignable(sourceMember, targetMember, unresolvedTypes)) {
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }
            }
            if (sourceTypeIsNotAssignableToAnyTargetType) {
                unresolvedTypes.remove(pair);
                return false;
            }
        }

        unresolvedTypes.add(pair);
        return true;
    }

    private boolean isJsonAnydataOrUserDefinedUnion(BType type) {
        int tag = type.tag;
        return tag == TypeTags.UNION || tag == TypeTags.JSON || tag == TypeTags.ANYDATA;
    }

    public boolean isSelfReferencedStructuredType(BType source, BType s) {
        if (source == s) {
            return true;
        }

        s = getReferredType(s);
        if (s.tag == TypeTags.ARRAY) {
            return isSelfReferencedStructuredType(source, ((BArrayType) s).eType);
        }
        if (s.tag == TypeTags.MAP) {
            return isSelfReferencedStructuredType(source, ((BMapType) s).constraint);
        }
        if (s.tag == TypeTags.TABLE) {
            return isSelfReferencedStructuredType(source, ((BTableType) s).constraint);
        }
        return false;
    }

    public BType updateSelfReferencedWithNewType(BType source, BType s, BType target) {
        if (s.tag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) s;
            if (arrayType.eType == source) {
                return new BArrayType(target, arrayType.tsymbol, arrayType.size,
                        arrayType.state, arrayType.flags);
            }
        }
        if (s.tag == TypeTags.MAP) {
            BMapType mapType = (BMapType) s;
            if (mapType.constraint == source) {
                return new BMapType(mapType.tag, target, mapType.tsymbol, mapType.flags);
            }
        }
        if (s.tag == TypeTags.TABLE) {
            BTableType tableType = (BTableType) s;
            if (tableType.constraint == source) {
                return new BTableType(tableType.tag, target, tableType.tsymbol,
                        tableType.flags);
            } else if (tableType.constraint instanceof BMapType) {
                return updateSelfReferencedWithNewType(source, (BMapType) tableType.constraint, target);
            }
        }
        return s;
    }

    public static void fixSelfReferencingSameUnion(BType originalMemberType, BUnionType origUnionType,
                                                    BType immutableMemberType, BUnionType newImmutableUnion,
                                                    LinkedHashSet<BType> readOnlyMemTypes) {
        boolean sameMember = originalMemberType == immutableMemberType;
        if (originalMemberType.tag == TypeTags.ARRAY) {
            var arrayType = (BArrayType) originalMemberType;
            if (origUnionType == arrayType.eType) {
                if (sameMember) {
                    BArrayType newArrayType = new BArrayType(newImmutableUnion, arrayType.tsymbol, arrayType.size,
                            arrayType.state, arrayType.flags);
                    readOnlyMemTypes.add(newArrayType);
                } else {
                    ((BArrayType) immutableMemberType).eType = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else if (originalMemberType.tag == TypeTags.MAP) {
            var mapType = (BMapType) originalMemberType;
            if (origUnionType == mapType.constraint) {
                if (sameMember) {
                    BMapType newMapType = new BMapType(mapType.tag, newImmutableUnion, mapType.tsymbol, mapType.flags);
                    readOnlyMemTypes.add(newMapType);
                } else {
                    ((BMapType) immutableMemberType).constraint = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else if (originalMemberType.tag == TypeTags.TABLE) {
            var tableType = (BTableType) originalMemberType;
            if (origUnionType == tableType.constraint) {
                if (sameMember) {
                    BTableType newTableType = new BTableType(tableType.tag, newImmutableUnion, tableType.tsymbol,
                            tableType.flags);
                    readOnlyMemTypes.add(newTableType);
                } else {
                    ((BTableType) immutableMemberType).constraint = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
                return;
            }

            var immutableConstraint = ((BTableType) immutableMemberType).constraint;
            if (tableType.constraint.tag == TypeTags.MAP) {
                sameMember = tableType.constraint == immutableConstraint;
                var mapType = (BMapType) tableType.constraint;
                if (origUnionType == mapType.constraint) {
                    if (sameMember) {
                        BMapType newMapType = new BMapType(mapType.tag, newImmutableUnion, mapType.tsymbol,
                                mapType.flags);
                        ((BTableType) immutableMemberType).constraint = newMapType;
                    } else {
                        ((BTableType) immutableMemberType).constraint = newImmutableUnion;
                    }
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else {
            readOnlyMemTypes.add(immutableMemberType);
        }
    }

    private Set<BType> getEffectiveMemberTypes(BUnionType unionType) {
        Set<BType> memTypes = new LinkedHashSet<>();

        for (BType memberType : unionType.getMemberTypes()) {
            switch (memberType.tag) {
                case TypeTags.INTERSECTION:
                    BType effectiveType = ((BIntersectionType) memberType).effectiveType;
                    BType refType = getReferredType(effectiveType);
                    if (refType.tag == TypeTags.UNION) {
                        memTypes.addAll(getEffectiveMemberTypes((BUnionType) refType));
                        continue;
                    }
                    if (refType.tag == TypeTags.INTERSECTION) {
                        memTypes.addAll(
                                getEffectiveMemberTypes((BUnionType) ((BIntersectionType) refType).effectiveType));
                        continue;
                    }
                    memTypes.add(effectiveType);
                    break;
                case TypeTags.UNION:
                    memTypes.addAll(getEffectiveMemberTypes((BUnionType) memberType));
                    break;
                case TypeTags.TYPEREFDESC:
                    BType constraint = getReferredType(memberType);
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

    private boolean isFiniteTypeAssignable(BFiniteType finiteType, BType targetType, Set<TypePair> unresolvedTypes) {
        BType expType = getReferredType(targetType);
        if (expType.tag == TypeTags.FINITE) {
            for (BLangExpression expression : finiteType.getValueSpace()) {
                ((BLangLiteral) expression).isFiniteContext = true;
                if (!isAssignableToFiniteType(expType, (BLangLiteral) expression)) {
                    return false;
                }
            }
            return true;
        }

        if (targetType.tag == TypeTags.UNION) {
            List<BType> unionMemberTypes = getAllTypes(targetType, true);
            for (BLangExpression valueExpr : finiteType.getValueSpace()) {
                ((BLangLiteral) valueExpr).isFiniteContext = true;
                if (unionMemberTypes.stream()
                        .noneMatch(targetMemType ->
                                getReferredType(targetMemType).tag == TypeTags.FINITE ?
                                        isAssignableToFiniteType(targetMemType, (BLangLiteral) valueExpr) :
                                        isAssignable(valueExpr.getBType(), targetMemType, unresolvedTypes) ||
                                                isLiteralCompatibleWithBuiltinTypeWithSubTypes(
                                                        (BLangLiteral) valueExpr, targetMemType))) {
                    return false;
                }
            }
            return true;
        }

        for (BLangExpression expression : finiteType.getValueSpace()) {
            if (!isLiteralCompatibleWithBuiltinTypeWithSubTypes((BLangLiteral) expression, targetType) &&
                    !isAssignable(expression.getBType(), expType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    boolean isAssignableToFiniteType(BType type, BLangLiteral literalExpr) {
        type = getReferredType(type);
        if (type.tag != TypeTags.FINITE) {
            return false;
        }

        BFiniteType expType = (BFiniteType) type;
        return expType.getValueSpace().stream().anyMatch(memberLiteral -> {
            if (((BLangLiteral) memberLiteral).value == null) {
                return literalExpr.value == null;
            }

            // If the literal which needs to be tested is from finite type and the type of the any member literal
            // is not the same type, the literal cannot be assignable to finite type.
            if (literalExpr.isFiniteContext && memberLiteral.getBType().tag != literalExpr.getBType().tag) {
                return false;
            }
            // Check whether the literal that needs to be tested is assignable to any of the member literal in the
            // value space.
            return checkLiteralAssignabilityBasedOnType((BLangLiteral) memberLiteral, literalExpr);
        });
    }

    /**
     * Method to check the literal assignability based on the types of the literals. For numeric literals the
     * assignability depends on the equivalency of the literals. If the candidate literal could either be a simple
     * literal or a constant. In case of a constant, it is assignable to the base literal if and only if both
     * literals have same type and equivalent values.
     *
     * @param baseLiteral      Literal based on which we check the assignability.
     * @param candidateLiteral Literal to be tested whether it is assignable to the base literal or not.
     * @return true if assignable; false otherwise.
     */
    boolean checkLiteralAssignabilityBasedOnType(BLangLiteral baseLiteral, BLangLiteral candidateLiteral) {
        // Different literal kinds.
        if (baseLiteral.getKind() != candidateLiteral.getKind()) {
            return false;
        }
        Object baseValue = baseLiteral.value;
        Object candidateValue = candidateLiteral.value;
        int candidateTypeTag = candidateLiteral.getBType().tag;

        // Numeric literal assignability is based on assignable type and numeric equivalency of values.
        // If the base numeric literal is,
        // (1) byte: we can assign byte or a int simple literal (Not an int constant) with the same value.
        // (2) int: we can assign int literal or int constants with the same value.
        // (3) float: we can assign int simple literal(Not an int constant) or a float literal/constant with same value.
        // (4) decimal: we can assign int simple literal or float simple literal (Not int/float constants) or decimal
        // with the same value.
        switch (baseLiteral.getBType().tag) {
            case TypeTags.BYTE:
                if (candidateTypeTag == TypeTags.BYTE || (candidateTypeTag == TypeTags.INT &&
                        !candidateLiteral.isConstant && isByteLiteralValue((Long) candidateValue))) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.INT:
                if (candidateTypeTag == TypeTags.INT) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED32_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned32LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED16_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned16LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED8_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned8LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED32_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned32LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED16_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned16LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED8_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned8LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.FLOAT:
                String baseValueStr = String.valueOf(baseValue);
                String originalValue = baseLiteral.originalValue != null ? baseLiteral.originalValue : baseValueStr;
                if (NumericLiteralSupport.isDecimalDiscriminated(originalValue)) {
                    return false;
                }
                double baseDoubleVal = Double.parseDouble(baseValueStr);
                double candidateDoubleVal;
                if (candidateTypeTag == TypeTags.INT && !candidateLiteral.isConstant) {
                    if (candidateLiteral.value instanceof Double) {
                        // Out of range value for int but in range for float
                        candidateDoubleVal = Double.parseDouble(String.valueOf(candidateValue));
                        return baseDoubleVal == candidateDoubleVal;
                    } else {
                        candidateDoubleVal = ((Long) candidateValue).doubleValue();
                        return baseDoubleVal == candidateDoubleVal;
                    }
                } else if (candidateTypeTag == TypeTags.FLOAT) {
                    candidateDoubleVal = Double.parseDouble(String.valueOf(candidateValue));
                    return baseDoubleVal == candidateDoubleVal;
                }
                break;
            case TypeTags.DECIMAL:
                BigDecimal baseDecimalVal = NumericLiteralSupport.parseBigDecimal(baseValue);
                BigDecimal candidateDecimalVal;
                if (candidateTypeTag == TypeTags.INT && !candidateLiteral.isConstant) {
                    if (candidateLiteral.value instanceof String) {
                        // out of range value for float but in range for decimal
                        candidateDecimalVal = NumericLiteralSupport.parseBigDecimal(candidateValue);
                        return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                    } else if (candidateLiteral.value instanceof Double) {
                        // out of range value for int in range for decimal and float
                        candidateDecimalVal = new BigDecimal((Double) candidateValue, MathContext.DECIMAL128);
                        return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                    } else {
                        candidateDecimalVal = new BigDecimal((long) candidateValue, MathContext.DECIMAL128);
                        return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                    }
                } else if (candidateTypeTag == TypeTags.FLOAT && !candidateLiteral.isConstant ||
                        candidateTypeTag == TypeTags.DECIMAL) {
                    if (NumericLiteralSupport.isFloatDiscriminated(String.valueOf(candidateValue))) {
                        return false;
                    }
                    candidateDecimalVal = NumericLiteralSupport.parseBigDecimal(candidateValue);
                    return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                }
                break;
            default:
                // Non-numeric literal kind.
                return baseValue.equals(candidateValue);
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
     * @param finiteType the finite type
     * @param targetType the target type
     * @return a new finite type if at least one value in the value space of the specified finiteType is
     * assignable to targetType (the same if all are assignable), else semanticError
     */
    BType getTypeForFiniteTypeValuesAssignableToType(BFiniteType finiteType, BType targetType) {
        // finiteType - type Foo "foo";
        // targetType - type FooBar "foo"|"bar";
        if (isAssignable(finiteType, targetType)) {
            return finiteType;
        }

        // Identify all the values from the value space of the finite type that are assignable to the target type.
        // e.g., finiteType - type Foo "foo"|1 ;
        Set<BLangExpression> matchingValues = new HashSet<>();
        for (BLangExpression expr : finiteType.getValueSpace()) {
            // case I: targetType - string ("foo" is assignable to string)
            BLangLiteral literal = (BLangLiteral) expr;
            if (isAssignable(expr.getBType(), targetType) ||
                    // case II: targetType - type Bar "foo"|"baz" ; ("foo" is assignable to Bar)
                    isAssignableToFiniteType(targetType, literal) ||
                    // type FooVal "foo";
                    // case III:  targetType - boolean|FooVal ("foo" is assignable to FooVal)
                    isAssignableToFiniteTypeMemberInUnion(literal, targetType) ||
                    // case IV:  targetType - int:Signed16 (1 is assignable to int:Signed16)
                    isAssignableToBuiltinSubtypeInTargetType(literal, targetType)) {
                matchingValues.add(expr);
            }
        }

        if (matchingValues.isEmpty()) {
            return symTable.semanticError;
        }

        // Create a new finite type representing the assignable values.
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, finiteType.tsymbol.flags,
                names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                finiteType.tsymbol.pkgID, null,
                finiteType.tsymbol.owner, finiteType.tsymbol.pos,
                VIRTUAL);
        BFiniteType intersectingFiniteType = new BFiniteType(finiteTypeSymbol, matchingValues);
        finiteTypeSymbol.type = intersectingFiniteType;
        return intersectingFiniteType;
    }

    private boolean isAssignableToFiniteTypeMemberInUnion(BLangLiteral expr, BType targetType) {
        if (targetType.tag != TypeTags.UNION) {
            return false;
        }

        for (BType memType : ((BUnionType) targetType).getMemberTypes()) {
            if (isAssignableToFiniteType(memType, expr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAssignableToBuiltinSubtypeInTargetType(BLangLiteral literal, BType targetType) {
        if (targetType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) targetType).getMemberTypes()) {
                if (isLiteralCompatibleWithBuiltinTypeWithSubTypes(literal, memberType)) {
                    return true;
                }
            }
        }

        return isLiteralCompatibleWithBuiltinTypeWithSubTypes(literal, targetType);
    }

    public boolean isLiteralCompatibleWithBuiltinTypeWithSubTypes(BLangLiteral literal, BType targetType) {
        BType literalType = literal.getBType();
        if (literalType.tag == targetType.tag) {
            return true;
        }

        switch (targetType.tag) {
            case TypeTags.BYTE:
                return literalType.tag == TypeTags.INT && isByteLiteralValue((Long) literal.value);
            case TypeTags.SIGNED32_INT:
                return literalType.tag == TypeTags.INT && isSigned32LiteralValue((Long) literal.value);
            case TypeTags.SIGNED16_INT:
                return literalType.tag == TypeTags.INT && isSigned16LiteralValue((Long) literal.value);
            case TypeTags.SIGNED8_INT:
                return literalType.tag == TypeTags.INT && isSigned8LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED32_INT:
                return literalType.tag == TypeTags.INT && isUnsigned32LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED16_INT:
                return literalType.tag == TypeTags.INT && isUnsigned16LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED8_INT:
                return literalType.tag == TypeTags.INT && isUnsigned8LiteralValue((Long) literal.value);
            case TypeTags.CHAR_STRING:
                return literalType.tag == TypeTags.STRING && isCharLiteralValue((String) literal.value);
            case TypeTags.TYPEREFDESC:
                return isLiteralCompatibleWithBuiltinTypeWithSubTypes(literal, getReferredType(targetType));
            default:
                return false;
        }
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
            return BUnionType.create(null, new LinkedHashSet<>(intersection));
        }
    }

    boolean validEqualityIntersectionExists(BType lhsType, BType rhsType) {
        if (!isAnydata(lhsType) && !isAnydata(rhsType)) {
            return false;
        }

        if (isAssignable(lhsType, rhsType) || isAssignable(rhsType, lhsType)) {
            return true;
        }

        Set<BType> lhsTypes = expandAndGetMemberTypesRecursive(lhsType);
        Set<BType> rhsTypes = expandAndGetMemberTypesRecursive(rhsType);
        return equalityIntersectionExists(lhsTypes, rhsTypes);
    }

    private boolean equalityIntersectionExists(Set<BType> lhsTypes, Set<BType> rhsTypes) {
        if ((lhsTypes.contains(symTable.anydataType) &&
                     rhsTypes.stream().anyMatch(type -> type.tag != TypeTags.ERROR)) ||
                (rhsTypes.contains(symTable.anydataType) &&
                         lhsTypes.stream().anyMatch(type -> type.tag != TypeTags.ERROR))) {
            return true;
        }

        boolean matchFound = false;
        for (BType lhsType : lhsTypes) {
            for (BType rhsType : rhsTypes) {
                if (isAssignable(lhsType, rhsType) || isAssignable(rhsType, lhsType)) {
                    matchFound = true;
                    break;
                }
            }
            if (matchFound) {
                break;
            }
        }

        if (!matchFound) {
            matchFound = equalityIntersectionExistsForComplexTypes(lhsTypes, rhsTypes);
        }

        return matchFound;
    }

    boolean validNumericStringOrXmlTypeExists(BType type, TypeExistenceValidationFunction validationFunction) {
        switch (type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) type;
                Set<BType> memberTypes = unionType.getMemberTypes();
                BType firstTypeInUnion = getBasicTypeOfBuiltinSubtype(getReferredType(memberTypes.iterator().next()));
                if (firstTypeInUnion.tag == TypeTags.FINITE) {
                    Set<BLangExpression> valSpace = ((BFiniteType) firstTypeInUnion).getValueSpace();
                    BType baseExprType = valSpace.iterator().next().getBType();
                    for (BType memType : memberTypes) {
                        if (memType.tag == TypeTags.TYPEREFDESC) {
                            memType = getReferredType(memType);
                        }
                        if (memType.tag == TypeTags.FINITE) {
                            if (!checkValueSpaceHasSameType((BFiniteType) memType, baseExprType)) {
                                return false;
                            }
                            continue;
                        }
                        if (!isSubTypeOfBaseType(memType, baseExprType.tag)) {
                            return false;
                        }
                    }
                } else {
                    for (BType memType : memberTypes) {
                        memType = getReferredType(memType);
                        if (memType.tag == TypeTags.FINITE) {
                            if (!checkValueSpaceHasSameType((BFiniteType) memType, firstTypeInUnion)) {
                                return false;
                            }
                            continue;
                        }
                        if (!isSubTypeOfBaseType(memType, firstTypeInUnion.tag)) {
                            return false;
                        }
                    }
                }
                return true;
            case TypeTags.FINITE:
                Set<BLangExpression> valSpace = ((BFiniteType) type).getValueSpace();
                BType baseExprType = valSpace.iterator().next().getBType();
                for (BLangExpression expr : valSpace) {
                    if (!checkValueSpaceHasSameType((BFiniteType) type, baseExprType)) {
                        return false;
                    }
                    if (!validationFunction.validate(expr.getBType())) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TYPEREFDESC:
                return validationFunction.validate(getReferredType(type));
            case TypeTags.INTERSECTION:
                return validationFunction.validate(((BIntersectionType) type).effectiveType);
            default:
                return false;
        }
    }

    boolean validNumericTypeExists(BType type) {
        if (type.isNullable() && type.tag != TypeTags.NIL) {
            type = getSafeType(type, true, false);
        }
        if (isBasicNumericType(type)) {
            return true;
        }
        return validNumericStringOrXmlTypeExists(type, this::validNumericTypeExists);
    }

    boolean validStringOrXmlTypeExists(BType type) {
        if (TypeTags.isStringTypeTag(type.tag) || TypeTags.isXMLTypeTag(type.tag)) {
            return true;
        }
        return validNumericStringOrXmlTypeExists(type, this::validStringOrXmlTypeExists);
    }

    boolean validIntegerTypeExists(BType bType) {
        BType type = getReferredType(bType);
        if (type.isNullable() && type.tag != TypeTags.NIL) {
            type = getSafeType(type, true, false);
        }
        if (TypeTags.isIntegerTypeTag(type.tag)) {
            return true;
        }
        switch (type.tag) {
            case TypeTags.BYTE:
                return true;
            case TypeTags.UNION:
                LinkedHashSet<BType> memberTypes = ((BUnionType) type).getMemberTypes();
                for (BType memberType : memberTypes) {
                    memberType = getReferredType(memberType);
                    if (!validIntegerTypeExists(memberType)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.FINITE:
                Set<BLangExpression> valueSpace = ((BFiniteType) type).getValueSpace();
                for (BLangExpression expr : valueSpace) {
                    if (!validIntegerTypeExists(expr.getBType())) {
                        return false;
                    }
                }
                return true;
            case TypeTags.INTERSECTION:
                return validIntegerTypeExists(((BIntersectionType) type).effectiveType);
            default:
                return false;
        }
    }

    public BType getBasicTypeOfBuiltinSubtype(BType type) {
        if (TypeTags.isIntegerTypeTag(type.tag) || type.tag == TypeTags.BYTE) {
            return symTable.intType;
        }
        if (TypeTags.isStringTypeTag(type.tag)) {
            return symTable.stringType;
        }
        if (TypeTags.isXMLTypeTag(type.tag)) {
            return symTable.xmlType;
        }
        return type;
    }

    public boolean checkTypeContainString(BType type) {
        if (TypeTags.isStringTypeTag(type.tag)) {
            return true;
        }
        switch (type.tag) {
            case TypeTags.UNION:
                for (BType memType : ((BUnionType) type).getMemberTypes()) {
                    if (!checkTypeContainString(memType)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.FINITE:
                Set<BLangExpression> valSpace = ((BFiniteType) type).getValueSpace();
                for (BLangExpression expr : valSpace) {
                    if (!checkTypeContainString(expr.getBType())) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TYPEREFDESC:
                return checkTypeContainString(getReferredType(type));
            default:
                return false;
        }
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
        Set<BType> memberTypes = new LinkedHashSet<>();
        switch (bType.tag) {
            case TypeTags.BYTE:
            case TypeTags.INT:
                memberTypes.add(symTable.intType);
                memberTypes.add(symTable.byteType);
                break;
            case TypeTags.FINITE:
                BFiniteType expType = (BFiniteType) bType;
                expType.getValueSpace().forEach(value -> {
                    memberTypes.add(value.getBType());
                });
                break;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) bType;
                if (!visited.add(unionType)) {
                    return memberTypes;
                }
                unionType.getMemberTypes().forEach(member -> {
                    memberTypes.addAll(expandAndGetMemberTypesRecursiveHelper(member, visited));
                });
                break;
            case TypeTags.ARRAY:
                BType arrayElementType = ((BArrayType) bType).getElementType();

                // add an unsealed array to allow comparison between closed and open arrays
                // TODO: 10/16/18 improve this, since it will allow comparison between sealed arrays of different sizes
                if (((BArrayType) bType).getSize() != -1) {
                    memberTypes.add(new BArrayType(arrayElementType));
                }

                if (arrayElementType.tag == TypeTags.UNION) {
                    Set<BType> elementUnionTypes = expandAndGetMemberTypesRecursiveHelper(arrayElementType, visited);
                    elementUnionTypes.forEach(elementUnionType -> {
                        memberTypes.add(new BArrayType(elementUnionType));
                    });
                }
                memberTypes.add(bType);
                break;
            case TypeTags.MAP:
                BType mapConstraintType = ((BMapType) bType).getConstraint();
                if (mapConstraintType.tag == TypeTags.UNION) {
                    Set<BType> constraintUnionTypes =
                            expandAndGetMemberTypesRecursiveHelper(mapConstraintType, visited);
                    constraintUnionTypes.forEach(constraintUnionType -> {
                        memberTypes.add(new BMapType(TypeTags.MAP, constraintUnionType, symTable.mapType.tsymbol));
                    });
                }
                memberTypes.add(bType);
                break;
            case TypeTags.INTERSECTION:
                memberTypes.addAll(expandAndGetMemberTypesRecursive(((BIntersectionType) bType).effectiveType));
                break;
            case TypeTags.TYPEREFDESC:
                return expandAndGetMemberTypesRecursiveHelper(getReferredType(bType), visited);
            default:
                memberTypes.add(bType);
        }
        return memberTypes;
    }

    private boolean tupleIntersectionExists(BTupleType lhsType, BTupleType rhsType) {
        if (lhsType.getTupleTypes().size() != rhsType.getTupleTypes().size()) {
            return false;
        }

        List<BType> lhsMemberTypes = lhsType.getTupleTypes();
        List<BType> rhsMemberTypes = rhsType.getTupleTypes();

        for (int i = 0; i < lhsType.getTupleTypes().size(); i++) {
            if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsMemberTypes.get(i)),
                                            expandAndGetMemberTypesRecursive(rhsMemberTypes.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private boolean equalityIntersectionExistsForComplexTypes(Set<BType> lhsTypes, Set<BType> rhsTypes) {
        for (BType lhsMemberType : lhsTypes) {
            if (isEqualityIntersectionExistsForMemberType(lhsMemberType, rhsTypes)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEqualityIntersectionExistsForMemberType(BType lhsMemberType, Set<BType> rhsTypes) {
        switch (lhsMemberType.tag) {
            case TypeTags.INT:
            case TypeTags.STRING:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.BOOLEAN:
            case TypeTags.NIL:
                if (rhsTypes.stream().map(Types::getReferredType)
                        .anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
                    return true;
                }
                break;
            case TypeTags.JSON:
                if (jsonEqualityIntersectionExists(rhsTypes)) {
                    return true;
                }
                break;
            // When expanding members for tuples, arrays and maps, set isValueDeepEquality to true, to allow
            // comparison between JSON lists/maps and primitive lists/maps since they are all reference types
            case TypeTags.TUPLE:
                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                tupleIntersectionExists((BTupleType) lhsMemberType, (BTupleType) rhsMemberType))) {
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                arrayTupleEqualityIntersectionExists((BArrayType) rhsMemberType,
                                        (BTupleType) lhsMemberType))) {
                    return true;
                }
                break;
            case TypeTags.ARRAY:
                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                equalityIntersectionExists(
                                        expandAndGetMemberTypesRecursive(((BArrayType) lhsMemberType).eType),
                                        expandAndGetMemberTypesRecursive(((BArrayType) rhsMemberType).eType)))) {
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                arrayTupleEqualityIntersectionExists((BArrayType) lhsMemberType,
                                        (BTupleType) rhsMemberType))) {
                    return true;
                }
                break;
            case TypeTags.MAP:
                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                equalityIntersectionExists(
                                        expandAndGetMemberTypesRecursive(((BMapType) lhsMemberType).constraint),
                                        expandAndGetMemberTypesRecursive(((BMapType) rhsMemberType).constraint)))) {
                    return true;
                }

                if (!isAssignable(((BMapType) lhsMemberType).constraint, symTable.errorType) &&
                        rhsTypes.stream().map(Types::getReferredType).anyMatch(rhsMemberType
                                -> rhsMemberType.tag == TypeTags.JSON)) {
                    // at this point it is guaranteed that the map is anydata
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                mapRecordEqualityIntersectionExists((BMapType) lhsMemberType,
                                        (BRecordType) rhsMemberType))) {
                    return true;
                }
                break;
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> checkStructEquivalency(rhsMemberType, lhsMemberType) ||
                                checkStructEquivalency(lhsMemberType, rhsMemberType))) {
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                recordEqualityIntersectionExists((BRecordType) lhsMemberType,
                                        (BRecordType) rhsMemberType))) {
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(rhsMemberType
                        -> rhsMemberType.tag == TypeTags.JSON) &&
                        jsonEqualityIntersectionExists(expandAndGetMemberTypesRecursive(lhsMemberType))) {
                    return true;
                }

                if (rhsTypes.stream().map(Types::getReferredType).anyMatch(
                        rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                mapRecordEqualityIntersectionExists((BMapType) rhsMemberType,
                                        (BRecordType) lhsMemberType))) {
                    return true;
                }
                break;
            case TypeTags.TYPEREFDESC:
                return isEqualityIntersectionExistsForMemberType(getReferredType(lhsMemberType), rhsTypes);
        }
        return false;
    }

    private boolean arrayTupleEqualityIntersectionExists(BArrayType arrayType, BTupleType tupleType) {
        Set<BType> elementTypes = expandAndGetMemberTypesRecursive(arrayType.eType);

        return tupleType.getTupleTypes().stream().allMatch(tupleMemType ->
                        equalityIntersectionExists(elementTypes, expandAndGetMemberTypesRecursive(tupleMemType)));
    }

    private boolean recordEqualityIntersectionExists(BRecordType lhsType, BRecordType rhsType) {
        Map<String, BField> lhsFields = lhsType.fields;
        Map<String, BField> rhsFields = rhsType.fields;

        List<Name> matchedFieldNames = new ArrayList<>();
        for (BField lhsField : lhsFields.values()) {
            if (rhsFields.containsKey(lhsField.name.value)) {
                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsField.type),
                                                expandAndGetMemberTypesRecursive(
                                                        rhsFields.get(lhsField.name.value).type))) {
                    return false;
                }
                matchedFieldNames.add(lhsField.getName());
            } else {
                if (Symbols.isFlagOn(lhsField.symbol.flags, Flags.OPTIONAL)) {
                    break;
                }

                if (rhsType.sealed) {
                    return false;
                }

                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsField.type),
                                                expandAndGetMemberTypesRecursive(rhsType.restFieldType))) {
                    return false;
                }
            }
        }

        for (BField rhsField : rhsFields.values()) {
            if (matchedFieldNames.contains(rhsField.getName())) {
                continue;
            }

            if (!Symbols.isFlagOn(rhsField.symbol.flags, Flags.OPTIONAL)) {
                if (lhsType.sealed) {
                    return false;
                }

                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(rhsField.type),
                                                expandAndGetMemberTypesRecursive(lhsType.restFieldType))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean mapRecordEqualityIntersectionExists(BMapType mapType, BRecordType recordType) {
        Set<BType> mapConstrTypes = expandAndGetMemberTypesRecursive(mapType.getConstraint());

        for (BField field : recordType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                    !equalityIntersectionExists(mapConstrTypes, expandAndGetMemberTypesRecursive(field.type))) {
                return false;
            }
        }

        return true;
    }

    private boolean jsonEqualityIntersectionExists(Set<BType> typeSet) {
        for (BType type : typeSet) {
            switch (type.tag) {
                case TypeTags.MAP:
                    if (!isAssignable(((BMapType) type).constraint, symTable.errorType)) {
                        return true;
                    }
                    break;
                case TypeTags.RECORD:
                    BRecordType recordType = (BRecordType) type;
                    if (recordType.fields.values().stream()
                            .allMatch(field -> Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) ||
                                    !isAssignable(field.type, symTable.errorType))) {
                        return true;
                    }
                    break;
                default:
                    if (isAssignable(type, symTable.jsonType)) {
                        return true;
                    }
            }
        }
        return false;
    }

    public BType getRemainingMatchExprType(BType originalType, BType typeToRemove, SymbolEnv env) {
        switch (originalType.tag) {
            case TypeTags.UNION:
                return getRemainingType((BUnionType) originalType, getAllTypes(typeToRemove, true));
            case TypeTags.FINITE:
                return getRemainingType((BFiniteType) originalType, getAllTypes(typeToRemove, true));
            case TypeTags.TUPLE:
                return getRemainingType((BTupleType) originalType, typeToRemove, env);
            default:
                return originalType;
        }
    }

    private BType getRemainingType(BTupleType originalType, BType typeToRemove, SymbolEnv env) {
        switch (typeToRemove.tag) {
            case TypeTags.TUPLE:
                return getRemainingType(originalType, (BTupleType) typeToRemove, env);
            case TypeTags.ARRAY:
                return getRemainingType(originalType, (BArrayType) typeToRemove, env);
            default:
                return originalType;
        }
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
            BVarSymbol varSymbol = new BVarSymbol(type.flags, null, null, type, null, null, null);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        if (typeToRemove.restType == null) {
            return new BTupleType(tupleTypes);
        }
        if (originalTupleTypes.size() == typesToRemove.size()) {
            return originalType;
        }
        for (int i = typesToRemove.size(); i < originalTupleTypes.size(); i++) {
            BType type = getRemainingMatchExprType(originalTupleTypes.get(i), typeToRemove.restType, env);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        return new BTupleType(tupleTypes);
    }

    private BType getRemainingType(BTupleType originalType, BArrayType typeToRemove, SymbolEnv env) {
        BType eType = typeToRemove.eType;
        List<BTupleMember> tupleTypes = new ArrayList<>();
        for (BType tupleMemberType : originalType.getTupleTypes()) {
            BType type = getRemainingMatchExprType(tupleMemberType, eType, env);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
            tupleTypes.add(new BTupleMember(type, varSymbol));
        }
        BTupleType remainingType = new BTupleType(tupleTypes);
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

                BType typeRemovedFromOriginalUnionType = getReferredType(getRemainingType((BUnionType) originalType,
                                                                                          getAllTypes(remainingType,
                                                                                                      true)));
                if (typeRemovedFromOriginalUnionType == symTable.nullSet ||
                        isSubTypeOfReadOnly(typeRemovedFromOriginalUnionType, env) ||
                        isSubTypeOfReadOnly(remainingType, env) ||
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
                BType refType = getReferredType(originalType);

                if (refType.tag == TypeTags.INTERSECTION) {
                    refType = ((BIntersectionType) refType).effectiveType;
                }

                if (refType.tag != TypeTags.UNION && refType.tag != TypeTags.FINITE) {
                    return originalType;
                }
                return getRemainingType(refType, typeToRemove, env);
        }

        if (Symbols.isFlagOn(getReferredType(originalType).flags, Flags.READONLY)) {
            return remainingType;
        }

        BType referredTypeToRemove = getReferredType(typeToRemove);
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

    public boolean isSubTypeOfReadOnly(BType type, SymbolEnv env) {
        return isInherentlyImmutableType(type) ||
                (isSelectivelyImmutableType(type, env.enclPkg.packageID) &&
                        Symbols.isFlagOn(type.flags, Flags.READONLY));
    }

    private boolean isClosedRecordTypes(BType type) {
        switch (type.tag) {
            case RECORD:
                BRecordType recordType = (BRecordType) type;
                return recordType.sealed || recordType.restFieldType == symTable.neverType;
            case UNION:
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (!isClosedRecordTypes(getReferredType(memberType))) {
                        return false;
                    }
                }
                return true;
        }
        return false;
    }

    private boolean removesDistinctRecords(BType typeToRemove, BType remainingType) {
        List<Set<String>> fieldsInRemainingTypes = new ArrayList<>();

        remainingType = getReferredType(remainingType);
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
                    BType referredMemberType = getReferredType(memberType);
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
        typeToRemove = getReferredType(typeToRemove);
        switch (typeToRemove.tag) {
            case RECORD:
                fieldsInRemovingTypes.add(((BRecordType) typeToRemove).fields.keySet());
                break;
            case UNION:
                for (BType memberType : ((BUnionType) typeToRemove).getMemberTypes()) {
                    BType referredType = getReferredType(memberType);

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
        BType referredRemainingType = getReferredType(remainingType);
        if (referredRemainingType.tag != UNION) {
            return false;
        }

        LinkedHashSet<BType> mutableRemainingTypes =
                filterMutableMembers(((BUnionType) referredRemainingType).getMemberTypes(), env);
        remainingType = mutableRemainingTypes.size() == 1 ? mutableRemainingTypes.iterator().next() :
                BUnionType.create(null, mutableRemainingTypes);

        BType referredTypeToRemove = getReferredType(typeToRemove);

        if (referredTypeToRemove.tag == UNION) {
            LinkedHashSet<BType> mutableTypesToRemove =
                    filterMutableMembers(((BUnionType) referredTypeToRemove).getMemberTypes(), env);
            typeToRemove = mutableTypesToRemove.size() == 1 ? mutableTypesToRemove.iterator().next() :
                    BUnionType.create(null, mutableTypesToRemove);
        } else {
            typeToRemove = referredTypeToRemove;
        }

        return removesDistinctBasicTypes(typeToRemove, remainingType);
    }

    private LinkedHashSet<BType> filterMutableMembers(LinkedHashSet<BType> types, SymbolEnv env) {
        LinkedHashSet<BType> remainingMemberTypes = new LinkedHashSet<>();

        for (BType type : types) {
            BType referredType = getReferredType(type);
            if (!isSubTypeOfReadOnly(referredType, env)) {
                remainingMemberTypes.add(referredType);
            }
        }

        return remainingMemberTypes;
    }

    // TODO: now only works for error. Probably we need to properly define readonly types here.
    private BType getRemainingType(BReadonlyType originalType, BType removeType) {
        if (removeType.tag == TypeTags.ERROR) {
            return symTable.anyAndReadonly;
        }

        return  originalType;
    }

    public BType getTypeIntersection(IntersectionContext intersectionContext, BType lhsType, BType rhsType,
                                     SymbolEnv env) {
        return getTypeIntersection(intersectionContext, lhsType, rhsType, env, new LinkedHashSet<>());
    }

    private BType getTypeIntersection(IntersectionContext intersectionContext, BType lhsType, BType rhsType,
                                     SymbolEnv env,
                                     LinkedHashSet<BType> visitedTypes) {
        List<BType> rhsTypeComponents = getAllTypes(rhsType, false);
        LinkedHashSet<BType> intersection = new LinkedHashSet<>(rhsTypeComponents.size());
        for (BType rhsComponent : rhsTypeComponents) {
            BType it = getIntersection(intersectionContext, lhsType, env, rhsComponent,
                    new LinkedHashSet<>(visitedTypes));
            if (it != null) {
                intersection.add(it);
            }
        }

        if (intersection.isEmpty()) {
            if (lhsType.tag == TypeTags.NULL_SET) {
                return lhsType;
            }
            return symTable.semanticError;
        }

        if (intersection.size() == 1) {
            return intersection.toArray(new BType[0])[0];
        } else {
            return BUnionType.create(null, intersection);
        }
    }

    private BType getIntersection(IntersectionContext intersectionContext, BType lhsType, SymbolEnv env, BType type,
                                  LinkedHashSet<BType> visitedTypes) {

        lhsType = getEffectiveTypeForIntersection(lhsType);
        type = getEffectiveTypeForIntersection(type);

        if (intersectionContext.preferNonGenerativeIntersection) {
            if (isAssignable(type, lhsType)) {
                return type;
            } else if (isAssignable(lhsType, type)) {
                return lhsType;
            }
        }
        type = getReferredType(type);
        lhsType = getReferredType(lhsType);

        // TODO: intersections with readonly types are not handled properly. Here, the logic works as follows.
        // Say we have an intersection called A & readonly and we have another type called B. As per the current
        // implementation, we cannot easily find the intersection between (A & readonly) and B. Instead, what we
        // do here is, first find the intersection between A and B then re-create the immutable type out of it.

        if (Symbols.isFlagOn(lhsType.flags, Flags.READONLY) && lhsType.tag == TypeTags.UNION &&
                ((BUnionType) lhsType).getIntersectionType().isPresent()) {
            BIntersectionType intersectionType = ((BUnionType) lhsType).getIntersectionType().get();
            BType finalType = type;
            List<BType> types = intersectionType.getConstituentTypes().stream().filter(t -> t.tag != TypeTags.READONLY)
                    .map(t -> getIntersection(intersectionContext, t, env, finalType, visitedTypes))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (types.size() == 1) {
                BType bType = types.get(0);

                if (isInherentlyImmutableType(bType) || Symbols.isFlagOn(bType.flags, Flags.READONLY)) {
                    return bType;
                }

                if (!isSelectivelyImmutableType(bType, new HashSet<>(), env.enclPkg.packageID)) {
                    return symTable.semanticError;
                }

                return ImmutableTypeCloner.getEffectiveImmutableType(intersectionContext.pos, this, bType,
                                                                     env, symTable, anonymousModelHelper, names);
            }
        }

        if (type.tag == TypeTags.ERROR && lhsType.tag == TypeTags.ERROR) {
            BType intersectionType = getIntersectionForErrorTypes(intersectionContext, lhsType, type, env,
                                                            visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.RECORD && lhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) lhsType,
                                                              (BRecordType) type, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.MAP && lhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) lhsType,
                                                              getEquivalentRecordType((BMapType) type), env,
                                                                visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.RECORD && lhsType.tag == TypeTags.MAP) {
            BType intersectionType = createRecordIntersection(intersectionContext,
                                                              getEquivalentRecordType((BMapType) lhsType),
                                                              (BRecordType) type, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (!intersectionContext.preferNonGenerativeIntersection && isAssignable(type, lhsType)) {
            return type;
        } else if (!intersectionContext.preferNonGenerativeIntersection && isAssignable(lhsType, type)) {
            return lhsType;
        } else if (lhsType.tag == TypeTags.FINITE) {
            BType intersectionType = getTypeForFiniteTypeValuesAssignableToType((BFiniteType) lhsType, type);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.FINITE) {
            BType intersectionType = getTypeForFiniteTypeValuesAssignableToType((BFiniteType) type, lhsType);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (lhsType.tag == TypeTags.UNION) {
            BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) lhsType, type, env,
                    intersectionContext, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.UNION) {
            BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) type, lhsType, env,
                    intersectionContext, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.MAP && lhsType.tag == TypeTags.MAP) {
            BType intersectionConstraintTypeType = getIntersection(intersectionContext, ((BMapType) lhsType).constraint,
                                                                   env, ((BMapType) type).constraint, visitedTypes);
            if (intersectionConstraintTypeType == null || intersectionConstraintTypeType == symTable.semanticError) {
                return null;
            }
            return new BMapType(TypeTags.MAP, intersectionConstraintTypeType, null);
        } else if (type.tag == TypeTags.ARRAY && lhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    (BArrayType) type, (BTupleType) lhsType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.TUPLE && lhsType.tag == TypeTags.ARRAY) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    (BArrayType) lhsType, (BTupleType) type, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.TUPLE && lhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createTupleAndTupleIntersection(intersectionContext,
                    (BTupleType) lhsType, (BTupleType) type, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(type) && lhsType.tag == TypeTags.RECORD) {
            BType intersectionType = createRecordIntersection(intersectionContext, (BRecordType) lhsType,
                    getEquivalentRecordType(getMapTypeForAnydataOrJson(type, env)), env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.RECORD && isAnydataOrJson(lhsType)) {
            BType intersectionType = createRecordIntersection(intersectionContext,
                    getEquivalentRecordType(getMapTypeForAnydataOrJson(lhsType, env)), (BRecordType) type, env,
                    visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(type) && lhsType.tag == TypeTags.MAP) {
            return getIntersection(intersectionContext, lhsType, env, getMapTypeForAnydataOrJson(type, env),
                    visitedTypes);
        } else if (type.tag == TypeTags.MAP && isAnydataOrJson(lhsType)) {
            return getIntersection(intersectionContext, getMapTypeForAnydataOrJson(lhsType, env), env, type,
                    visitedTypes);
        } else if (isAnydataOrJson(type) && lhsType.tag == TypeTags.TUPLE) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    getArrayTypeForAnydataOrJson(type, env), (BTupleType) lhsType, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (type.tag == TypeTags.TUPLE && isAnydataOrJson(lhsType)) {
            BType intersectionType = createArrayAndTupleIntersection(intersectionContext,
                    getArrayTypeForAnydataOrJson(lhsType, env), (BTupleType) type, env, visitedTypes);
            if (intersectionType != symTable.semanticError) {
                return intersectionType;
            }
        } else if (isAnydataOrJson(type) && lhsType.tag == TypeTags.ARRAY) {
            BType elementIntersection = getIntersection(intersectionContext, ((BArrayType) lhsType).eType, env,
                                                        type, visitedTypes);
            if (elementIntersection == null) {
                return elementIntersection;
            }
            return new BArrayType(elementIntersection);
        } else if (type.tag == TypeTags.ARRAY && isAnydataOrJson(lhsType)) {
            BType elementIntersection = getIntersection(intersectionContext, lhsType, env, ((BArrayType) type).eType,
                    visitedTypes);
            if (elementIntersection == null) {
                return elementIntersection;
            }
            return new BArrayType(elementIntersection);
        } else if (type.tag == TypeTags.NULL_SET) {
            return type;
        }
        return null;
    }

    private BType getEffectiveTypeForIntersection(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag != TypeTags.INTERSECTION) {
            return bType;
        }

        BType effectiveType = ((BIntersectionType) type).effectiveType;

        // Don't return a cyclic type as the effective type due to
        // https://github.com/ballerina-platform/ballerina-lang/issues/30681.
        return effectiveType.tag == TypeTags.UNION && ((BUnionType) effectiveType).isCyclic ? type : effectiveType;
    }

    private boolean isAnydataOrJson(BType type) {
        switch (type.tag) {
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
                return true;
        }
        return false;
    }

    private BMapType getMapTypeForAnydataOrJson(BType type, SymbolEnv env) {
        BMapType mapType = type.tag == TypeTags.ANYDATA ? symTable.mapAnydataType : symTable.mapJsonType;

        if (isImmutable(type)) {
            return (BMapType) ImmutableTypeCloner.getEffectiveImmutableType(null, this, mapType, env, symTable,
                    anonymousModelHelper, names);
        }
        return mapType;
    }

    private BArrayType getArrayTypeForAnydataOrJson(BType type, SymbolEnv env) {
        BArrayType arrayType = type.tag == TypeTags.ANYDATA ? symTable.arrayAnydataType : symTable.arrayJsonType;

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
        if (arrayType.state == BArrayState.CLOSED && tupleTypes.size() != arrayType.size) {
            if (tupleTypes.size() > arrayType.size) {
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
            return new BTupleType(null, tupleMemberTypes);
        }

        BType restIntersectionType = getTypeIntersection(intersectionContext, tupleType.restType, eType, env,
                visitedTypes);
        if (restIntersectionType == symTable.semanticError) {
            return new BTupleType(null, tupleMemberTypes);
        }
        return new BTupleType(null, tupleMemberTypes, restIntersectionType, 0);
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
            BVarSymbol varSymbol = new BVarSymbol(intersectionType.flags, null, null, intersectionType,
                    null, null, null);
            tupleMemberTypes.add(new BTupleMember(intersectionType, varSymbol));
        }

        if (lhsTupleType.restType != null && tupleType.restType != null) {
            BType restIntersectionType = getTypeIntersection(intersectionContext, tupleType.restType,
                    lhsTupleType.restType, env, visitedTypes);
            if (restIntersectionType == symTable.semanticError) {
                return new BTupleType(null, tupleMemberTypes);
            }
            return new BTupleType(null, tupleMemberTypes, restIntersectionType, 0);
        }

        return new BTupleType(null, tupleMemberTypes);
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
            newType.flags |= Flags.READONLY;
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

            if (intersectionFieldType.tag == TypeTags.INVOKABLE && intersectionFieldType.tsymbol != null) {
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
                                                   intersectionFieldType, newTypeSymbol, lhsRecordField.pos, SOURCE);
            }

            newTypeFields.put(key, new BField(name, null, recordFieldSymbol));
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
        recordSymbol.name = names.fromString(
                anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID));
        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                                         bInvokableType, symTable.builtinPos);
        recordSymbol.scope = new Scope(recordSymbol);

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.tsymbol = recordSymbol;
        recordSymbol.type = recordType;

        return recordType;
    }

    private BRecordType getEquivalentRecordType(BMapType mapType) {
        BRecordType equivalentRecordType = new BRecordType(null);
        equivalentRecordType.sealed = false;
        equivalentRecordType.restFieldType = mapType.constraint;
        return equivalentRecordType;
    }

    private BErrorType createErrorType(BType lhsType, BType rhsType, BType detailType, SymbolEnv env) {
        BErrorType lhsErrorType = (BErrorType) lhsType;
        BErrorType rhsErrorType = (BErrorType) rhsType;

        BErrorType errorType = createErrorType(detailType, lhsType.flags, env);
        errorType.tsymbol.flags |= rhsType.flags;

        errorType.typeIdSet = BTypeIdSet.getIntersection(lhsErrorType.typeIdSet, rhsErrorType.typeIdSet);

        return errorType;
    }

    public BErrorType createErrorType(BType detailType, long flags, SymbolEnv env) {
        String name = anonymousModelHelper.getNextAnonymousIntersectionErrorTypeName(env.enclPkg.packageID);
        BErrorTypeSymbol errorTypeSymbol = Symbols.createErrorSymbol(flags | Flags.ANONYMOUS, names.fromString(name),
                                                                     env.enclPkg.symbol.pkgID, null,
                                                                     env.scope.owner, symTable.builtinPos, VIRTUAL);
        errorTypeSymbol.scope = new Scope(errorTypeSymbol);
        BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;
        errorType.typeIdSet = BTypeIdSet.emptySet();

        return errorType;
    }

    private boolean populateRecordFields(IntersectionContext diagnosticContext, BRecordType newType,
                                         BType originalType, SymbolEnv env, BType constraint) {
        BTypeSymbol intersectionRecordSymbol = newType.tsymbol;
        // If the detail type is BMapType simply ignore since the resulting detail type has `anydata` as rest type.
        if (originalType.getKind() != TypeKind.RECORD) {
            return true;
        }
        BRecordType originalRecordType = (BRecordType) originalType;
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (BField origField : originalRecordType.fields.values()) {
            org.wso2.ballerinalang.compiler.util.Name origFieldName = origField.name;
            String nameString = origFieldName.value;

            if (!validateRecordFieldDefaultValueForIntersection(diagnosticContext, origField, originalRecordType)) {
                return false;
            }

            BType recordFieldType = validateRecordField(diagnosticContext, newType, origField, constraint, env);
            if (recordFieldType == symTable.semanticError) {
                return false;
            }

            BVarSymbol recordFieldSymbol = new BVarSymbol(origField.symbol.flags, origFieldName,
                                                          env.enclPkg.packageID, recordFieldType,
                                                          intersectionRecordSymbol, origField.pos, SOURCE);

            if (recordFieldType == symTable.neverType && Symbols.isFlagOn(recordFieldSymbol.flags, Flags.OPTIONAL)) {
                recordFieldSymbol.flags &= (~Flags.REQUIRED);
                recordFieldSymbol.flags |= Flags.OPTIONAL;
            }

            if (recordFieldType.tag == TypeTags.INVOKABLE && recordFieldType.tsymbol != null) {
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) recordFieldType.tsymbol;
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) recordFieldSymbol;
                invokableSymbol.params = tsymbol.params == null ? null : new ArrayList<>(tsymbol.params);
                invokableSymbol.restParam = tsymbol.restParam;
                invokableSymbol.retType = tsymbol.returnType;
                invokableSymbol.flags = tsymbol.flags;
            }

            fields.put(nameString, new BField(origFieldName, null, recordFieldSymbol));
            intersectionRecordSymbol.scope.define(origFieldName,  recordFieldSymbol);
        }
        newType.fields.putAll(fields);

        return true;
    }


    private boolean validateRecordFieldDefaultValueForIntersection(IntersectionContext diagnosticContext,
                                                                   BField field, BRecordType recordType) {
        if (field.symbol != null && field.symbol.isDefaultable && !diagnosticContext.ignoreDefaultValues) {
            diagnosticContext.logError(DiagnosticErrorCode.INTERSECTION_NOT_ALLOWED_WITH_TYPE, recordType, field.name);
            return false;
        }
        return true;
    }

    private BType validateRecordField(IntersectionContext intersectionContext,
                                      BRecordType newType, BField origField, BType constraint, SymbolEnv env) {
        if (hasField(newType, origField)) {
            return validateOverlappingFields(newType, origField);
        }

        if (constraint == null) {
            return origField.type;
        }

        BType fieldType = getTypeIntersection(intersectionContext, origField.type, constraint, env);
        if (fieldType.tag == TypeTags.NEVER && !Symbols.isOptional(origField.symbol)) {
            return symTable.semanticError;
        }

        if (fieldType != symTable.semanticError) {
            return fieldType;
        }

        if (Symbols.isOptional(origField.symbol)) {
            return symTable.neverType;
        }

        return symTable.semanticError;
    }

    private boolean hasField(BRecordType recordType, BField origField) {
        return recordType.fields.containsKey(origField.name.value);
    }

    private BType validateOverlappingFields(BRecordType newType, BField origField) {
        if (!hasField(newType, origField)) {
            return origField.type;
        }

        BField overlappingField = newType.fields.get(origField.name.value);
        if (isAssignable(overlappingField.type, origField.type)) {
            return overlappingField.type;
        }

        if (isAssignable(origField.type, overlappingField.type)) {
            return origField.type;
        }
        return symTable.semanticError;
    }

    private void removeErrorFromReadonlyType(List<BType> remainingTypes) {
        Iterator<BType> remainingIterator = remainingTypes.listIterator();
        boolean addAnyAndReadOnly = false;
        while (remainingIterator.hasNext()) {
            BType remainingType = remainingIterator.next();
            if (remainingType.tag != TypeTags.READONLY) {
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
            if (!hasErrorToRemove && removeType.tag == TypeTags.ERROR) {
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

        return BUnionType.create(null, new LinkedHashSet<>(remainingTypes));
    }

    private BType getRemainingType(BFiniteType originalType, List<BType> removeTypes) {
        Set<BLangExpression> remainingValueSpace = new LinkedHashSet<>();

        for (BLangExpression valueExpr : originalType.getValueSpace()) {
            boolean matchExists = false;
            for (BType remType : removeTypes) {
                if (isAssignable(valueExpr.getBType(), remType) ||
                        isAssignableToFiniteType(remType, (BLangLiteral) valueExpr)) {
                    matchExists = true;
                    break;
                }
            }

            if (!matchExists) {
                remainingValueSpace.add(valueExpr);
            }
        }

        if (remainingValueSpace.isEmpty()) {
            return symTable.semanticError;
        }

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, originalType.tsymbol.flags,
                names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                originalType.tsymbol.pkgID, null,
                originalType.tsymbol.owner, originalType.tsymbol.pos,
                VIRTUAL);
        BFiniteType intersectingFiniteType = new BFiniteType(finiteTypeSymbol, remainingValueSpace);
        finiteTypeSymbol.type = intersectingFiniteType;
        return intersectingFiniteType;
    }

    public BType getSafeType(BType bType, boolean liftNil, boolean liftError) {
        BType type = getReferredType(bType);
        // Since JSON, ANY and ANYDATA by default contain null, we need to create a new respective type which
        // is not-nullable.
        if (liftNil) {
            switch (type.tag) {
                case TypeTags.JSON:
                    return new BJSONType((BJSONType) type, false);
                case TypeTags.ANY:
                    return new BAnyType(type.tag, type.tsymbol, false);
                case TypeTags.ANYDATA:
                    return new BAnydataType((BAnydataType) type, false);
                case TypeTags.READONLY:
                    if (liftError) {
                        return symTable.anyAndReadonly;
                    }
                    return new BReadonlyType(type.tag, type.tsymbol, false);
            }
        }

        if (type.tag != TypeTags.UNION) {
            return bType;
        }

        BUnionType unionType = (BUnionType) type;
        LinkedHashSet<BType> memTypes = new LinkedHashSet<>(unionType.getMemberTypes());
        BUnionType errorLiftedType = BUnionType.create(null, memTypes);

        if (liftNil) {
            errorLiftedType.remove(symTable.nilType);
        }

        if (liftError) {
            LinkedHashSet<BType> bTypes = new LinkedHashSet<>();
            for (BType t : errorLiftedType.getMemberTypes()) {
                if (t.tag != TypeTags.ERROR) {
                    bTypes.add(t);
                }
            }
            memTypes = bTypes;
            errorLiftedType = BUnionType.create(null, memTypes);
        }

        if (errorLiftedType.getMemberTypes().size() == 1) {
            return errorLiftedType.getMemberTypes().toArray(new BType[0])[0];
        }

        if (errorLiftedType.getMemberTypes().size() == 0) {
            return symTable.semanticError;
        }

        return errorLiftedType;
    }

    public List<BType> getAllTypes(BType type, boolean getReferenced) {
        if (type.tag != TypeTags.UNION) {
            if (getReferenced && type.tag == TypeTags.TYPEREFDESC) {
                return getAllTypes(((BTypeReferenceType) type).referredType, true);
            } else {
                return Lists.of(type);
            }
        }

        List<BType> memberTypes = new LinkedList<>();
        ((BUnionType) type).getMemberTypes().forEach(memberType -> memberTypes.addAll(getAllTypes(memberType, true)));
        return memberTypes;
    }

    public List<BType> getAllReferredTypes(BUnionType unionType) {
        List<BType> memberTypes = new LinkedList<>();
        for (BType type : unionType.getMemberTypes()) {
            if (type.tag == UNION) {
                memberTypes.addAll(getAllReferredTypes((BUnionType) type));
            } else {
                memberTypes.add(Types.getReferredType(type));
            }
        }
        return memberTypes;
    }

    public boolean isAllowedConstantType(BType type) {
        switch (type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.INT:
                // TODO : Fix this, Issue : #21542
//            case TypeTags.SIGNED32_INT:
//            case TypeTags.SIGNED16_INT:
//            case TypeTags.SIGNED8_INT:
//            case TypeTags.UNSIGNED32_INT:
//            case TypeTags.UNSIGNED16_INT:
//            case TypeTags.UNSIGNED8_INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
                // TODO : Fix this, Issue : #21542
//            case TypeTags.CHAR_STRING:
            case TypeTags.NIL:
                return true;
            case TypeTags.MAP:
                return isAllowedConstantType(((BMapType) type).constraint);
            case TypeTags.FINITE:
                BLangExpression finiteValue = ((BFiniteType) type).getValueSpace().toArray(new BLangExpression[0])[0];
                return isAllowedConstantType(finiteValue.getBType());
            case TypeTags.TYPEREFDESC:
                return isAllowedConstantType(((BTypeReferenceType) type).referredType);
            default:
                return false;
        }
    }

    public boolean isValidLiteral(BLangLiteral literal, BType targetType) {
        BType literalType = literal.getBType();
        if (literalType.tag == targetType.tag) {
            return true;
        }

        switch (targetType.tag) {
            case TypeTags.BYTE:
                return literalType.tag == TypeTags.INT && isByteLiteralValue((Long) literal.value);
            case TypeTags.DECIMAL:
                return literalType.tag == TypeTags.FLOAT || literalType.tag == TypeTags.INT;
            case TypeTags.FLOAT:
                return literalType.tag == TypeTags.INT;
            case TypeTags.SIGNED32_INT:
                return literalType.tag == TypeTags.INT && isSigned32LiteralValue((Long) literal.value);
            case TypeTags.SIGNED16_INT:
                return literalType.tag == TypeTags.INT && isSigned16LiteralValue((Long) literal.value);
            case TypeTags.SIGNED8_INT:
                return literalType.tag == TypeTags.INT && isSigned8LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED32_INT:
                return literalType.tag == TypeTags.INT && isUnsigned32LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED16_INT:
                return literalType.tag == TypeTags.INT && isUnsigned16LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED8_INT:
                return literalType.tag == TypeTags.INT && isUnsigned8LiteralValue((Long) literal.value);
            case TypeTags.CHAR_STRING:
                return literalType.tag == TypeTags.STRING && isCharLiteralValue((String) literal.value);
            default:
                return false;
        }
    }

    /**
     * Validate if the return type of the given function is a subtype of `error?`, containing `()`.
     *
     * @param function          The function of which the return type should be validated
     * @param diagnosticCode    The code to log if the return type is invalid
     */
    public void validateErrorOrNilReturn(BLangFunction function, DiagnosticCode diagnosticCode) {
        BType returnType = getReferredType(function.returnTypeNode.getBType());

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

        for (BType memType : type.getMemberTypes()) {
            BType referredMemType = getReferredType(memType);
            if (referredMemType.tag != TypeTags.NIL && referredMemType.tag != TypeTags.ERROR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Type vector of size two, to hold the source and the target types.
     *
     * @since 0.982.0
     */
    private static class TypePair {
        BType sourceType;
        BType targetType;

        public TypePair(BType sourceType, BType targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypePair)) {
                return false;
            }

            TypePair other = (TypePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceType, targetType);
        }
    }

    /**
     * A functional interface for parameterizing the type of type checking that needs to be done on the source and
     * target types.
     *
     * @since 0.995.0
     */
    private interface TypeEqualityPredicate {
        boolean test(BType source, BType target, Set<TypePair> unresolvedTypes);
    }

    /**
     * A functional interface to validate numeric, string or xml type existence.
     *
     * @since 2201.1.0
     */
    private interface TypeExistenceValidationFunction {
        boolean validate(BType type);
    }

    public boolean hasFillerValue(BType type) {
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
                return checkFillerValue((BFiniteType) type);
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
            case TypeTags.TYPEREFDESC:
                return hasFillerValue(getReferredType(type));
            case TypeTags.INTERSECTION:
                return hasFillerValue(((BIntersectionType) type).effectiveType);
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
     * This will handle two types. Singleton : As singleton can have one value that value should it self be a valid fill
     * value Union : 1. if nil is a member it is the fill values 2. else all the values should belong to same type and
     * the default value for that type should be a member of the union precondition : value space should have at least
     * one element
     *
     * @param type BFiniteType union or finite
     * @return boolean whether type has a valid filler value or not
     */
    private boolean checkFillerValue(BFiniteType type) {
        if (type.isNullable()) {
            return true;
        }
        if (type.getValueSpace().size() == 1) { // For singleton types, that value is the implicit initial value
            return true;
        }
        Iterator iterator = type.getValueSpace().iterator();
        BLangExpression firstElement = (BLangExpression) iterator.next();
        boolean defaultFillValuePresent = isImplicitDefaultValue(firstElement);

        while (iterator.hasNext()) {
            BLangExpression value = (BLangExpression) iterator.next();
            if (!isSameBasicType(value.getBType(), firstElement.getBType())) {
                return false;
            }
            if (!defaultFillValuePresent && isImplicitDefaultValue(value)) {
                defaultFillValuePresent = true;
            }
        }

        return defaultFillValuePresent;
    }

    private boolean hasImplicitDefaultValue(Set<BLangExpression> valueSpace) {
        for (BLangExpression expression : valueSpace) {
            if (isImplicitDefaultValue(expression)) {
                return true;
            }
        }
        return false;
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
                Set<BType> uniqueValues = getValueTypes(((BFiniteType) member).getValueSpace());
                memberTypes.addAll(uniqueValues);
                if (!hasFillerValue && hasImplicitDefaultValue(((BFiniteType) member).getValueSpace())) {
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
        int sourceTag = source.tag;
        int targetTag = target.tag;
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

    private Set<BType> getValueTypes(Set<BLangExpression> valueSpace) {
        Set<BType> uniqueType = new HashSet<>();
        for (BLangExpression expression : valueSpace) {
            uniqueType.add(expression.getBType());
        }
        return uniqueType;
    }

    private boolean isImplicitDefaultValue(BLangExpression expression) {
        if ((expression.getKind() == NodeKind.LITERAL) || (expression.getKind() == NodeKind.NUMERIC_LITERAL)) {
            BLangLiteral literalExpression = (BLangLiteral) expression;
            BType literalExprType = literalExpression.getBType();
            Object value = literalExpression.getValue();
            switch (literalExprType.getKind()) {
                case INT:
                case BYTE:
                    return value.equals(0L);
                case STRING:
                    return value == null || value.equals("");
                case DECIMAL:
                    return value.equals(String.valueOf(0)) || value.equals(0L);
                case FLOAT:
                    return value.equals(String.valueOf(0.0));
                case BOOLEAN:
                    return value.equals(Boolean.FALSE);
                case NIL:
                    return true;
                default:
                    return false;
            }
        }
        return false;
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
        if (type.size == -1) {
            return true;
        }
        return hasFillerValue(type.eType);
    }

    /**
     * Get result type of the query output.
     *
     * @param type type of query expression.
     * @return result type.
     */
    public BType resolveExprType(BType type) {
        switch (type.tag) {
            case TypeTags.STREAM:
                return ((BStreamType) type).constraint;
            case TypeTags.TABLE:
                return ((BTableType) type).constraint;
            case TypeTags.ARRAY:
                return ((BArrayType) type).eType;
            case TypeTags.UNION:
                List<BType> exprTypes = new ArrayList<>(((BUnionType) type).getMemberTypes());
                for (BType returnType : exprTypes) {
                    switch (returnType.tag) {
                        case TypeTags.STREAM:
                            return ((BStreamType) returnType).constraint;
                        case TypeTags.TABLE:
                            return ((BTableType) returnType).constraint;
                        case TypeTags.ARRAY:
                            return ((BArrayType) returnType).eType;
                        case TypeTags.STRING:
                        case TypeTags.XML:
                            return returnType;
                    }
                }
            default:
                return type;
        }
    }

    /**
     * Check whether a type is an ordered type.
     *
     * @param type type.
     * @param hasCycle whether there is a cycle.
     * @return boolean whether the type is an ordered type or not.
     */
    public boolean isOrderedType(BType type, boolean hasCycle) {
        switch (type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) type;
                if (hasCycle) {
                    return true;
                }
                if (unionType.isCyclic) {
                    hasCycle = true;
                }
                Set<BType> memberTypes = unionType.getMemberTypes();
                boolean allMembersOrdered = false;
                BType firstTypeInUnion = getTypeWithEffectiveIntersectionTypes(getReferredType(
                        memberTypes.stream().filter(m -> !isNil(m)).findFirst().orElse(memberTypes.iterator().next())));
                if (isNil(firstTypeInUnion)) {
                    // Union contains only the nil type.
                    return true;
                }
                boolean isFirstTypeInUnionFinite = firstTypeInUnion.tag == TypeTags.FINITE;
                for (BType memType : memberTypes) {
                    memType = getEffectiveTypeForIntersection(getReferredType(memType));
                    if (isFirstTypeInUnionFinite && memType.tag == TypeTags.FINITE && !isNil(memType)) {
                        Set<BLangExpression> valSpace = ((BFiniteType) firstTypeInUnion).getValueSpace();
                        BType baseExprType = valSpace.iterator().next().getBType();
                        if (!checkValueSpaceHasSameType((BFiniteType) memType, baseExprType)) {
                            return false;
                        }
                    } else if (memType.tag == TypeTags.UNION || memType.tag == TypeTags.ARRAY ||
                            memType.tag == TypeTags.TUPLE) {
                        if (isSameOrderedType(memType, firstTypeInUnion)) {
                            allMembersOrdered = true;
                            continue;
                        }
                        return false;
                    } else if (memType.tag != firstTypeInUnion.tag && !isNil(memType) &&
                            !isIntOrStringType(memType.tag, firstTypeInUnion.tag)) {
                        return false;
                    }
                    allMembersOrdered = isOrderedType(memType, hasCycle);
                    if (!allMembersOrdered) {
                        break;
                    }
                }
                return allMembersOrdered;
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                return isOrderedType(elementType, hasCycle);
            case TypeTags.TUPLE:
                List<BType> tupleMemberTypes = ((BTupleType) type).getTupleTypes();
                for (BType memType : tupleMemberTypes) {
                    if (!isOrderedType(memType, hasCycle)) {
                        return false;
                    }
                }
                BType restType = ((BTupleType) type).restType;
                return restType == null || isOrderedType(restType, hasCycle);
            case TypeTags.FINITE:
                boolean isValueSpaceOrdered = false;
                Set<BLangExpression> valSpace = ((BFiniteType) type).getValueSpace();
                BType baseExprType = valSpace.iterator().next().getBType();
                for (BLangExpression expr : valSpace) {
                    if (!checkValueSpaceHasSameType((BFiniteType) type, baseExprType)) {
                        return false;
                    }
                    isValueSpaceOrdered = isOrderedType(expr.getBType(), hasCycle);
                    if (!isValueSpaceOrdered) {
                        break;
                    }
                }
                return isValueSpaceOrdered;
            case TypeTags.TYPEREFDESC:
                return isOrderedType(getReferredType(type), hasCycle);
            case TypeTags.INTERSECTION:
                return isOrderedType(getEffectiveTypeForIntersection(type), hasCycle);
            default:
                return isSimpleBasicType(type.tag);
        }
    }

    private boolean isIntOrStringType(int firstTypeTag, int secondTypeTag) {
        return ((TypeTags.isIntegerTypeTag(firstTypeTag) || firstTypeTag == TypeTags.BYTE) &&
                (TypeTags.isIntegerTypeTag(secondTypeTag) || secondTypeTag == TypeTags.BYTE)) ||
                ((TypeTags.isStringTypeTag(firstTypeTag)) && (TypeTags.isStringTypeTag(secondTypeTag)));
    }

    public boolean isUnionOfSimpleBasicTypes(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.UNION) {
            Set<BType> memberTypes = ((BUnionType) type).getMemberTypes();
            for (BType memType : memberTypes) {
                memType = getReferredType(memType);
                if (!isSimpleBasicType(memType.tag)) {
                    return false;
                }
            }
            return true;
        }
        return isSimpleBasicType(type.tag);
    }

    public BType findCompatibleType(BType type) {
        switch (type.tag) {
            case TypeTags.DECIMAL:
            case TypeTags.FLOAT:
            case TypeTags.XML:
            case TypeTags.XML_TEXT:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
            case TypeTags.XML_COMMENT:
                return type;
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                return symTable.intType;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                return symTable.stringType;
            case TypeTags.UNION:
                LinkedHashSet<BType> memberTypes = ((BUnionType) type).getMemberTypes();
                return findCompatibleType(memberTypes.iterator().next());
            case TypeTags.TYPEREFDESC:
                return findCompatibleType(((BTypeReferenceType) type).referredType);
            default:
                Set<BLangExpression> valueSpace = ((BFiniteType) type).getValueSpace();
                return findCompatibleType(valueSpace.iterator().next().getBType());
        }
    }

    public boolean isNonNilSimpleBasicTypeOrString(BType bType) {
        BType type = getReferredType(bType);
        if (type.tag == TypeTags.UNION) {
            Set<BType> memberTypes = ((BUnionType) type).getMemberTypes();
            for (BType member : memberTypes) {
                BType memType = getReferredType(member);
                if (memType.tag == TypeTags.FINITE || memType.tag == TypeTags.UNION) {
                    isNonNilSimpleBasicTypeOrString(memType);
                    continue;
                }
                if (memType.tag == TypeTags.NIL || !isSimpleBasicType(memType.tag)) {
                    return false;
                }
            }
            return true;
        } else if (type.tag == TypeTags.FINITE) {
            for (BLangExpression expression: ((BFiniteType) type).getValueSpace()) {
                BType exprType = getReferredType(expression.getBType());
                if (exprType.tag == TypeTags.NIL || !isSimpleBasicType(exprType.tag)) {
                    return false;
                }
            }
            return true;
        }
        return type.tag != TypeTags.NIL && isSimpleBasicType(type.tag);
    }

    public boolean isSubTypeOfReadOnlyOrIsolatedObjectUnion(BType bType) {
        BType type = getReferredType(bType);
        if (isInherentlyImmutableType(type) || Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return true;
        }

        int tag = type.tag;

        if (tag == TypeTags.OBJECT) {
            return isIsolated(type);
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isSubTypeOfReadOnlyOrIsolatedObjectUnion(memberType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isIsolated(BType type) {
        return Symbols.isFlagOn(type.flags, Flags.ISOLATED);
    }

    private boolean isImmutable(BType type) {
        return Symbols.isFlagOn(type.flags, Flags.READONLY);
    }

    BType getTypeWithoutNil(BType type) {
        BType constraint = getReferredType(type);
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

        return BUnionType.create(null, new LinkedHashSet<>(nonNilTypes));
    }

    public boolean isFixedLengthTuple(BTupleType bTupleType) {
        return bTupleType.restType == null || isNeverTypeOrStructureTypeWithARequiredNeverMember(bTupleType.restType);
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
                return isNeverTypeOrStructureTypeWithARequiredNeverMember(getReferredType(type), visitedTypeSet);
            default:
                return false;
        }
    }

    public boolean isNeverType(BType type) {
        if (type.tag == NEVER) {
            return true;
        } else if (type.tag == TypeTags.TYPEREFDESC) {
            return isNeverType(getReferredType(type));
        } else if (type.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = ((BUnionType) type).getMemberTypes();
            return memberTypes.stream().allMatch(this::isNeverType);
        }
        return false;
    }

    boolean isSingletonType(BType bType) {
        BType type = getReferredType(bType);
        return type.tag == TypeTags.FINITE && ((BFiniteType) type).getValueSpace().size() == 1;
    }

    boolean isSameSingletonType(BFiniteType type1, BFiniteType type2) {
        BLangLiteral expr1 = (BLangLiteral) type1.getValueSpace().iterator().next();
        BLangLiteral expr2 = (BLangLiteral) type2.getValueSpace().iterator().next();
        return expr1.value.equals(expr2.value);
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

    public static String getPackageIdString(PackageID packageID) {
        return packageID.isTestPkg ? packageID.toString() + "_testable" : packageID.toString();
    }

    private static class ListenerValidationModel {
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
                    BUnionType.create(null, symtable.stringType, symtable.arrayStringType, symtable.nilType);
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
            BType type = Types.getReferredType(bType);
            if (type.tag == TypeTags.UNION) {
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (!isServiceObject(memberType)) {
                        return false;
                    }
                }
                return true;
            }

            if (type.tag != TypeTags.OBJECT) {
                return false;
            }

            return Symbols.isService(type.tsymbol);
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
        LEFT, RIGHT, NON;
    }

    private void populateBasicTypes(BType type, Set<BasicTypes> basicTypes) {
        type = getReferredType(type);

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
            case TypeTags.INTERSECTION:
                populateBasicTypes(((BIntersectionType) type).effectiveType, basicTypes);
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
                for (BLangExpression expression : ((BFiniteType) type).getValueSpace()) {
                    populateBasicTypes(expression.getBType(), basicTypes);
                }
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
        Stack<SymbolEnv> queryEnvs = new Stack<>();
        Stack<BLangNode> queryFinalClauses = new Stack<>();
        HashSet<BType> checkedErrorList = new HashSet<>();
        boolean breakToParallelQueryEnv = false;
        int letCount = 0;
        boolean nonErrorLoggingCheck = false;
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
}
