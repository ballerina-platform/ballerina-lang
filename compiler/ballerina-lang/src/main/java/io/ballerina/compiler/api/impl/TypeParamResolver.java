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
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeParamResolver implements BTypeVisitor<BType, BType> {

    private final Map<BType, BType> boundTypes = new HashMap<>();
    private BType typeParam;

    public TypeParamResolver(BType typeParam) {
        this.typeParam = typeParam;
    }

    public BType resolve(BType typeParam, BType boundType) {
        if (boundTypes.containsKey(typeParam)) {
            return boundTypes.get(typeParam);
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
        if (isTypeParam(typeInSymbol)) {
            this.boundTypes.put(typeInSymbol, boundType);
            return boundType;
        }
        return typeInSymbol;
    }

    @Override
    public BType visit(BAnydataType typeInSymbol, BType boundType) {
        if (isTypeParam(typeInSymbol)) {
            this.boundTypes.put(typeInSymbol, boundType);
            return boundType;
        }
        return typeInSymbol;
    }

    @Override
    public BType visit(BMapType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BXMLType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BJSONType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BArrayType typeInSymbol, BType boundType) {
        if (isTypeParam(typeInSymbol)) {
            return boundType;
        }

        BType boundElemType = resolve(typeInSymbol.eType, boundType);
        return new BArrayType(boundElemType, typeInSymbol.tsymbol, typeInSymbol.size, typeInSymbol.state,
                              typeInSymbol.flags);
    }

    @Override
    public BType visit(BObjectType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BRecordType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BTupleType typeInSymbol, BType boundType) {
        if (isTypeParam(typeInSymbol)) {
            return boundType;
        }

        List<BType> newTupleTypes = new ArrayList<>();

        List<BType> tupleTypes = typeInSymbol.tupleTypes;
        for (int i = 0; i < tupleTypes.size(); i++) {
            BType type = tupleTypes.get(i);
            BType newType = resolve(type, boundType);
            newTupleTypes.add(newType);
        }

        BType newRestType = typeInSymbol.restType != null ? resolve(typeInSymbol.restType, boundType) : null;
        return new BTupleType(typeInSymbol.tsymbol, newTupleTypes, newRestType, typeInSymbol.flags,
                              typeInSymbol.isCyclic);
    }

    @Override
    public BType visit(BStreamType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BTableType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BInvokableType typeInSymbol, BType boundType) {
        return typeInSymbol;
    }

    @Override
    public BType visit(BUnionType typeInSymbol, BType boundType) {
        if (isTypeParam(typeInSymbol)) {
            return boundType;
        }

        return typeInSymbol;
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

    private boolean isTypeParam(BType type) {
        return type == this.typeParam;
    }
}
