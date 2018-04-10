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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            return true;
        }

        BSymbol symbol = symResolver.resolveImplicitConversionOp(source, target);
        if (symbol != symTable.notFoundSymbol) {
            return true;
        }

        if ((target.tag == TypeTags.UNION || source.tag == TypeTags.UNION) &&
                isAssignableToUnionType(source, target)) {
            return true;
        }

        if (target.tag == TypeTags.JSON) {
            if (source.tag == TypeTags.JSON) {
                return ((BJSONType) target).constraint.tag == TypeTags.NONE;
            }
            if (source.tag == TypeTags.ARRAY) {
                return isArrayTypesAssignable(source, target);
            }
        }

        if (target.tag == TypeTags.FUTURE && source.tag == TypeTags.FUTURE) {
            if (((BFutureType) target).constraint.tag == TypeTags.NONE) {
                return true;
            }
            return isAssignable(((BFutureType) source).constraint, ((BFutureType) target).constraint);
        }

        if (target.tag == TypeTags.MAP && source.tag == TypeTags.MAP) {
            // Here source condition is added for prevent assigning map union constrained
            // to map any constrained.
            if (((BMapType) target).constraint.tag == TypeTags.ANY &&
                    ((BMapType) source).constraint.tag != TypeTags.UNION) {
                return true;
            }
            if (checkStructEquivalency(((BMapType) source).constraint,
                    ((BMapType) target).constraint)) {
                return true;
            }
        }

        if (source.tag == TypeTags.STRUCT && target.tag == TypeTags.STRUCT) {
            return checkStructEquivalency(source, target);
        }

        if (target.tag == TypeTags.FINITE) {
            return isFiniteTypeAssignable(source, target);
        }

        return source.tag == TypeTags.ARRAY && target.tag == TypeTags.ARRAY &&
                isArrayTypesAssignable(source, target);
    }

    public boolean isFiniteTypeAssignable(BType source, BType target) {
        BFiniteType finiteType = (BFiniteType) target;

        boolean foundMemberType = finiteType.memberTypes
                .stream()
                .map(memberType -> isAssignable(source, memberType))
                .anyMatch(foundType -> foundType);

        return foundMemberType;
    }

    public boolean isArrayTypesAssignable(BType source, BType target) {
        if (target.tag == TypeTags.ARRAY && source.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhsArrayType = (BArrayType) target;
            BArrayType rhsArrayType = (BArrayType) source;
            return isArrayTypesAssignable(rhsArrayType.eType, lhsArrayType.eType);

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

        // Now both types are not array types and they have to be equal
        if (target == source) {
            // TODO Figure out this.
            return true;
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return target.tag == TypeTags.ANY && !isValueType(source);
    }

    public boolean checkFunctionTypeEquality(BInvokableType source, BInvokableType target) {
        if (source.paramTypes.size() != target.paramTypes.size()) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.size(); i++) {
            if (target.paramTypes.get(i).tag != TypeTags.ANY
                    && !isAssignable(source.paramTypes.get(i), target.paramTypes.get(i))) {
                return false;
            }
        }

        if (source.retType == null && target.retType == null) {
            return true;
        } else if (source.retType == null || target.retType == null) {
            return false;
        }

        return isAssignable(source.retType, target.retType);
    }

    public boolean checkArrayEquality(BType source, BType target) {
        if (target.tag == TypeTags.ARRAY && source.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) target;
            BArrayType rhsArrayType = (BArrayType) source;
            return checkArrayEquality(lhrArrayType.eType, rhsArrayType.eType);

        }

        // Now one or both types are not array types and they have to be equal
        return isSameType(source, target);
    }

    public boolean checkStructEquivalency(BType rhsType, BType lhsType) {
        if (rhsType.tag != TypeTags.STRUCT || lhsType.tag != TypeTags.STRUCT) {
            return false;
        }

        // Both structs should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Symbols.isFlagOn(lhsType.tsymbol.flags ^ rhsType.tsymbol.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both structs are private, they should be in the same package.
        if (Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        //RHS type should have at least all the fields as well attached functions of LHS type.
        BStructType lhsStructType = (BStructType) lhsType;
        BStructType rhsStructType = (BStructType) rhsType;
        if (lhsStructType.fields.size() > rhsStructType.fields.size()) {
            return false;
        }

        return Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID == lhsType.tsymbol.pkgID ?
                checkEquivalencyOfTwoPrivateStructs(lhsStructType, rhsStructType) :
                checkEquivalencyOfPublicStructs(lhsStructType, rhsStructType);
    }

    public boolean checkConnectorEquivalency(BType actualType, BType expType) {
        if (actualType.tag != TypeTags.CONNECTOR || expType.tag != TypeTags.CONNECTOR) {
            return false;
        }

        if (isSameType(actualType, expType)) {
            return true;
        }

        BConnectorType expConnectorType = (BConnectorType) expType;
        BConnectorType actualConnectorType = (BConnectorType) actualType;

        // take actions in connectors
        List<BInvokableSymbol> expActions = symResolver.getConnectorActionSymbols(expConnectorType.tsymbol.scope);
        List<BInvokableSymbol> actActions = symResolver.getConnectorActionSymbols(actualConnectorType.tsymbol.scope);

        if (expActions.isEmpty() && actActions.isEmpty()) {
            return true;
        }

        if (expActions.size() != actActions.size()) {
            return false;
        }

        //check every action signatures are matching or not
        for (BInvokableSymbol expAction : expActions) {
            if (actActions.stream().filter(v -> checkActionTypeEquality(expAction, v))
                    .collect(Collectors.toList()).size() != 1) {
                return false;
            }
        }
        return true;
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
                } else {
                    maxSupportedTypes = 1;
                    errorTypes = Lists.of(tableType.constraint);
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

    private boolean checkActionTypeEquality(BInvokableSymbol source, BInvokableSymbol target) {
        return source.name.equals(target.name) &&
                checkFunctionTypeEquality((BInvokableType) source.type, (BInvokableType) target.type);
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType expType) {
        BSymbol symbol = symResolver.resolveImplicitConversionOp(actualType, expType);
        if (expType.tag == TypeTags.UNION && isValueType(actualType)) {
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
        if (type.tag != TypeTags.STRUCT) {
            return false;
        }

        List<BStructField> fields = ((BStructType) type).fields;
        for (int i = 0; i < fields.size(); i++) {
            BType fieldType = fields.get(i).type;
            if (checkStructFieldToJSONCompatibility(type, fieldType)) {
                continue;
            } else {
                return false;
            }
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
        if (t.tag == TypeTags.ARRAY && s.tag == TypeTags.ARRAY) {
            return getExplicitArrayConversionOperator(((BArrayType) t).eType, ((BArrayType) s).eType, origT, origS);

        } else if (t.tag == TypeTags.ARRAY) {
            // If the target type is JSON array, and the source type is a JSON
            if (s.tag == TypeTags.JSON && getElementType(t).tag == TypeTags.JSON) {
                return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }

            // If only the target type is an array type, then the source type must be of type 'any'
            if (s.tag == TypeTags.ANY) {
                return createConversionOperatorSymbol(origS, origT, false, InstructionCodes.CHECKCAST);
            }
            return symTable.notFoundSymbol;

        } else if (s.tag == TypeTags.ARRAY) {
            if (t.tag == TypeTags.JSON) {
                return getExplicitArrayConversionOperator(symTable.jsonType, ((BArrayType) s).eType, origT, origS);
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
        
        if (s.tag == TypeTags.STRUCT && t.tag == TypeTags.STRUCT) {
            if (checkStructEquivalency(s, t)) {
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

    private boolean checkStructFieldToJSONCompatibility(BType structType, BType fieldType) {
        // If the struct field type is the struct
        if (structType == fieldType) {
            return true;
        }

        if (fieldType.tag == TypeTags.STRUCT) {
            return checkStructToJSONCompatibility(fieldType);
        }

        if (isAssignable(fieldType, symTable.jsonType)) {
            return true;
        }

        if (fieldType.tag == TypeTags.ARRAY) {
            return checkStructFieldToJSONCompatibility(structType, getElementType(fieldType));
        }

        return false;
    }

    /**
     * Check whether a given struct can be converted into a JSON.
     *
     * @param type struct type
     * @return flag indicating possibility of conversion
     */
    private boolean checkStructToJSONConvertibility(BType type) {
        if (type.tag != TypeTags.STRUCT) {
            return false;
        }

        List<BStructField> fields = ((BStructType) type).fields;
        for (int i = 0; i < fields.size(); i++) {
            BType fieldType = fields.get(i).type;
            if (checkStructFieldToJSONConvertibility(type, fieldType)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isNullable(BType fieldType) {
        return fieldType.isNullable();
    }

    private boolean checkStructFieldToJSONConvertibility(BType structType, BType fieldType) {
        // If the struct field type is the struct
        if (structType == fieldType) {
            return true;
        }

        if (fieldType.tag == TypeTags.MAP || fieldType.tag == TypeTags.ANY) {
            return true;
        }

        if (fieldType.tag == TypeTags.STRUCT) {
            return checkStructToJSONConvertibility(fieldType);
        }

        if (fieldType.tag == TypeTags.ARRAY) {
            return checkStructFieldToJSONConvertibility(structType, getElementType(fieldType));
        }

        return isAssignable(fieldType, symTable.jsonType);

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
            } else if (s.tag == TypeTags.STRUCT) {
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
            } else if (s.tag == TypeTags.STRUCT) {
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
                return getExplicitArrayConversionOperator(t, s, t, s);
            } else if (s.tag == TypeTags.UNION) {
                if (checkUnionTypeToJSONConvertibility((BUnionType) s, t)) {
                    return createConversionOperatorSymbol(s, t, false, InstructionCodes.CHECK_CONVERSION);
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
        public BSymbol visit(BStructType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2T);
            }

            if (s.tag == TypeTags.STRUCT && checkStructEquivalency(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            } else if (s.tag == TypeTags.STRUCT || s.tag == TypeTags.ANY) {
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
            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BStreamType t, BType s) {
            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BConnectorType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2C);
            } else if (s.tag == TypeTags.CONNECTOR && checkConnectorEquivalency(s, t)) {
                return createConversionOperatorSymbol(s, t, true, InstructionCodes.NOP);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BEnumType t, BType s) {
            if (s == symTable.anyType) {
                return createConversionOperatorSymbol(s, t, false, InstructionCodes.ANY2E);
            }

            return symTable.notFoundSymbol;
        }

        @Override
        public BSymbol visit(BInvokableType t, BType s) {
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
            return s.tag == TypeTags.ARRAY && checkArrayEquality(s, t);
        }

        @Override
        public Boolean visit(BStructType t, BType s) {
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
        public Boolean visit(BConnectorType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BEnumType t, BType s) {
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
    };

    private boolean checkEquivalencyOfTwoPrivateStructs(BStructType lhsType, BStructType rhsType) {
        for (int fieldCounter = 0; fieldCounter < lhsType.fields.size(); fieldCounter++) {
            BStructField lhsField = lhsType.fields.get(fieldCounter);
            BStructField rhsField = rhsType.fields.get(fieldCounter);
            if (lhsField.name.equals(rhsField.name) &&
                    isSameType(rhsField.type, lhsField.type)) {
                continue;
            }
            return false;
        }

        BStructSymbol lhsStructSymbol = (BStructSymbol) lhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BStructSymbol) rhsType.tsymbol).attachedFuncs;
        int lhsAttachedFuncCount = lhsStructSymbol.initializerFunc != null ? lhsFuncs.size() - 1 : lhsFuncs.size();
        if (lhsAttachedFuncCount > rhsFuncs.size()) {
            return false;
        }

        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsStructSymbol.initializerFunc || lhsFunc == lhsStructSymbol.defaultsValuesInitFunc) {
                continue;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkEquivalencyOfPublicStructs(BStructType lhsType, BStructType rhsType) {
        int fieldCounter = 0;
        for (; fieldCounter < lhsType.fields.size(); fieldCounter++) {
            BStructField lhsField = lhsType.fields.get(fieldCounter);
            BStructField rhsField = rhsType.fields.get(fieldCounter);
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

        BStructSymbol lhsStructSymbol = (BStructSymbol) lhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BStructSymbol) rhsType.tsymbol).attachedFuncs;
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

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc);
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

    private BAttachedFunction getMatchingInvokableType(List<BAttachedFunction> rhsFuncList,
                                                       BAttachedFunction lhsFunc) {
        return rhsFuncList.stream()
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEquality(rhsFunc.type, lhsFunc.type))
                .findFirst()
                .orElse(null);
    }

    private boolean isAssignableToUnionType(BType source, BType target) {
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
                        .anyMatch(t -> isAssignable(s, t)))
                .anyMatch(assignable -> !assignable);

        return !notAssignable;
    }

    /**
     * Check whether a given type has a default value.
     * i.e: A variable of the given type can be initialized without a rhs expression.
     * eg: foo x;
     *
     * @param type Type to check the existence if a default value
     * @return Flag indicating whether the given type has a default value
     */
    public boolean defaultValueExists(BType type) {
        if (type.isNullable()) {
            return true;
        }

        if (type.tag == TypeTags.INVOKABLE || type.tag == TypeTags.FINITE) {
            return false;
        }

        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (defaultValueExists(memberType)) {
                    return true;
                }
            }
        }
        return true;
    }
}
