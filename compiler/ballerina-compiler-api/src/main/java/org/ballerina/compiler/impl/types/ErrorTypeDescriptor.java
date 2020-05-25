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
package org.ballerina.compiler.impl.types;

import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.type.TypeDescKind;
import org.ballerina.compiler.impl.semantic.BallerinaTypeDesc;
import org.ballerina.compiler.impl.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;

/**
 * Represents an error type descriptor.
 *
 * @since 1.3.0
 */
public class ErrorTypeDescriptor extends BallerinaTypeDesc {

    private BallerinaTypeDescriptor reason;

    private BallerinaTypeDescriptor detail;

    public ErrorTypeDescriptor(ModuleID moduleID,
                               BErrorType errorType) {
        super(TypeDescKind.ERROR, moduleID, errorType);
    }

    /**
     * Get the reason type descriptor.
     *
     * @return {@link BallerinaTypeDescriptor} reason
     */
    public BallerinaTypeDescriptor getReason() {
        if (this.reason == null) {
            this.reason = TypesFactory.getTypeDescriptor(((BErrorType) this.getBType()).getReasonType());
        }
        return this.reason;
    }

    /**
     * Get the detail type descriptor.
     *
     * @return {@link BallerinaTypeDescriptor} detail
     */
    public BallerinaTypeDescriptor getDetail() {
        if (this.detail == null) {
            this.detail = TypesFactory.getTypeDescriptor(((BErrorType) this.getBType()).getDetailType());
        }
        return this.detail;
    }

    @Override
    public String signature() {
        if (this.moduleID().getModuleName().equals("lang.annotations")
                && this.moduleID().getOrgName().equals("ballerina")) {
            return ((BErrorType) this.getBType()).name.getValue();
        }
        return this.moduleID().getModulePrefix() + ":" + ((BErrorType) this.getBType()).name.getValue();
    }
}
