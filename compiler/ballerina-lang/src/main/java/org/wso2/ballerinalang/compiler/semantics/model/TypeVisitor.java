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
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Visit ballerina types and maps them to instances T.
 *
 * @since 0.995.0
 */
public abstract class TypeVisitor {

    public abstract void visit(BAnnotationType bAnnotationType);

    public abstract void visit(BArrayType bArrayType);

    public abstract void visit(BAnyType bAnyType);

    public abstract void visit(BAnydataType bAnydataType);

    public abstract void visit(BErrorType bErrorType);

    public abstract void visit(BFiniteType bFiniteType);

    public abstract void visit(BInvokableType bInvokableType);

    public abstract void visit(BJSONType bjsonType);

    public abstract void visit(BMapType bMapType);

    public abstract void visit(BStreamType bStreamType);

    public abstract void visit(BTypedescType bTypedescType);

    public abstract void visit(BTypeReferenceType bTypeReferenceType);

    public abstract void visit(BParameterizedType bTypedescType);

    public abstract void visit(BNeverType bNeverType);

    public abstract void visitNilType(BType bType);

    public abstract void visit(BNoType bNoType);

    public abstract void visit(BPackageType bPackageType);

    public abstract void visit(BStructureType bStructureType);

    public abstract void visit(BTupleType bTupleType);

    public abstract void visit(BUnionType bUnionType);

    public abstract void visit(BIntersectionType bIntersectionType);

    public abstract void visit(BXMLType bxmlType);

    public abstract void visit(BTableType bTableType);

    public abstract void visit(BRecordType bRecordType);

    public abstract void visit(BObjectType bObjectType);

    public void visit(BType type) {
        if (type == null) { // TODO: see if we can remove
            return;
        }

        switch (type.tag) {
            case TypeTags.NIL:
                visitNilType(type);
        }
    }

    public abstract void visit(BFutureType bFutureType);

    public abstract void visit(BHandleType bHandleType);
}
