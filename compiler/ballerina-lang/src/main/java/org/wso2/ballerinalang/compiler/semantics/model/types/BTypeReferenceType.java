/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.Optional;

import static org.wso2.ballerinalang.compiler.util.TypeTags.TYPEREFDESC;

/**
 * @since 2.0.0
 */
public class BTypeReferenceType extends BType implements SelectivelyImmutableReferenceType {

    public BType referredType;
    public BTypeDefinitionSymbol typeDefinitionSymbol;
    public final String definitionName;
    public BIntersectionType immutableType;
    private BIntersectionType intersectionType = null;

    public BTypeReferenceType(BType referredType, BTypeSymbol tsymbol, BTypeDefinitionSymbol typeDefinitionSymbol,
                              long flags) {
        super(TYPEREFDESC, tsymbol, flags);
        this.referredType = referredType;
        this.definitionName = tsymbol.getName().getValue();
        this.typeDefinitionSymbol = typeDefinitionSymbol;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return definitionName.equals(referredType.name.getValue()) || definitionName.startsWith("$anonType$")
                ? referredType.toString() : tsymbol.toString();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TYPEREFDESC;
    }

    @Override
    public boolean isNullable() {
        return this.referredType.isNullable();
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public Optional<BIntersectionType> getIntersectionType() {
        return Optional.ofNullable(this.intersectionType);
    }

    @Override
    public void setIntersectionType(BIntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
