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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.SemType.BasicTypeCode;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.Core;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.BSubType;

import java.util.ArrayList;
import java.util.List;

// NOTE: this is so that we don't have to expose any utility constructors as public to builder
public final class BTypeConverter {

    private BTypeConverter() {
    }

    private static SemType from(Type type) {
        if (type instanceof SemType semType) {
            return semType;
        } else if (type instanceof BType bType) {
            return from(bType);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public static SemType from(BType innerType) {
        if (innerType instanceof BNeverType) {
            return Builder.neverType();
        } else if (innerType instanceof BUnionType unionType) {
            return fromUnionType(unionType);
        } else if (innerType instanceof BRecordType recordType) {
            return fromRecordType(recordType);
        } else if (innerType instanceof BTupleType tupleType) {
            return fromTupleType(tupleType);
        }
        return wrapAsPureBType(innerType);
    }

    private static SemType fromTupleType(BTupleType tupleType) {
        for (Type type : tupleType.getTupleTypes()) {
            if (Core.isNever(from(type))) {
                return Builder.neverType();
            }
        }
        return wrapAsPureBType(tupleType);
    }

    private static SemType wrapAsPureBType(BType tupleType) {
        return Builder.basicSubType(BasicTypeCode.BT_B_TYPE, BSubType.wrap(tupleType));
    }

    private static SemType fromRecordType(BRecordType recordType) {
        for (Field field : recordType.fields.values()) {
            if (!SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL)) {
                SemType fieldType = from(field.getFieldType());
                if (Core.isNever(fieldType)) {
                    return Builder.neverType();
                }
            }
        }
        return wrapAsPureBType(recordType);
    }

    private static SemType fromUnionType(BUnionType unionType) {
        List<Type> members = unionType.getMemberTypes();
        List<Type> bTypeMembers = new ArrayList<>(members.size());
        SemType semTypePart = Builder.neverType();
        for (Type member : members) {
            if (isSemType(member)) {
                semTypePart = Core.union(from(member), semTypePart);
            } else {
                bTypeMembers.add(member);
            }
        }
        BUnionType newUnionType = unionType.cloneWithMembers(bTypeMembers);
        SemType bTypePart = Builder.basicSubType(BasicTypeCode.BT_B_TYPE, BSubType.wrap(newUnionType));
        return Core.union(semTypePart, bTypePart);
    }

    private static boolean isSemType(Type type) {
        return type instanceof BNeverType;
    }
}
