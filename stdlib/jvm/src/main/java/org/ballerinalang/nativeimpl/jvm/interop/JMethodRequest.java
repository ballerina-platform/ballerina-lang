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
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.B_ERROR_TYPE_NAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.B_FUNC_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.KIND_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPES_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPE_CONSTRAINTS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.RETURN_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.TYPE_NAME_FIELD;

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

    BType[] bParamTypes;
    BType bReturnType;
    boolean throwsException = false;

    private JMethodRequest() {
    }

    static JMethodRequest build(MapValue jMethodReqBValue) {
        JMethodRequest jMethodReq = new JMethodRequest();
        jMethodReq.kind = JMethodKind.getKind((String) jMethodReqBValue.get(KIND_FIELD));
        jMethodReq.methodName = (String) jMethodReqBValue.get(NAME_FIELD);
        jMethodReq.declaringClass = JInterop.loadClass((String) jMethodReqBValue.get(CLASS_FIELD));
        jMethodReq.paramTypeConstraints = JInterop.buildParamTypeConstraints(
                (ArrayValue) jMethodReqBValue.get(PARAM_TYPE_CONSTRAINTS_FIELD));

        MapValue bFuncType = (MapValue) jMethodReqBValue.get(B_FUNC_TYPE_FIELD);
        ArrayValue paramTypes = (ArrayValue) bFuncType.get(PARAM_TYPES_FIELD);
        jMethodReq.bFuncParamCount = paramTypes.size();
        jMethodReq.bParamTypes = getBParamTypes(paramTypes);
        BType returnType = getBType(bFuncType.get(RETURN_TYPE_FIELD));
        jMethodReq.bReturnType = returnType;
        jMethodReq.throwsException = returnType.toString().contains(B_ERROR_TYPE_NAME);

        return jMethodReq;
    }

    private static BType[] getBParamTypes(ArrayValue paramTypes) {
        BType[] bParamTypes = new BType[paramTypes.size()];
        for (int i = 0 ; i < paramTypes.size() ; i ++) {
            bParamTypes[i] =  getBType(paramTypes.get(i));
        }
        return bParamTypes;
    }


    private static BType getBType(Object bTypeValue) {
        BType bType = TypeChecker.getType(bTypeValue);
        if (bType.getTag() == TypeTags.STRING_TAG) {
            String typeName = (String) bTypeValue;
            switch (typeName) {
                case "int":
                    return BTypes.typeInt;
                case "string":
                    return BTypes.typeString;
                case "byte":
                    return BTypes.typeByte;
                case "boolean":
                    return BTypes.typeBoolean;
                case "float":
                    return BTypes.typeFloat;
                case "decimal":
                    return BTypes.typeDecimal;
                case "any":
                    return BTypes.typeAny;
                case "anydata":
                    return BTypes.typeAnydata;
                case "()":
                    return BTypes.typeNull;
                case "json":
                    return BTypes.typeJSON;
                case "xml":
                    return BTypes.typeXML;
                default:
                    throw new UnsupportedOperationException("JMethodRequest does not support type '" + bType + "'");
            }
        }

        if (bType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            String typeName = ((MapValue) bTypeValue).getStringValue(TYPE_NAME_FIELD);
            switch (typeName) {
                case "handle":
                    return BTypes.typeHandle;
                case "service":
                    return BTypes.typeAnyService;
                case "typedesc":
                    return BTypes.typeTypedesc;
                case "map":
                    return BTypes.typeMap;
                case "table":
                    return BTypes.typeTable;
                case "stream":
                    return BTypes.typeStream;
                case "error":
                    return BTypes.typeError;
                case "future":
                    return BTypes.typeFuture;
                case "union":
                    ArrayValue members = ((MapValue) bTypeValue).getArrayValue("members");
                    List<BType> memberTypes = new ArrayList<>();
                    for (int i = 0; i < members.size(); i++) {
                        memberTypes.add(getBType(members.get(i)));
                    }
                    return new BUnionType(memberTypes);
                case "array":
                case "record":
                case "object":
                case "tuple":
                case "finite":
                default:
                    throw new UnsupportedOperationException("JMethodRequest does not support type '" + bType + "'");
            }
        }

        throw new UnsupportedOperationException("JMethodRequest does not support type '" + bType + "'");
    }
}
