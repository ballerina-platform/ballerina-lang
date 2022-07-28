/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;
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

import java.util.List;

/**
 * Utility methods used for runtime api @{@link io.ballerina.runtime.internal.types.BTypeReferenceType} testing.
 *
 * @since 2.3.0
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

        if (functionType.getReturnType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }

        if (functionType.getReturnParameterType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }

        Type restType = functionType.getRestType();
        if (((BArrayType) restType).getElementType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateIntersectionType(BTypedesc typedesc) {
        BIntersectionType
                intersectionType = (BIntersectionType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(
                StringUtils.fromString("intersection type API provided a non type reference type."));

        if (intersectionType.getEffectiveType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw error;
        }

        List<Type> constituentTypes = intersectionType.getConstituentTypes();
        if (constituentTypes.get(0).getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateMapType(BTypedesc typedesc) {
        BMapType mapType = (BMapType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(StringUtils.fromString("map type API provided a non type reference " +
                "type."));

        if (mapType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateRecordType(BTypedesc typedesc) {
        BRecordType recordType =
                (BRecordType) (TypeUtils.getReferredType(typedesc.getDescribingType()));
        BError error = ErrorCreator.createError(StringUtils.fromString("record type API provided a non type reference" +
                " type."));
        if (recordType.getRestFieldType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateStreamType(BTypedesc typedesc) {
        BStreamType streamType = (BStreamType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(StringUtils.fromString("stream type API provided a non type reference" +
                " type."));
        if (streamType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }

        if (streamType.getCompletionType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
        }
        return true;
    }

    public static Boolean validateTableType(BTypedesc typedesc) {
        BTableType tableType = (BTableType) ((ReferenceType) typedesc.getDescribingType()).getReferredType();
        BError error = ErrorCreator.createError(StringUtils.fromString("table type API provided a non type reference" +
                " type."));
        if (tableType.getConstrainedType().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
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
        BError error = ErrorCreator.createError(StringUtils.fromString("typdesc type API provided a non type " +
                "reference type."));
        if (typedescType.getConstraint().getTag() != TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            throw error;
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
        ObjectType objectType = objectValue.getType();
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
        BError error = ErrorCreator.createError(StringUtils.fromString("TypeUtils API provided a non type " +
                "reference type."));
        if (!TypeUtils.isValueType(type)) {
            throw error;
        }
        return true;
    }

    public static Object getInt(ObjectValue objectValue, BTypedesc td) {
        return true;
    }
}
