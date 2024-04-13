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

public abstract class SimpleBTypeAnalyzer<T> extends BTypeAnalyzer<T> {

    public abstract void analyzeType(BType bType, T data);

    public void visitType(BType bType, T data) {
        if (bType == null) {
            return;
        }
        bType.accept(this, data);
    }

    @Override
    public void visit(BAnnotationType bAnnotationType, T data) {
        analyzeType(bAnnotationType, data);
    }

    @Override
    public void visit(BArrayType bArrayType, T data) {
        analyzeType(bArrayType, data);
        visitType(bArrayType.eType, data);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType, T data) {
        analyzeType(bBuiltInRefType, data);
    }

    @Override
    public void visit(BAnyType bAnyType, T data) {
        analyzeType(bAnyType, data);
    }

    @Override
    public void visit(BAnydataType bAnydataType, T data) {
        analyzeType(bAnydataType, data);
    }

    @Override
    public void visit(BErrorType bErrorType, T data) {
        analyzeType(bErrorType, data);
        visitType(bErrorType.detailType, data);
    }

    @Override
    public void visit(BFiniteType bFiniteType, T data) {
        analyzeType(bFiniteType, data);
    }

    @Override
    public void visit(BInvokableType bInvokableType, T data) {
        analyzeType(bInvokableType, data);
        if (bInvokableType.paramTypes != null) {
            bInvokableType.paramTypes.forEach(param -> visitType(param, data));
        }
        visitType(bInvokableType.retType, data);
    }

    @Override
    public void visit(BJSONType bjsonType, T data) {
        analyzeType(bjsonType, data);
    }

    @Override
    public void visit(BMapType bMapType, T data) {
        analyzeType(bMapType, data);
        visitType(bMapType.mutableType, data);
        visitType(bMapType.constraint, data);
    }

    @Override
    public void visit(BStreamType bStreamType, T data) {
        analyzeType(bStreamType, data);
        visitType(bStreamType.constraint, data);
        visitType(bStreamType.completionType, data);
    }

    @Override
    public void visit(BTypedescType bTypedescType, T data) {
        analyzeType(bTypedescType, data);
        visitType(bTypedescType.constraint, data);
    }

    @Override
    public void visit(BTypeReferenceType bTypeReferenceType, T data) {
        analyzeType(bTypeReferenceType, data);
        visitType(bTypeReferenceType.referredType, data);
    }

    @Override
    public void visit(BParameterizedType bTypedescType, T data) {
        analyzeType(bTypedescType, data);
    }

    @Override
    public void visit(BNeverType bNeverType, T data) {
        analyzeType(bNeverType, data);
    }

    @Override
    public void visit(BNilType bNilType, T data) {
        analyzeType(bNilType, data);
    }

    @Override
    public void visit(BNoType bNoType, T data) {
        analyzeType(bNoType, data);
    }

    @Override
    public void visit(BPackageType bPackageType, T data) {
        analyzeType(bPackageType, data);
    }

    @Override
    public void visit(BStructureType bStructureType, T data) {
        analyzeType(bStructureType, data);
        bStructureType.fields.values().forEach(field -> visitType(field.type, data));
    }

    @Override
    public void visit(BTupleType bTupleType, T data) {
        analyzeType(bTupleType, data);
        bTupleType.getMembers().forEach(member -> visitType(member.type, data));
    }

    @Override
    public void visit(BUnionType bUnionType, T data) {
        analyzeType(bUnionType, data);
        bUnionType.getOriginalMemberTypes().forEach(member -> visitType(member, data));
    }

    @Override
    public void visit(BIntersectionType bIntersectionType, T data) {
        analyzeType(bIntersectionType, data);
        visitType(bIntersectionType.effectiveType, data);
        bIntersectionType.getConstituentTypes().forEach(member -> visitType(member, data));
    }

    @Override
    public void visit(BXMLType bxmlType, T data) {
        analyzeType(bxmlType, data);
    }

    @Override
    public void visit(BTableType bTableType, T data) {
        analyzeType(bTableType, data);
        visitType(bTableType.constraint, data);
    }

    @Override
    public void visit(BRecordType bRecordType, T data) {
        analyzeType(bRecordType, data);
        visitType(bRecordType.mutableType, data);
        visitType(bRecordType.restFieldType, data);
        bRecordType.typeInclusions.forEach(inclusionType -> visitType(inclusionType, data));
        bRecordType.fields.values().forEach(field -> visitType(field.type, data));
    }

    @Override
    public void visit(BObjectType bObjectType, T data) {
        analyzeType(bObjectType, data);
        visitType(bObjectType.mutableType, data);
        bObjectType.typeInclusions.forEach(inclusionType -> visitType(inclusionType, data));
        bObjectType.fields.values().forEach(field -> visitType(field.type, data));
    }

    @Override
    public void visit(BType bType, T data) {
        analyzeType(bType, data);
    }

    @Override
    public void visit(BFutureType bFutureType, T data) {
        analyzeType(bFutureType, data);
    }

    @Override
    public void visit(BHandleType bHandleType, T data) {
        analyzeType(bHandleType, data);
    }
}
