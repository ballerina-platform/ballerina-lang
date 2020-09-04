/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code JMethodRequest} represents Java method request bean issued by the Java interop logic written in Ballerina.
 *
 * @since 1.2.0
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
    BType receiverType = null;

    private static ResolvedTypeBuilder typeBuilder = new ResolvedTypeBuilder();

    private JMethodRequest() {

    }

    static JMethodRequest build(InteropValidationRequest.MethodValidationRequest methodValidationRequest,
                                ClassLoader classLoader, boolean isEntryModuleValidation) {

        JMethodRequest jMethodReq = new JMethodRequest();
        jMethodReq.kind = methodValidationRequest.methodKind;
        jMethodReq.methodName = methodValidationRequest.name;
        jMethodReq.declaringClass = JInterop.loadClass(methodValidationRequest.klass, classLoader);
        jMethodReq.receiverType = methodValidationRequest.receiverType;
        jMethodReq.paramTypeConstraints =
                JInterop.buildParamTypeConstraints(methodValidationRequest.paramTypeConstraints, classLoader);

        BInvokableType bFuncType = methodValidationRequest.bFuncType;
        List<BType> paramTypes = new ArrayList<>();
        if (!isEntryModuleValidation) {
            int i = 0;
            while (i < bFuncType.paramTypes.size()) {
                paramTypes.add(bFuncType.paramTypes.get(i));
                i = i + 2;
            }
        } else {
            paramTypes.addAll(bFuncType.paramTypes);
        }

        BType restType = bFuncType.restType;
        if (restType != null) {
            paramTypes.add(restType);
        }

        jMethodReq.bFuncParamCount = paramTypes.size();
        jMethodReq.bParamTypes = paramTypes.toArray(new BType[0]);

        BType returnType = typeBuilder.build(bFuncType.retType);
        jMethodReq.bReturnType = returnType;
        if (returnType.tag == TypeTags.UNION) {
            for (BType bType : ((BUnionType) returnType).getMemberTypes()) {
                if (bType.tag == TypeTags.ERROR) {
                    jMethodReq.returnsBErrorType = true;
                    break;
                }
            }
        } else {
            jMethodReq.returnsBErrorType = returnType.tag == TypeTags.ERROR;
        }
        jMethodReq.restParamExist = methodValidationRequest.restParamExist;
        return jMethodReq;
    }
}
