/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

/**
 * Visit ballerina types and maps them to instances T.
 *
 * @param <R> return type of visit methods
 * @since Swan Lake
 */
public abstract class UniqueTypeVisitor<R> {

    R visit(UniqueTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public abstract boolean isVisited(BType type);

    public abstract void reset();

    public abstract R visit(BAnnotationType bAnnotationType);

    public abstract R visit(BArrayType bArrayType);

    public abstract R visit(BReadonlyType bReadonlyType);

    public abstract R visit(BAnyType bAnyType);

    public abstract R visit(BAnydataType bAnydataType);

    public abstract R visit(BErrorType bErrorType);

    public abstract R visit(BFiniteType bFiniteType);

    public abstract R visit(BInvokableType bInvokableType);

    public abstract R visit(BJSONType bjsonType);

    public abstract R visit(BMapType bMapType);

    public abstract R visit(BStreamType bStreamType);

    public abstract R visit(BTypedescType bTypedescType);

    public abstract R visit(BParameterizedType bTypedescType);

    public abstract R visit(BNeverType bNeverType);

    public abstract R visitNilType(BType bType);

    public abstract R visit(BNoType bNoType);

    public abstract R visit(BPackageType bPackageType);

    public abstract R visit(BStructureType bStructureType);

    public abstract R visit(BTupleType bTupleType);

    public abstract R visit(BUnionType bUnionType);

    public abstract R visit(BIntersectionType bIntersectionType);

    public abstract R visit(BTypeReferenceType bTypeReferenceType);

    public abstract R visit(BXMLType bxmlType);

    public abstract R visit(BTableType bTableType);

    public abstract R visit(BRecordType bRecordType);

    public abstract R visit(BObjectType bObjectType);

    public abstract R visit(BType bType);

    public abstract R visit(BFutureType bFutureType);

    public abstract R visit(BHandleType bHandleType);

    public abstract R visit(BIntSubType intSubType);

    public abstract R visit(BXMLSubType bxmlSubType);
}
