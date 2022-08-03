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
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * @since 0.94
 */
public class BMapType extends BBuiltInRefType implements ConstrainedType, SelectivelyImmutableReferenceType {

    public BType constraint;

    private BIntersectionType intersectionType = null;
    private boolean resolvingToString = false;

    public BRecordType mutableType;

    public BMapType(int tag, BType constraint, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
        this.constraint = constraint;
    }

    public BMapType(int tag, BType constraint, BTypeSymbol tsymbol, long flags) {
        super(tag, tsymbol, flags);
        this.constraint = constraint;
    }

    @Override
    public BType getConstraint() {
        return constraint;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        String stringRep;
        // To handle recursive map types.
        if (this.resolvingToString) {
            if (tsymbol != null && !tsymbol.getName().getValue().isEmpty()) {
                return this.tsymbol.toString();
            }
            return "...";
        }
        this.resolvingToString = true;
        if (constraint == null) {
            stringRep = super.toString() + "<" + ">";
        } else if (constraint.tag == TypeTags.ANY) {
            stringRep = super.toString();
        } else {
            stringRep = super.toString() + "<" + constraint + ">";
        }

        return !Symbols.isFlagOn(flags, Flags.READONLY) ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
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
