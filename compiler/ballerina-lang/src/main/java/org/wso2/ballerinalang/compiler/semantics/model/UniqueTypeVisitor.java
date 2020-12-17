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

import org.wso2.ballerinalang.compiler.semantics.model.types.*;

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
