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

import org.ballerinalang.model.tree.NodeKind;
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
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
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
public class ResolvedTypeBuilder implements BTypeVisitor<BType, BType> {

    private Map<String, BType> paramValueTypes;
    private Set<BType> visitedTypes;
    private boolean isInvocation;

    public BType build(BType originalType, BLangInvocation invocation) {
        this.isInvocation = invocation != null;
        this.visitedTypes = new HashSet<>();
        if (this.isInvocation) {
            createParamMap(invocation);
        }
        BType newType = originalType.accept(this, null);
        reset();
        return newType;
    }

    public BType build(BType originalType) {
        return build(originalType, null);
    }

    @Override
    public BType visit(BType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BBuiltInRefType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BAnyType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BAnydataType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BMapType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BMapType newMType = new BMapType(originalType.tag, newConstraint, null);
        setFlags(newMType, originalType.flags);
        return newMType;
    }

    @Override
    public BType visit(BXMLType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BXMLType newXMLType = new BXMLType(newConstraint, null);
        setFlags(newXMLType, originalType.flags);
        return newXMLType;
    }

    @Override
    public BType visit(BJSONType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BArrayType originalType, BType newType) {
        BType newElemType = originalType.eType.accept(this, null);

        if (newElemType == originalType.eType) {
            return originalType;
        }

        BArrayType newArrayType = new BArrayType(newElemType, null, originalType.size, originalType.state);
        setFlags(newArrayType, originalType.flags);
        return newArrayType;
    }

    @Override
    public BType visit(BObjectType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BRecordType originalType, BType newType) {
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
    public BType visit(BTupleType originalType, BType newType) {
        boolean hasNewType = false;
        List<BType> members = new ArrayList<>();
        for (BType member : originalType.tupleTypes) {
            BType newMem = member.accept(this, null);
            members.add(newMem);

            if (newMem != member) {
                hasNewType = true;
            }
        }

        BType rest = originalType.restType;
        if (rest != null) {
            rest = rest.accept(this, null);

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
    public BType visit(BStreamType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);
        BType newError = originalType.error != null ? originalType.error.accept(this, null) : null;

        if (newConstraint == originalType.constraint && newError == originalType.error) {
            return originalType;
        }

        BStreamType type = new BStreamType(originalType.tag, newConstraint, newError, null);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BTableType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);
        BType newKeyTypeConstraint = originalType.keyTypeConstraint != null ?
                originalType.keyTypeConstraint.accept(this, null) : null;

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
    public BType visit(BInvokableType originalType, BType newType) {
        boolean hasNewType = false;
        List<BType> paramTypes = new ArrayList<>();
        for (BType type : originalType.paramTypes) {
            BType newT = type.accept(this, null);
            paramTypes.add(newT);

            if (newT != type) {
                hasNewType = true;
            }
        }

        BType rest = originalType.restType;
        if (rest != null) {
            rest = rest.accept(this, null);

            if (rest != originalType.restType) {
                hasNewType = true;
            }
        }

        BType retType = originalType.retType.accept(this, null);

        if (!hasNewType && retType != originalType.retType) {
            return originalType;
        }

        BType type = new BInvokableType(paramTypes, rest, retType, null);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BUnionType originalType, BType newType) {
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
    public BType visit(BIntersectionType originalType, BType newType) {
        BType newEffectiveType = originalType.effectiveType.accept(this, null);

        if (newEffectiveType == originalType.effectiveType) {
            return originalType;
        }

        BIntersectionType type = new BIntersectionType(null, (LinkedHashSet<BType>) originalType.getConstituentTypes(),
                                                       newEffectiveType);
        setFlags(type, originalType.flags);
        return originalType;
    }

    @Override
    public BType visit(BErrorType originalType, BType newType) {
        BType newDetail = originalType.detailType.accept(this, null);

        if (newDetail == originalType.detailType) {
            return originalType;
        }

        BErrorType type = new BErrorType(null, newDetail);
        setFlags(type, originalType.flags);
        return type;
    }

    @Override
    public BType visit(BFutureType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BFutureType newFutureType = new BFutureType(originalType.tag, newConstraint, null,
                                                    originalType.workerDerivative);
        setFlags(newFutureType, originalType.flags);
        return newFutureType;
    }

    @Override
    public BType visit(BFiniteType originalType, BType newType) {
        // Not applicable for finite types since the descriptor has to be defined before using in the return type.
        return originalType;
    }

    @Override
    public BType visit(BServiceType originalType, BType newType) {
        return originalType;
    }

    @Override
    public BType visit(BTypedescType originalType, BType newType) {
        BType newConstraint = originalType.constraint.accept(this, null);

        if (newConstraint == originalType.constraint) {
            return originalType;
        }

        BTypedescType newTypedescType = new BTypedescType(newConstraint, null);
        setFlags(newTypedescType, originalType.flags);
        return newTypedescType;
    }

    @Override
    public BType visit(BParameterizedType originalType, BType newType) {
        String paramVarName = originalType.paramSymbol.name.value;
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

    private void reset() {
        this.visitedTypes = null;
        this.paramValueTypes = null;
        this.isInvocation = false;
    }
}
