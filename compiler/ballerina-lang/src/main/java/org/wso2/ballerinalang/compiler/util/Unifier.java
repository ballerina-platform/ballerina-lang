/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Util class for building concrete BType types from parameterized types.
 *
 * @since 2.0.0
 */
public class Unifier implements BTypeVisitor<BType, BType> {

    private Map<String, BType> paramValueTypes;
    private Set<BType> visitedTypes = new HashSet<>();
    private boolean isInvocation;
    private BLangInvocation invocation;
    private BLangFunction function;
    private SymbolTable symbolTable;
    private SymbolEnv env;
    private Types types;
    private BLangDiagnosticLog dlog;

    public BType build(BType originalType, BType expType, BLangInvocation invocation, Types types,
                       BLangDiagnosticLog dlog) {
        this.isInvocation = invocation != null;
        if (this.isInvocation) {
            this.invocation = invocation;
            createParamMap(invocation);
        }
        this.types = types;
        this.dlog = dlog;
        BType newType = originalType.accept(this, expType);
        resetBuildArgs();
        return newType;
    }

    public BType build(BType originalType) {
        return build(originalType, null, null, null, null);
    }

    public void validate(BType returnType, BLangFunction function, SymbolTable symbolTable, SymbolEnv env, Types types,
                         BLangDiagnosticLog dlog) {
        this.function = function;
        this.symbolTable = symbolTable;
        this.env = env;
        this.types = types;
        this.dlog = dlog;
        returnType.accept(this, null);
        resetValidateArgs();
    }

    @Override
    public BType visit(BType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BBuiltInRefType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BAnyType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BAnydataType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BMapType originalType, BType expType) {
        BType expConstraint = isDifferentTypeKind(originalType, expType) ? null : ((BMapType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BMapType newMType = new BMapType(originalType.tag, newConstraint, null);
        setFlags(newMType, originalType.flags);
        return newMType;
    }

    @Override
    public BType visit(BXMLType originalType, BType expType) {
        BType expConstraint = isDifferentTypeKind(originalType, expType) ? null : ((BXMLType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BXMLType newXMLType = new BXMLType(newConstraint, null);
        setFlags(newXMLType, originalType.flags);
        return newXMLType;
    }

    @Override
    public BType visit(BJSONType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BArrayType originalType, BType expType) {
        BType expElemType = isDifferentTypeKind(originalType, expType) ? null : ((BArrayType) expType).eType;
        BType newElemType = originalType.eType.accept(this, expElemType);

        if (newElemType == originalType.eType) {
            return originalType;
        }

        BArrayType newArrayType = new BArrayType(newElemType, null, originalType.size, originalType.state);
        setFlags(newArrayType, originalType.flags);
        return newArrayType;
    }

    @Override
    public BType visit(BObjectType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BRecordType originalType, BType expType) {
        if (!Symbols.isFlagOn(originalType.tsymbol.flags, Flags.PARAMETERIZED)) {
            return originalType;
        }

        LinkedHashMap<String, BField> newFields = new LinkedHashMap();
        for (BField field : originalType.fields.values()) {
            if (this.visitedTypes.contains(field.type)) {
                continue;
            }

            this.visitedTypes.add(field.type);
            BType newFType = field.type.accept(this, null);
            this.visitedTypes.remove(field.type);

            if (newFType == field.type) {
                newFields.put(field.name.value, field);
                continue;
            }

            BField newField = new BField(field.name, field.pos, field.symbol);
            newField.type = newFType;
            newFields.put(newField.name.value, newField);
        }

        BType newRestType = originalType.restFieldType.accept(this, null);

        BRecordType newRecordType = new BRecordType(null);
        newRecordType.fields = newFields;
        newRecordType.restFieldType = newRestType;
        setFlags(newRecordType, originalType.flags);
        return originalType;
    }

    @Override
    public BType visit(BTupleType originalType, BType expType) {
        boolean hasNewType = false;
        boolean isDifferentType = isDifferentTypeKind(originalType, expType);
        List<BType> members = new ArrayList<>();
        List<BType> expTupleTypes = isDifferentType ? Collections.singletonList(null) :
                ((BTupleType) expType).tupleTypes;
        int delta = isDifferentType ? 0 : 1;

        // TODO: Consider different tuple member list sizes
        List<BType> tupleTypes = originalType.tupleTypes;
        for (int i = 0, j = 0; i < tupleTypes.size(); i++, j += delta) {
            BType member = tupleTypes.get(i);
            BType expMember = expTupleTypes.get(j);
            BType newMem = member.accept(this, expMember);
            members.add(newMem);

            if (newMem != member) {
                hasNewType = true;
            }
        }

        BType rest = originalType.restType;
        if (rest != null) {
            BType expRestType = isDifferentType ? null : ((BTupleType) expType).restType;
            rest = rest.accept(this, expRestType);

            if (rest != originalType.restType) {
                hasNewType = true;
            }
        }

        if (!hasNewType) {
            return originalType;
        }

        BTupleType type = new BTupleType(null, members);
        type.restType = rest;
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BStreamType originalType, BType expType) {
        boolean isDifferentType = isDifferentTypeKind(originalType, expType);

        BType expConstraint = isDifferentType ? null : ((BStreamType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        BType newError = null;
        if (originalType.error != null) {
            BType expError = isDifferentType ? null : ((BStreamType) expType).error;
            newError = originalType.error.accept(this, expError);
        }

        if (newConstraint == originalType.constraint && newError == originalType.error) {
            return originalType;
        }

        BStreamType type = new BStreamType(originalType.tag, newConstraint, newError, null);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BTableType originalType, BType expType) {
        boolean isDifferentType = isDifferentTypeKind(originalType, expType);
        BType expConstraint = isDifferentType ? null : ((BTableType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        BType newKeyTypeConstraint = null;
        if (originalType.keyTypeConstraint != null) {
            BType expKeyConstraint = isDifferentType ? null : ((BTableType) expType).keyTypeConstraint;
            originalType.keyTypeConstraint.accept(this, expKeyConstraint);
        }

        if (newConstraint == originalType.constraint && newKeyTypeConstraint == originalType.keyTypeConstraint) {
            return originalType;
        }

        BTableType newTableType = new BTableType(TypeTags.TABLE, newConstraint, null);
        newTableType.keyTypeConstraint = newKeyTypeConstraint;
        newTableType.fieldNameList = originalType.fieldNameList;
        newTableType.constraintPos = originalType.constraintPos;
        newTableType.isTypeInlineDefined = originalType.isTypeInlineDefined;
        newTableType.keyPos = originalType.keyPos;
        setFlags(newTableType, originalType.flags);
        return newTableType;
    }

    @Override
    public BType visit(BInvokableType originalType, BType expType) {
        boolean hasNewType = false;
        boolean isDifferentType = isDifferentTypeKind(originalType, expType);
        List<BType> paramTypes = new ArrayList<>();
        List<BType> expParamTypes = isDifferentType ? Collections.singletonList(null) :
                ((BInvokableType) expType).paramTypes;
        int delta = isDifferentType ? 0 : 1;

        List<BType> bTypes = originalType.paramTypes;
        for (int i = 0, j = 0; i < bTypes.size(); i++, j += delta) {
            BType type = bTypes.get(i);
            BType expParamType = expParamTypes.get(j);
            BType newT = type.accept(this, expParamType);
            paramTypes.add(newT);

            if (newT != type) {
                hasNewType = true;
            }
        }

        BType rest = originalType.restType;
        if (rest != null) {
            BType expRestType = isDifferentType ? null : ((BInvokableType) expType).restType;
            rest = rest.accept(this, expRestType);

            if (rest != originalType.restType) {
                hasNewType = true;
            }
        }

        BType expRetType = isDifferentType ? null : ((BInvokableType) expType).retType;
        BType retType = originalType.retType.accept(this, expRetType);

        if (!hasNewType && retType != originalType.retType) {
            return originalType;
        }

        BType type = new BInvokableType(paramTypes, rest, retType, null);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BUnionType originalType, BType expType) {
        if (!visitedTypes.add(originalType)) {
            return originalType;
        }

        boolean hasNewType = false;
        LinkedHashSet<BType> newMemberTypes = new LinkedHashSet<>();

        for (BType member : originalType.getMemberTypes()) {
            if (this.visitedTypes.contains(member)) {
                continue;
            }

            if (this.function != null && Symbols.isFlagOn(member.flags, Flags.PARAMETERIZED)) {
                BParameterizedType parameterizedType = (BParameterizedType) member;
                BType paramConstraint = getParamConstraintTypeIfInferred(this.function, parameterizedType);
                if (paramConstraint != symbolTable.noType && !isDisjointMemberType(parameterizedType, originalType)) {
                    dlog.error(this.function.returnTypeNode.pos,
                               DiagnosticErrorCode.INVALID_DEPENDENTLY_TYPED_RETURN_TYPE_WITH_INFERRED_TYPEDESC_PARAM);
                    return originalType;
                }
            }

            BType newMember = member.accept(this,
                                            getExpectedTypeForInferredTypedescMember(originalType, expType, member));
            newMemberTypes.add(newMember);

            if (newMember != member) {
                hasNewType = true;
            }
        }

        if (!hasNewType) {
            return originalType;
        }

        BUnionType type = BUnionType.create(null, newMemberTypes);
        setFlags(type, originalType.flags);
        return type;
    }

    private BType getExpectedTypeForInferredTypedescMember(BUnionType originalType, BType expType, BType member) {
        if (expType == null || this.invocation == null || member.tag != TypeTags.PARAMETERIZED_TYPE) {
            return null;
        }

        for (BVarSymbol param : ((BInvokableSymbol) this.invocation.symbol).params) {
            if (!param.name.value.equals(((Name) member.name).value)) {
                continue;
            }

            if (!Symbols.isFlagOn(param.flags, Flags.INFER)) {
                return null;
            }
            break;
        }

        if (expType.tag != TypeTags.UNION) {
            return expType;
        }

        LinkedHashSet<BType> types = new LinkedHashSet<>();
        for (BType expMemType : ((BUnionType) expType).getMemberTypes()) {
            boolean hasMatchWithOtherType = false;
            for (BType origMemType : originalType.getMemberTypes()) {
                if (origMemType == member) {
                    continue;
                }

                if (hasSameBasicType(expMemType, origMemType)) {
                    hasMatchWithOtherType = true;
                    break;
                }
            }

            if (!hasMatchWithOtherType) {
                types.add(expMemType);
            }
        }

        if (types.isEmpty()) {
            return null;
        }

        if (types.size() == 1) {
            return types.iterator().next();
        }

        return BUnionType.create(null, types);
    }

    @Override
    public BType visit(BIntersectionType originalType, BType expType) {
        BType expEffectiveType = isDifferentTypeKind(originalType, expType) ?
                null : ((BIntersectionType) expType).effectiveType;
        BType newEffectiveType = originalType.effectiveType.accept(this, expEffectiveType);

        if (newEffectiveType == originalType.effectiveType) {
            return originalType;
        }

        BIntersectionType type = new BIntersectionType(null, (LinkedHashSet<BType>) originalType.getConstituentTypes(),
                                                       newEffectiveType);
        setFlags(type, originalType.flags);
        return originalType;
    }

    @Override
    public BType visit(BErrorType originalType, BType expType) {
        BType expDetailType = isDifferentTypeKind(originalType, expType) ? null : ((BErrorType) expType).detailType;
        BType newDetail = originalType.detailType.accept(this, expDetailType);

        if (newDetail == originalType.detailType) {
            return originalType;
        }

        BErrorType type = new BErrorType(null, newDetail);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BFutureType originalType, BType expType) {
        BType expConstraint = isDifferentTypeKind(originalType, expType) ? null : ((BFutureType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BFutureType newFutureType = new BFutureType(originalType.tag, newConstraint, null,
                                                    originalType.workerDerivative);
        setFlags(newFutureType, originalType.flags);
        return newFutureType;
    }

    @Override
    public BType visit(BFiniteType originalType, BType expType) {
        // Not applicable for finite types since the descriptor has to be defined before using in the return type.
        return originalType;
    }

    @Override
    public BType visit(BTypedescType originalType, BType expType) {
        BType expConstraint = isDifferentTypeKind(originalType, expType) ?
                null : ((BTypedescType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BTypedescType newTypedescType = new BTypedescType(newConstraint, null);
        setFlags(newTypedescType, originalType.flags);
        return newTypedescType;
    }

    @Override
    public BType visit(BParameterizedType originalType, BType expType) {
        String paramVarName = originalType.paramSymbol.name.value;

        if (Symbols.isFlagOn(originalType.paramSymbol.flags, Flags.INFER)) {
            BType paramSymbolType = ((BTypedescType) originalType.paramSymbol.type).constraint;
            if (expType != null) {
                if (!types.isAssignable(expType, paramSymbolType)) {
                    if (!paramValueTypes.containsKey(paramVarName)) {
                        // Log an error only if the user has not explicitly passed an argument. If the passed
                        // argument is invalid, the type checker will log the error.
                        dlog.error(invocation.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_FOR_INFERRED_TYPEDESC_VALUE,
                                   paramVarName, paramSymbolType, expType);
                    }
                    return paramSymbolType;
                }

                int index = pos(originalType.paramSymbol);
                if (index >= invocation.requiredArgs.size()) {
                    BLangTypedescExpr typedescExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
                    typedescExpr.pos = this.invocation.pos;
                    typedescExpr.resolvedType = expType;
                    typedescExpr.type = new BTypedescType(expType, null);

                    this.invocation.argExprs.add(index, typedescExpr);
                    this.invocation.requiredArgs.add(index, typedescExpr);
                }
                return expType;
            }
            // TODO: Instead of this, see if a more specific error message can be given here.
            // e.g., incompatible inferred type: expected '(string|float)', inferred 'int'
            return paramSymbolType;
        }

        BType type;
        if (this.isInvocation) {
            // This would return null if the calling function gets analyzed before the callee function. This only
            // happens when the invocation uses the default value of the param.
            type = paramValueTypes.get(paramVarName);

            if (type == null) {
                return originalType.paramValueType;
            }

            if (type.tag == TypeTags.SEMANTIC_ERROR) {
                return type;
            }

            type = ((BTypedescType) type).constraint;
        } else {
            type = ((BTypedescType) originalType.paramSymbol.type).constraint;
        }
        return type;
    }

    private void createParamMap(BLangInvocation invocation) {
        paramValueTypes = new LinkedHashMap<>();
        BInvokableSymbol symbol = (BInvokableSymbol) invocation.symbol;
        List<BLangExpression> requiredArgs = invocation.requiredArgs;
        List<BVarSymbol> params = symbol.params;

        int argIndex = 0;

        List<BLangExpression> restArgs = invocation.restArgs;
        boolean hasRestArg = !restArgs.isEmpty() &&
                restArgs.get(restArgs.size() - 1).getKind() == NodeKind.REST_ARGS_EXPR;

        for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
            if (argIndex < requiredArgs.size()) {
                BLangExpression arg = requiredArgs.get(argIndex);

                if (arg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                    // If this is a positional arg, it should correspond to the current param.
                    paramValueTypes.put(params.get(paramIndex).name.value, arg.type);
                    argIndex++;
                    continue;
                } else {
                    // If this is a named arg, from this point onward the order in which args are specified doesn't
                    // have to correspond to the order in which the params were specified.
                    populateParamMapFromNamedArgs(symbol, params, requiredArgs, paramIndex, argIndex);
                    return;
                }
            }

            if (hasRestArg) {
                // Param defaults are not added if there is a rest arg, so we can populate all the remaining param
                // types based on the rest arg.
                populateParamMapFromRestArg(params, paramIndex, restArgs.get(restArgs.size() - 1));
                return;
            }

            BVarSymbol param = params.get(paramIndex);
            String paramName = param.name.value;
            if (param.defaultableParam && !Symbols.isFlagOn(param.flags, Flags.INFER)) {
                paramValueTypes.put(paramName, symbol.paramDefaultValTypes.get(paramName));
            }
        }
    }

    private void populateParamMapFromNamedArgs(BInvokableSymbol symbol, List<BVarSymbol> params,
                                               List<BLangExpression> requiredArgs,
                                               int currentParamIndex, int currentArgIndex) {
        for (int paramIndex = currentParamIndex; paramIndex < params.size(); paramIndex++) {
            BVarSymbol param = params.get(paramIndex);
            if (Symbols.isFlagOn(param.flags, Flags.INCLUDED)) {
                // Return types cannot be dependent on included record param fields, so we don't have to populate the
                // map for them.
                continue;
            }

            String name = param.name.value;
            boolean argProvided = false;

            for (int argIndex = currentArgIndex; argIndex < requiredArgs.size(); argIndex++) {
                BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) requiredArgs.get(argIndex);

                if (name.equals(namedArg.name.value)) {
                    paramValueTypes.put(name, namedArg.type);
                    argProvided = true;
                    break;
                }
            }

            if (argProvided) {
                continue;
            }

            if (param.defaultableParam && !Symbols.isFlagOn(param.flags, Flags.INFER)) {
                paramValueTypes.put(name, symbol.paramDefaultValTypes.get(name));
            }
        }
    }

    private void populateParamMapFromRestArg(List<BVarSymbol> params, int currentParamIndex, BLangExpression restArg) {
        BType type = restArg.type;
        int tag = type.tag;
        if (tag == TypeTags.RECORD) {
            populateParamMapFromRecordRestArg(params, currentParamIndex, (BRecordType) type);
            return;
        }

        if (tag == TypeTags.ARRAY) {
            populateParamMapFromArrayRestArg(params, currentParamIndex, (BArrayType) type);
            return;
        }

        populateParamMapFromTupleRestArg(params, currentParamIndex, (BTupleType) type);
    }

    private void populateParamMapFromRecordRestArg(List<BVarSymbol> params, int currentParamIndex,
                                                   BRecordType recordType) {
        for (int i = currentParamIndex; i < params.size(); i++) {
            String paramName = params.get(i).name.value;
            paramValueTypes.put(paramName, recordType.fields.get(paramName).type);
        }
    }

    private void populateParamMapFromArrayRestArg(List<BVarSymbol> params, int currentParamIndex,
                                                  BArrayType arrayType) {
        BType elementType = arrayType.eType;
        for (int i = currentParamIndex; i < params.size(); i++) {
            paramValueTypes.put(params.get(i).name.value, elementType);
        }
    }

    private void populateParamMapFromTupleRestArg(List<BVarSymbol> params, int currentParamIndex,
                                                  BTupleType tupleType) {
        int tupleIndex = 0;
        List<BType> tupleTypes = tupleType.tupleTypes;
        for (int i = currentParamIndex; i < params.size(); i++) {
            paramValueTypes.put(params.get(i).name.value, tupleTypes.get(tupleIndex++));
        }
    }

    private void setFlags(BType type, long originalFlags) {
        type.flags = originalFlags & (~Flags.PARAMETERIZED);
    }

    private boolean isDifferentTypeKind(BType source, BType target) {
        // TODO: Consider subtypes
        if (target == null) {
            return true;
        }

        if (source.tag != target.tag) {
            return true;
        }

        return false;
    }

    private int pos(BVarSymbol sym) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocation.symbol;
        List<BVarSymbol> params = invokableSymbol.params;
        for (int i = 0; i < params.size(); i++) {
            BVarSymbol param = params.get(i);
            if (param.equals(sym)) {
                return i;
            }
        }

        throw new IllegalStateException(
                String.format("Param '%s' not found in function '%s'", sym.name, invokableSymbol.name));
    }

    private BType getParamConstraintTypeIfInferred(BLangFunction function, BParameterizedType parameterizedType) {
        String paramName = parameterizedType.paramSymbol.name.value;

        for (BLangSimpleVariable requiredParam : function.requiredParams) {
            if (!requiredParam.name.value.equals(paramName)) {
                continue;
            }

            BLangExpression expr = requiredParam.expr;
            if (expr == null || expr.getKind() != NodeKind.INFER_TYPEDESC_EXPR) {
                return symbolTable.noType;
            }

            BType paramType = requiredParam.type;
            if (paramType.tag != TypeTags.TYPEDESC) {
                return symbolTable.noType;
            }

            return ((BTypedescType) paramType).constraint;
        }
        return symbolTable.noType;
    }

    private boolean isDisjointMemberType(BParameterizedType parameterizedType, BUnionType unionType) {
        BType paramValueType = parameterizedType.paramValueType;

        if (paramValueType.tag == TypeTags.UNION) {
            return isDisjoint((BUnionType) paramValueType, unionType, parameterizedType);
        }

        for (BType memberType : unionType.getMemberTypes()) {
            if (memberType == parameterizedType) {
                continue;
            }

            if (hasSameBasicType(paramValueType, memberType) || hasIntersection(paramValueType, memberType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDisjoint(BUnionType t1, BUnionType t2, BParameterizedType parameterizedType) {
        for (BType memType1 : t1.getMemberTypes()) {
            for (BType memType2 : t2.getMemberTypes()) {
                if (memType2 == parameterizedType) {
                    continue;
                }

                if (hasSameBasicType(memType1, memType2) || hasIntersection(memType1, memType2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasIntersection(BType t1, BType t2) {
        BType typeIntersection = types.getTypeIntersection(t1, t2, env);
        return typeIntersection != null && typeIntersection != symbolTable.semanticError;
    }

    private boolean hasSameBasicType(BType t1, BType t2) {
        int tag1 = t1.tag;
        int tag2 = t2.tag;

        if (tag1 == tag2) {
            return true;
        }

        if (TypeTags.isIntegerTypeTag(tag1) != TypeTags.isIntegerTypeTag(tag2)) {
            return false;
        }

        if (TypeTags.isStringTypeTag(tag1) != TypeTags.isStringTypeTag(tag2)) {
            return false;
        }

        if (TypeTags.isXMLTypeTag(tag1) != TypeTags.isXMLTypeTag(tag2)) {
            return false;
        }

        if (isMappingType(tag1) != isMappingType(tag2)) {
            return true;
        }

        return isListType(tag1) && isListType(tag2);
    }

    private boolean isMappingType(int tag) {
        return tag == TypeTags.MAP || tag == TypeTags.RECORD;
    }

    private boolean isListType(int tag) {
        return tag == TypeTags.ARRAY || tag == TypeTags.TUPLE;
    }

    private void resetBuildArgs() {
        this.visitedTypes = new HashSet<>();
        this.paramValueTypes = null;
        this.isInvocation = false;
    }

    private void resetValidateArgs() {
        this.visitedTypes = new HashSet<>();
        this.function = null;
        this.env = null;
    }
}
