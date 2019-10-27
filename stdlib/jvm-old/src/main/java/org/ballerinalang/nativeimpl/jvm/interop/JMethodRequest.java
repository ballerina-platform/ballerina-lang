/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.interop;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.ARRAY_ELEMENT_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.ARRAY_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.B_FUNC_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.KIND_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.OBJECT_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPES_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPE_CONSTRAINTS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.RECORD_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.REST_PARAM_EXIST_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.RETURN_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.TUPLE_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.TUPLE_TYPE_MEMBERS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.TYPE_NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.UNION_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.UNION_TYPE_MEMBERS_FIELD;

/**
 * {@code JMethodRequest} represents Java method request bean issued by the Java interop logic written in Ballerina.
 *
 * @since 1.0.0
 */
class JMethodRequest {
    Class<?> declaringClass;
    String methodName;
    JMethodKind kind;
    ParamTypeConstraint[] paramTypeConstraints = {};
    // Parameter count of the Ballerina function
    int bFuncParamCount;

    BType[] bParamTypes = null;
    BType bReturnType = null;
    boolean returnsBErrorType = false;
    boolean restParamExist = false;

    private JMethodRequest() {
    }

    static JMethodRequest build(MapValue jMethodReqBValue, ClassLoader classLoader) {
        JMethodRequest jMethodReq = new JMethodRequest();
        jMethodReq.kind = JMethodKind.getKind((String) jMethodReqBValue.get(KIND_FIELD));
        jMethodReq.methodName = (String) jMethodReqBValue.get(NAME_FIELD);
        jMethodReq.declaringClass = JInterop.loadClass((String) jMethodReqBValue.get(CLASS_FIELD), classLoader);
        jMethodReq.paramTypeConstraints = JInterop.buildParamTypeConstraints(
                (ArrayValue) jMethodReqBValue.get(PARAM_TYPE_CONSTRAINTS_FIELD), classLoader);

        MapValue bFuncType = (MapValue) jMethodReqBValue.get(B_FUNC_TYPE_FIELD);
        ArrayValue paramTypes = (ArrayValue) bFuncType.get(PARAM_TYPES_FIELD);
        jMethodReq.bFuncParamCount = paramTypes.size();
        jMethodReq.bParamTypes = getBParamTypes(paramTypes);
        BType returnType = getBType(bFuncType.get(RETURN_TYPE_FIELD));
        jMethodReq.bReturnType = returnType;
        jMethodReq.returnsBErrorType = returnType.toString().contains(TypeConstants.ERROR);
        jMethodReq.restParamExist = jMethodReqBValue.getBooleanValue(REST_PARAM_EXIST_FIELD);
        return jMethodReq;
    }

    private static BType[] getBParamTypes(ArrayValue paramTypes) {
        BType[] bParamTypes = new BType[paramTypes.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            bParamTypes[i] = getBType(paramTypes.get(i));
        }
        return bParamTypes;
    }


    private static BType getBType(Object bTypeValue) {
        BType bType = TypeChecker.getType(bTypeValue);
        if (bType.getTag() == TypeTags.STRING_TAG) {
            String typeName = (String) bTypeValue;
            switch (typeName) {
                case TypeConstants.INT_TNAME:
                    return BTypes.typeInt;
                case TypeConstants.STRING_TNAME:
                    return BTypes.typeString;
                case TypeConstants.BYTE_TNAME:
                    return BTypes.typeByte;
                case TypeConstants.BOOLEAN_TNAME:
                    return BTypes.typeBoolean;
                case TypeConstants.FLOAT_TNAME:
                    return BTypes.typeFloat;
                case TypeConstants.DECIMAL_TNAME:
                    return BTypes.typeDecimal;
                case TypeConstants.ANY_TNAME:
                    return BTypes.typeAny;
                case TypeConstants.ANYDATA_TNAME:
                    return BTypes.typeAnydata;
                case TypeConstants.NULL_TNAME:
                    return BTypes.typeNull;
                case TypeConstants.JSON_TNAME:
                    return BTypes.typeJSON;
                case TypeConstants.XML_TNAME:
                    return BTypes.typeXML;
                default:
                    throw new UnsupportedOperationException("JInterop does not support type '" + bType + "'");
            }
        }

        if (bType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            BRecordType bRecordType = ((BRecordType) bType);
            MapValue arrayTypeValue = ((MapValue) bTypeValue);
            String typeName = arrayTypeValue.getStringValue(TYPE_NAME_FIELD);
            switch (typeName) {
                case TypeConstants.HANDLE_TNAME:
                    return BTypes.typeHandle;
                case TypeConstants.SERVICE:
                    return BTypes.typeAnyService;
                case TypeConstants.TYPEDESC_TNAME:
                    return BTypes.typeTypedesc;
                case TypeConstants.MAP_TNAME:
                    return BTypes.typeMap;
                case TypeConstants.TABLE_TNAME:
                    return BTypes.typeTable;
                case TypeConstants.STREAM_TNAME:
                    return BTypes.typeStream;
                case TypeConstants.ERROR:
                    return BTypes.typeError;
                case TypeConstants.FUTURE_TNAME:
                    return BTypes.typeFuture;
                case UNION_TNAME:
                    ArrayValue members = ((MapValue) bTypeValue).getArrayValue(UNION_TYPE_MEMBERS_FIELD);
                    List<BType> memberTypes = new ArrayList<>();
                    for (int i = 0; i < members.size(); i++) {
                        memberTypes.add(getBType(members.get(i)));
                    }
                    return new BUnionType(memberTypes);
                case OBJECT_TNAME:
                    return new BObjectType(bRecordType.getName(), bRecordType.getPackage(), bRecordType.flags);
                case TUPLE_TNAME:
                    members = ((MapValue) bTypeValue).getArrayValue(TUPLE_TYPE_MEMBERS_FIELD);
                    memberTypes = new ArrayList<>();
                    for (int i = 0; i < members.size(); i++) {
                        memberTypes.add(getBType(members.get(i)));
                    }
                    return new BTupleType(memberTypes);
                case RECORD_TNAME:
                    return new BRecordType(bRecordType.getName(), bRecordType.getPackage(), bRecordType.flags,
                            bRecordType.sealed);
                case ARRAY_TNAME:
                    return new BArrayType(getBType(arrayTypeValue.get(ARRAY_ELEMENT_TYPE_FIELD)));
                default:
                    throw new UnsupportedOperationException("JInterop does not support type '" + bType + "'");
            }
        }

        throw new UnsupportedOperationException("JInterop does not support type '" + bType + "'");
    }
}
