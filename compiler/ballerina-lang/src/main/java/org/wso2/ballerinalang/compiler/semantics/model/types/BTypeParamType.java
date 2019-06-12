/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.types.Type;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeParamType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Implementation of @{@link org.ballerinalang.model.types.TypeParamType}
 *
 * @since JB 1.0.0
 */
public class BTypeParamType extends BType implements TypeParamType {

    public final BType constraint;
    public final Name name;

    public BTypeParamType(BType constraint, BTypeSymbol tsymbol) {

        super(TypeTags.TYPE_PARAM, tsymbol);
        this.name = tsymbol.name;
        this.constraint = constraint;
    }

    @Override
    public Type getConstraint() {

        return this.constraint;
    }

    @Override
    public String getDesc() {

        return TypeDescriptor.SIG_TYPEPARAM + constraint.getDesc();
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {

        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {

        return TypeKind.TYPEPARAM;
    }

    @Override
    public void accept(TypeVisitor visitor) {

        visitor.visit(this);
    }

    @Override
    public String toString() {

        return "@typeParam " + name.toString() + " " + constraint.toString();
    }
}
