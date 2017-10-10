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
 * @param <R> the return type of the visit methods.
 * @since 0.94
 */
public interface BTypeVisitor<R> {
    R visit(BType t, BType s);

    R visit(BBuiltInRefType t, BType s);

    R visit(BAnyType t, BType s);

    R visit(BMapType t, BType s);

    R visit(BJSONType t, BType s);

    R visit(BArrayType t, BType s);

    R visit(BStructType t, BType s);

    R visit(BConnectorType t, BType s);

    R visit(BEnumType t, BType s);

    R visit(BErrorType t, BType s);
}
