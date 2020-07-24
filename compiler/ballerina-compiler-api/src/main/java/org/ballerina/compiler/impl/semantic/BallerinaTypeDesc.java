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
package org.ballerina.compiler.impl.semantic;

import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.type.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents a Ballerina Type Descriptor.
 * 
 * @since 1.3.0
 */
public abstract class BallerinaTypeDesc implements BallerinaTypeDescriptor {
    private TypeDescKind typeDescKind;
    private ModuleID moduleID;
    private BType bType;

    public BallerinaTypeDesc(TypeDescKind typeDescKind, ModuleID moduleID, BType bType) {
        this.typeDescKind = typeDescKind;
        this.moduleID = moduleID;
        this.bType = bType;
    }

    /**
     * Get the BType.
     * 
     * @return {@link BType} associated with the type desc
     */
    protected BType getBType() {
        return bType;
    }

    public TypeDescKind kind() {
        return typeDescKind;
    }

    public ModuleID moduleID() {
        return moduleID;
    }
    
    public abstract String signature();
}
