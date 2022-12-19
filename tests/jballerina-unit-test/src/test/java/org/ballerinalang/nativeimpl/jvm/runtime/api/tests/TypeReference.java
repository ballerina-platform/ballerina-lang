/*
 *  Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BParameterizedType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.TableValue;

import java.util.List;

/**
 * Utility methods used for runtime api @{@link io.ballerina.runtime.internal.types.BTypeReferenceType} testing.
 *
 * @since 2201.4.0
 */
public class TypeReference {

    public static Boolean validateGetDetailType(BTypedesc typedesc) {
        BErrorType errorType = (BErrorType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        Type detailType = errorType.getDetailType();
        if (detailType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            return true;
        }
        throw ErrorCreator.createError(StringUtils.fromString("error detail type provided a non type reference type."));
    }

    public static Boolean validateFunctionType(BTypedesc typedesc) {
        BFunctionType functionType = (BFunctionType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(
                StringUtils.fromString("function type API provided a non type reference type."));

        for (Type type : functionType.getParameterTypes()) {
            if (type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                throw error;
            }
        }

        for (Parameter parameter : functionType.getParameters()) {
            if (parameter.type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                throw error;
            }
        }
        Type restType = functionType.getRestType();

        if (functionType.getReturnType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                functionType.getReturnParameterType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                (((BArrayType) restType).getElementType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG)) {
            throw error;
        }
        return true;
    }

    public static Boolean validateIntersectionType(BTypedesc typedesc) {
        BIntersectionType
                intersectionType = (BIntersectionType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(
                StringUtils.fromString("intersection type API provided a non type reference type."));
        List<Type> constituentTypes = intersectionType.getConstituentTypes();

        if (intersectionType.getEffectiveType().getTag() != TypeTags.RECORD_TYPE_TAG ||
                (constituentTypes.get(0).getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG)) {
            throw error;
        }
        return true;
    }

    public static Boolean validateMapType(BTypedesc typedesc) {
        BMapType mapType = (BMapType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();

        if (mapType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("map type API provided a non type reference " +
                    "type."));
        }
        return true;
    }

    public static Boolean validateRecordType(BTypedesc typedesc) {
        BRecordType recordType =
                (BRecordType) (TypeUtils.getReferredType(typedesc.getDescribingType()));
        if (recordType.getRestFieldType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("record type API provided a non type reference" +
                    " type."));
        }
        return true;
    }

    public static Boolean validateStreamType(BTypedesc value1, BStream value2) {
        BStreamType streamType = (BStreamType) ((ReferenceType) value1.getDescribingType()).getReferredType();
        if (streamType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                streamType.getCompletionType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                value2.getConstraintType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                value2.getCompletionType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("stream API provided a non type reference" +
                    " type."));
        }
        return true;
    }

    public static Boolean validateTableType(BTypedesc typedesc, TableValue tableValue) {
        BTableType tableType = (BTableType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        if (tableType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                tableValue.getKeyType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                tableValue.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("table type API provided a non type reference" +
                    " type."));
        }
        return true;
    }

    public static Boolean validateTupleType(BTypedesc typedesc) {
        BTupleType tupleType = (BTupleType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(StringUtils.fromString("table type API provided a non type reference" +
                " type."));
        for (Type type : tupleType.getTupleTypes()) {
            if (type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                throw error;
            }
        }

        if (tupleType.getRestType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }

        return true;
    }

    public static Boolean validateTypedescType(BTypedesc typedesc) {
        BTypedescType typedescType = (BTypedescType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        if (typedescType.getConstraint().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("typdesc type API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static Boolean validateUnionType(BTypedesc typedesc) {
        BUnionType unionType = (BUnionType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(StringUtils.fromString("union type API provided a non type " +
                "reference type."));

        for (Type type : unionType.getMemberTypes()) {
            if (type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                throw error;
            }
        }

        for (Type type : unionType.getOriginalMemberTypes()) {
            if (type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                throw error;
            }
        }

        return true;
    }

    public static Boolean validateParameterizedType(ObjectValue objectValue) {
        BError error = ErrorCreator.createError(StringUtils.fromString("parameterized type API provided a non type " +
                "reference type."));
        ObjectType objectType = (ObjectType) objectValue.getType();
        BFunctionType functionType = (BFunctionType) objectType.getMethods()[0].getType();
        BParameterizedType parameterizedType =
                (BParameterizedType) ((UnionType) functionType.getReturnType()).getMemberTypes().get(0);
        if (parameterizedType.getParamValueType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        parameterizedType =
                (BParameterizedType) ((UnionType) functionType.getReturnType()).getOriginalMemberTypes().get(0);
        if (parameterizedType.getParamValueType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateTypeUtilsAPI(BTypedesc typedesc) {
        Type type = typedesc.getDescribingType();
        if (!TypeUtils.isValueType(type)) {
            throw ErrorCreator.createError(StringUtils.fromString("TypeUtils API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static Object getInt(ObjectValue objectValue, BTypedesc td) {
        return true;
    }

    public static Boolean validateBArray(BArray value1, BArray value2) {
        if (value1.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                value2.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                value1.getElementType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("BArray getType API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static Boolean validateBMap(BMap value1, BMap value2) {
        if (value1.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                value2.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("BMap getType API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static Boolean validateBError(BError value) {
        if (value.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("BError getType API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static Boolean validateBFunctionPointer(BFunctionPointer value) {
        if (value.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw  ErrorCreator.createError(StringUtils.fromString("Function Pointer getType API provided a non " +
                    "type reference type."));
        }
        return true;
    }

    public static Boolean validateBObject(BObject value) {
        if (value.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("BObject getType API provided a non type " +
                    "reference type."));
        }
        return true;
    }

    public static boolean validateUnionTypeNarrowing(Object value, BTypedesc typedesc) {
        Type describingType = typedesc.getDescribingType();
        Type type = TypeChecker.getType(value);
        if (type.getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG ||
                type.getTag() != describingType.getTag() || !type.toString().equals(describingType.toString())) {
            throw ErrorCreator.createError(StringUtils.fromString("RefValue getType API provided a wrong type " +
                    "reference type."));
        }
        return true;
    }

    public static boolean validateTableKeys(BTable table) {
        if (table.getType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG || table.size() != 1 ||
                table.getKeyType().getTag() != TypeTags.TUPLE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("Table keys does not provide type-reference " +
                    "type."));
        }
        return true;
    }

}
