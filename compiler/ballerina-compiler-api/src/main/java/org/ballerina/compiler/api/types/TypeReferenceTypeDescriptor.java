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
package org.ballerina.compiler.api.types;

import org.ballerina.compiler.api.model.ModuleID;
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents a TypeReference type descriptor.
 *
 * @since 1.3.0
 */
public class TypeReferenceTypeDescriptor extends BallerinaTypeDesc {

    private String definitionName;

    private BType bType;

    private TypeDescriptor typeDescriptor;

    public TypeReferenceTypeDescriptor(ModuleID moduleID,
                                        BType bType,
                                        String definitionName) {
        super(TypeDescKind.TYPE_REFERENCE, moduleID);
        this.bType = bType;
        this.definitionName = definitionName;
    }

    public TypeDescriptor getTypeDescriptor() {
        if (this.typeDescriptor == null) {
            this.typeDescriptor = TypesFactory.getTypeDescriptor(this.bType);
        }
        return typeDescriptor;
    }

    @Override
    public String getSignature() {
        if (this.getModuleID() == null) {
            return this.definitionName;
        }
        return this.getModuleID().getModulePrefix() + ":" + this.definitionName;
    }
}
