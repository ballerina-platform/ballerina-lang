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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BSemanticErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
        return isAnydata(type, new HashSet<>());
    }

    public boolean isAnydata(BType type, Set<BType> unresolvedTypes) {
        if (type.tag <= TypeTags.ANYDATA) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.MAP:
                return isAnydata(((BMapType) type).constraint, unresolvedTypes);
            case TypeTags.RECORD:
                if (unresolvedTypes.contains(type)) {
                    return true;
                }
                unresolvedTypes.add(type);
                BRecordType recordType = (BRecordType) type;
                List<BType> fieldTypes = recordType.fields.stream()
                                                          .map(field -> field.type)
                                                          .collect(Collectors.toList());
                return isAnydata(fieldTypes, unresolvedTypes) &&
                        (recordType.sealed || isAnydata(recordType.restFieldType, unresolvedTypes));
            case TypeTags.UNION:
                return isAnydata(((BUnionType) type).memberTypes, unresolvedTypes);
            case TypeTags.TUPLE:
                return isAnydata(((BTupleType) type).tupleTypes, unresolvedTypes);
            case TypeTags.ARRAY:
                return isAnydata(((BArrayType) type).eType, unresolvedTypes);
            case TypeTags.FINITE:
                Set<BType> valSpaceTypes = ((BFiniteType) type).valueSpace.stream()
                                                                          .map(val -> val.type).collect(
                                Collectors.toSet());
                return isAnydata(valSpaceTypes, unresolvedTypes);
            default:
                return false;
        }
    }

    private boolean isAnydata(Collection<BType> types, Set<BType> unresolvedTypes) {
        return types.stream().allMatch(bType -> isAnydata(bType, unresolvedTypes));
    }

    public boolean isLikeAnydataOrNotNil(BType type) {
        if (type.tag == TypeTags.NIL || (!isAnydata(type) && !isLikeAnydata(type))) {
            return false;
        }
        return true;
    }

    private boolean isLikeAnydata(BType type) {
        return isLikeAnydata(type, new HashSet<>());
    }
    
    private boolean isLikeAnydata(BType type, Set<BType> unresolvedTypes) {
        int typeTag = type.tag;
        if (typeTag == TypeTags.ANY) {
            return true;
        }

        // check for anydata element/member types as part of recursive calls with structured/union types
        if (type.tag == TypeTags.RECORD) {
            if (unresolvedTypes.contains(type)) {
                return true;
            } else {
                unresolvedTypes.add(type);
                if (isAnydata(type)) {
                    return true;
                }
            }
        } else if (isAnydata(type)) {
            return true;
        }

        if (type.tag == TypeTags.MAP && isLikeAnydata(((BMapType) type).constraint, unresolvedTypes)) {
            return true;
        }

        if (type.tag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) type;
            return recordType.fields.stream()
                    .noneMatch(field -> !Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                            !(isLikeAnydata(field.type, unresolvedTypes)));
        }

        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            return unionType.memberTypes.stream().anyMatch(bType -> isLikeAnydata(bType, unresolvedTypes));
        }

        if (type.tag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) type;
            return tupleType.getTupleTypes().stream().allMatch(bType -> isLikeAnydata(bType, unresolvedTypes));
        }

        return type.tag == TypeTags.ARRAY && isLikeAnydata(((BArrayType) type).eType, unresolvedTypes);
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
            return source.tag == TypeTags.JSON || source.tag == TypeTags.RECORD || source.tag == TypeTags.MAP;
        } else if (target.tag == TypeTags.MAP) {
            if (source.tag == TypeTags.MAP) {
                return isStampingAllowed(((BMapType) source).getConstraint(), ((BMapType) target).getConstraint());
            } else if (source.tag == TypeTags.UNION) {
                return checkUnionEquivalencyForStamping(source, target);
            }
        } else if (target.tag == TypeTags.ARRAY) {
            if (source.tag == TypeTags.JSON) {
                return true;
            } else if (source.tag == TypeTags.TUPLE) {
                BType arrayElementType = ((BArrayType) target).eType;
                for (BType tupleMemberType : ((BTupleType) source).getTupleTypes()) {
                    if (!isStampingAllowed(tupleMemberType, arrayElementType)) {
                        return false;
                    }
                }
                return true;
            } else if (source.tag == TypeTags.ARRAY) {
                return checkTypeEquivalencyForStamping(((BArrayType) source).eType, ((BArrayType) target).eType);
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
        Set<BType> sourceTypes = new LinkedHashSet<>();
        Set<BType> targetTypes = new LinkedHashSet<>();

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

        if (target.getKind() == TypeKind.SERVICE && source.getKind() == TypeKind.SERVICE) {
            // Special casing services, until we figure out service type concept.
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

        BSymbol symbol = symResolver.resolveImplicitCastOp(source, target);
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
                return true;
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
                return isAssignable(((BArrayType) source).getElementType(), target, unresolvedTypes);
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
        if (!rhsType.sealed && !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    void setForeachTypedBindingPatternType(BLangForeach foreachNode) {
        BType collectionType = foreachNode.collection.type;
        BMapType mapType = new BMapType(TypeTags.MAP, null, symTable.mapType.tsymbol);
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        memberTypes.add(mapType);
        BUnionType unionType = new BUnionType(null, memberTypes, true);
        switch (collectionType.tag) {
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) collectionType;
                mapType.constraint = arrayType.eType;
                break;
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                mapType.constraint = new BTupleType(new LinkedList<BType>() {{
                    add(symTable.stringType);
                    add(bMapType.constraint);
                }});
                break;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                mapType.constraint = new BTupleType(new LinkedList<BType>() {{
                    add(symTable.stringType);
                    add(inferRecordFieldType(recordType));
                }});
                break;
            case TypeTags.XML:
                LinkedHashSet<BType> bTypes = new LinkedHashSet<>();
                bTypes.add(symTable.xmlType);
                bTypes.add(symTable.stringType);
                mapType.constraint = new BUnionType(null, bTypes, false);
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                if (tableType.constraint.tag == TypeTags.NONE) {
                    mapType.constraint = symTable.anydataType;
                    foreachNode.varType = mapType.constraint;
                    foreachNode.nillableResultType = unionType;
                    return;
                }
                mapType.constraint = tableType.constraint;
                break;
            case TypeTags.SEMANTIC_ERROR:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                return;
            default:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                dlog.error(foreachNode.collection.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_COLLECTION,
                        collectionType);
                return;
        }
        foreachNode.varType = mapType.constraint;
        foreachNode.resultType = mapType;
        foreachNode.nillableResultType = unionType;
    }

    public BType inferRecordFieldType(BRecordType recordType) {
        List<BField> fields = recordType.fields;

        // If there are no fields in the record, return the rest field type as the inferred type.
        if (fields.isEmpty()) {
            return recordType.restFieldType;
        }

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
        BSymbol symbol = symResolver.resolveImplicitCastOp(actualType, expType);
        if ((expType.tag == TypeTags.UNION || expType.tag == TypeTags.FINITE) && isValueType(actualType)) {
            symbol = symResolver.resolveImplicitCastOp(actualType, symTable.anyType);
        }

        if (symbol == symTable.notFoundSymbol) {
            return;
        }

        BCastOperatorSymbol conversionSym = (BCastOperatorSymbol) symbol;
        BLangTypeConversionExpr implicitConversionExpr =
                (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        implicitConversionExpr.pos = expr.pos;
        implicitConversionExpr.expr = expr.impConversionExpr == null ? expr : expr.impConversionExpr;
        implicitConversionExpr.type = expType;
        implicitConversionExpr.targetType = expType;
        implicitConversionExpr.conversionSymbol = conversionSym;
        expr.impConversionExpr = implicitConversionExpr;
    }

    public BSymbol getCastOperator(BType sourceType, BType targetType) {
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
                sourceType == targetType) {
            return createCastOperatorSymbol(sourceType, targetType, true, InstructionCodes.NOP);
        }

        return targetType.accept(castVisitor, sourceType);
    }

    public BSymbol getConversionOperator(BType sourceType, BType targetType) {
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
                sourceType == targetType) {
            return createCastOperatorSymbol(sourceType, targetType, true, InstructionCodes.NOP);
        }
        BSymbol bSymbol = symResolver.resolveOperator(Names.CONVERSION_OP, Lists.of(sourceType, targetType));
        if (bSymbol != null && bSymbol != symTable.notFoundSymbol) {
            return bSymbol;
        }
        return symResolver.resolveOperator(Names.CAST_OP, Lists.of(sourceType, targetType));
    }

    BSymbol getTypeAssertionOperator(BType sourceType, BType targetType) {
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR) {
            return createCastOperatorSymbol(sourceType, targetType, true, InstructionCodes.NOP);
        }

        if (isValueType(targetType)) {
            return symResolver.getExplicitlyTypedExpressionSymbol(sourceType, targetType);
        } else if (isAssignable(targetType, sourceType)) {
            return symResolver.createTypeAssertionSymbol(sourceType, targetType);
        }
        return symTable.notFoundSymbol;
    }

    public BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    public boolean checkListenerCompatibility(SymbolEnv env, BType type) {
        if (type.tag != TypeTags.OBJECT) {
            return false;
        }
        final BSymbol bSymbol = symResolver.lookupSymbol(env, Names.ABSTRACT_LISTENER, SymTag.TYPE);
        if (bSymbol == symTable.notFoundSymbol || bSymbol.type.tag != TypeTags.OBJECT) {
            throw new AssertionError("AbstractListener object not defined.");
        }
        BObjectType rhsType = (BObjectType) type;
        BObjectType lhsType = (BObjectType) bSymbol.type;

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

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, new ArrayList<>());
            if (rhsFunc == null || !Symbols.isPublic(rhsFunc.symbol)) {
                return false;
            }
        }
        return true;
    }


    // private methods

    private BCastOperatorSymbol createCastOperatorSymbol(BType sourceType,
                                                         BType targetType,
                                                         boolean safe,
                                                         int opcode) {
        return Symbols.createCastOperatorSymbol(sourceType, targetType, symTable.errorType,
                                                false, safe, opcode, null, null);
    }

    private BSymbol getExplicitArrayCastOperator(BType t, BType s, BType origT, BType origS) {
        return getExplicitArrayCastOperator(t, s, origT, origS, new ArrayList<>());
    }

    private BSymbol getExplicitArrayCastOperator(BType t, BType s, BType origT, BType origS, 
                                                 List<TypePair> unresolvedTypes) {
        if (t.tag == TypeTags.ARRAY && s.tag == TypeTags.ARRAY) {
            return getExplicitArrayCastOperator(((BArrayType) t).eType, ((BArrayType) s).eType, origT, origS,
                                                unresolvedTypes);
        } else if (t.tag == TypeTags.ARRAY) {
            if (s.tag == TypeTags.JSON) {
                // If the target type is JSON array, and the source type is a JSON
                if (getElementType(t).tag == TypeTags.JSON) {
                    return createCastOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
                } else {
                    return createCastOperatorSymbol(origS, origT, false, InstructionCodes.JSON2ARRAY);
                }
            }

            // If only the target type is an array type, then the source type must be of type 'any' or 'anydata'
            if (s.tag == TypeTags.ANY || s.tag == TypeTags.ANYDATA) {
                return createCastOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }
            return symTable.notFoundSymbol;

        } else if (s.tag == TypeTags.ARRAY) {
            if (t.tag == TypeTags.JSON) {
                if (getElementType(s).tag == TypeTags.JSON) {
                    return createCastOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
                } else {
                    // the conversion visitor below may report back a conversion symbol, which is
                    // unsafe (e.g. T2JSON), so we must make our one also unsafe
                    if (castVisitor.visit((BJSONType) t, ((BArrayType) s).eType) != symTable.notFoundSymbol) {
                        return createCastOperatorSymbol(origS, origT, false, InstructionCodes.ARRAY2JSON);
                    }
                }
            }

            // If only the source type is an array type, then the target type must be of type 'any'
            if (t.tag == TypeTags.ANY) {
                return createCastOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
            }
            return symTable.notFoundSymbol;
        }

        // Now both types are not array types
        if (s == t) {
            return createCastOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
        }

        if ((s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD)
                && (t.tag == TypeTags.OBJECT || t.tag == TypeTags.RECORD)) {
            if (checkStructEquivalency(s, t, unresolvedTypes)) {
                return createCastOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
            } else {
                return createCastOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }
        }

        if (isAssignable(s, t)) {
            return createCastOperatorSymbol(origS, origT, true, InstructionCodes.NOP);
        }

        if (isAssignable(t, s)) {
            return createCastOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
        }

        return symTable.notFoundSymbol;
    }

    private boolean isNullable(BType fieldType) {
        return fieldType.isNullable();
    }

    private boolean checkUnionTypeToJSONConvertibility(BUnionType type, BJSONType target) {
        // Check whether all the member types are convertible to JSON
        return type.memberTypes.stream()
                .anyMatch(memberType -> castVisitor.visit(memberType, target) == symTable.notFoundSymbol);
    }

    private boolean checkJsonToMapConvertibility(BJSONType src, BMapType target) {
        return true;
    }

    private boolean checkMapToJsonConvertibility(BMapType src, BJSONType target) {
        return true;
    }

    private BTypeVisitor<BType, BSymbol> castVisitor = new BTypeVisitor<BType, BSymbol>() {

        @Override
        public BSymbol visit(BType t, BType s) {
            return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BBuiltInRefType t, BType s) {
            return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BAnyType t, BType s) {
            if (isValueType(s)) {
                return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
            }

            // TODO: 11/1/18 Remove the below check after verifying it doesn't break anything
            // Here condition is added for prevent explicit cast assigning map union constrained
            // to map any constrained.
            if (s.tag == TypeTags.MAP &&
                    ((BMapType) s).constraint.tag == TypeTags.UNION) {
                return symTable.notFoundSymbol;
            }

            return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
        }

        @Override
        public BSymbol visit(BAnydataType t, BType s) {
            if (isValueType(s)) {
                return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
            }

            return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
        }

        @Override
        public BSymbol visit(BMapType t, BType s) {
            if (isSameType(s, t)) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.MAP) {
                if (t.constraint.tag == TypeTags.ANY) {
                    return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
                } else if (((BMapType) s).constraint.tag == TypeTags.ANY) {
                    return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
                } else if (checkStructEquivalency(((BMapType) s).constraint,
                        t.constraint)) {
                    return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
                } else {
                    return symTable.notFoundSymbol;
                }
            } else if (s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.T2MAP);
            } else if (s.tag == TypeTags.JSON) {
                if (!checkJsonToMapConvertibility((BJSONType) s, t)) {
                    return symTable.notFoundSymbol;
                }
                return createCastOperatorSymbol(s, t, false, InstructionCodes.JSON2MAP);
            } else if (s.tag == TypeTags.ANYDATA) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.ANY2MAP);
            }

            return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BXMLType t, BType s) {
            return visit((BBuiltInRefType) t, s);
        }

        @Override
        public BSymbol visit(BJSONType t, BType s) {
            if (isSameType(s, t)) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.OBJECT) {
//                TODO: do type checking and fail for obvious incompatible types
//                if (checkStructToJSONConvertibility(s)) {
//                    return createCastOperatorSymbol(s, t, false, InstructionCodes.T2JSON);
//                } else {
//                    return symTable.notFoundSymbol;
//                }
                return createCastOperatorSymbol(s, t, false, InstructionCodes.T2JSON);
            } else if (s.tag == TypeTags.JSON) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.ARRAY) {
                return getExplicitArrayCastOperator(t, s, t, s);
            } else if (s.tag == TypeTags.UNION) {
                if (checkUnionTypeToJSONConvertibility((BUnionType) s, t)) {
                    return createCastOperatorSymbol(s, t, false, InstructionCodes.O2JSON);
                }
                return symTable.notFoundSymbol;
            } else if (s.tag == TypeTags.MAP) {
                if (!checkMapToJsonConvertibility((BMapType) s, t)) {
                    return symTable.notFoundSymbol;
                }
                return createCastOperatorSymbol(s, t, false, InstructionCodes.MAP2JSON);
            }

            return symResolver.resolveOperator(Names.CAST_OP, Lists.of(s, t));
        }

        @Override
        public BSymbol visit(BArrayType t, BType s) {
            return getExplicitArrayCastOperator(t, s, t, s);
        }

        @Override
        public BSymbol visit(BObjectType t, BType s) {
            if (s == symTable.anyType) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.ANY2T);
            }

            if ((s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD) && checkStructEquivalency(s, t)) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.OBJECT || s.tag == TypeTags.RECORD || s.tag == TypeTags.ANY) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.MAP) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.MAP2T);
            } else if (s.tag == TypeTags.JSON) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.JSON2T);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BRecordType t, BType s) {
            if (s == symTable.anyType || s == symTable.anydataType) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.ANY2T);
            }

            if ((s.tag == TypeTags.RECORD || s.tag == TypeTags.OBJECT) && checkStructEquivalency(s, t)) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.RECORD || s.tag == TypeTags.OBJECT || s.tag == TypeTags.ANY) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.MAP) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.MAP2T);
            } else if (s.tag == TypeTags.JSON) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.JSON2T);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BTableType t, BType s) {
            if (s == symTable.anyType || s.tag == symTable.anydataType.tag) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.ANY2DT);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BTupleType t, BType s) {
            if (s == symTable.anyType || s == symTable.anydataType) {
                return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
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
                return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            } else if (s.tag == TypeTags.INVOKABLE && checkFunctionTypeEquality((BInvokableType) s, t)) {
                return createCastOperatorSymbol(s, t, true, InstructionCodes.NOP);
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
                return createCastOperatorSymbol(s, t, false, InstructionCodes.CHECKCAST);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BServiceType t, BType s) {
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
            return s.tag == TypeTags.JSON;
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

            Set<BType> sourceTypes = new LinkedHashSet<>(sUnionType.memberTypes);
            Set<BType> targetTypes = new LinkedHashSet<>(tUnionType.memberTypes);

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
        public Boolean visit(BServiceType t, BType s) {
            return t == s;
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
        Set<BType> sourceTypes = new LinkedHashSet<>();
        Set<BType> targetTypes = new LinkedHashSet<>();

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
                    .anyMatch(memberLiteral -> {
                        if (((BLangLiteral) memberLiteral).value == null) {
                            return literalExpr.value == null;
                        } else {
                            return (((BLangLiteral) memberLiteral).value.equals(literalExpr.value) &&
                                    ((BLangLiteral) memberLiteral).typeTag == literalExpr.typeTag);
                        }
                    });
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

        Set<BType> lhsTypes = new LinkedHashSet<>(expandAndGetMemberTypesRecursive(lhsType));
        Set<BType> rhsTypes = new LinkedHashSet<>(expandAndGetMemberTypesRecursive(rhsType));
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
     * e.g., (string|int)[] would cause three entries as string[], int[], (string|int)[]
     *
     * @param bType the type for which member types needs to be identified
     * @return  a set containing all the retrieved member types
     */
    public Set<BType> expandAndGetMemberTypesRecursive(BType bType) {
        Set<BType> memberTypes = new LinkedHashSet<>();
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
                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
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

                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
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

                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
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

    public BType getRemainingType(BType originalType, LinkedHashSet<BType> typesToRemove) {
        return getRemainingType(originalType, new BUnionType(null, typesToRemove, false));
    }

    public BType getRemainingType(BType originalType, BType typeToRemove) {
        if (originalType.tag != TypeTags.UNION) {
            return originalType;
        }

        List<BType> removeTypes = getAllTypes(typeToRemove);
        return getRemainingType(originalType, removeTypes);
    }

    private BType getRemainingType(BType originalType, List<BType> removeTypes) {
        List<BType> remainingTypes = getAllTypes(originalType);
        removeTypes.forEach(removeType -> remainingTypes.removeIf(type -> isAssignable(type, removeType)));

        if (remainingTypes.size() == 1) {
            return remainingTypes.get(0);
        }

        return new BUnionType(null, new LinkedHashSet<>(remainingTypes), remainingTypes.contains(symTable.nilType));
    }

    public BType getSafeType(BType type, boolean liftError) {
        // Since JSON, ANY and ANYDATA by default contain null, we need to create a new respective type which
        // is not-nullable.
        switch (type.tag) {
            case TypeTags.JSON:
                BJSONType jsonType = (BJSONType) type;
                return new BJSONType(jsonType.tag, jsonType.tsymbol, false);
            case TypeTags.ANY:
                return new BAnyType(type.tag, type.tsymbol, false);
            case TypeTags.ANYDATA:
                return new BAnydataType(type.tag, type.tsymbol, false);
        }

        if (type.tag != TypeTags.UNION) {
            return type;
        }

        BUnionType unionType = (BUnionType) type;
        BUnionType errorLiftedType =
                new BUnionType(null, new LinkedHashSet<>(unionType.memberTypes), unionType.isNullable());

        // Lift nil always. Lift error only if safe navigation is used.
        errorLiftedType.memberTypes.remove(symTable.nilType);
        if (liftError) {
            errorLiftedType.memberTypes.remove(symTable.errorType);
        }

        if (errorLiftedType.memberTypes.size() == 1) {
            return errorLiftedType.memberTypes.toArray(new BType[0])[0];
        }
        return errorLiftedType;
    }

    private List<BType> getAllTypes(BType type) {
        if (type.tag != TypeTags.UNION) {
            return Lists.of(type);
        }

        List<BType> memberTypes = new ArrayList<>();
        ((BUnionType) type).getMemberTypes().forEach(memberType -> memberTypes.addAll(getAllTypes(memberType)));
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
