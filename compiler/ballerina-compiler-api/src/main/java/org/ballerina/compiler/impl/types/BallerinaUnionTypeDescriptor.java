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
import org.ballerina.compiler.api.types.UnionTypeDescriptor;
import org.ballerina.compiler.impl.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an union type descriptor.
 *
 * @since 1.3.0
 */
public class BallerinaUnionTypeDescriptor extends AbstractTypeDescriptor implements UnionTypeDescriptor {

    private List<BallerinaTypeDescriptor> memberTypes;

    public BallerinaUnionTypeDescriptor(ModuleID moduleID, BUnionType unionType) {
        super(TypeDescKind.UNION, moduleID, unionType);
    }

    @Override
    public List<BallerinaTypeDescriptor> getMemberTypes() {
        if (this.memberTypes == null) {
            this.memberTypes = new ArrayList<>();
            for (BType memberType : ((BUnionType) this.getBType()).getMemberTypes()) {
                this.memberTypes.add(TypesFactory.getTypeDescriptor(memberType));
            }
        }
        return this.memberTypes;
    }

    @Override
    public String signature() {
        StringJoiner joiner = new StringJoiner("|");
        this.getMemberTypes().forEach(typeDescriptor -> joiner.add(typeDescriptor.signature()));
        return joiner.toString();
    }
}
