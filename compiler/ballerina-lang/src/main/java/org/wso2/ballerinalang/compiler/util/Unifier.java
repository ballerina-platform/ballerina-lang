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
import org.ballerinalang.model.types.IntersectableReferenceType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
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
                       SymbolTable symbolTable, BLangDiagnosticLog dlog) {
        this.isInvocation = invocation != null;
        if (this.isInvocation) {
            this.invocation = invocation;
            createParamMap(invocation);
        }
        this.types = types;
        this.symbolTable = symbolTable;
        this.dlog = dlog;
        BType newType = originalType.accept(this, expType);
        resetBuildArgs();
        return newType;
    }

    public BType build(BType originalType) {
        return build(originalType, null, null, null, null, null);
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
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expConstraint = matchingType == null ? null : ((BMapType) matchingType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (isSameType(newConstraint, originalType.constraint)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint)) {
            return symbolTable.semanticError;
        }

        BMapType newMType = new BMapType(originalType.tag, newConstraint, null);
        setFlags(newMType, originalType.flags);
        return newMType;
    }

    @Override
    public BType visit(BXMLType originalType, BType expType) {
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expConstraint = matchingType == null ? null : ((BXMLType) matchingType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (isSameType(newConstraint, originalType.constraint)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint)) {
            return symbolTable.semanticError;
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
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expElemType = matchingType == null ? null : ((BArrayType) matchingType).eType;
        BType newElemType = originalType.eType.accept(this, expElemType);

        if (isSameType(newElemType, originalType.eType)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newElemType)) {
            return symbolTable.semanticError;
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
        return originalType;
    }

    @Override
    public BType visit(BTupleType originalType, BType expType) {
        if (!visitedTypes.add(originalType)) {
            return originalType;
        }

        boolean hasNewType = false;

        BTupleType matchingType = (BTupleType) getMatchingTypeForInferrableType(originalType, expType);
        boolean hasMatchedTupleType = matchingType != null;
        BTupleType expTupleType = hasMatchedTupleType ? matchingType : null;

        if (hasMatchedTupleType) {
            if (expTupleType.getMembers().size() != originalType.getMembers().size()) {
                hasMatchedTupleType = false;
            } else {
                BType expRestType = expTupleType.restType;
                BType originalRestType = originalType.restType;

                if ((expRestType == null || originalRestType == null) && expRestType != originalRestType) {
                    hasMatchedTupleType = false;
                }
            }
        }

        List<BType> expTupleTypes = hasMatchedTupleType ? List.copyOf(expTupleType.getTupleTypes()) :
                Collections.singletonList(null);

        List<BTupleMember> members = new ArrayList<>();
        int delta = hasMatchedTupleType ? 1 : 0;

        boolean errored = false;

        List<BType> tupleTypes = originalType.getTupleTypes();
        for (int i = 0, j = 0; i < tupleTypes.size(); i++, j += delta) {
            if (this.visitedTypes.contains(tupleTypes.get(i))) {
                continue;
            }
            BType member = tupleTypes.get(i);
            BType expMember = expTupleTypes.get(j);
            BType newMem = member.accept(this, expMember);
            BVarSymbol varSymbol = new BVarSymbol(newMem.flags, null, null, newMem, null, null, null);
            members.add(new BTupleMember(newMem, varSymbol));

            if (isSemanticErrorInInvocation(newMem)) {
                errored = true;
            } else if (!isSameType(newMem, member)) {
                hasNewType = true;
            }
        }

        BType restType = originalType.restType;
        BType newRestType = null;
        if (restType != null) {
            BType expRestType = hasMatchedTupleType ? matchingType.restType : null;
            newRestType = restType.accept(this, expRestType);

            if (isSemanticErrorInInvocation(newRestType)) {
                errored = true;
            } else if (!isSameType(newRestType, restType)) {
                hasNewType = true;
            }
        }

        if (errored) {
            return expType;
        }

        if (!hasNewType) {
            return expType != null ? expType : originalType;
        }

        BTupleType type = new BTupleType(null, members);
        type.restType = newRestType;
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BStreamType originalType, BType expType) {
        BStreamType matchingType = (BStreamType) getMatchingTypeForInferrableType(originalType, expType);
        boolean hasMatchedStreamType = matchingType != null;
        BStreamType expStreamType = hasMatchedStreamType ? matchingType : null;

        BType expConstraint = hasMatchedStreamType ? expStreamType.constraint : null;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        BType newError = null;
        if (originalType.completionType != null) {
            BType expError = hasMatchedStreamType ? matchingType.completionType : null;
            newError = originalType.completionType.accept(this, expError);
        }

        if (isSameType(newConstraint, originalType.constraint) && isSameType(newError, originalType.completionType)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint) || isSemanticErrorInInvocation(newError)) {
            return symbolTable.semanticError;
        }

        BStreamType type = new BStreamType(originalType.tag, newConstraint, newError, null);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BTableType originalType, BType expType) {
        BTableType matchingType = (BTableType) getMatchingTypeForInferrableType(originalType, expType);
        boolean hasMatchedTableType = matchingType != null;
        BType expConstraint = hasMatchedTableType ? matchingType.constraint : null;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        BType newKeyTypeConstraint = null;
        if (originalType.keyTypeConstraint != null) {
            BType expKeyConstraint = hasMatchedTableType ? matchingType.keyTypeConstraint : null;
            originalType.keyTypeConstraint.accept(this, expKeyConstraint);
        }

        if (isSameType(newConstraint, originalType.constraint) &&
                isSameType(newKeyTypeConstraint, originalType.keyTypeConstraint)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint) || isSemanticErrorInInvocation(newKeyTypeConstraint)) {
            return symbolTable.semanticError;
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
        if (Symbols.isFlagOn(originalType.flags, Flags.ANY_FUNCTION)) {
            return originalType;
        }

        boolean hasNewType = false;

        BInvokableType matchingType = (BInvokableType) getMatchingTypeForInferrableType(originalType, expType);
        boolean hasMatchingInvokableType = matchingType != null;

        List<BType> paramTypes = new ArrayList<>();
        List<BType> expParamTypes = hasMatchingInvokableType ? matchingType.paramTypes :
                Collections.singletonList(null);
        int delta = hasMatchingInvokableType ? 1 : 0;

        boolean errored = false;

        List<BType> bTypes = originalType.paramTypes;
        for (int i = 0, j = 0; i < bTypes.size(); i++, j += delta) {
            BType type = bTypes.get(i);
            BType expParamType = expParamTypes.get(j);
            BType newT = type.accept(this, expParamType);
            paramTypes.add(newT);

            if (isSemanticErrorInInvocation(newT)) {
                errored = true;
            } else if (!isSameType(newT, type)) {
                hasNewType = true;
            }
        }

        BType restType = originalType.restType;
        BType newRestType = null;
        if (restType != null) {
            BType expRestType = hasMatchingInvokableType ? matchingType.restType : null;
            newRestType = restType.accept(this, expRestType);

            if (isSemanticErrorInInvocation(newRestType)) {
                errored = true;
            } else if (!isSameType(newRestType, restType)) {
                hasNewType = true;
            }
        }

        BType expRetType = hasMatchingInvokableType ? matchingType.retType : null;
        BType retType = originalType.retType.accept(this, expRetType);

        if (errored) {
            return expType;
        }

        if (!hasNewType) {
            if (isSameType(retType, originalType.retType)) {
                return originalType;
            }

            if (isSemanticErrorInInvocation(retType)) {
                return symbolTable.semanticError;
            }
        }

        BType type = new BInvokableType(paramTypes, newRestType, retType, null);
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

            if (this.function != null && member.tag == TypeTags.PARAMETERIZED_TYPE) {
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

            if (this.isInvocation) {
                if (newMember == symbolTable.semanticError) {
                    return symbolTable.semanticError;
                }

                if (newMember == member && Symbols.isFlagOn(member.flags, Flags.PARAMETERIZED)) {
                    return expType;
                }
            }

            newMemberTypes.add(newMember);

            if (!isSameTypeOrError(newMember, member)) {
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

    @Override
    public BType visit(BIntersectionType originalType, BType expType) {
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expEffectiveType = matchingType == null ?
                null : ((BIntersectionType) matchingType).effectiveType;
        BType newEffectiveType = originalType.effectiveType.accept(this, expEffectiveType);

        if (isSameType(newEffectiveType, originalType.effectiveType)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newEffectiveType)) {
            return symbolTable.semanticError;
        }

        BIntersectionType type = new BIntersectionType(null, (LinkedHashSet<BType>) originalType.getConstituentTypes(),
                                                       (IntersectableReferenceType) newEffectiveType);
        setFlags(type, originalType.flags);
        return originalType;
    }

    @Override
    public BType visit(BErrorType originalType, BType expType) {
        return originalType;
    }

    @Override
    public BType visit(BFutureType originalType, BType expType) {
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expConstraint = matchingType == null ? null : ((BFutureType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (isSameType(newConstraint, originalType.constraint)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint)) {
            return symbolTable.semanticError;
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
        BType matchingType = getMatchingTypeForInferrableType(originalType, expType);
        BType expConstraint = matchingType == null ? null : ((BTypedescType) expType).constraint;
        BType newConstraint = originalType.constraint.accept(this, expConstraint);

        if (isSameType(newConstraint, originalType.constraint)) {
            return originalType;
        }

        if (isSemanticErrorInInvocation(newConstraint)) {
            return symbolTable.semanticError;
        }

        BTypedescType newTypedescType = new BTypedescType(newConstraint, null);
        setFlags(newTypedescType, originalType.flags);
        return newTypedescType;
    }

    @Override
    public BType visit(BParameterizedType originalType, BType expType) {
        String paramVarName = originalType.paramSymbol.name.value;

        if (Symbols.isFlagOn(originalType.paramSymbol.flags, Flags.INFER)) {
            BTypedescType paramSymbolTypedescType =
                    (BTypedescType) getConstraintFromReferenceType(originalType.paramSymbol.type);
            BType paramSymbolType = paramSymbolTypedescType.constraint;
            if (expType != null) {
                if (expType == symbolTable.noType) {
                    if (!paramValueTypes.containsKey(paramVarName)) {
                        // Log an error only if the user has not explicitly passed an argument. If the passed
                        // argument is invalid, the type checker will log the error.
                        logCannotInferTypedescArgumentError(paramVarName);
                        return symbolTable.semanticError;
                    }

                    BType type = paramValueTypes.get(paramVarName);
                    return type == symbolTable.semanticError ? expType : ((BTypedescType) type).constraint;
                }

                if (!types.isAssignable(expType, paramSymbolType)) {
                    if (!paramValueTypes.containsKey(paramVarName)) {
                        // Log an error only if the user has not explicitly passed an argument. If the passed
                        // argument is invalid, the type checker will log the error.
                        dlog.error(invocation.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_FOR_INFERRED_TYPEDESC_VALUE,
                                   paramVarName, paramSymbolTypedescType, new BTypedescType(expType, null));
                        return symbolTable.semanticError;
                    }
                    BType type = paramValueTypes.get(paramVarName);
                    return type == symbolTable.semanticError ? expType : ((BTypedescType) type).constraint;
                }

                BType type = getTypeAddingArgIfNotProvided(originalType, expType);
                return type == symbolTable.semanticError ? expType : type;
            }

            if (this.isInvocation) {
                if (paramValueTypes.containsKey(paramVarName)) {
                    return getConstraintTypeIfNotError(paramValueTypes.get(paramVarName));
                }

                logCannotInferTypedescArgumentError(paramVarName);
                return symbolTable.semanticError;
            }

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

            type = ((BTypedescType) getConstraintFromReferenceType(type)).constraint;
        } else {
            type = ((BTypedescType) getConstraintFromReferenceType(originalType.paramSymbol.type)).constraint;
        }
        return type;
    }

    private void logCannotInferTypedescArgumentError(String paramName) {
        if (invocation.expectedType == symbolTable.noType) {
            dlog.error(invocation.pos,
                    DiagnosticErrorCode.CANNOT_INFER_TYPEDESC_ARGUMENT_WITHOUT_CET,
                    paramName);
        } else {
            dlog.error(invocation.pos,
                    DiagnosticErrorCode.CANNOT_INFER_TYPEDESC_ARGUMENT_FROM_CET, paramName,
                    invocation.expectedType, ((BInvokableSymbol) invocation.symbol).retType);
        }
    }

    public BType visit(BTypeReferenceType t, BType s) {
        return visit(getConstraintFromReferenceType(t), s);
    }

    private BType getConstraintFromReferenceType(BType type) {
        BType constraint = type;
        if (type.tag == TypeTags.TYPEREFDESC) {
            constraint = getConstraintFromReferenceType(((BTypeReferenceType) type).referredType);
        }
        return constraint;
    }

    private BType getTypeAddingArgIfNotProvided(BParameterizedType originalType, BType expType) {
        BVarSymbol paramSymbol = originalType.paramSymbol;

        String paramName = paramSymbol.name.value;

        List<BLangExpression> requiredArgs = invocation.requiredArgs;

        for (BLangExpression arg : requiredArgs) {
            if (arg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                continue;
            }

            BLangNamedArgsExpression namedArgsExpression = (BLangNamedArgsExpression) arg;
            if (namedArgsExpression.name.value.equals(paramName)) {
                return getConstraintTypeIfNotError(namedArgsExpression.expr.getBType());
            }
        }

        int index = getParamPosition(paramSymbol);

        List<BLangExpression> restArgs = invocation.restArgs;

        int requiredArgCount = requiredArgs.size();
        if (index < requiredArgCount) {
            BLangExpression argAtIndex = requiredArgs.get(index);

            if (argAtIndex.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                return getConstraintTypeIfNotError(argAtIndex.getBType());
            }
        } else if (!restArgs.isEmpty()) {
            BType restArgType = restArgs.get(0).getBType();

            if (restArgType.tag == TypeTags.RECORD) {
                return getConstraintTypeIfNotError(((BRecordType) restArgType).fields.get(paramName).type);
            }
            return getConstraintTypeIfNotError(
                    ((BTupleType) restArgType).getTupleTypes().get(index - requiredArgCount));
        }

        BLangNamedArgsExpression namedArg = createTypedescExprNamedArg(expType, paramName);
        this.invocation.argExprs.add(namedArg);
        this.invocation.requiredArgs.add(namedArg);
        return expType;
    }

    private BType getConstraintTypeIfNotError(BType type) {
        if (type == symbolTable.semanticError) {
            return type;
        }

        return ((BTypedescType) type).constraint;
    }

    private BLangNamedArgsExpression createTypedescExprNamedArg(BType expType, String name) {
        BLangTypedescExpr typedescExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typedescExpr.pos = this.symbolTable.builtinPos;
        typedescExpr.resolvedType = expType;
        typedescExpr.setBType(new BTypedescType(expType, null));

        BLangNamedArgsExpression namedArgsExpression = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        BLangIdentifier identifierNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        identifierNode.value = name;
        namedArgsExpression.name = identifierNode;
        namedArgsExpression.expr = typedescExpr;
        namedArgsExpression.pos = this.symbolTable.builtinPos;
        return namedArgsExpression;
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
                    paramValueTypes.put(params.get(paramIndex).name.value, arg.getBType());
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
            if (param.isDefaultable && !Symbols.isFlagOn(param.flags, Flags.INFER)) {
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
                    paramValueTypes.put(name, namedArg.getBType());
                    argProvided = true;
                    break;
                }
            }

            if (argProvided) {
                continue;
            }

            if (param.isDefaultable && !Symbols.isFlagOn(param.flags, Flags.INFER)) {
                paramValueTypes.put(name, symbol.paramDefaultValTypes.get(name));
            }
        }
    }

    private void populateParamMapFromRestArg(List<BVarSymbol> params, int currentParamIndex, BLangExpression restArg) {
        BType type = Types.getReferredType(restArg.getBType());
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
        List<BType> tupleTypes = tupleType.getTupleTypes();
        for (int i = currentParamIndex; i < params.size(); i++) {
            paramValueTypes.put(params.get(i).name.value, tupleTypes.get(tupleIndex++));
        }
    }

    private void setFlags(BType type, long originalFlags) {
        type.flags = originalFlags & (~Flags.PARAMETERIZED);
    }

    private int getParamPosition(BVarSymbol sym) {
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

            BType paramType = requiredParam.getBType();
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
        BType typeIntersection =
                types.getTypeIntersection(Types.IntersectionContext.compilerInternalIntersectionContext(), t1, t2, env);
        return typeIntersection != null && typeIntersection != symbolTable.semanticError;
    }

    private boolean hasSameBasicType(BType t1, BType t2) {
        int tag1 = Types.getReferredType(t1).tag;
        int tag2 = Types.getReferredType(t2).tag;

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
            return false;
        }

        return isListType(tag1) && isListType(tag2);
    }

    private boolean isMappingType(int tag) {
        return tag == TypeTags.MAP || tag == TypeTags.RECORD;
    }

    private boolean isListType(int tag) {
        return tag == TypeTags.ARRAY || tag == TypeTags.TUPLE;
    }

    private BType getMatchingTypeForInferrableType(BType originalType, BType expType) {
        if (expType == null || !this.isInvocation) {
            return null;
        }

        List<String> paramsWithInferredTypedescDefault =
                getParamsWithInferredTypedescDefault(((BInvokableSymbol) this.invocation.symbol).params);
        if (paramsWithInferredTypedescDefault.isEmpty()) {
            return null;
        }

        List<BType> inferableTypes = new ArrayList<>();

        if (originalType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) originalType).getMemberTypes()) {
                if (!Symbols.isFlagOn(memberType.flags, Flags.PARAMETERIZED)) {
                    continue;
                }

                if (!refersInferableParamName(paramsWithInferredTypedescDefault, memberType)) {
                    continue;
                }

                inferableTypes.add(memberType);
            }
        } else if (refersInferableParamName(paramsWithInferredTypedescDefault, originalType)) {
            inferableTypes.add(originalType);
        }

        if (inferableTypes.isEmpty()) {
            return null;
        }

        Set<BType> expectedTypes = expType.tag == TypeTags.UNION ? ((BUnionType) expType).getMemberTypes() :
                Set.of(expType);

        HashSet<BType> matchedTypes = new LinkedHashSet<>();

        for (BType inferableType : inferableTypes) {
            for (BType expectedType : expectedTypes) {
                if (inferableType.tag == expectedType.tag) {
                    matchedTypes.add(expectedType);
                }
            }
        }

        if (matchedTypes.size() == 1) {
            return matchedTypes.iterator().next();
        }

        return null;
    }

    public boolean refersInferableParamName(String paramWithInferredTypedescDefault, BType type) {
        return refersInferableParamName(List.of(paramWithInferredTypedescDefault), type, new HashSet<>());
    }

    private boolean refersInferableParamName(List<String> paramsWithInferredTypedescDefault, BType type) {
        return refersInferableParamName(paramsWithInferredTypedescDefault, type, new HashSet<>());
    }

    private boolean refersInferableParamName(List<String> paramsWithInferredTypedescDefault, BType type,
                                             Set<BType> unresolvedTypes) {
        if (!unresolvedTypes.add(type)) {
            return false;
        }

        switch (type.tag) {
            case TypeTags.PARAMETERIZED_TYPE:
                String paramName = ((BParameterizedType) type).paramSymbol.name.value;
                return paramsWithInferredTypedescDefault.contains(paramName);
            case TypeTags.XML:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BXMLType) type).constraint,
                                                unresolvedTypes);
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) type;
                if (refersInferableParamName(paramsWithInferredTypedescDefault, tableType.constraint,
                                             unresolvedTypes)) {
                    return true;
                }

                BType keyTypeConstraint = tableType.keyTypeConstraint;
                if (keyTypeConstraint == null) {
                    return false;
                }

                return refersInferableParamName(paramsWithInferredTypedescDefault, keyTypeConstraint, unresolvedTypes);
            case TypeTags.TYPEDESC:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BTypedescType) type).constraint,
                                                unresolvedTypes);
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) type;
                if (refersInferableParamName(paramsWithInferredTypedescDefault, streamType.constraint,
                                             unresolvedTypes)) {
                    return true;
                }

                BType completionType = streamType.completionType;
                if (completionType == null) {
                    return false;
                }
                return refersInferableParamName(paramsWithInferredTypedescDefault, completionType, unresolvedTypes);
            case TypeTags.INVOKABLE:
                if (Symbols.isFlagOn(type.flags, Flags.ANY_FUNCTION)) {
                    return false;
                }
                BInvokableType invokableType = (BInvokableType) type;
                for (BType paramType : invokableType.paramTypes) {
                    if (refersInferableParamName(paramsWithInferredTypedescDefault, paramType, unresolvedTypes)) {
                        return true;
                    }
                }

                BType funcRestType = invokableType.restType;
                if (funcRestType != null &&
                        refersInferableParamName(paramsWithInferredTypedescDefault, funcRestType, unresolvedTypes)) {
                    return true;
                }

                BType funcRetType = invokableType.retType;
                if (funcRetType == null) {
                    return false;
                }
                return refersInferableParamName(paramsWithInferredTypedescDefault, funcRetType, unresolvedTypes);
            case TypeTags.ARRAY:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BArrayType) type).eType,
                                                unresolvedTypes);
            case TypeTags.UNION:
                for (BType unionMemType : ((BUnionType) type).getMemberTypes()) {
                    if (refersInferableParamName(paramsWithInferredTypedescDefault, unionMemType, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.INTERSECTION:
                for (BType intersectionMemType : ((BIntersectionType) type).getConstituentTypes()) {
                    if (refersInferableParamName(paramsWithInferredTypedescDefault, intersectionMemType,
                                                 unresolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.MAP:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BMapType) type).constraint,
                                                unresolvedTypes);
            case TypeTags.ERROR:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BErrorType) type).detailType,
                                                unresolvedTypes);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;

                for (BType tupleMember : tupleType.getTupleTypes()) {
                    if (refersInferableParamName(
                            paramsWithInferredTypedescDefault, tupleMember, unresolvedTypes)) {
                        return true;
                    }
                }

                BType tupleRestType = tupleType.restType;
                if (tupleRestType == null) {
                    return false;
                }

                return refersInferableParamName(paramsWithInferredTypedescDefault, tupleRestType, unresolvedTypes);
            case TypeTags.FUTURE:
                return refersInferableParamName(paramsWithInferredTypedescDefault, ((BFutureType) type).constraint,
                                                unresolvedTypes);
            case TypeTags.TYPEREFDESC:
                return refersInferableParamName(paramsWithInferredTypedescDefault, Types.getReferredType(type),
                        unresolvedTypes);
        }
        return false;
    }

    private List<String> getParamsWithInferredTypedescDefault(List<BVarSymbol> params) {
        List<String> paramsWithInferredTypedescDefault = new ArrayList<>();

        for (BVarSymbol param : params) {
            if (Symbols.isFlagOn(param.flags, Flags.INFER)) {
                paramsWithInferredTypedescDefault.add(param.name.value);
            }
        }
        return paramsWithInferredTypedescDefault;
    }

    private BType getExpectedTypeForInferredTypedescMember(BUnionType originalType, BType expType, BType member) {
        if (expType == null || !this.isInvocation || !Symbols.isFlagOn(member.flags, Flags.PARAMETERIZED)) {
            return null;
        }

        if (Types.getReferredType(expType).tag != TypeTags.UNION) {
            return expType;
        }

        LinkedHashSet<BType> types = new LinkedHashSet<>();
        for (BType expMemType : ((BUnionType) Types.getReferredType(expType)).getMemberTypes()) {
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

    private boolean isSameTypeOrError(BType newType, BType originalType) {
        return isSameType(newType, originalType) || isSemanticErrorInInvocation(newType);
    }

    private boolean isSameType(BType newType, BType originalType) {
        return newType == originalType;
    }

    private boolean isSemanticErrorInInvocation(BType newType) {
        return this.isInvocation && newType == symbolTable.semanticError;
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
