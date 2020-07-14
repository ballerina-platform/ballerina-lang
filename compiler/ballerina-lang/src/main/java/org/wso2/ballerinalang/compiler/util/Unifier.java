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
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Util class for building concrete BType types from parameterized types.
 *
 * @since 2.0.0
 */
public class Unifier implements BTypeVisitor<BType, BType> {

    private static final CompilerContext.Key<Unifier> UNIFIER_KEY = new CompilerContext.Key<>();

    private Map<String, BType> paramValueTypes;
    private boolean isInvocation;
    private BLangInvocation invocation;
    private BLangDiagnosticLogHelper dlogHelper;
    private final SymbolTable symTable;
    private final Types types;

    private Unifier(CompilerContext context) {
        context.put(UNIFIER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlogHelper = BLangDiagnosticLogHelper.getInstance(context);
    }

    public static Unifier getInstance(CompilerContext context) {
        Unifier unifier = context.get(UNIFIER_KEY);
        if (unifier == null) {
            unifier = new Unifier(context);
        }

        return unifier;
    }

    public BType build(BType originalType, BType expType, BLangInvocation invocation) {
        this.isInvocation = invocation != null;
        if (this.isInvocation) {
            this.invocation = invocation;
            createParamMap(invocation);
        }
        BType newType = originalType.accept(this, expType);
        reset();
        return newType;
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
        boolean hasNewType = false;
        LinkedHashSet<BType> newMemberTypes = new LinkedHashSet<>();

        for (BType member : originalType.getMemberTypes()) {
            BType newMember = member.accept(this, null);
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
    public BType visit(BServiceType originalType, BType expType) {
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
            if (expType != null && types.isAssignable(expType, paramSymbolType)) {
                BLangTypedescExpr typedescExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
                typedescExpr.pos = this.invocation.pos;
                typedescExpr.resolvedType = expType;
                typedescExpr.type = new BTypedescType(expType, null);

                int indx = pos(originalType.paramSymbol);
                if (indx >= invocation.requiredArgs.size()) {
                    this.invocation.argExprs.add(indx, typedescExpr);
                    this.invocation.requiredArgs.add(indx, typedescExpr);
                }
                return expType;
            }
            // TODO: Instead of this, see if a more specific error message can be given here.
            // e.g., incompatible inferred type: expected '(string|float)', inferred 'int'
            return paramSymbolType;
        }

        // This would return null if the calling function gets analyzed before the callee function. This only
        // happens when the invocation uses the default value of the param.
        BType type = paramValueTypes.get(paramVarName);

        if (type == null) {
            return originalType.paramValueType;
        }

        if (type.tag == TypeTags.SEMANTIC_ERROR) {
            return type;
        }

        type = ((BTypedescType) type).constraint;
        return type;
    }

    private void createParamMap(BLangInvocation invocation) {
        paramValueTypes = new LinkedHashMap<>();
        BInvokableSymbol symbol = (BInvokableSymbol) invocation.symbol;
        int nArgs = invocation.requiredArgs.size();
        int nParams = symbol.params.size();
        BVarSymbol param;

        for (int i = 0; i < nParams; i++) {
            param = symbol.params.get(i);

            if (param.defaultableParam &&
                    (i >= nArgs || invocation.requiredArgs.get(i).getKind() == NodeKind.IGNORE_EXPR)) {
                paramValueTypes.put(param.name.value, symbol.paramDefaultValTypes.get(param.name.value));
                continue;
            }

            paramValueTypes.put(param.name.value, invocation.requiredArgs.get(i).type);
        }
    }

    private void setFlags(BType type, int originalFlags) {
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

    private void reset() {
        this.paramValueTypes = null;
        this.isInvocation = false;
    }
}
