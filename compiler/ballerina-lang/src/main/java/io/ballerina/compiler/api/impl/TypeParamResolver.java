/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides an API which given a type containing a type param component, returns a new type with the type
 * param component bound to the specified bound type.
 *
 * @since 2.0.0
 */
public class TypeParamResolver implements BTypeVisitor<BType, BType> {

    private final Map<BType, BType> boundTypes = new HashMap<>();
    private final BType typeParam;

    public TypeParamResolver(BType typeParam) {
        this.typeParam = typeParam;
    }

    /**
     * Given a type containing a type param component, this method will return a new type of the same kind with the type
     * param components bound to the specified bound type. This only works for a single type param, specified when
     * creating the TypeParamResolver instance. If the type doesn't contain any type param components, it'll return the
     * same type instance.
     *
     * @param typeParam The type containing the type param component
     * @param boundType The type to bind the type param to
     * @return The type param resolved type instance
     */
    public BType resolve(BType typeParam, BType boundType) {
        if (boundTypes.containsKey(typeParam)) {
            return boundTypes.get(typeParam);
        }

        if (isTypeParam(typeParam)) {
            this.boundTypes.put(typeParam, boundType);
            return boundType;
        }

        BType type = typeParam.accept(this, boundType);
        boundTypes.put(typeParam, type);
        return type;
    }

    @Override
    public BType visit(BType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BBuiltInRefType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BAnyType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BAnydataType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BMapType typeInSymbol, BType boundType) {
        BType boundConstraintType = resolve(typeInSymbol.constraint, boundType);

        if (boundConstraintType == typeInSymbol) {
            return typeInSymbol;
        }

        return new BMapType(typeInSymbol.tag, boundConstraintType, typeInSymbol.tsymbol, typeInSymbol.flags);
    }

    @Override
    public BType visit(BXMLType typeInSymbol, BType boundType) {
        BType boundConstraintType = resolve(typeInSymbol.constraint, boundType);

        if (boundConstraintType == typeInSymbol) {
            return typeInSymbol;
        }

        return new BXMLType(boundConstraintType, typeInSymbol.tsymbol, typeInSymbol.flags);
    }

    @Override
    public BType visit(BJSONType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BArrayType typeInSymbol, BType boundType) {
        BType boundElemType = resolve(typeInSymbol.eType, boundType);

        if (boundElemType == typeInSymbol) {
            return typeInSymbol;
        }

        return new BArrayType(boundElemType, typeInSymbol.tsymbol, typeInSymbol.size, typeInSymbol.state,
                              typeInSymbol.flags);
    }

    @Override
    public BType visit(BObjectType typeInSymbol, BType boundType) {

        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeInSymbol.tsymbol;
        LinkedHashMap<String, BField> newObjectFields = new LinkedHashMap<>();

        // Fields
        Set<Map.Entry<String, BField>> entries = typeInSymbol.getFields().entrySet();
        for (Map.Entry<String, BField> entry: entries) {
            BType newType = resolve(entry.getValue().type, boundType);
            BVarSymbol newVarSymbol = createNewVarSymbol(entry.getValue().symbol, newType);
            BField newField = new BField(newVarSymbol.getName(), newVarSymbol.getPosition(), newVarSymbol);
            newObjectFields.put(entry.getKey(), newField);
        }

        // Attached functions
        List<BAttachedFunction> newAttachedFuncs = new ArrayList<>();
        for (BAttachedFunction attachedFunc : objectTypeSymbol.attachedFuncs) {
            BType newType = resolve(attachedFunc.type, boundType);
            BInvokableSymbol newInvokableSymbol = resolveInvokableSymbol(attachedFunc.symbol, (BInvokableType) newType,
                                                                         boundType);
            BAttachedFunction newAttachedFunc = new BAttachedFunction(attachedFunc.funcName, newInvokableSymbol,
                                                    (BInvokableType) newType, attachedFunc.pos);
            newAttachedFuncs.add(newAttachedFunc);
        }

        BObjectTypeSymbol newTypeSymbol = new BObjectTypeSymbol(objectTypeSymbol.tag, objectTypeSymbol.flags,
                objectTypeSymbol.name, objectTypeSymbol.pkgID, objectTypeSymbol.getType(), objectTypeSymbol.owner,
                objectTypeSymbol.pos, objectTypeSymbol.origin);
        BObjectType newObjectType = new BObjectType(newTypeSymbol, typeInSymbol.flags);

        newObjectType.fields = newObjectFields;
        newTypeSymbol.attachedFuncs = newAttachedFuncs;
        newTypeSymbol.type = newObjectType;
        return newObjectType;
    }

    @Override
    public BType visit(BRecordType typeInSymbol, BType boundType) {
        LinkedHashMap<String, BField> newRecordFields = new LinkedHashMap<>();
        Set<Map.Entry<String, BField>> entries = typeInSymbol.fields.entrySet();
        for (Map.Entry<String, BField> entry: entries) {
            BType newType = resolve(entry.getValue().type, boundType);
            BVarSymbol newVarSymbol = createNewVarSymbol(entry.getValue().symbol, newType);
            BField newField = new BField(newVarSymbol.getName(), newVarSymbol.getPosition(), newVarSymbol);
            newRecordFields.put(entry.getKey(), newField);
        }

        BType newRestType = resolve(typeInSymbol.restFieldType, boundType);
        BRecordType newRecordType = new BRecordType(typeInSymbol.tsymbol, typeInSymbol.flags);

        newRecordType.fields = newRecordFields;
        newRecordType.restFieldType = newRestType;
        return newRecordType;
    }

    @Override
    public BType visit(BTupleType typeInSymbol, BType boundType) {
        List<BTupleMember> newTupleMembers = new ArrayList<>();

        List<BTupleMember> tupleMembers = typeInSymbol.getMembers();
        boolean areAllSameType = true;

        for (BTupleMember tupleMember : tupleMembers) {
            BType newType = resolve(tupleMember.type, boundType);
            areAllSameType &= newType == tupleMember.type;
            BVarSymbol varSymbol = createNewVarSymbol(tupleMember.symbol, newType);
            newTupleMembers.add(new BTupleMember(newType, varSymbol));
        }

        BType newRestType = typeInSymbol.restType != null ? resolve(typeInSymbol.restType, boundType) : null;

        if (areAllSameType && newRestType == typeInSymbol.restType) {
            return typeInSymbol;
        }

        return new BTupleType(typeInSymbol.tsymbol, newTupleMembers, newRestType, typeInSymbol.flags,
                              typeInSymbol.isCyclic);
    }

    @Override
    public BType visit(BStreamType typeInSymbol, BType boundType) {
        BType boundConstraintType = resolve(typeInSymbol.constraint, boundType);
        // TODO: The completion type igonred for now

        if (boundConstraintType == typeInSymbol) {
            return typeInSymbol;
        }

        return new BStreamType(typeInSymbol.tag, boundConstraintType, typeInSymbol.completionType,
                               typeInSymbol.tsymbol);
    }

    @Override
    public BType visit(BTableType typeInSymbol, BType boundType) {
        BType boundConstraintType = resolve(typeInSymbol.constraint, boundType);
        // TODO: Resolving the key constraint type ignored for now

        if (boundConstraintType == typeInSymbol) {
            return typeInSymbol;
        }

        BTableType bTableType = new BTableType(typeInSymbol.tag, boundConstraintType, typeInSymbol.tsymbol,
                                               typeInSymbol.flags);
        bTableType.keyTypeConstraint = typeInSymbol.keyTypeConstraint;
        return bTableType;
    }

    @Override
    public BType visit(BInvokableType typeInSymbol, BType boundType) {
        List<BType> newParamTypes = new ArrayList<>();
        for (BType paramType : typeInSymbol.paramTypes) {
            BType newType = resolve(paramType, boundType);
            newParamTypes.add(newType);
        }

        BType newRestParamType = null;
        if (typeInSymbol.restType != null) {
            newRestParamType = resolve(typeInSymbol.restType, boundType);
        }

        BType newReturnType = null;
        if (typeInSymbol.retType != null) {
            newReturnType = resolve(typeInSymbol.retType, boundType);
        }

        return new BInvokableType(newParamTypes, newRestParamType, newReturnType, typeInSymbol.tsymbol);
    }

    @Override
    public BType visit(BUnionType typeInSymbol, BType boundType) {
        LinkedHashSet<BType> newMembers = new LinkedHashSet<>();
        boolean areAllSameType = true;

        if (typeInSymbol.isCyclic) {
            return typeInSymbol;   // TODO: Resolve cyclic union-types with type-params [Fix #36519]
        }

        for (BType memberType : typeInSymbol.getOriginalMemberTypes()) {
            BType newType = resolve(memberType, boundType);
            areAllSameType &= newType == memberType;
            newMembers.add(newType);
        }

        if (areAllSameType) {
            return typeInSymbol;
        }

        return BUnionType.create(typeInSymbol.tsymbol, newMembers);
    }

    @Override
    public BType visit(BIntersectionType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BErrorType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BFutureType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BFiniteType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BTypedescType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BParameterizedType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    public BType visit(BTypeReferenceType typeInSymbol, BType s) {
        return typeInSymbol;
    }

    private boolean isTypeParam(BType type) {
        return type == this.typeParam;
    }

    private BVarSymbol createNewVarSymbol(BVarSymbol symbol, BType newType) {

        if (symbol == null) {
            return null;
        }

        BType originalType = symbol.getType();

        if (originalType == newType) {
            return symbol;
        }

        BVarSymbol duplicateSymbol = duplicateSymbol(symbol);
        duplicateSymbol.type = newType;

        return duplicateSymbol;
    }

    private BInvokableSymbol resolveInvokableSymbol(BInvokableSymbol symbol,
                                                    BInvokableType newInvokableType,
                                                    BType boundType) {
        BInvokableSymbol newInvokableSymbol = duplicateSymbol(symbol);

        for (BVarSymbol param : symbol.params) {
            BType newParamType = resolve(param.type, boundType);
            BVarSymbol newVarSymbol = createNewVarSymbol(param, newParamType);
            newInvokableSymbol.params.add(newVarSymbol);
        }

        if (symbol.restParam != null) {
            BType newRestParam = resolve(symbol.restParam.type, boundType);
            newInvokableSymbol.restParam = createNewVarSymbol(symbol.restParam, newRestParam);
        }

        newInvokableSymbol.retType = resolve(symbol.retType, boundType);

        BInvokableTypeSymbol originalTSymbol = (BInvokableTypeSymbol) newInvokableType.tsymbol;
        BInvokableTypeSymbol newInvokableTypeSym = Symbols.createInvokableTypeSymbol(originalTSymbol.tag,
                originalTSymbol.flags, originalTSymbol.pkgID, newInvokableType, originalTSymbol.owner,
                originalTSymbol.pos, originalTSymbol.origin);
        newInvokableTypeSym.params = newInvokableSymbol.params;
        newInvokableTypeSym.restParam = newInvokableSymbol.restParam;
        newInvokableTypeSym.returnType = newInvokableSymbol.retType;

        newInvokableType.tsymbol = newInvokableTypeSym;
        newInvokableSymbol.type = newInvokableType;

        return newInvokableSymbol;
    }

    private BVarSymbol duplicateSymbol(BVarSymbol original) {
        BVarSymbol duplicate = new BVarSymbol(original.flags, original.isWildcard, original.name, original.originalName,
                original.pkgID, original.type, original.owner, original.pos,
                original.origin);
        duplicate.markdownDocumentation = original.markdownDocumentation;

        for (AnnotationAttachmentSymbol annot : original.getAnnotations()) {
            duplicate.addAnnotation(annot);
        }

        duplicate.isDefaultable = original.isDefaultable;
        duplicate.state = original.state;

        return duplicate;
    }

    private BInvokableSymbol duplicateSymbol(BInvokableSymbol original) {
        BInvokableSymbol duplicate = Symbols.createInvokableSymbol(original.tag, original.flags, original.name,
                original.originalName, original.pkgID, original.type,
                original.owner, original.pos, original.origin);
        ((List<AnnotationAttachmentSymbol>) duplicate.getAnnotations()).addAll(original.getAnnotations());
        duplicate.bodyExist = original.bodyExist;
        duplicate.markdownDocumentation = original.markdownDocumentation;
        duplicate.receiverSymbol = original.receiverSymbol;

        return duplicate;
    }
}
