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
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BSemanticErrorType;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        if (expr.type.tag == TypeTags.SEMANTIC_ERROR) {
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

    public boolean isSameType(BType source, BType target) {
        return target.accept(sameTypeVisitor, source);
    }

    public boolean isValueType(BType type) {
        return type.tag < TypeTags.JSON;
    }

    public boolean isAnydata(BType type) {
        if (type.tag <= TypeTags.ANYDATA) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.MAP:
                return isAnydata(((BMapType) type).constraint);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                List<BType> fieldTypes = recordType.fields.stream()
                        .map(field -> field.type).collect(Collectors.toList());
                return isAnydata(fieldTypes) && (recordType.sealed || isAnydata(recordType.restFieldType));
            case TypeTags.UNION:
                return isAnydata(((BUnionType) type).memberTypes);
            case TypeTags.TUPLE:
                return isAnydata(((BTupleType) type).tupleTypes);
            case TypeTags.ARRAY:
                return isAnydata(((BArrayType) type).eType);
            case TypeTags.FINITE:
                Set<BType> valSpaceTypes = ((BFiniteType) type).valueSpace.stream()
                        .map(val -> val.type).collect(Collectors.toSet());
                return isAnydata(valSpaceTypes);
            default:
                return false;
        }
    }

    private boolean isAnydata(Collection<BType> types) {
        return types.stream().allMatch(this::isAnydata);
    }

    public boolean isLikeAnydataOrNotNil(BType type) {
        if (type.tag == TypeTags.NIL || (!isAnydata(type) && !isLikeAnydata(type))) {
            return false;
        }
        return true;
    }

    private boolean isLikeAnydata(BType type) {
        int typeTag = type.tag;
        if (typeTag == TypeTags.ANY) {
            return true;
        }

        // check for anydata element/member types as part of recursive calls with structured/union types
        if (isAnydata(type)) {
            return true;
        }

        if (type.tag == TypeTags.MAP && isLikeAnydata(((BMapType) type).constraint)) {
            return true;
        }

        if (type.tag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) type;
            return recordType.fields.stream()
                    .noneMatch(field -> !Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                            !(isLikeAnydata(field.type)));
        }

        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            return unionType.memberTypes.stream().anyMatch(this::isLikeAnydata);
        }

        if (type.tag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) type;
            return tupleType.getTupleTypes().stream().allMatch(this::isLikeAnydata);
        }

        return type.tag == TypeTags.ARRAY && isLikeAnydata(((BArrayType) type).eType);
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

    boolean isStampingAllowed(BType source, BType target) {
        return (isAssignable(source, target) || isAssignable(target, source) ||
                checkTypeEquivalencyForStamping(source, target) || checkTypeEquivalencyForStamping(target, source));
    }

    private boolean checkTypeEquivalencyForStamping(BType source, BType target) {

        if (target.tag == TypeTags.RECORD) {
            if (source.tag == TypeTags.RECORD) {
                TypePair pair = new TypePair(source, target);
                List<TypePair> unresolvedTypes = new ArrayList<>();
                unresolvedTypes.add(pair);
                return checkRecordEquivalencyForStamping((BRecordType) source, (BRecordType) target, unresolvedTypes);
            } else if (source.tag == TypeTags.MAP) {
                int mapConstraintTypeTag = ((BMapType) source).constraint.tag;
                if ((!(mapConstraintTypeTag == TypeTags.ANY || mapConstraintTypeTag == TypeTags.ANYDATA)) &&
                        ((BRecordType) target).sealed) {
                    for (BField field : ((BStructureType) target).getFields()) {
                        if (field.getType().tag != mapConstraintTypeTag) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else if (target.tag == TypeTags.JSON) {
            if (((BJSONType) target).getConstraint().tag != TypeTags.NONE) {
                if (source.tag == TypeTags.RECORD) {
                    return isStampingAllowed(((BRecordType) source).restFieldType,
                            ((BJSONType) target).getConstraint());
                } else if (source.tag == TypeTags.JSON) {
                    if (((BJSONType) source).getConstraint().tag != TypeTags.NONE) {
                        return isStampingAllowed(((BJSONType) source).getConstraint(),
                                ((BJSONType) target).getConstraint());
                    }
                } else if (source.tag == TypeTags.MAP) {
                    return isStampingAllowed(((BMapType) source).getConstraint(),
                            ((BJSONType) target).getConstraint());
                }
            } else if (source.tag == TypeTags.JSON || source.tag == TypeTags.RECORD || source.tag == TypeTags.MAP) {
                return true;
            }

        } else if (target.tag == TypeTags.MAP) {
            if (source.tag == TypeTags.MAP) {
                return isStampingAllowed(((BMapType) source).getConstraint(), ((BMapType) target).getConstraint());
            } else if (source.tag == TypeTags.UNION) {
                return checkUnionEquivalencyForStamping(source, target);
            }
        } else if (target.tag == TypeTags.ARRAY) {
            if (source.tag == TypeTags.JSON) {
                return ((BJSONType) source).getConstraint().tag == TypeTags.NONE ||
                        isStampingAllowed(((BJSONType) source).getConstraint(), ((BArrayType) target).eType);
            } else if (source.tag == TypeTags.TUPLE) {
                BType arrayElementType = ((BArrayType) target).eType;
                for (BType tupleMemberType : ((BTupleType) source).getTupleTypes()) {
                    if (!isStampingAllowed(tupleMemberType, arrayElementType)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (target.tag == TypeTags.UNION) {
            return checkUnionEquivalencyForStamping(source, target);
        } else if (target.tag == TypeTags.TUPLE && source.tag == TypeTags.TUPLE) {
            return checkTupleEquivalencyForStamping(source, target);
        }

        return false;
    }

    private boolean checkRecordEquivalencyForStamping(BRecordType rhsType, BRecordType lhsType,
                                                      List<TypePair> unresolvedTypes) {
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

        return checkFieldEquivalencyForStamping(lhsType, rhsType, unresolvedTypes);
    }

    private boolean checkFieldEquivalencyForStamping(BStructureType lhsType, BStructureType rhsType,
                                                     List<TypePair> unresolvedTypes) {
        Map<Name, BField> rhsFields = rhsType.fields.stream().collect(
                Collectors.toMap(BField::getName, field -> field));

        for (BField lhsField : lhsType.fields) {
            BField rhsField = rhsFields.get(lhsField.name);

            if (rhsField == null || !isStampingAllowed(rhsField.type, lhsField.type)) {
                return false;
            }
        }

        Map<Name, BField> lhsFields = lhsType.fields.stream().collect(
                Collectors.toMap(BField::getName, field -> field));
        for (BField rhsField : rhsType.fields) {
            BField lhsField = lhsFields.get(rhsField.name);

            if (lhsField == null && !isStampingAllowed(rhsField.type, ((BRecordType) lhsType).restFieldType)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkUnionEquivalencyForStamping(BType source, BType target) {
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
                        .anyMatch(t -> isStampingAllowed(s, t)))
                .anyMatch(assignable -> !assignable);

        return !notAssignable;
    }

    private boolean checkTupleEquivalencyForStamping(BType source, BType target) {
        if (source.tag != TypeTags.TUPLE || target.tag != TypeTags.TUPLE) {
            return false;
        }

        BTupleType lhsTupleType = (BTupleType) target;
        BTupleType rhsTupleType = (BTupleType) source;

        if (lhsTupleType.tupleTypes.size() != rhsTupleType.tupleTypes.size()) {
            return false;
        }

        for (int i = 0; i < lhsTupleType.tupleTypes.size(); i++) {
            if (!isStampingAllowed(rhsTupleType.tupleTypes.get(i), lhsTupleType.tupleTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isAssignable(BType source, BType target, List<TypePair> unresolvedTypes) {
        if (isSameType(source, target)) {
            return true;
        }

        if (source.tag == TypeTags.ERROR && target.tag == TypeTags.ERROR) {
            return isErrorTypeAssignable((BErrorType) source, (BErrorType) target);
        } else if (source.tag == TypeTags.ERROR && target.tag == TypeTags.ANY) {
            return false;
        }

        if (source.tag == TypeTags.NIL && (isNullable(target) || target.tag == TypeTags.JSON)) {
            return true;
        }

        // TODO: Remove the isValueType() check
        if (target.tag == TypeTags.ANY && !isValueType(source)) {
            return true;
        }

        if (target.tag == TypeTags.ANYDATA && isAnydata(source)) {
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

        // Check whether the source is a proper sub set of the target.
        if (source.tag == TypeTags.FINITE && target.tag == TypeTags.FINITE) {
            return ((BFiniteType) source).valueSpace.stream()
                    .allMatch(expression -> isAssignableToFiniteType(target, (BLangLiteral) expression));
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

            return isAssignable(((BMapType) source).constraint, ((BMapType) target).constraint, unresolvedTypes);
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

    private boolean isErrorTypeAssignable(BErrorType source, BErrorType target) {
        if (target == symTable.errorType) {
            return true;
        }
        return isAssignable(source.reasonType, target.reasonType) && isAssignable(source.detailType, target.detailType);
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
            // should only be a JSON supported type.
            if (target.tag == TypeTags.JSON) {
                return ((BJSONType) target).constraint.tag == TypeTags.NONE &&
                        isAssignable(((BArrayType) source).getElementType(), target, unresolvedTypes);
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
        if (!Symbols.isPublic(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size()) {
            return false;
        }

        return !Symbols.isPublic(lhsType.tsymbol) && rhsType.tsymbol.pkgID == lhsType.tsymbol.pkgID ?
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
        if (!Symbols.isPublic(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        // If the LHS record is closed and the RHS record is open, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // If both are open records, the rest field type of the RHS record should be assignable to the rest field
        // type of the LHS type.
        if ((!lhsType.sealed && !rhsType.sealed) &&
                !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
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
            case TypeTags.SEMANTIC_ERROR:
                return Collections.nCopies(variableSize, symTable.semanticError);
            default:
                dlog.error(collection.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_COLLECTION, collectionType);
                return Collections.nCopies(variableSize, symTable.semanticError);
        }
        dlog.error(collection.pos, DiagnosticCode.ITERABLE_TOO_MANY_VARIABLES, collectionType);
        errorTypes.addAll(Collections.nCopies(variableSize - maxSupportedTypes, symTable.semanticError));
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
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
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
        return Symbols.createConversionOperatorSymbol(sourceType, targetType, symTable.errorType,
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

            // TODO: 11/1/18 Remove the below check after verifying it doesn't break anything
            // Here condition is added for prevent explicit cast assigning map union constrained
            // to map any constrained.
            if (s.tag == TypeTags.MAP &&
                    ((BMapType) s).constraint.tag == TypeTags.UNION) {
                return symTable.notFoundSymbol;
            }

            return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
        }

        @Override
        public BSymbol visit(BAnydataType t, BType s) {
            if (isValueType(s)) {
                return symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(s, t));
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
            } else if (s.tag == TypeTags.ANYDATA) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2MAP);
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
            if (s == symTable.anyType || s == symTable.anydataType) {
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
            if (s == symTable.anyType || s.tag == symTable.anydataType.tag) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2DT);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BTupleType t, BType s) {
            if (s == symTable.anyType || s == symTable.anydataType) {
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
        public BSymbol visit(BSemanticErrorType t, BType s) {
            // TODO Implement. Not needed for now.
            throw new AssertionError();
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
            if (s.tag == symTable.anyType.tag || s.tag == symTable.anydataType.tag) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            }

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
        public Boolean visit(BAnydataType t, BType s) {
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
        public Boolean visit(BSemanticErrorType t, BType s) {
            return true;
        }

        @Override
        public Boolean visit(BErrorType t, BType s) {
            if (s.tag != TypeTags.ERROR) {
                return false;
            }
            BErrorType source = (BErrorType) s;
            return isSameType(source.reasonType, t.reasonType) && isSameType(source.detailType, t.detailType);
        }

        @Override
        public Boolean visit(BFiniteType t, BType s) {
            return s == t;
        }
    };

    private boolean checkPrivateObjectEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                  List<TypePair> unresolvedTypes) {
        Map<Name, BField> rhsFields =
                rhsType.fields.stream().collect(Collectors.toMap(BField::getName, field -> field));
        for (BField lhsField : lhsType.fields) {
            BField rhsField = rhsFields.get(lhsField.name);
            if (rhsField == null || !isAssignable(rhsField.type, lhsField.type)) {
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

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFieldEquivalency(BRecordType lhsType, BRecordType rhsType, List<TypePair> unresolvedTypes) {
        Map<Name, BField> rhsFields = rhsType.fields.stream().collect(Collectors.toMap(BField::getName, f -> f));

        // Check if the RHS record has corresponding fields to those of the LHS record.
        for (BField lhsField : lhsType.fields) {
            BField rhsField = rhsFields.get(lhsField.name);

            // If the LHS field is a required one, there has to be a corresponding required field in the RHS record.
            if (!Symbols.isOptional(lhsField.symbol) && (rhsField == null || Symbols.isOptional(rhsField.symbol))) {
                return false;
            }

            // If there is a corresponding RHS field, it should be assignable to the LHS field.
            if (rhsField != null && !isAssignable(rhsField.type, lhsField.type, unresolvedTypes)) {
                return false;
            }

            rhsFields.remove(lhsField.name);
        }

        // If there are any remaining RHS fields, the types of those should be assignable to the rest field type of
        // the LHS record.
        return rhsFields.entrySet().stream().allMatch(
                fieldEntry -> isAssignable(fieldEntry.getValue().type, lhsType.restFieldType, unresolvedTypes));
    }

    private boolean checkPublicObjectEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                 List<TypePair> unresolvedTypes) {
        Map<Name, BField> rhsFields =
                rhsType.fields.stream().collect(Collectors.toMap(BField::getName, field -> field));

        // Check the whether there is any private fields in RHS type
        if (rhsType.fields.stream().anyMatch(field -> !Symbols.isPublic(field.symbol))) {
            return false;
        }

        for (BField lhsField : lhsType.fields) {
            BField rhsField = rhsFields.get(lhsField.name);
            if (rhsField == null || !Symbols.isPublic(lhsField.symbol) || !isAssignable(rhsField.type, lhsField.type)) {
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

            if (!Symbols.isPublic(lhsFunc.symbol)) {
                return false;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null || !Symbols.isPublic(rhsFunc.symbol)) {
                return false;
            }
        }

        // Check for private attached function of the RHS type
        for (BAttachedFunction rhsFunc : rhsFuncs) {
            if (!Symbols.isPublic(rhsFunc.symbol)) {
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

    boolean validEqualityIntersectionExists(BType lhsType, BType rhsType) {
        if (!isAnydata(lhsType) || !isAnydata(rhsType)) {
            return false;
        }

        if (isAssignable(lhsType, rhsType) || isAssignable(rhsType, lhsType)) {
            return true;
        }

        Set<BType> lhsTypes = new HashSet<>();
        Set<BType> rhsTypes = new HashSet<>();

        lhsTypes.addAll(expandAndGetMemberTypesRecursive(lhsType));
        rhsTypes.addAll(expandAndGetMemberTypesRecursive(rhsType));
        return equalityIntersectionExists(lhsTypes, rhsTypes);
    }

    private boolean equalityIntersectionExists(Set<BType> lhsTypes, Set<BType> rhsTypes) {
        if (lhsTypes.contains(symTable.anydataType) || rhsTypes.contains(symTable.anydataType)) {
            return true;
        }

        boolean matchFound = lhsTypes
                .stream()
                .anyMatch(s -> rhsTypes
                        .stream()
                        .anyMatch(t -> isSameType(s, t)));

        if (!matchFound) {
            matchFound = equalityIntersectionExistsForComplexTypes(lhsTypes, rhsTypes);
        }

        return matchFound;
    }

    /**
     * Retrieves member types of the specified type, expanding maps/arrays of/constrained by unions types to individual
     * maps/arrays.
     *
     * e.g., (string|int)[] would cause three entries --> string[], int[], (string|int)[]
     *
     * @param bType the type for which member types needs to be identified
     * @return  a set containing all the retrieved member types
     */
    private Set<BType> expandAndGetMemberTypesRecursive(BType bType) {
        Set<BType> memberTypes = new HashSet<>();
        switch (bType.tag) {
            case TypeTags.BYTE:
            case TypeTags.INT:
                memberTypes.add(symTable.intType);
                memberTypes.add(symTable.byteType);
                break;
            case TypeTags.FINITE:
                BFiniteType expType = (BFiniteType) bType;
                expType.valueSpace.forEach(value -> {
                    memberTypes.add(value.type);
                });
                break;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) bType;
                unionType.getMemberTypes().forEach(member -> {
                    memberTypes.addAll(expandAndGetMemberTypesRecursive(member));
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
                    Set<BType> elementUnionTypes = expandAndGetMemberTypesRecursive(arrayElementType);
                    elementUnionTypes.forEach(elementUnionType -> {
                        memberTypes.add(new BArrayType(elementUnionType));
                    });
                }
                memberTypes.add(bType);
                break;
            case TypeTags.MAP:
                BType mapConstraintType = ((BMapType) bType).getConstraint();
                if (mapConstraintType.tag == TypeTags.UNION) {
                    Set<BType> constraintUnionTypes = expandAndGetMemberTypesRecursive(mapConstraintType);
                    constraintUnionTypes.forEach(constraintUnionType -> {
                        memberTypes.add(new BMapType(TypeTags.MAP, constraintUnionType, symTable.mapType.tsymbol));
                    });
                }
                memberTypes.add(bType);
                break;
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
            switch (lhsMemberType.tag) {
                case TypeTags.INT:
                case TypeTags.STRING:
                case TypeTags.FLOAT:
                case TypeTags.BOOLEAN:
                case TypeTags.NIL:
                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON &&
                            ((BJSONType) rhsMemberType).constraint == symTable.noType)) {
                        return true;
                    }
                    break;
                case TypeTags.JSON:
                    if (jsonEqualityIntersectionExists((BJSONType) lhsMemberType, rhsTypes)) {
                        return true;
                    }
                    break;
                // When expanding members for tuples, arrays and maps, set isValueDeepEquality to true, to allow
                // comparison between JSON lists/maps and primitive lists/maps since they are all reference types
                case TypeTags.TUPLE:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                    tupleIntersectionExists((BTupleType) lhsMemberType, (BTupleType) rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                    arrayTupleEqualityIntersectionExists((BArrayType) rhsMemberType,
                                                                         (BTupleType) lhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.ARRAY:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                    equalityIntersectionExists(
                                            expandAndGetMemberTypesRecursive(((BArrayType) lhsMemberType).eType),
                                            expandAndGetMemberTypesRecursive(((BArrayType) rhsMemberType).eType)))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                    arrayTupleEqualityIntersectionExists((BArrayType) lhsMemberType,
                                                                         (BTupleType) rhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.MAP:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                    equalityIntersectionExists(
                                            expandAndGetMemberTypesRecursive(((BMapType) lhsMemberType).constraint),
                                            expandAndGetMemberTypesRecursive(((BMapType) rhsMemberType).constraint)))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON &&
                            (((BJSONType) rhsMemberType).constraint == symTable.noType ||
                                     mapRecordEqualityIntersectionExists((BMapType) lhsMemberType,
                                                                         (BRecordType)
                                                                         ((BJSONType) rhsMemberType).constraint)))) {
                        // at this point it is guaranteed that the map is anydata
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                    mapRecordEqualityIntersectionExists((BMapType) lhsMemberType,
                                                                        (BRecordType) rhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.OBJECT:
                case TypeTags.RECORD:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> checkStructEquivalency(rhsMemberType, lhsMemberType) ||
                                    checkStructEquivalency(lhsMemberType, rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                    recordEqualityIntersectionExists((BRecordType) lhsMemberType,
                                                                     (BRecordType) rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.JSON &&
                                    (((BJSONType) rhsMemberType).constraint == symTable.noType ||
                                             recordEqualityIntersectionExists((BRecordType) lhsMemberType,
                                                                              (BRecordType) ((BJSONType) rhsMemberType)
                                                                                      .constraint)))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                    mapRecordEqualityIntersectionExists((BMapType) rhsMemberType,
                                                                        (BRecordType) lhsMemberType))) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean recordEqualityIntersectionExists(BRecordType lhsType, BRecordType rhsType) {
        if (Symbols.isFlagOn(lhsType.tsymbol.flags ^ rhsType.tsymbol.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both records are private, they should be in the same package.
        if (Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        return allRecordFieldsEqualityIntersectionExists(lhsType, rhsType) ||
                allRecordFieldsEqualityIntersectionExists(rhsType, lhsType);
    }

    private boolean arrayTupleEqualityIntersectionExists(BArrayType arrayType, BTupleType tupleType) {
        Set<BType> elementTypes = expandAndGetMemberTypesRecursive(arrayType.eType);

        return tupleType.tupleTypes.stream()
                .allMatch(tupleMemType -> equalityIntersectionExists(elementTypes,
                                                                     expandAndGetMemberTypesRecursive(tupleMemType)));
    }

    private boolean allRecordFieldsEqualityIntersectionExists(BRecordType lhsType, BRecordType rhsType) {
        List<BField> lhsFields = lhsType.fields;
        List<BField> rhsFields = rhsType.fields;
        for (BField lhsField : lhsFields) {
            Optional<BField> match =
                    rhsFields.stream().filter(rhsField -> lhsField.name.equals(rhsField.name)).findFirst();

            if (match.isPresent()) {
                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsField.type),
                                                expandAndGetMemberTypesRecursive(match.get().type))) {
                    return false;
                }
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
        return true;
    }

    private boolean mapRecordEqualityIntersectionExists(BMapType mapType, BRecordType recordType) {
        Set<BType> mapConstrTypes = expandAndGetMemberTypesRecursive(mapType.getConstraint());

        if (!recordType.sealed &&
                !equalityIntersectionExists(mapConstrTypes,
                                            expandAndGetMemberTypesRecursive(recordType.restFieldType))) {
            return false;
        }

        return recordType.fields.stream().allMatch(
                fieldType -> equalityIntersectionExists(mapConstrTypes,
                                                        expandAndGetMemberTypesRecursive(fieldType.type)));
    }

    private boolean jsonEqualityIntersectionExists(BJSONType jsonType, Set<BType> typeSet) {
        if (jsonType.constraint != symTable.noType) {
            for (BType type : typeSet) {
                switch (type.tag) {
                    case TypeTags.JSON:
                        if (((BJSONType) type).constraint == symTable.noType) {
                            return true;
                        }

                        // Check constrained JSON compatibility
                        if (equalityIntersectionExists(expandAndGetMemberTypesRecursive((jsonType).constraint),
                                                       expandAndGetMemberTypesRecursive(
                                                               ((BJSONType) type).constraint))) {
                            return true;
                        }
                        break;
                    case TypeTags.MAP:
                        if (mapRecordEqualityIntersectionExists((BMapType) type,
                                                                (BRecordType) (jsonType).constraint)) {
                            return true;
                        }
                        break;
                    case TypeTags.RECORD:
                        if (equalityIntersectionExists(expandAndGetMemberTypesRecursive((jsonType).constraint),
                                                       expandAndGetMemberTypesRecursive(type))) {
                            return true;
                        }
                }
            }
            return false;
        }

        if (typeSet.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON ||
                rhsMemberType.tag == TypeTags.STRING || rhsMemberType.tag == TypeTags.INT ||
                rhsMemberType.tag == TypeTags.FLOAT || rhsMemberType.tag == TypeTags.BOOLEAN ||
                rhsMemberType.tag == TypeTags.NIL)) {
            return true;
        }

        // We only reach here, if unconstrained and at this point it is guaranteed that the map/record is anydata
        if (typeSet.stream().anyMatch(typeToCheck -> typeToCheck.tag == TypeTags.MAP ||
                typeToCheck.tag == TypeTags.RECORD)) {
            return true;
        }
        return false;
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
