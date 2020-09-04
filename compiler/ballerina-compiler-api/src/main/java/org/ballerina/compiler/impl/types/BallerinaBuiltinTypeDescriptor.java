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
import org.ballerina.compiler.api.types.BuiltinTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Represents the builtin type descriptor.
 * 
 * @since 1.3.0
 */
public class BallerinaBuiltinTypeDescriptor extends AbstractTypeDescriptor implements BuiltinTypeDescriptor {

    private String typeName;

    public BallerinaBuiltinTypeDescriptor(ModuleID moduleID, String name, BType bType) {
        super(TypeDescKind.BUILTIN, moduleID, bType);
        this.typeName = name;
    }

    @Override
    public String signature() {
        // For the builtin types, return the type kind as the type name
        return this.typeName;
    }

    @Override
    public TypeDescKind kind() {
        return TypeDescKind.getFromName(this.typeName);
    }
}
