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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.BIR_UNION_TYPE_VALUE;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.B_FUNC_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.KIND_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPES_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPE_CONSTRAINTS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.RETURN_TYPE_FIELD;

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

    ArrayValue bParamTypes;
    Object bReturnTypes;
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
        jMethodReq.bParamTypes = paramTypes;
        Object returnTypes = bFuncType.get(RETURN_TYPE_FIELD);
        jMethodReq.bReturnTypes = returnTypes;
        jMethodReq.throwsException = TypeChecker.checkIsLikeType(BIR_UNION_TYPE_VALUE,
                TypeChecker.getType(returnTypes));

        return jMethodReq;
    }
}
