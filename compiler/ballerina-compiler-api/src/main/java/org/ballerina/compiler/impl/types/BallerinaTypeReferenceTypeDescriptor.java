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

import org.ballerina.compiler.api.ModuleID;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import org.ballerina.compiler.impl.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents a TypeReference type descriptor.
 *
 * @since 1.3.0
 */
public class BallerinaTypeReferenceTypeDescriptor extends AbstractTypeDescriptor
        implements TypeReferenceTypeDescriptor {

    private static final String ANON_ORG = "$anon";
    private String definitionName;
    private BallerinaTypeDescriptor typeDescriptorImpl;

    public BallerinaTypeReferenceTypeDescriptor(ModuleID moduleID, BType bType, String definitionName) {
        super(TypeDescKind.TYPE_REFERENCE, moduleID, bType);
        this.definitionName = definitionName;
    }

    @Override
    public BallerinaTypeDescriptor typeDescriptor() {
        if (this.typeDescriptorImpl == null) {
            this.typeDescriptorImpl = TypesFactory.getTypeDescriptor(this.getBType(), true);
        }
        return typeDescriptorImpl;
    }

    @Override
    public String signature() {
        if (this.moduleID() == null || (this.moduleID().moduleName().equals("lang.annotations") &&
                this.moduleID().orgName().equals("ballerina"))) {
            return this.definitionName;
        }
        return !this.isAnonOrg(this.moduleID()) ? this.moduleID().modulePrefix() + ":" + this.definitionName
                : this.definitionName;
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.orgName());
    }
}
