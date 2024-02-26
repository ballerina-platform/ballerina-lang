/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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

public abstract class BTypeAnalyzer<T> {

    public abstract void visit(BAnnotationType bAnnotationType, T data);

    public abstract void visit(BArrayType bArrayType, T data);

    public abstract void visit(BBuiltInRefType bBuiltInRefType, T data);

    public abstract void visit(BAnyType bAnyType, T data);

    public abstract void visit(BAnydataType bAnydataType, T data);

    public abstract void visit(BErrorType bErrorType, T data);

    public abstract void visit(BFiniteType bFiniteType, T data);

    public abstract void visit(BInvokableType bInvokableType, T data);

    public abstract void visit(BJSONType bjsonType, T data);

    public abstract void visit(BMapType bMapType, T data);

    public abstract void visit(BStreamType bStreamType, T data);

    public abstract void visit(BTypedescType bTypedescType, T data);

    public abstract void visit(BTypeReferenceType bTypeReferenceType, T data);

    public abstract void visit(BParameterizedType bTypedescType, T data);

    public abstract void visit(BNeverType bNeverType, T data);

    public abstract void visit(BNilType bNilType, T data);

    public abstract void visit(BNoType bNoType, T data);

    public abstract void visit(BPackageType bPackageType, T data);

    public abstract void visit(BStructureType bStructureType, T data);

    public abstract void visit(BTupleType bTupleType, T data);

    public abstract void visit(BUnionType bUnionType, T data);

    public abstract void visit(BIntersectionType bIntersectionType, T data);

    public abstract void visit(BXMLType bxmlType, T data);

    public abstract void visit(BTableType bTableType, T data);

    public abstract void visit(BRecordType bRecordType, T data);

    public abstract void visit(BObjectType bObjectType, T data);

    public abstract void visit(BType bType, T data);

    public abstract void visit(BFutureType bFutureType, T data);

    public abstract void visit(BHandleType bHandleType, T data);

}
