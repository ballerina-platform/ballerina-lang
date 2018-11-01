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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This class consists of utility methods which operate on types.
 * These utility methods allows you to check the compatibility of two types,
 * i.e. check whether two types are equal, check whether one type is assignable to another type etc.
 *
 * @since 0.94
 */
public class Types {

    /**
     * @since 0.94
     */
    public enum RecordKind {
        STRUCT("struct"),
        MAP("map"),
        JSON("json");

        public String value;

        RecordKind(String value) {
            this.value = value;
        }
    }

    private static final CompilerContext.Key<Types> TYPES_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private BLangDiagnosticLog dlog;

    private Stack<BType> typeStack;

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
        this.typeStack = new Stack<>();
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
        return checkType(node, actualType, expType, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    public BType checkType(BLangExpression expr,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        expr.type = checkType(expr.pos, actualType, expType, diagCode);
        if (expr.type.tag == TypeTags.ERROR) {
            return expr.type;
        }

        // Set an implicit cast expression, if applicable
        setImplicitCastExpr(expr, actualType, expType);

        return expr.type;
    }

    public BType checkType(DiagnosticPos pos,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        if (expType.tag == TypeTags.ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return actualType;
        } else if (actualType.tag == TypeTags.ERROR) {
            return actualType;
        } else if (isAssignable(actualType, expType)) {
            return actualType;
        }

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(pos, diagCode, expType, actualType);
        return symTable.errType;
    }

    public boolean isSameType(BType source, BType target) {
        return target.accept(sameTypeVisitor, source);
    }

    public boolean isValueType(BType type) {
        return type.tag < TypeTags.TYPEDESC;
    }

    public boolean isBrandedType(BType type) {
        return type.tag < TypeTags.ANY;
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
        return isAssignable(source, target, new ArrayList<>());
    }

    private boolean isAssignable(BType source, BType target, List<TypePair> unresolvedTypes) {
        if (isSameType(source, target)) {
            return true;
        }

        if (source.tag == TypeTags.NIL && (isNullable(target) || target.tag == TypeTags.JSON)) {
            return true;
        }

        if (target.tag == TypeTags.ANY && !isValueType(source)) {
            return true;
        }

        // This doesn't compare constraints as there is a requirement to be able to return raw table type and assign
        // it to a constrained table reference.
        if (target.tag == TypeTags.TABLE && source.tag == TypeTags.TABLE) {
            return true;
        }

        if (target.tag == TypeTags.STREAM && source.tag == TypeTags.STREAM) {
            return isAssignable(((BStreamType) source).constraint, ((BStreamType) target).constraint, unresolvedTypes);
        }

        BSymbol symbol = symResolver.resolveImplicitConversionOp(source, target);
        if (symbol != symTable.notFoundSymbol) {
            return true;
        }

        if ((target.tag == TypeTags.UNION || source.tag == TypeTags.UNION) &&
                isAssignableToUnionType(source, target, unresolvedTypes)) {
            return true;
        }

        if (target.tag == TypeTags.JSON) {
            if (source.tag == TypeTags.JSON) {
                return ((BJSONType) target).constraint.tag == TypeTags.NONE;
            }
            if (source.tag == TypeTags.ARRAY) {
                return isArrayTypesAssignable(source, target, unresolvedTypes);
            }
        }

        if (target.tag == TypeTags.FUTURE && source.tag == TypeTags.FUTURE) {
            if (((BFutureType) target).constraint.tag == TypeTags.NONE) {
                return true;
            }
            return isAssignable(((BFutureType) source).constraint, ((BFutureType) target).constraint, unresolvedTypes);
        }

        if (target.tag == TypeTags.MAP && source.tag == TypeTags.MAP) {
            // Here source condition is added for prevent assigning map union constrained
            // to map any constrained.
            if (((BMapType) target).constraint.tag == TypeTags.ANY &&
                    ((BMapType) source).constraint.tag != TypeTags.UNION) {
                return true;
            }
            if (checkStructEquivalency(((BMapType) source).constraint, ((BMapType) target).constraint,
                    unresolvedTypes)) {
                return true;
            }
        }

        if ((source.tag == TypeTags.OBJECT || source.tag == TypeTags.RECORD)
                && (target.tag == TypeTags.OBJECT || target.tag == TypeTags.RECORD)) {
            return checkStructEquivalency(source, target, unresolvedTypes);
        }

        if (source.tag == TypeTags.TUPLE || target.tag == TypeTags.TUPLE) {
            return isTupleTypeAssignable(source, target, unresolvedTypes);
        }

        return source.tag == TypeTags.ARRAY && target.tag == TypeTags.ARRAY &&
                isArrayTypesAssignable(source, target, unresolvedTypes);
    }

    private boolean isTupleTypeAssignable(BType source, BType target, List<TypePair> unresolvedTypes) {
        if (source.tag != TypeTags.TUPLE || target.tag != TypeTags.TUPLE) {
            return false;
        }

        BTupleType lhsTupleType = (BTupleType) target;
        BTupleType rhsTupleType = (BTupleType) source;

        if (lhsTupleType.tupleTypes.size() != rhsTupleType.tupleTypes.size()) {
            return false;
        }

        for (int i = 0; i < lhsTupleType.tupleTypes.size(); i++) {
            if (!isAssignable(rhsTupleType.tupleTypes.get(i), lhsTupleType.tupleTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    public boolean isArrayTypesAssignable(BType source, BType target, List<TypePair> unresolvedTypes) {
        if (target.tag == TypeTags.ARRAY && source.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhsArrayType = (BArrayType) target;
            BArrayType rhsArrayType = (BArrayType) source;
            if (lhsArrayType.state == BArrayState.UNSEALED) {
                return isArrayTypesAssignable(rhsArrayType.eType, lhsArrayType.eType, unresolvedTypes);
            }
            return checkSealedArraySizeEquality(rhsArrayType, lhsArrayType)
                    && isArrayTypesAssignable(rhsArrayType.eType, lhsArrayType.eType, unresolvedTypes);

        } else if (source.tag == TypeTags.ARRAY) {
            // Only the right-hand side is an array type

            // If the target type is a JSON, then element type of the rhs array
            // should only be JSON. This is to avoid assigning of value-type arrays
            // to JSON.
            if (target.tag == TypeTags.JSON) {
                return getElementType(source).tag == TypeTags.JSON;
            }

            // Then lhs type should 'any' type
            return target.tag == TypeTags.ANY;

        } else if (target.tag == TypeTags.ARRAY) {
            // Only the left-hand side is an array type
            return false;
        }

        // Now both types are not array types and they have to be assignable
        if (isAssignable(source, target, unresolvedTypes)) {
            return true;
        }

        if (target.tag == TypeTags.UNION) {
            return isAssignable(source, target, unresolvedTypes);
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return target.tag == TypeTags.ANY && !isValueType(source);
    }

    public boolean checkFunctionTypeEquality(BInvokableType source, BInvokableType target) {
        return checkFunctionTypeEquality(source, target, new ArrayList<>());
    }

    private boolean checkFunctionTypeEquality(BInvokableType source, BInvokableType target,
                                              List<TypePair> unresolvedTypes) {
        if (source.paramTypes.size() != target.paramTypes.size()) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.size(); i++) {
            if (target.paramTypes.get(i).tag != TypeTags.ANY
                    && !isAssignable(source.paramTypes.get(i), target.paramTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }

        if (source.retType == null && target.retType == null) {
            return true;
        } else if (source.retType == null || target.retType == null) {
            return false;
        }

        return isAssignable(source.retType, target.retType, unresolvedTypes);
    }

    public boolean checkArrayEquality(BType source, BType target, List<TypePair> unresolvedTypes) {
        if (target.tag == TypeTags.ARRAY && source.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhsArrayType = (BArrayType) target;
            BArrayType rhsArrayType = (BArrayType) source;
            if (lhsArrayType.state == BArrayState.UNSEALED) {
                return checkArrayEquality(lhsArrayType.eType, rhsArrayType.eType, unresolvedTypes);
            }
            return checkSealedArraySizeEquality(rhsArrayType, lhsArrayType)
                    && isArrayTypesAssignable(rhsArrayType.eType, lhsArrayType.eType, unresolvedTypes);
        }

        // Now one or both types are not array types and they have to be equal
        return isSameType(source, target);
    }

    public boolean checkSealedArraySizeEquality(BArrayType rhsArrayType, BArrayType lhsArrayType) {
        return lhsArrayType.size == rhsArrayType.size;
    }

    public boolean checkStructEquivalency(BType rhsType, BType lhsType) {
        return checkStructEquivalency(rhsType, lhsType, new ArrayList<>());
    }

    private boolean checkStructEquivalency(BType rhsType, BType lhsType, List<TypePair> unresolvedTypes) {
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

    public boolean checkObjectEquivalency(BObjectType rhsType, BObjectType lhsType, List<TypePair> unresolvedTypes) {
        // Both objects should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Symbols.isFlagOn(lhsType.tsymbol.flags ^ rhsType.tsymbol.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both objects are private, they should be in the same package.
        if (Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size()) {
            return false;
        }

        return Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID == lhsType.tsymbol.pkgID ?
                checkPrivateObjectEquivalency(lhsType, rhsType, unresolvedTypes) :
                checkPublicObjectEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    public boolean checkRecordEquivalency(BRecordType rhsType, BRecordType lhsType, List<TypePair> unresolvedTypes) {
        // Both records should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Symbols.isFlagOn(lhsType.tsymbol.flags ^ rhsType.tsymbol.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both records are private, they should be in the same package.
        if (Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size()) {
            return false;
        }

        // If only one is a closed record, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // The rest field types should match if they are open records
        if ((!lhsType.sealed && !rhsType.sealed) &&
                !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType);
    }

    List<BType> checkForeachTypes(BLangNode collection, int variableSize) {
        BType collectionType = collection.type;
        List<BType> errorTypes;
        int maxSupportedTypes;
        switch (collectionType.tag) {
            case TypeTags.ARRAY:
                BArrayType bArrayType = (BArrayType) collectionType;
                if (variableSize == 1) {
                    return Lists.of(bArrayType.eType);
                } else if (variableSize == 2) {
                    return Lists.of(symTable.intType, bArrayType.eType);
                } else {
                    maxSupportedTypes = 2;
                    errorTypes = Lists.of(symTable.intType, bArrayType.eType);
                }
                break;
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                if (variableSize == 1) {
                    return Lists.of(bMapType.constraint);
                } else if (variableSize == 2) {
                    return Lists.of(symTable.stringType, bMapType.constraint);
                } else {
                    maxSupportedTypes = 2;
                    errorTypes = Lists.of(symTable.stringType, bMapType.constraint);
                }
                break;
            case TypeTags.JSON:
                if (variableSize == 1) {
                    return Lists.of(symTable.jsonType);
                } else {
                    maxSupportedTypes = 1;
                    errorTypes = Lists.of(symTable.jsonType);
                }
                break;
            case TypeTags.XML:
                if (variableSize == 1) {
                    return Lists.of(symTable.xmlType);
                } else if (variableSize == 2) {
                    return Lists.of(symTable.intType, symTable.xmlType);
                } else {
                    maxSupportedTypes = 2;
                    errorTypes = Lists.of(symTable.intType, symTable.xmlType);
                }
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                if (variableSize == 1) {
                    if (tableType.constraint.tag == TypeTags.NONE) {
                        return Lists.of(symTable.anyType);
                    }
                    return Lists.of(tableType.constraint);
                } else if (variableSize == 2) {
                    return Lists.of(symTable.intType, tableType.constraint);
                } else {
                    maxSupportedTypes = 1;
                    errorTypes = Lists.of(tableType.constraint);
                }
                break;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                if (variableSize == 1) {
                    return Lists.of(inferRecordFieldType(recordType));
                } else if (variableSize == 2) {
                    return Lists.of(symTable.stringType, inferRecordFieldType(recordType));
                } else {
                    maxSupportedTypes = 2;
                    errorTypes = Lists.of(symTable.stringType, symTable.anyType);
                }
                break;
            case TypeTags.ERROR:
                return Collections.nCopies(variableSize, symTable.errType);
            default:
                dlog.error(collection.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_COLLECTION, collectionType);
                return Collections.nCopies(variableSize, symTable.errType);
        }
        dlog.error(collection.pos, DiagnosticCode.ITERABLE_TOO_MANY_VARIABLES, collectionType);
        errorTypes.addAll(Collections.nCopies(variableSize - maxSupportedTypes, symTable.errType));
        return errorTypes;
    }

    public BType inferRecordFieldType(BRecordType recordType) {
        List<BField> fields = recordType.fields;
        BType inferredType = fields.get(0).type; // If all the fields are the same, doesn't matter which one we pick

        // If it's an open record, the rest field type should also be of the same type as the mandatory fields.
        if (!recordType.sealed && recordType.restFieldType.tag != inferredType.tag) {
            return symTable.anyType;
        }

        for (int i = 1; i < fields.size(); i++) {
            if (inferredType.tag != fields.get(i).type.tag) {
                return symTable.anyType;
            }
        }

        return inferredType;
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType expType) {
        BSymbol symbol = symResolver.resolveImplicitConversionOp(actualType, expType);
        if ((expType.tag == TypeTags.UNION || expType.tag == TypeTags.FINITE) && isValueType(actualType)) {
            symbol = symResolver.resolveImplicitConversionOp(actualType, symTable.anyType);
        }

        if (symbol == symTable.notFoundSymbol) {
            return;
        }

        BConversionOperatorSymbol conversionSym = (BConversionOperatorSymbol) symbol;
        BLangTypeConversionExpr implicitConversionExpr =
                (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        implicitConversionExpr.pos = expr.pos;
        implicitConversionExpr.expr = expr.impConversionExpr == null ? expr : expr.impConversionExpr;
        implicitConversionExpr.type = expType;
        implicitConversionExpr.targetType = expType;
        implicitConversionExpr.conversionSymbol = conversionSym;
        expr.impConversionExpr = implicitConversionExpr;
    }

    public BSymbol getConversionOperator(BType sourceType, BType targetType) {
        if (sourceType.tag == TypeTags.ERROR ||
                targetType.tag == TypeTags.ERROR ||
                sourceType == targetType) {
            return createConversionOperatorSymbol(sourceType, targetType, true, InstructionCodes.NOP);
        }

        return targetType.accept(conversionVisitor, sourceType);
    }

    public BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    /**
     * Check whether a given struct can be used to constraint a JSON.
     *
     * @param type struct type
     * @return flag indicating possibility of constraining
     */
    public boolean checkStructToJSONCompatibility(BType type) {
        return checkStructToJSONCompatibility(type, new ArrayList<>());
    }

    private boolean checkStructToJSONCompatibility(BType type, List<TypePair> unresolvedTypes) {
        if (type.tag != TypeTags.OBJECT && type.tag != TypeTags.RECORD) {
            return false;
        }

        List<BField> fields = ((BStructureType) type).fields;
        for (int i = 0; i < fields.size(); i++) {
            BType fieldType = fields.get(i).type;
            if (checkStructFieldToJSONCompatibility(type, fieldType, unresolvedTypes)) {
                continue;
            } else {
                return false;
            }
        }

        if (type.tag == TypeTags.RECORD && !((BRecordType) type).sealed) {
            return checkStructFieldToJSONCompatibility(type, ((BRecordType) type).restFieldType, unresolvedTypes);
        }

        return true;
    }


    // private methods

    private BConversionOperatorSymbol createConversionOperatorSymbol(BType sourceType,
                                                                     BType targetType,
                                                                     boolean safe,
                                                                     int opcode) {
        return Symbols.createConversionOperatorSymbol(sourceType, targetType, symTable.errStructType,
                false, safe, opcode, null, null);
    }

    private BSymbol getExplicitArrayConversionOperator(BType t, BType s, BType origT, BType origS) {
        return getExplicitArrayConversionOperator(t, s, origT, origS, new ArrayList<>());
    }

    private BSymbol getExplicitArrayConversionOperator(BType t, BType s, BType origT, BType origS,
                                                       List<TypePair> unresolvedTypes) {
        if (t.tag == TypeTags.ARRAY && s.tag == TypeTags.ARRAY) {
            return getExplicitArrayConversionOperator(((BArrayType) t).eType, ((BArrayType) s).eType, origT, origS,
                    unresolvedTypes);
        } else if (t.tag == TypeTags.ARRAY) {
            if (s.tag == TypeTags.JSON) {
                // If the source JSON is constrained, then it is not an array 
                if (((BJSONType) s).constraint != null && ((BJSONType) s).constraint.tag != TypeTags.NONE) {
                    return symTable.notFoundSymbol;
                }
                // If the target type is JSON array, and the source type is a JSON
                if (getElementType(t).tag == TypeTags.JSON) {
                    return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
                } else {
                    return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.JSON2ARRAY);
                }
            }

            // If only the target type is an array type, then the source type must be of type 'any'
            if (s.tag == TypeTags.ANY) {
                return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }
            return symTable.notFoundSymbol;

        } else if (s.tag == TypeTags.ARRAY) {
            if (t.tag == TypeTags.JSON) {
                if (getElementType(s).tag == TypeTags.JSON) {
                    return createConversionOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
                } else {
                    // the conversion visitor below may report back a conversion symbol, which is
                    // unsafe (e.g. T2JSON), so we must make our one also unsafe
                    if (conversionVisitor.visit((BJSONType) t, ((BArrayType) s).eType) != symTable.notFoundSymbol) {
                        return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.ARRAY2JSON);
                    }
                }
            }

            // If only the source type is an array type, then the target type must be of type 'any'
            if (t.tag == TypeTags.ANY) {
                return createConversionOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
            }
            return symTable.notFoundSymbol;
        }

        // Now both types are not array types
        if (s == t) {
            return createConversionOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
        }

        if ((s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD)
                && (t.tag == TypeTags.OBJECT || t.tag == TypeTags.RECORD)) {
            if (checkStructEquivalency(s, t, unresolvedTypes)) {
                return createConversionOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
            } else {
                return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }
        }

        // In this case, target type should be of type 'any' and the source type cannot be a value type
        if (t == symTable.anyType && !isValueType(s)) {
            return createConversionOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
        }

        if (!isValueType(t) && s == symTable.anyType) {
            return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
        }
        return symTable.notFoundSymbol;
    }

    private boolean checkStructFieldToJSONCompatibility(BType structType, BType fieldType,
                                                        List<TypePair> unresolvedTypes) {
        // If the struct field type is the struct
        if (structType == fieldType) {
            return true;
        }

        if (fieldType.tag == TypeTags.OBJECT || fieldType.tag == TypeTags.RECORD) {
            return checkStructToJSONCompatibility(fieldType, unresolvedTypes);
        }

        if (isAssignable(fieldType, symTable.jsonType, unresolvedTypes)) {
            return true;
        }

        if (fieldType.tag == TypeTags.ARRAY) {
            return checkStructFieldToJSONCompatibility(structType, getElementType(fieldType), unresolvedTypes);
        }

        return false;
    }

    /**
     * Check whether a given struct can be converted into a JSON.
     *
     * @param type struct type
     * @return flag indicating possibility of conversion
     */
    private boolean checkStructToJSONConvertibility(BType type, List<TypePair> unresolvedTypes) {
        if (type.tag != TypeTags.OBJECT && type.tag != TypeTags.RECORD) {
            return false;
        }

        List<BField> fields = ((BStructureType) type).fields;
        for (int i = 0; i < fields.size(); i++) {
            BType fieldType = fields.get(i).type;
            if (checkStructFieldToJSONConvertibility(type, fieldType, unresolvedTypes)) {
                continue;
            } else {
                return false;
            }
        }

        if (type.tag == TypeTags.RECORD && !((BRecordType) type).sealed) {
            return checkStructFieldToJSONConvertibility(type, ((BRecordType) type).restFieldType, unresolvedTypes);
        }

        return true;
    }

    private boolean isNullable(BType fieldType) {
        return fieldType.isNullable();
    }

    private boolean checkStructFieldToJSONConvertibility(BType structType, BType fieldType,
                                                         List<TypePair> unresolvedTypes) {
        // If the struct field type is the struct
        if (structType == fieldType) {
            return true;
        }

        if (fieldType.tag == TypeTags.MAP || fieldType.tag == TypeTags.ANY) {
            return true;
        }

        if (fieldType.tag == TypeTags.OBJECT || fieldType.tag == TypeTags.RECORD) {
            return checkStructToJSONConvertibility(fieldType, unresolvedTypes);
        }

        if (fieldType.tag == TypeTags.ARRAY) {
            return checkStructFieldToJSONConvertibility(structType, getElementType(fieldType), unresolvedTypes);
        }

        return isAssignable(fieldType, symTable.jsonType, unresolvedTypes);
    }

    private boolean checkUnionTypeToJSONConvertibility(BUnionType type, BJSONType target) {
        // Check whether all the member types are convertible to JSON
        return type.memberTypes.stream()
                .anyMatch(memberType -> conversionVisitor.visit(memberType, target) == symTable.notFoundSymbol);
    }

    private boolean checkJsonToMapConvertibility(BJSONType src, BMapType target) {
        return true;
    }

    private boolean checkMapToJsonConvertibility(BMapType src, BJSONType target) {
        return true;
    }

    private BTypeVisitor<BType, BSymbol> conversionVisitor = new BTypeVisitor<BType, BSymbol>() {

        @Override
        public BSymbol visit(BType t, BType s) {
            return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BBuiltInRefType t, BType s) {
            return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BAnyType t, BType s) {
            if (isValueType(s)) {
                return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
            }

            // Here condition is added for prevent explicit cast assigning map union constrained
            // to map any constrained.
            if (s.tag == TypeTags.MAP &&
                    ((BMapType) s).constraint.tag == TypeTags.UNION) {
                return symTable.notFoundSymbol;
            }

            return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
        }

        @Override
        public BSymbol visit(BMapType t, BType s) {
            if (isSameType(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.MAP) {
                if (t.constraint.tag == TypeTags.ANY) {
                    return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
                } else if (((BMapType) s).constraint.tag == TypeTags.ANY) {
                    return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
                } else if (checkStructEquivalency(((BMapType) s).constraint,
                        t.constraint)) {
                    return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
                } else {
                    return symTable.notFoundSymbol;
                }
            } else if (s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.T2MAP);
            } else if (s.tag == TypeTags.JSON) {
                if (!checkJsonToMapConvertibility((BJSONType) s, t)) {
                    return symTable.notFoundSymbol;
                }
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.JSON2MAP);
            } else if (t.constraint.tag != TypeTags.ANY) {
                // Semantically fail rest of the casts for Constrained Maps.
                // Eg:- ANY2MAP cast is undefined for Constrained Maps.
                return symTable.notFoundSymbol;
            }

            return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BXMLType t, BType s) {
            return visit((BBuiltInRefType) t, s);
        }

        @Override
        public BSymbol visit(BJSONType t, BType s) {
            // Handle constrained JSON
            if (isSameType(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD) {
//                TODO: do type checking and fail for obvious incompatible types
//                if (checkStructToJSONConvertibility(s)) {
//                    return createConversionOperatorSymbol(s, t, false, InstructionCodes.T2JSON);
//                } else {
//                    return symTable.notFoundSymbol;
//                }
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.T2JSON);
            } else if (s.tag == TypeTags.JSON) {
                if (t.constraint.tag == TypeTags.NONE) {
                    return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
                } else if (((BJSONType) s).constraint.tag == TypeTags.NONE) {
                    return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
                } else if (checkStructEquivalency(((BJSONType) s).constraint, t.constraint)) {
                    return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
                }
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.ARRAY) {
                if (t.constraint != null && t.constraint.tag != TypeTags.NONE) {
                    return symTable.notFoundSymbol;
                }
                return getExplicitArrayConversionOperator(t, s, t, s);
            } else if (s.tag == TypeTags.UNION) {
                if (checkUnionTypeToJSONConvertibility((BUnionType) s, t)) {
                    return createConversionOperatorSymbol(s, t, false, InstructionCodes.O2JSON);
                }
                return symTable.notFoundSymbol;
            } else if (s.tag == TypeTags.MAP) {
                if (!checkMapToJsonConvertibility((BMapType) s, t)) {
                    return symTable.notFoundSymbol;
                }
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.MAP2JSON);
            } else if (t.constraint.tag != TypeTags.NONE) {
                return symTable.notFoundSymbol;
            }

            return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BArrayType t, BType s) {
            return getExplicitArrayConversionOperator(t, s, t, s);
        }

        @Override
        public BSymbol visit(BObjectType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2T);
            }

            if ((s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD) && checkStructEquivalency(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD || s.tag == TypeTags.ANY) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.MAP) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.MAP2T);
            } else if (s.tag == TypeTags.JSON) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.JSON2T);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BRecordType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2T);
            }

            if ((s.tag == TypeTags.RECORD || s.tag == TypeTags.OBJECT) && checkStructEquivalency(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.RECORD || s.tag == TypeTags.OBJECT || s.tag == TypeTags.ANY) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.MAP) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.MAP2T);
            } else if (s.tag == TypeTags.JSON) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.JSON2T);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BTableType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2DT);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BTupleType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            }
            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BStreamType t, BType s) {
            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BInvokableType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.INVOKABLE && checkFunctionTypeEquality((BInvokableType) s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BUnionType t, BType s) {

            // TODO handle union type to
            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BErrorType t, BType s) {
            // TODO Implement. Not needed for now.
            throw new AssertionError();
        }

        @Override
        public BSymbol visit(BFutureType t, BType s) {
            return null;
        }

        @Override
        public BSymbol visit(BFiniteType t, BType s) {
            return symTable.notFoundSymbol;
        }
    };

    private BTypeVisitor<BType, Boolean> sameTypeVisitor = new BTypeVisitor<BType, Boolean>() {
        @Override
        public Boolean visit(BType t, BType s) {
            return t == s;

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
        public Boolean visit(BMapType t, BType s) {
            if (s.tag != TypeTags.MAP) {
                return false;
            }
            // At this point both source and target types are of map types. Inorder to be equal in type as whole
            // constraints should be in equal type.
            BMapType sType = ((BMapType) s);
            return isSameType(sType.constraint, t.constraint);
        }

        @Override
        public Boolean visit(BFutureType t, BType s) {
            return s.tag == TypeTags.FUTURE && t.constraint.tag == ((BFutureType) s).constraint.tag;
        }

        @Override
        public Boolean visit(BXMLType t, BType s) {
            return visit((BBuiltInRefType) t, s);
        }

        @Override
        public Boolean visit(BJSONType t, BType s) {
            if (s.tag != TypeTags.JSON) {
                return false;
            }

            BJSONType srcType = ((BJSONType) s);
            if (srcType.constraint.tag == t.constraint.tag && t.constraint.tag == TypeTags.NONE) {
                // Both source and the target types are JSON types with no constraints
                return true;
            }

            return isSameType(((BJSONType) s).constraint, t.constraint);
        }

        @Override
        public Boolean visit(BArrayType t, BType s) {
            return s.tag == TypeTags.ARRAY && checkArrayEquality(s, t, new ArrayList<>());
        }

        @Override
        public Boolean visit(BObjectType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BRecordType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BTableType t, BType s) {
            return t == s;
        }

        public Boolean visit(BTupleType t, BType s) {
            if (s.tag != TypeTags.TUPLE) {
                return false;
            }
            BTupleType source = (BTupleType) s;
            if (source.tupleTypes.size() != t.tupleTypes.size()) {
                return false;
            }
            for (int i = 0; i < source.tupleTypes.size(); i++) {
                if (t.getTupleTypes().get(i) == symTable.noType) {
                    continue;
                }
                if (!isSameType(source.getTupleTypes().get(i), t.tupleTypes.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visit(BStreamType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BInvokableType t, BType s) {
            return s.tag == TypeTags.INVOKABLE &&
                    checkFunctionTypeEquality((BInvokableType) s, t);
        }

        @Override
        public Boolean visit(BUnionType tUnionType, BType s) {
            if (s.tag != TypeTags.UNION) {
                return false;
            }

            BUnionType sUnionType = (BUnionType) s;

            if (sUnionType.memberTypes.size()
                    != tUnionType.memberTypes.size()) {
                return false;
            }

            Set<BType> sourceTypes = new HashSet<>();
            Set<BType> targetTypes = new HashSet<>();
            sourceTypes.addAll(sUnionType.memberTypes);
            targetTypes.addAll(tUnionType.memberTypes);

            boolean notSameType = sourceTypes
                    .stream()
                    .map(sT -> targetTypes
                            .stream()
                            .anyMatch(it -> isSameType(it, sT)))
                    .anyMatch(foundSameType -> !foundSameType);
            return !notSameType;
        }

        @Override
        public Boolean visit(BErrorType t, BType s) {
            return true;
        }

        @Override
        public Boolean visit(BFiniteType t, BType s) {
            return s == t;
        }
    };

    private boolean checkPrivateObjectEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                        List<TypePair> unresolvedTypes) {
        for (int fieldCounter = 0; fieldCounter < lhsType.fields.size(); fieldCounter++) {
            BField lhsField = lhsType.fields.get(fieldCounter);
            BField rhsField = rhsType.fields.get(fieldCounter);
            if (lhsField.name.equals(rhsField.name) &&
                    isSameType(rhsField.type, lhsField.type)) {
                continue;
            }
            return false;
        }

        BStructureTypeSymbol lhsStructSymbol = (BStructureTypeSymbol) lhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BStructureTypeSymbol) rhsType.tsymbol).attachedFuncs;
        int lhsAttachedFuncCount = lhsStructSymbol.initializerFunc != null ? lhsFuncs.size() - 1 : lhsFuncs.size();
        if (lhsAttachedFuncCount > rhsFuncs.size()) {
            return false;
        }

        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsStructSymbol.initializerFunc || lhsFunc == lhsStructSymbol.defaultsValuesInitFunc) {
                continue;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFieldEquivalency(BStructureType lhsType, BStructureType rhsType) {
        Map<Name, BField> rhsFields = rhsType.fields.stream().collect(
                Collectors.toMap(BField::getName, field -> field));

        for (BField lhsField : lhsType.fields) {
            BField rhsField = rhsFields.get(lhsField.name);

            if (rhsField == null) {
                return false;
            }

            if (!isSameType(rhsField.type, lhsField.type)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkPublicObjectEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                List<TypePair> unresolvedTypes) {
        int fieldCounter = 0;
        for (; fieldCounter < lhsType.fields.size(); fieldCounter++) {
            BField lhsField = lhsType.fields.get(fieldCounter);
            BField rhsField = rhsType.fields.get(fieldCounter);
            if (Symbols.isPrivate(lhsField.symbol) ||
                    Symbols.isPrivate(rhsField.symbol)) {
                return false;
            }

            if (lhsField.name.equals(rhsField.name) &&
                    isSameType(rhsField.type, lhsField.type)) {
                continue;
            }
            return false;
        }

        // Check the rest of the fields in RHS type
        for (; fieldCounter < rhsType.fields.size(); fieldCounter++) {
            if (Symbols.isPrivate(rhsType.fields.get(fieldCounter).symbol)) {
                return false;
            }
        }

        BStructureTypeSymbol lhsStructSymbol = (BStructureTypeSymbol) lhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BStructureTypeSymbol) rhsType.tsymbol).attachedFuncs;
        int lhsAttachedFuncCount = lhsStructSymbol.initializerFunc != null ? lhsFuncs.size() - 1 : lhsFuncs.size();
        if (lhsAttachedFuncCount > rhsFuncs.size()) {
            return false;
        }

        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsStructSymbol.initializerFunc || lhsFunc == lhsStructSymbol.defaultsValuesInitFunc) {
                continue;
            }

            if (Symbols.isPrivate(lhsFunc.symbol)) {
                return false;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null || Symbols.isPrivate(rhsFunc.symbol)) {
                return false;
            }
        }

        // Check for private attached function of the RHS type
        for (BAttachedFunction rhsFunc : rhsFuncs) {
            if (Symbols.isPrivate(rhsFunc.symbol)) {
                return false;
            }
        }

        return true;
    }

    private BAttachedFunction getMatchingInvokableType(List<BAttachedFunction> rhsFuncList, BAttachedFunction lhsFunc,
                                                       List<TypePair> unresolvedTypes) {
        return rhsFuncList.stream()
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEquality(rhsFunc.type, lhsFunc.type, unresolvedTypes))
                .findFirst()
                .orElse(null);
    }

    private boolean isAssignableToUnionType(BType source, BType target, List<TypePair> unresolvedTypes) {
        Set<BType> sourceTypes = new HashSet<>();
        Set<BType> targetTypes = new HashSet<>();

        if (source.tag == TypeTags.UNION) {
            BUnionType sourceUnionType = (BUnionType) source;
            sourceTypes.addAll(sourceUnionType.memberTypes);
        } else {
            sourceTypes.add(source);
        }

        if (target.tag == TypeTags.UNION) {
            BUnionType targetUnionType = (BUnionType) target;
            targetTypes.addAll(targetUnionType.memberTypes);
        } else {
            targetTypes.add(target);
        }

        boolean notAssignable = sourceTypes
                .stream()
                .map(s -> targetTypes
                        .stream()
                        .anyMatch(t -> isAssignable(s, t, unresolvedTypes)))
                .anyMatch(assignable -> !assignable);

        return !notAssignable;
    }

    /**
     * Check whether a given type has a default value.
     * i.e: A variable of the given type can be initialized without a rhs expression.
     * eg: foo x;
     *
     * @param pos position of the variable.
     * @param type Type to check the existence if a default value
     * @return Flag indicating whether the given type has a default value
     */
    public boolean defaultValueExists(DiagnosticPos pos, BType type) {
        if (type.tsymbol != null && Symbols.isFlagOn(type.tsymbol.flags, Flags.DEFAULTABLE)) {
            return true;
        }

        if (typeStack.contains(type)) {
            dlog.error(pos, DiagnosticCode.CYCLIC_TYPE_REFERENCE, typeStack);
            return false;
        }
        typeStack.add(type);

        boolean result;

        if (type.tsymbol == null) {
            result = checkDefaultable(pos, type);
            typeStack.pop();
            return result;
        }

        if ((type.tsymbol.flags & Flags.DEFAULTABLE_CHECKED) == Flags.DEFAULTABLE_CHECKED) {
            result = (type.tsymbol.flags & Flags.DEFAULTABLE) == Flags.DEFAULTABLE;
            typeStack.pop();
            if (result) {
                type.tsymbol.flags |= Flags.DEFAULTABLE;
            }
            return result;
        }
        if (checkDefaultable(pos, type)) {
            type.tsymbol.flags |= Flags.asMask(EnumSet.of(Flag.DEFAULTABLE_CHECKED, Flag.DEFAULTABLE));
            typeStack.pop();
            return true;
        }
        type.tsymbol.flags |= Flags.asMask(EnumSet.of(Flag.DEFAULTABLE_CHECKED));
        typeStack.pop();
        return false;
    }

    private boolean checkDefaultable(DiagnosticPos pos, BType type) {
        if (type.isNullable()) {
            return true;
        }

        if (type.tag == TypeTags.OBJECT || type.tag == TypeTags.RECORD) {
            BStructureType structType = (BStructureType) type;

            if (structType.tsymbol.kind == SymbolKind.RECORD) {
                for (BField field : structType.fields) {
                    if (!field.expAvailable && !defaultValueExists(pos, field.type)) {
                        return false;
                    }
                }
                return true;
            }

            BObjectTypeSymbol structSymbol = (BObjectTypeSymbol) structType.tsymbol;
            if ((structSymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
                return false;
            } else if (structSymbol.initializerFunc != null && structSymbol.initializerFunc.symbol.params.size() > 0) {
                return false;
            }

            for (BAttachedFunction func : structSymbol.attachedFuncs) {
                if ((func.symbol.flags & Flags.INTERFACE) == Flags.INTERFACE) {
                    return false;
                }
            }
            for (BField field : structType.fields) {
                if (!field.expAvailable && !defaultValueExists(pos, field.type)) {
                    return false;
                }
            }
            return true;
        }

        if (type.tag == TypeTags.INVOKABLE || type.tag == TypeTags.FINITE || type.tag == TypeTags.TYPEDESC) {
            return false;
        }

        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (defaultValueExists(pos, memberType)) {
                    return true;
                }
            }
        }
        return true;
    }

    public boolean isAssignableToFiniteType(BType type,
                                            BLangLiteral literalExpr) {
        if (type.tag == TypeTags.FINITE) {
            BFiniteType expType = (BFiniteType) type;
            boolean foundMember = expType.valueSpace
                    .stream()
                    .map(memberLiteral -> {
                        if (((BLangLiteral) memberLiteral).value == null) {
                            return literalExpr.value == null;
                        } else {
                            return ((BLangLiteral) memberLiteral).value.equals(literalExpr.value);
                        }
                    })
                    .anyMatch(found -> found);
            return foundMember;
        }
        return false;
    }

    public boolean isIntersectionExist(BType lhsType,
                                       BType rhsType) {
        Set<BType> lhsTypes = new HashSet<>();
        Set<BType> rhsTypes = new HashSet<>();

        lhsTypes.addAll(getMemberTypesRecursive(lhsType));
        rhsTypes.addAll(getMemberTypesRecursive(rhsType));

        if (lhsTypes.contains(symTable.anyType) ||
                rhsTypes.contains(symTable.anyType)) {
            return true;
        }

        boolean matchFound = lhsTypes
                .stream()
                .map(s -> rhsTypes
                        .stream()
                        .anyMatch(t -> isSameType(s, t)))
                .anyMatch(found -> found);
        return matchFound;
    }

    private Set<BType> getMemberTypesRecursive(BType bType) {
        Set<BType> memberTypes = new HashSet<>();
        if (bType.tag == TypeTags.FINITE) {
            BFiniteType expType = (BFiniteType) bType;
            expType.valueSpace.forEach(value -> {
                memberTypes.add(value.type);
            });
        } else if (bType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) bType;
            unionType.getMemberTypes().forEach(member -> {
                memberTypes.addAll(getMemberTypesRecursive(member));
            });
        } else {
            memberTypes.add(bType);
        }
        return memberTypes;
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
    }
}
