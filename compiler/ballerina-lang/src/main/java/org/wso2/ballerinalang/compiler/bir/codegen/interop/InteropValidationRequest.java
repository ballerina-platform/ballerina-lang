/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;

import java.util.List;

public abstract class InteropValidationRequest {
    public String name;
    public String klass;
    public BInvokableType bFuncType;

    public InteropValidationRequest(String name, String klass, BInvokableType bFuncType) {
        this.name = name;
        this.klass = klass;
        this.bFuncType = bFuncType;
    }

    static class MethodValidationRequest extends InteropValidationRequest {
        JInterop.MethodKind methodKind;
        List<JType> paramTypeConstraints;
        boolean restParamExist = false;

        MethodValidationRequest(String name, String klass, BInvokableType bFuncType,
                                JInterop.MethodKind methodKind) {

            super(name, klass, bFuncType);
            this.methodKind = methodKind;
        }
    }

    static class FieldValidationRequest extends InteropValidationRequest {
        JInterop.FieldMethod method;

        FieldValidationRequest(String name, String klass, BInvokableType bFuncType,
                               JInterop.FieldMethod method) {
            super(name, klass, bFuncType);
            this.method = method;
        }
    }
}
