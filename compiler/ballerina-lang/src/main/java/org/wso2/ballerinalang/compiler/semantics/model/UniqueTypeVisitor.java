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
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

/**
 * Visit ballerina types and maps them to instances T.
 *
 * @param <R> return type of visit methods
 * @since Swan Lake
 */
public interface UniqueTypeVisitor<R> {

    default R visit(UniqueTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    boolean isVisited(BType type);

    void reset();

    R visit(BAnnotationType bAnnotationType);

    R visit(BArrayType bArrayType);

    R visit(BBuiltInRefType bBuiltInRefType);

    R visit(BAnyType bAnyType);

    R visit(BAnydataType bAnydataType);

    R visit(BErrorType bErrorType);

    R visit(BFiniteType bFiniteType);

    R visit(BInvokableType bInvokableType);

    R visit(BJSONType bjsonType);

    R visit(BMapType bMapType);

    R visit(BStreamType bStreamType);

    R visit(BTypedescType bTypedescType);

    R visit(BParameterizedType bTypedescType);

    R visit(BNeverType bNeverType);

    R visit(BNilType bNilType);

    R visit(BNoType bNoType);

    R visit(BPackageType bPackageType);

    R visit(BStructureType bStructureType);

    R visit(BTupleType bTupleType);

    R visit(BUnionType bUnionType);

    R visit(BIntersectionType bIntersectionType);

    R visit(BTypeReferenceType bTypeReferenceType);

    R visit(BXMLType bxmlType);

    R visit(BTableType bTableType);

    R visit(BRecordType bRecordType);

    R visit(BObjectType bObjectType);

    R visit(BType bType);

    R visit(BFutureType bFutureType);

    R visit(BHandleType bHandleType);

    R visit(BIntSubType intSubType);

    R visit(BXMLSubType bxmlSubType);
}
