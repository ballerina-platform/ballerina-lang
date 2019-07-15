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

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.KIND_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.PARAM_TYPE_CONSTRAINTS_FIELD;

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

    private JMethodRequest() {
    }

    static JMethodRequest build(MapValue jMethodReqBValue) {
        JMethodRequest jMethodReq = new JMethodRequest();
        jMethodReq.kind = JMethodKind.getKind((String) jMethodReqBValue.get(KIND_FIELD));
        jMethodReq.methodName = (String) jMethodReqBValue.get(NAME_FIELD);
        jMethodReq.declaringClass = JInterop.loadClass((String) jMethodReqBValue.get(CLASS_FIELD));
        jMethodReq.paramTypeConstraints = JInterop.buildParamTypeConstraints(
                (ArrayValue) jMethodReqBValue.get(PARAM_TYPE_CONSTRAINTS_FIELD), jMethodReq.kind);
        return jMethodReq;
    }
}
