/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.ErrorTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * Represents an error type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaErrorTypeDescriptor extends AbstractTypeDescriptor implements ErrorTypeDescriptor {

    private BallerinaTypeDescriptor detail;

    public BallerinaErrorTypeDescriptor(ModuleID moduleID, BErrorType errorType) {
        super(TypeDescKind.ERROR, moduleID, errorType);
    }

    /**
     * Get the detail type descriptor.
     *
     * @return {@link BallerinaTypeDescriptor} detail
     */
    @Override
    public BallerinaTypeDescriptor detailTypeDescriptor() {
        if (this.detail == null) {
            this.detail = TypesFactory.getTypeDescriptor(((BErrorType) this.getBType()).getDetailType());
        }
        return this.detail;
    }

    @Override
    public String signature() {
        if (this.moduleID().moduleName().equals("lang.annotations") && this.moduleID().orgName().equals("ballerina")) {
            return ((BErrorType) this.getBType()).name.getValue();
        }
        return this.moduleID().orgName() + Names.ORG_NAME_SEPARATOR +
                this.moduleID().moduleName() + Names.VERSION_SEPARATOR + this.moduleID().version() + ":" +
                ((BErrorType) this.getBType()).name.getValue();
    }
}
