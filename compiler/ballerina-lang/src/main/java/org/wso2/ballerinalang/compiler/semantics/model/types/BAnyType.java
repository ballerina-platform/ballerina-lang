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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemTypeResolver;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * @since 0.94
 */
public class BAnyType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    private BIntersectionType intersectionType = null;
    private boolean nullable = true;
    private HybridType hybridType;

    public BAnyType(BTypeSymbol tsymbol) {
        super(TypeTags.ANY, tsymbol);
        SemTypeResolver.resolveBAnyHybridType(this);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flag) {
        super(TypeTags.ANY, tsymbol);
        this.name = name;
        this.flags = flag;
        SemTypeResolver.resolveBAnyHybridType(this);
    }

    public BAnyType(BTypeSymbol tsymbol, boolean nullable) {
        super(TypeTags.ANY, tsymbol);
        this.nullable = nullable;
        SemTypeResolver.resolveBAnyHybridType(this);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flags, boolean nullable) {
        super(TypeTags.ANY, tsymbol);
        this.name = name;
        this.flags = flags;
        this.nullable = nullable;
        SemTypeResolver.resolveBAnyHybridType(this);
    }

    private BAnyType(Name name, long flags) {
        super(TypeTags.ANY, null);
        this.name = name;
        this.flags = flags;
    }

    /**
     * Creates BReadonly for {@link HybridType#getBTypeComponent}.
     *
     * @return The created readonly type
     */
    public static BAnyType createBTypeComponent(Name name, long flags) {
        return new BAnyType(name, flags);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANY;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public Optional<BIntersectionType> getIntersectionType() {
        return Optional.ofNullable(this.intersectionType);
    }

    @Override
    public void setIntersectionType(BIntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    public HybridType getHybridType() {
        return hybridType;
    }

    public void setHybridType(HybridType hybridType) {
        this.hybridType = hybridType;
    }
}
