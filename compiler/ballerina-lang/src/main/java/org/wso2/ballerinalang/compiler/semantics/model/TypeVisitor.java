/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

/**
 * Visit ballerina types and maps them to instances T.
 *
 * @since 0.995.0
 */
public interface TypeVisitor {

    void visit(BAnnotationType bAnnotationType);

    void visit(BArrayType bArrayType);

    void visit(BBuiltInRefType bBuiltInRefType);

    void visit(BAnyType bAnyType);

    void visit(BAnydataType bAnydataType);

    void visit(BErrorType bErrorType);

    void visit(BFiniteType bFiniteType);

    void visit(BInvokableType bInvokableType);

    void visit(BJSONType bjsonType);

    void visit(BMapType bMapType);

    void visit(BStreamType bStreamType);

    void visit(BTypedescType bTypedescType);

    void visit(BNeverType bNeverType);

    void visit(BNilType bNilType);

    void visit(BNoType bNoType);

    void visit(BPackageType bPackageType);

    void visit(BServiceType bServiceType);

    void visit(BStructureType bStructureType);

    void visit(BTupleType bTupleType);

    void visit(BUnionType bUnionType);

    void visit(BIntersectionType bIntersectionType);

    void visit(BXMLType bxmlType);

    void visit(BTableType bTableType);

    void visit(BRecordType bRecordType);

    void visit(BObjectType bObjectType);

    void visit(BType bType);

    void visit(BFutureType bFutureType);

    void visit(BHandleType bHandleType);

}
