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

import org.wso2.ballerinalang.compiler.bir.codegen.model.JMethodKind;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

/**
 * JInterop Validation Request modeling class.
 *
 * @since 1.2.0
 */
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

        JMethodKind methodKind;
        List<JType> paramTypeConstraints;
        boolean restParamExist = false;
        BType receiverType = null;

        MethodValidationRequest(String name, String klass, BInvokableType bFuncType,
                                JMethodKind methodKind) {

            super(name, klass, bFuncType);
            this.methodKind = methodKind;
        }
    }

    static class FieldValidationRequest extends InteropValidationRequest {

        JFieldMethod fieldMethod;

        FieldValidationRequest(String name, String klass, BInvokableType bFuncType,
                               JFieldMethod fieldMethod) {

            super(name, klass, bFuncType);
            this.fieldMethod = fieldMethod;
        }
    }
}
