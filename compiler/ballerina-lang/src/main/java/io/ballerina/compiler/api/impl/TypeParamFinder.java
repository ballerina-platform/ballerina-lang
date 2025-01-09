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

import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashSet;
import java.util.Set;

/**
 * A util class for finding the type param component in a given type.
 *
 * @since 2.0.0
 */
public class TypeParamFinder implements TypeVisitor {

    private final Set<BType> visited = new HashSet<>();
    private BType typeParam;

    /**
     * Given a type, this method will lookup and return the first type param it encounters within the given type.
     *
     * @param type The type to look for the type param in
     * @return Returns the type param if there is one, else returns null
     */
    public BType find(BType type) {
        if (type == null || this.visited.contains(type)) {
            return null;
        }

        setContainsTypeParam(type);

        if (this.typeParam != null) {
            return this.typeParam;
        }

        this.visited.add(type);
        type.accept(this);

        return this.typeParam;
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
    }

    @Override
    public void visit(BArrayType bArrayType) {
        find(bArrayType.eType);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
    }

    @Override
    public void visit(BAnyType bAnyType) {
    }

    @Override
    public void visit(BAnydataType bAnydataType) {
    }

    @Override
    public void visit(BErrorType bErrorType) {
        find(bErrorType.detailType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        for (BType paramType : bInvokableType.paramTypes) {
            find(paramType);
        }

        find(bInvokableType.restType);
        find(bInvokableType.retType);
    }

    @Override
    public void visit(BJSONType bjsonType) {
    }

    @Override
    public void visit(BMapType bMapType) {
        find(bMapType.constraint);
    }

    @Override
    public void visit(BStreamType bStreamType) {
        find(bStreamType.constraint);
//        find(bStreamType.completionType); TODO: Ignoring completion type for now
    }

    @Override
    public void visit(BTypedescType bTypedescType) {
        find(bTypedescType.constraint);
    }

    @Override
    public void visit(BTypeReferenceType bTypeReferenceType) {
        find(bTypeReferenceType.referredType);
    }

    @Override
    public void visit(BParameterizedType bTypedescType) {
    }

    @Override
    public void visit(BNeverType bNeverType) {
    }

    @Override
    public void visit(BNilType bNilType) {
    }

    @Override
    public void visit(BNoType bNoType) {
    }

    @Override
    public void visit(BPackageType bPackageType) {
    }

    @Override
    public void visit(BStructureType bStructureType) {
    }

    @Override
    public void visit(BTupleType bTupleType) {
        for (BType tupleMemberType : bTupleType.getTupleTypes()) {
            find(tupleMemberType);
        }

        find(bTupleType.restType);
    }

    @Override
    public void visit(BUnionType bUnionType) {
        for (BType memberType : bUnionType.getOriginalMemberTypes()) {
            find(memberType);
        }
    }

    @Override
    public void visit(BIntersectionType bIntersectionType) {
        for (BType constituentType : bIntersectionType.getConstituentTypes()) {
            find(constituentType);
        }
    }

    @Override
    public void visit(BXMLType bxmlType) {
        find(bxmlType.constraint);
    }

    @Override
    public void visit(BTableType bTableType) {
//        find(bTableType.keyTypeConstraint); TODO: ignoring key constraint type for now
        find(bTableType.constraint);
    }

    @Override
    public void visit(BRecordType bRecordType) {
        for (BField field : bRecordType.fields.values()) {
            find(field.getType());
        }

        if (bRecordType.restFieldType != null) {
            find(bRecordType.restFieldType);
        }
    }

    @Override
    public void visit(BObjectType bObjectType) {
        for (BField field : bObjectType.fields.values()) {
            find(field.getType());
        }

        for (BAttachedFunction method : ((BObjectTypeSymbol) bObjectType.tsymbol).attachedFuncs) {
            find(method.type);
        }
    }

    @Override
    public void visit(BType bType) {
    }

    @Override
    public void visit(BFutureType bFutureType) {
        find(bFutureType.constraint);
    }

    @Override
    public void visit(BHandleType bHandleType) {
    }

    private void setContainsTypeParam(BType type) {
        if (Symbols.isFlagOn(type.flags, Flags.TYPE_PARAM)) {
            this.typeParam = type;
        }
    }
}
