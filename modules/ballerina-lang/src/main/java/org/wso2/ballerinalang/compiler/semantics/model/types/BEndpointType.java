/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.ConstrainedType;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;

/**
 * @since 0.95
 */
public class BEndpointType extends BType implements ConstrainedType {

    public BType pkgConstraint;
    public BType connectorType;

    public BEndpointType(int tag, BType constraint, BPackageSymbol tsymbol) {
        super(tag, tsymbol);
        this.pkgConstraint = constraint;
    }

    @Override
    public Type getConstraint() {
        return this.pkgConstraint;
    }

    public String getDesc() {
        return TypeDescriptor.SIG_CONNECTOR + connectorType.tsymbol.pkgID.name
                + ":" + connectorType.tsymbol.name + ";";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ENDPOINT;
    }

    @Override
    public <R> R accept(BTypeVisitor<R> visitor, BType type) {
        return visitor.visit(this, type);
    }

    @Override
    public String toString() {
        return this.tsymbol.name.value;
    }
}
