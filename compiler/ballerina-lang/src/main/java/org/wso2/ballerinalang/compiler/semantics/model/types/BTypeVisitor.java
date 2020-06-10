/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

/**
 * @param <T> the argument type of the visit methods.
 * @param <R> the return type of the visit methods.
 * @since 0.94
 */
public interface BTypeVisitor<T, R> {
    R visit(BType t, T s);

    R visit(BBuiltInRefType t, T s);

    R visit(BAnyType t, T s);

    R visit(BAnydataType t, T s);

    R visit(BMapType t, T s);

    R visit(BXMLType t, T s);

    R visit(BJSONType t, T s);

    R visit(BArrayType t, T s);

    R visit(BObjectType t, T s);

    R visit(BRecordType t, T s);

    R visit(BTupleType t, T s);

    R visit(BStreamType t, T s);

    R visit(BTableType t, T s);

    R visit(BInvokableType t, T s);

    R visit(BUnionType t, T s);

    R visit(BIntersectionType t, T s);

    R visit(BErrorType t, T s);
    
    R visit(BFutureType t, T s);

    R visit(BFiniteType t, T s);

    R visit(BServiceType t, T s);

    R visit(BTypedescType t, T s);

}
