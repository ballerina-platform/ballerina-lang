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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents a TypeReference type descriptor.
 *
 * @since 1.3.0
 */
public class TypeReferenceTypeDescriptor extends BallerinaTypeDesc {

    private static final String ANON_ORG = "$anon";

    private String definitionName;

    private BallerinaTypeDescriptor typeDescriptorImpl;

    public TypeReferenceTypeDescriptor(ModuleID moduleID,
                                       BType bType,
                                       String definitionName) {
        super(TypeDescKind.TYPE_REFERENCE, moduleID, bType);
        this.definitionName = definitionName;
    }

    public BallerinaTypeDescriptor getTypeDescriptorImpl() {
        if (this.typeDescriptorImpl == null) {
            this.typeDescriptorImpl = TypesFactory.getTypeDescriptor(this.getBType(), true);
        }
        return typeDescriptorImpl;
    }

    @Override
    public String signature() {
        if (this.moduleID() == null || (this.moduleID().getModuleName().equals("lang.annotations")
                && this.moduleID().getOrgName().equals("ballerina"))) {
            return this.definitionName;
        }
        return !this.isAnonOrg(this.moduleID()) ? this.moduleID().getModulePrefix() + ":" + this.definitionName
                : this.definitionName;
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.getOrgName());
    }
}
